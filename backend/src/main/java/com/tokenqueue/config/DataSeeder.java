package com.tokenqueue.config;

import com.tokenqueue.model.*;
import com.tokenqueue.repository.*;
import com.tokenqueue.service.QueueManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DataSeeder - Populates the database with demo data on application startup.
 * This ensures the application is not empty and can be demonstrated immediately.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private CounterRepository counterRepository;
    @Autowired private TokenRepository tokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private QueueManagementService queueService;

    @Override
    public void run(String... args) {
        // Only seed if database is empty
        if (userRepository.count() > 0) {
            initializeDSAStructures();
            return;
        }

        System.out.println("=== Seeding Database ===");

        // Create users
        User admin = userRepository.save(User.builder()
                .username("admin")
                .email("admin@tokenqueue.com")
                .password(passwordEncoder.encode("admin123"))
                .fullName("Admin User")
                .phone("9876543210")
                .role(User.Role.ADMIN)
                .build());

        User visitor1 = userRepository.save(User.builder()
                .username("visitor1")
                .email("visitor1@email.com")
                .password(passwordEncoder.encode("visitor123"))
                .fullName("Rahul Sharma")
                .phone("9876543211")
                .role(User.Role.VISITOR)
                .build());

        User visitor2 = userRepository.save(User.builder()
                .username("visitor2")
                .email("visitor2@email.com")
                .password(passwordEncoder.encode("visitor123"))
                .fullName("Priya Patel")
                .phone("9876543212")
                .role(User.Role.VISITOR)
                .build());

        User visitor3 = userRepository.save(User.builder()
                .username("visitor3")
                .email("visitor3@email.com")
                .password(passwordEncoder.encode("visitor123"))
                .fullName("Amit Kumar")
                .phone("9876543213")
                .role(User.Role.VISITOR)
                .build());

        System.out.println("  Users created");

        // Create locations
        Location temple = createLocation("Shree Mahalakshmi Temple", "Parvati, Pune",
                "Pune", "Temple", "Famous temple in Pune for darshan and prasad",
                "https://images.unsplash.com/photo-1548013146-72479768bada?w=400");

        Location bank = createLocation("SBI Kothrud Branch", "Kothrud, Pune",
                "Pune", "Bank", "State Bank of India branch for banking services",
                "https://images.unsplash.com/photo-1541354329998-f4d9a9f9297f?w=400");

        Location rto = createLocation("Pune RTO", "Sangamwadi, Pune",
                "Pune", "Government Office", "Regional Transport Office for driving licenses and vehicle registration",
                "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=400");

        Location railway = createLocation("Pune Railway Station", "Pune Station Area",
                "Pune", "Railway Station", "Main railway station for general and reservation tickets",
                "https://images.unsplash.com/photo-1474487548417-781cb71495f3?w=400");

        Location diagnostic = createLocation("CityCare Diagnostic Centre", "Shivajinagar, Pune",
                "Pune", "Hospital", "Multi-specialty diagnostic centre for health checkups",
                "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?w=400");

        Location govOffice = createLocation("Government Office", "Civil Lines, Pune",
                "Pune", "Government Office", "Government services and document verification",
                "https://images.unsplash.com/photo-1541888946425-d81bb19240f5?w=400");

        System.out.println("  Locations created");

        // Create services for each location
        createService("Darshan", "General darshan service", 8, temple);
        createService("Special Darshan", "VIP darshan with prasad", 5, temple);
        createService("Prasad Counter", "Prasad distribution", 3, temple);

        ServiceEntity cashWithdrawal = createService("Cash Withdrawal", "Cash withdrawal from account", 5, bank);
        createService("Cash Deposit", "Cash deposit to account", 5, bank);
        createService("Account Services", "New account, KYC, etc.", 10, bank);
        createService("Loan Inquiry", "Home, personal, and vehicle loans", 15, bank);

        createService("Driving License", "New/renewal driving license", 15, rto);
        createService("Vehicle Registration", "New vehicle registration", 20, rto);
        createService("Document Verification", "Document verification service", 10, rto);

        createService("General Ticket", "General unreserved ticket", 3, railway);
        createService("Reservation Counter", "Reserved ticket booking", 8, railway);

        createService("Blood Test", "Blood sample collection", 5, diagnostic);
        createService("X-Ray", "X-Ray imaging", 10, diagnostic);
        createService("Health Checkup", "Full body health checkup", 20, diagnostic);

        createService("Certificate Verification", "Document/certificate verification", 10, govOffice);
        createService("Application Submission", "Government application submission", 8, govOffice);

        System.out.println("  Services created");

        // Create counters for each location
        createCounters(temple, 3);
        createCounters(bank, 4);
        createCounters(rto, 3);
        createCounters(railway, 4);
        createCounters(diagnostic, 3);
        createCounters(govOffice, 2);

        System.out.println("  Counters created");

        // Create sample tokens
        createSampleToken("T100", visitor1, bank, cashWithdrawal, Token.Category.NORMAL, Token.Status.COMPLETED);
        createSampleToken("T101", visitor2, bank, cashWithdrawal, Token.Category.SENIOR_CITIZEN, Token.Status.COMPLETED);
        createSampleToken("T102", visitor3, bank, cashWithdrawal, Token.Category.NORMAL, Token.Status.SERVING);
        createSampleToken("T103", visitor1, bank, cashWithdrawal, Token.Category.DIFFERENTLY_ABLED, Token.Status.WAITING);
        createSampleToken("T104", visitor2, bank, cashWithdrawal, Token.Category.NORMAL, Token.Status.WAITING);

        System.out.println("  Sample tokens created");

        // Initialize DSA structures
        initializeDSAStructures();

        System.out.println("=== Database Seeding Complete ===");
    }

    private Location createLocation(String name, String address, String city, String type, String description, String imageUrl) {
        return locationRepository.save(Location.builder()
                .name(name)
                .address(address)
                .city(city)
                .type(type)
                .description(description)
                .imageUrl(imageUrl)
                .active(true)
                .build());
    }

    private ServiceEntity createService(String name, String description, int avgTime, Location location) {
        return serviceRepository.save(ServiceEntity.builder()
                .name(name)
                .description(description)
                .avgServiceTime(avgTime)
                .active(true)
                .location(location)
                .build());
    }

    private void createCounters(Location location, int count) {
        for (int i = 1; i <= count; i++) {
            counterRepository.save(Counter.builder()
                    .counterNumber(i)
                    .location(location)
                    .status(i <= 2 ? Counter.CounterStatus.IDLE : Counter.CounterStatus.IDLE)
                    .tokensInQueue(0)
                    .capacity(20)
                    .build());
        }
    }

    private void createSampleToken(String tokenNumber, User visitor, Location location,
                                   ServiceEntity service, Token.Category category, Token.Status status) {
        int priority = Token.categoryToPriority(category);
        Token token = Token.builder()
                .tokenNumber(tokenNumber)
                .visitor(visitor)
                .location(location)
                .service(service)
                .category(category)
                .priority(priority)
                .status(status)
                .queuePosition(status == Token.Status.WAITING ? 1 : 0)
                .estimatedWaitTime(status == Token.Status.WAITING ? priority * service.getAvgServiceTime() : 0)
                .build();
        tokenRepository.save(token);
    }

    private void initializeDSAStructures() {
        // Initialize counter arrays for each location with DSA structures
        List<Location> locations = locationRepository.findAll();
        for (Location location : locations) {
            List<Counter> counters = counterRepository.findByLocationId(location.getId());
            if (!counters.isEmpty()) {
                queueService.initializeCounters(location.getId(), counters.size());
            }
        }

        // Load waiting tokens into DSA queues
        List<Token> waitingTokens = tokenRepository.findByStatusOrderByCreatedAtDesc(Token.Status.WAITING);
        for (Token token : waitingTokens) {
            queueService.addTokenToQueue(token.getId(), token.getTokenNumber(),
                    token.getService().getId(), token.getPriority());
        }

        System.out.println("  DSA structures initialized with " + waitingTokens.size() + " waiting tokens");
    }
}
