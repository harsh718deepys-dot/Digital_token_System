package com.tokenqueue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class TokenDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateTokenRequest {
        private Long locationId;
        private Long serviceId;
        private String category; // NORMAL, SENIOR_CITIZEN, DIFFERENTLY_ABLED, PREGNANT_WOMAN, EMERGENCY
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TokenResponse {
        private Long id;
        private String tokenNumber;
        private String visitorName;
        private Long visitorId;
        private String locationName;
        private Long locationId;
        private String serviceName;
        private Long serviceId;
        private String category;
        private int priority;
        private String status;
        private int queuePosition;
        private int estimatedWaitTime;
        private Integer counterNumber;
        private Long counterId;
        private LocalDateTime createdAt;
        private LocalDateTime calledAt;
        private LocalDateTime completedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QueueStatusResponse {
        private String serviceName;
        private String locationName;
        private int totalInQueue;
        private int totalServing;
        private int totalCompleted;
        private List<TokenResponse> queueTokens;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardResponse {
        private int totalTokens;
        private int waiting;
        private int serving;
        private int completed;
        private int cancelled;
        private List<CounterStatusDTO> counterStatuses;
        private Map<String, Integer> categoryDistribution;
        private List<QueueChartData> queueOverview;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CounterStatusDTO {
        private Long id;
        private int counterNumber;
        private String status;
        private String currentTokenNumber;
        private int tokensInQueue;
        private int capacity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QueueChartData {
        private String time;
        private int normal;
        private int priority;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CallNextResponse {
        private TokenResponse token;
        private int counterNumber;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatisticsResponse {
        private int totalTokensToday;
        private int avgWaitTime;
        private int peakHourTokens;
        private double completionRate;
        private Map<String, Integer> tokensByCategory;
        private Map<String, Integer> tokensByStatus;
        private Map<String, Integer> tokensByHour;
        private Map<String, Integer> tokensByService;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NotificationDTO {
        private Long id;
        private String title;
        private String message;
        private String type;
        private String tokenNumber;
        private boolean read;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocationDTO {
        private Long id;
        private String name;
        private String address;
        private String city;
        private String type;
        private String description;
        private String imageUrl;
        private boolean active;
        private List<ServiceDTO> services;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ServiceDTO {
        private Long id;
        private String name;
        private String description;
        private int avgServiceTime;
        private boolean active;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptimizationResponse {
        private int[] optimalAllocation;
        private int minimizedMaxWaitTime;
        private int totalWaitTime;
        private String comparison;
    }
}
