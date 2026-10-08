package com.tokenqueue.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tokenNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visitor_id")
    private User visitor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private int priority; // 1=Emergency, 2=Senior, 3=Disabled, 4=Pregnant, 5=Normal

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private int queuePosition;

    private int estimatedWaitTime; // in minutes

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counter_id")
    private Counter counter;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime calledAt;

    private LocalDateTime completedAt;

    public enum Category {
        NORMAL, SENIOR_CITIZEN, DIFFERENTLY_ABLED, PREGNANT_WOMAN, EMERGENCY
    }

    public enum Status {
        WAITING, CALLED, SERVING, COMPLETED, CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = Status.WAITING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Maps category to priority number.
     * Lower number = Higher priority.
     */
    public static int categoryToPriority(Category category) {
        switch (category) {
            case EMERGENCY: return 1;
            case SENIOR_CITIZEN: return 2;
            case DIFFERENTLY_ABLED: return 3;
            case PREGNANT_WOMAN: return 4;
            case NORMAL: return 5;
            default: return 5;
        }
    }
}
