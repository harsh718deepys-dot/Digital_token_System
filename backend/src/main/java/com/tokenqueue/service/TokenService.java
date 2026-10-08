package com.tokenqueue.service;

import com.tokenqueue.dto.TokenDTO.*;
import com.tokenqueue.model.*;
import com.tokenqueue.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private CounterRepository counterRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private QueueManagementService queueService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Token counter for generating unique numbers
    private int tokenCounter = 1000;

    /**
     * Creates a new token for a visitor.
     * Flow: Validate → Generate Token Number → Add to DSA Queue → Save to DB → Notify
     */
    @Transactional
    public TokenResponse createToken(String username, CreateTokenRequest request) {
        User visitor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new RuntimeException("Location not found"));

        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        // Parse category and determine priority
        Token.Category category = Token.Category.valueOf(request.getCategory());
        int priority = Token.categoryToPriority(category);

        // Generate unique token number
        String tokenNumber = "T" + (++tokenCounter);

        // Use Greedy Algorithm to allocate counter
        int counterIndex = queueService.allocateCounter(location.getId(), priority);

        // Find the corresponding counter entity
        List<Counter> counters = counterRepository.findByLocationId(location.getId());
        Counter assignedCounter = null;
        if (counterIndex >= 0 && counterIndex < counters.size()) {
            assignedCounter = counters.get(counterIndex);
        }

        // Create token entity
        Token token = Token.builder()
                .tokenNumber(tokenNumber)
                .visitor(visitor)
                .location(location)
                .service(service)
                .category(category)
                .priority(priority)
                .status(Token.Status.WAITING)
                .counter(assignedCounter)
                .build();

        token = tokenRepository.save(token);

        // Add to DSA queue/heap
        queueService.addTokenToQueue(token.getId(), tokenNumber, service.getId(), priority);

        // Calculate queue position and wait time
        int position = queueService.getQueuePosition(token.getId(), service.getId(), priority);
        int waitTime = position * service.getAvgServiceTime();

        token.setQueuePosition(position);
        token.setEstimatedWaitTime(waitTime);
        token = tokenRepository.save(token);

        // Update counter info
        if (assignedCounter != null) {
            assignedCounter.setTokensInQueue(assignedCounter.getTokensInQueue() + 1);
            counterRepository.save(assignedCounter);
        }

        // Create notification
        createNotification(visitor, "Token Generated",
                "Token " + tokenNumber + " generated successfully. Estimated wait time: ~" + waitTime + " minutes.",
                "TOKEN_GENERATED", tokenNumber);

        // Send WebSocket update
        sendQueueUpdate(service.getId());

        return mapToResponse(token);
    }

    /**
     * Gets a token by ID.
     */
    public TokenResponse getToken(Long tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found"));
        
        // Update position from DSA
        if (token.getStatus() == Token.Status.WAITING) {
            int position = queueService.getQueuePosition(token.getId(), token.getService().getId(), token.getPriority());
            if (position > 0) {
                token.setQueuePosition(position);
                token.setEstimatedWaitTime(position * token.getService().getAvgServiceTime());
            }
        }

        return mapToResponse(token);
    }

    /**
     * Gets a token by token number (uses hash table for fast lookup).
     */
    public TokenResponse getTokenByNumber(String tokenNumber) {
        // Use hash table for O(1) lookup
        Long tokenId = queueService.lookupToken(tokenNumber);
        if (tokenId != null) {
            return getToken(tokenId);
        }
        // Fallback to database
        Token token = tokenRepository.findByTokenNumber(tokenNumber)
                .orElseThrow(() -> new RuntimeException("Token not found: " + tokenNumber));
        return mapToResponse(token);
    }

    /**
     * Cancels a token.
     */
    @Transactional
    public TokenResponse cancelToken(Long tokenId, String username) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        if (token.getStatus() != Token.Status.WAITING && token.getStatus() != Token.Status.CALLED) {
            throw new RuntimeException("Cannot cancel token with status: " + token.getStatus());
        }

        // Remove from DSA queue
        queueService.removeTokenFromQueue(token.getId(), token.getTokenNumber(),
                token.getService().getId(), token.getPriority());

        // Release counter
        if (token.getCounter() != null) {
            List<Counter> counters = counterRepository.findByLocationId(token.getLocation().getId());
            int counterIndex = counters.indexOf(token.getCounter());
            if (counterIndex >= 0) {
                queueService.releaseCounter(token.getLocation().getId(), counterIndex);
            }
            token.getCounter().setTokensInQueue(Math.max(0, token.getCounter().getTokensInQueue() - 1));
            counterRepository.save(token.getCounter());
        }

        token.setStatus(Token.Status.CANCELLED);
        token = tokenRepository.save(token);

        // Notify
        createNotification(token.getVisitor(), "Token Cancelled",
                "Token " + token.getTokenNumber() + " has been cancelled.",
                "TOKEN_CANCELLED", token.getTokenNumber());

        sendQueueUpdate(token.getService().getId());

        return mapToResponse(token);
    }

    /**
     * Admin calls the next token using DSA queue/heap.
     * The DSA engine determines which token to serve next.
     */
    @Transactional
    public CallNextResponse callNextToken(Long serviceId, Long counterId) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        Counter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> new RuntimeException("Counter not found"));

        // DSA determines next token (priority heap first, then normal queue)
        Long nextTokenId = queueService.getNextToken(serviceId);

        if (nextTokenId == null) {
            throw new RuntimeException("No tokens waiting in queue");
        }

        Token token = tokenRepository.findById(nextTokenId)
                .orElseThrow(() -> new RuntimeException("Token not found in database"));

        // Update token status
        token.setStatus(Token.Status.CALLED);
        token.setCounter(counter);
        token.setCalledAt(LocalDateTime.now());
        token = tokenRepository.save(token);

        // Update counter status
        counter.setStatus(Counter.CounterStatus.SERVING);
        counter.setCurrentTokenNumber(token.getTokenNumber());
        counterRepository.save(counter);

        // Notify the visitor via WebSocket
        createNotification(token.getVisitor(), "Your Turn!",
                "Your token " + token.getTokenNumber() + " is next! Please proceed to Counter " + counter.getCounterNumber() + ".",
                "TOKEN_CALLED", token.getTokenNumber());

        // Also notify nearby tokens
        notifyNearbyTokens(serviceId);

        // Broadcast queue update
        sendQueueUpdate(serviceId);
        sendDashboardUpdate();

        return CallNextResponse.builder()
                .token(mapToResponse(token))
                .counterNumber(counter.getCounterNumber())
                .message("Token " + token.getTokenNumber() + " called to Counter " + counter.getCounterNumber())
                .build();
    }

    /**
     * Marks a token as completed.
     */
    @Transactional
    public TokenResponse completeToken(Long tokenId) {
        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        token.setStatus(Token.Status.COMPLETED);
        token.setCompletedAt(LocalDateTime.now());
        token = tokenRepository.save(token);

        // Add to DSA history stack
        queueService.addToHistory(tokenId);

        // Release counter
        if (token.getCounter() != null) {
            Counter counter = token.getCounter();
            counter.setStatus(Counter.CounterStatus.IDLE);
            counter.setCurrentTokenNumber(null);
            counter.setTokensInQueue(Math.max(0, counter.getTokensInQueue() - 1));
            counterRepository.save(counter);

            // Release in DSA counter array
            List<Counter> counters = counterRepository.findByLocationId(token.getLocation().getId());
            int counterIndex = counters.indexOf(counter);
            if (counterIndex >= 0) {
                queueService.releaseCounter(token.getLocation().getId(), counterIndex);
            }
        }

        // Notify visitor
        createNotification(token.getVisitor(), "Service Completed",
                "Token " + token.getTokenNumber() + " has been completed. Thank you for visiting!",
                "TOKEN_COMPLETED", token.getTokenNumber());

        sendQueueUpdate(token.getService().getId());
        sendDashboardUpdate();

        return mapToResponse(token);
    }

    /**
     * Gets queue status for a service.
     */
    public QueueStatusResponse getQueueStatus(Long serviceId) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        List<Token> waitingTokens = tokenRepository
                .findByServiceIdAndStatusInOrderByPriorityAscCreatedAtAsc(
                        serviceId, Arrays.asList(Token.Status.WAITING, Token.Status.CALLED));

        List<TokenResponse> tokenResponses = waitingTokens.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        int serving = (int) waitingTokens.stream()
                .filter(t -> t.getStatus() == Token.Status.CALLED)
                .count();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        int completed = tokenRepository.countTodayTokensByStatus(Token.Status.COMPLETED, startOfDay);

        return QueueStatusResponse.builder()
                .serviceName(service.getName())
                .locationName(service.getLocation().getName())
                .totalInQueue(waitingTokens.size())
                .totalServing(serving)
                .totalCompleted(completed)
                .queueTokens(tokenResponses)
                .build();
    }

    /**
     * Gets visitor's token history.
     */
    public List<TokenResponse> getVisitorHistory(String username) {
        User visitor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return tokenRepository.findByVisitorIdOrderByCreatedAtDesc(visitor.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets visitor's active token.
     */
    public TokenResponse getActiveToken(String username) {
        User visitor = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Token> tokens = tokenRepository.findByVisitorIdOrderByCreatedAtDesc(visitor.getId());
        Token activeToken = tokens.stream()
                .filter(t -> t.getStatus() == Token.Status.WAITING || t.getStatus() == Token.Status.CALLED || t.getStatus() == Token.Status.SERVING)
                .findFirst()
                .orElse(null);

        if (activeToken == null) return null;

        // Update position from DSA
        if (activeToken.getStatus() == Token.Status.WAITING) {
            int position = queueService.getQueuePosition(activeToken.getId(), activeToken.getService().getId(), activeToken.getPriority());
            if (position > 0) {
                activeToken.setQueuePosition(position);
                activeToken.setEstimatedWaitTime(position * activeToken.getService().getAvgServiceTime());
            }
        }

        return mapToResponse(activeToken);
    }

    /**
     * Gets dashboard data for admin.
     */
    public DashboardResponse getDashboard() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        int totalTokens = tokenRepository.countTodayTokens(startOfDay);
        int waiting = tokenRepository.countTodayTokensByStatus(Token.Status.WAITING, startOfDay);
        int serving = tokenRepository.countTodayTokensByStatus(Token.Status.CALLED, startOfDay)
                    + tokenRepository.countTodayTokensByStatus(Token.Status.SERVING, startOfDay);
        int completed = tokenRepository.countTodayTokensByStatus(Token.Status.COMPLETED, startOfDay);
        int cancelled = tokenRepository.countTodayTokensByStatus(Token.Status.CANCELLED, startOfDay);

        // Counter statuses
        List<Counter> counters = counterRepository.findAll();
        List<CounterStatusDTO> counterStatuses = counters.stream()
                .map(c -> CounterStatusDTO.builder()
                        .id(c.getId())
                        .counterNumber(c.getCounterNumber())
                        .status(c.getStatus().name())
                        .currentTokenNumber(c.getCurrentTokenNumber())
                        .tokensInQueue(c.getTokensInQueue())
                        .capacity(c.getCapacity())
                        .build())
                .collect(Collectors.toList());

        // Category distribution
        Map<String, Integer> categoryDist = new LinkedHashMap<>();
        categoryDist.put("Normal", 0);
        categoryDist.put("Senior Citizen", 0);
        categoryDist.put("Differently-Abled", 0);
        categoryDist.put("Pregnant Woman", 0);
        categoryDist.put("Emergency", 0);

        try {
            List<Object[]> catCounts = tokenRepository.countByCategoryToday(startOfDay);
            for (Object[] row : catCounts) {
                Token.Category cat = (Token.Category) row[0];
                int count = ((Number) row[1]).intValue();
                switch (cat) {
                    case NORMAL: categoryDist.put("Normal", count); break;
                    case SENIOR_CITIZEN: categoryDist.put("Senior Citizen", count); break;
                    case DIFFERENTLY_ABLED: categoryDist.put("Differently-Abled", count); break;
                    case PREGNANT_WOMAN: categoryDist.put("Pregnant Woman", count); break;
                    case EMERGENCY: categoryDist.put("Emergency", count); break;
                }
            }
        } catch (Exception e) {
            // Fallback: count from all tokens today
        }

        // Queue overview (mock hourly data)
        List<QueueChartData> queueOverview = generateQueueOverviewData();

        return DashboardResponse.builder()
                .totalTokens(totalTokens)
                .waiting(waiting)
                .serving(serving)
                .completed(completed)
                .cancelled(cancelled)
                .counterStatuses(counterStatuses)
                .categoryDistribution(categoryDist)
                .queueOverview(queueOverview)
                .build();
    }

    /**
     * Gets statistics for admin.
     */
    public StatisticsResponse getStatistics() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        
        int totalToday = tokenRepository.countTodayTokens(startOfDay);
        int completedToday = tokenRepository.countTodayTokensByStatus(Token.Status.COMPLETED, startOfDay);
        
        double completionRate = totalToday > 0 ? (double) completedToday / totalToday * 100 : 0;

        Map<String, Integer> tokensByStatus = new LinkedHashMap<>();
        tokensByStatus.put("Waiting", tokenRepository.countTodayTokensByStatus(Token.Status.WAITING, startOfDay));
        tokensByStatus.put("Called", tokenRepository.countTodayTokensByStatus(Token.Status.CALLED, startOfDay));
        tokensByStatus.put("Completed", completedToday);
        tokensByStatus.put("Cancelled", tokenRepository.countTodayTokensByStatus(Token.Status.CANCELLED, startOfDay));

        return StatisticsResponse.builder()
                .totalTokensToday(totalToday)
                .avgWaitTime(15)
                .peakHourTokens(0)
                .completionRate(completionRate)
                .tokensByStatus(tokensByStatus)
                .build();
    }

    /**
     * Gets all tokens for admin management.
     */
    public List<TokenResponse> getAllTokens() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return tokenRepository.findTodayTokens(startOfDay).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets all token history.
     */
    public List<TokenResponse> getAllHistory() {
        return tokenRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(100)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ===== Notifications =====

    public List<NotificationDTO> getNotifications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(n -> NotificationDTO.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .type(n.getType())
                        .tokenNumber(n.getTokenNumber())
                        .read(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void markNotificationRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    // ===== DP Optimization =====

    public OptimizationResponse runOptimization(Long locationId) {
        List<Counter> counters = counterRepository.findByLocationId(locationId);
        int totalWaiting = tokenRepository.countByStatus(Token.Status.WAITING);

        if (counters.isEmpty() || totalWaiting == 0) {
            return OptimizationResponse.builder()
                    .optimalAllocation(new int[0])
                    .minimizedMaxWaitTime(0)
                    .totalWaitTime(0)
                    .comparison("No data available for optimization.")
                    .build();
        }

        var result = queueService.optimizeAllocation(totalWaiting, counters.size(), 5);
        String comparison = queueService.compareAllocations(locationId, totalWaiting, 5);

        return OptimizationResponse.builder()
                .optimalAllocation(result.getAllocation())
                .minimizedMaxWaitTime(result.getMinimizedMaxWaitTime())
                .totalWaitTime(result.getTotalWaitTime())
                .comparison(comparison)
                .build();
    }

    // ===== Helper Methods =====

    private void createNotification(User user, String title, String message, String type, String tokenNumber) {
        if (user == null) return;
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .tokenNumber(tokenNumber)
                .build();
        notificationRepository.save(notification);

        // Also send via WebSocket
        NotificationDTO dto = NotificationDTO.builder()
                .title(title)
                .message(message)
                .type(type)
                .tokenNumber(tokenNumber)
                .createdAt(LocalDateTime.now())
                .build();
        messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), dto);
    }

    private void notifyNearbyTokens(Long serviceId) {
        List<Token> waitingTokens = tokenRepository
                .findByServiceIdAndStatusInOrderByPriorityAscCreatedAtAsc(
                        serviceId, Arrays.asList(Token.Status.WAITING));

        for (int i = 0; i < Math.min(3, waitingTokens.size()); i++) {
            Token t = waitingTokens.get(i);
            int pos = i + 1;
            String msg = pos == 1
                    ? "Your token " + t.getTokenNumber() + " is next! Please be ready."
                    : "Your token " + t.getTokenNumber() + " is " + pos + " numbers away. Please be ready.";
            createNotification(t.getVisitor(), "Queue Update", msg, "TOKEN_NEAR", t.getTokenNumber());
        }
    }

    private void sendQueueUpdate(Long serviceId) {
        try {
            QueueStatusResponse status = getQueueStatus(serviceId);
            messagingTemplate.convertAndSend("/topic/queue/" + serviceId, status);
        } catch (Exception e) {
            // Log but don't fail
        }
    }

    private void sendDashboardUpdate() {
        try {
            DashboardResponse dashboard = getDashboard();
            messagingTemplate.convertAndSend("/topic/dashboard", dashboard);
        } catch (Exception e) {
            // Log but don't fail
        }
    }

    private TokenResponse mapToResponse(Token token) {
        return TokenResponse.builder()
                .id(token.getId())
                .tokenNumber(token.getTokenNumber())
                .visitorName(token.getVisitor() != null ? token.getVisitor().getFullName() : null)
                .visitorId(token.getVisitor() != null ? token.getVisitor().getId() : null)
                .locationName(token.getLocation().getName())
                .locationId(token.getLocation().getId())
                .serviceName(token.getService().getName())
                .serviceId(token.getService().getId())
                .category(token.getCategory().name())
                .priority(token.getPriority())
                .status(token.getStatus().name())
                .queuePosition(token.getQueuePosition())
                .estimatedWaitTime(token.getEstimatedWaitTime())
                .counterNumber(token.getCounter() != null ? token.getCounter().getCounterNumber() : null)
                .counterId(token.getCounter() != null ? token.getCounter().getId() : null)
                .createdAt(token.getCreatedAt())
                .calledAt(token.getCalledAt())
                .completedAt(token.getCompletedAt())
                .build();
    }

    private List<QueueChartData> generateQueueOverviewData() {
        List<QueueChartData> data = new ArrayList<>();
        String[] hours = {"9 AM", "10 AM", "11 AM", "12 PM", "1 PM", "2 PM", "3 PM", "4 PM"};
        Random rand = new Random(42);
        for (String hour : hours) {
            data.add(QueueChartData.builder()
                    .time(hour)
                    .normal(10 + rand.nextInt(30))
                    .priority(2 + rand.nextInt(10))
                    .build());
        }
        return data;
    }
}
