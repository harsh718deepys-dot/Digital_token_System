package com.tokenqueue.service;

import com.tokenqueue.dsa.array.CounterArray;
import com.tokenqueue.dsa.greedy.CounterAllocationGreedy;
import com.tokenqueue.dsa.dp.SlotOptimizationDP;
import com.tokenqueue.dsa.hashing.TokenHashTable;
import com.tokenqueue.dsa.heap.PriorityTokenHeap;
import com.tokenqueue.dsa.queue.TokenQueue;
import com.tokenqueue.dsa.stack.TokenHistoryStack;
import com.tokenqueue.model.Token;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * QueueManagementService - DSA Engine
 * ====================================
 * 
 * This service is the CORE DSA ENGINE of the project.
 * It manages the in-memory data structures that drive the queuing system.
 * 
 * Data Flow:
 *   Controller → TokenService → QueueManagementService (DSA) → Repository (DB)
 * 
 * DSA Structures Used:
 *   1. TokenQueue (FIFO)         → Normal visitor queue
 *   2. PriorityTokenHeap (Heap)  → Priority visitor queue
 *   3. TokenHashTable (Hashing)  → Fast token lookup by ID
 *   4. TokenHistoryStack (Stack) → Recently completed tokens
 *   5. CounterArray (Array)      → Counter load tracking
 *   6. CounterAllocationGreedy   → Greedy counter assignment
 *   7. SlotOptimizationDP        → DP-based optimization
 */
@Service
public class QueueManagementService {

    // ===== DSA Data Structures =====

    // Per-service queues: serviceId → normal queue
    private final Map<Long, TokenQueue<Long>> normalQueues = new ConcurrentHashMap<>();

    // Per-service priority heaps: serviceId → priority heap
    private final Map<Long, PriorityTokenHeap<Long>> priorityHeaps = new ConcurrentHashMap<>();

    // Global hash table for fast token lookup: tokenNumber → tokenId
    private final TokenHashTable<String, Long> tokenLookup = new TokenHashTable<>();

    // Global history stack for recently completed tokens
    private final TokenHistoryStack<Long> completedHistory = new TokenHistoryStack<>(200);

    // Per-location counter arrays: locationId → counter array
    private final Map<Long, CounterArray> counterArrays = new ConcurrentHashMap<>();

    // Per-location greedy allocators: locationId → greedy allocator
    private final Map<Long, CounterAllocationGreedy> greedyAllocators = new ConcurrentHashMap<>();

    // DP optimizer (stateless, can be shared)
    private final SlotOptimizationDP dpOptimizer = new SlotOptimizationDP();

    // ===== Queue Management =====

    /**
     * Adds a token to the appropriate queue based on its priority.
     * Normal tokens (priority 5) go to the FIFO queue.
     * Priority tokens (1-4) go to the min-heap.
     * All tokens are registered in the hash table for fast lookup.
     */
    public void addTokenToQueue(Long tokenId, String tokenNumber, Long serviceId, int priority) {
        // Register in hash table for O(1) lookup
        tokenLookup.insert(tokenNumber, tokenId);

        if (priority >= 5) {
            // NORMAL token → FIFO Queue
            normalQueues.computeIfAbsent(serviceId, k -> new TokenQueue<>());
            normalQueues.get(serviceId).enqueue(tokenId);
        } else {
            // PRIORITY token → Min-Heap
            priorityHeaps.computeIfAbsent(serviceId, k -> new PriorityTokenHeap<>());
            priorityHeaps.get(serviceId).insert(tokenId, priority);
        }
    }

    /**
     * Gets the next token to be served for a given service.
     * Priority tokens (from heap) are served before normal tokens (from queue).
     * 
     * Algorithm:
     * 1. Check if priority heap has any tokens → serve that first.
     * 2. If no priority tokens, check normal queue → serve from FIFO.
     * 3. If both are empty, return null (no one waiting).
     */
    public Long getNextToken(Long serviceId) {
        // First check priority heap
        PriorityTokenHeap<Long> heap = priorityHeaps.get(serviceId);
        if (heap != null && !heap.isEmpty()) {
            return heap.extractMin();
        }

        // Then check normal queue
        TokenQueue<Long> queue = normalQueues.get(serviceId);
        if (queue != null && !queue.isEmpty()) {
            return queue.dequeue();
        }

        return null; // No tokens waiting
    }

    /**
     * Looks up a token ID by its token number using the hash table.
     * Time Complexity: O(1) average case.
     */
    public Long lookupToken(String tokenNumber) {
        return tokenLookup.search(tokenNumber);
    }

    /**
     * Removes a token from the queue (for cancellation).
     */
    public boolean removeTokenFromQueue(Long tokenId, String tokenNumber, Long serviceId, int priority) {
        // Remove from hash table
        tokenLookup.delete(tokenNumber);

        if (priority >= 5) {
            TokenQueue<Long> queue = normalQueues.get(serviceId);
            return queue != null && queue.remove(tokenId);
        } else {
            PriorityTokenHeap<Long> heap = priorityHeaps.get(serviceId);
            return heap != null && heap.remove(tokenId);
        }
    }

    /**
     * Gets the queue position of a token.
     */
    public int getQueuePosition(Long tokenId, Long serviceId, int priority) {
        if (priority >= 5) {
            TokenQueue<Long> queue = normalQueues.get(serviceId);
            if (queue != null) {
                // Count priority tokens ahead (they'll be served first)
                PriorityTokenHeap<Long> heap = priorityHeaps.get(serviceId);
                int priorityAhead = (heap != null) ? heap.size() : 0;
                int normalPosition = queue.getPosition(tokenId);
                return normalPosition > 0 ? priorityAhead + normalPosition : -1;
            }
        } else {
            PriorityTokenHeap<Long> heap = priorityHeaps.get(serviceId);
            if (heap != null) {
                return heap.getPosition(tokenId);
            }
        }
        return -1;
    }

    /**
     * Gets total number of people waiting for a service.
     */
    public int getTotalWaiting(Long serviceId) {
        int normalCount = 0;
        int priorityCount = 0;

        TokenQueue<Long> queue = normalQueues.get(serviceId);
        if (queue != null) normalCount = queue.size();

        PriorityTokenHeap<Long> heap = priorityHeaps.get(serviceId);
        if (heap != null) priorityCount = heap.size();

        return normalCount + priorityCount;
    }

    // ===== History Management (Stack) =====

    /**
     * Pushes a completed token onto the history stack.
     */
    public void addToHistory(Long tokenId) {
        completedHistory.push(tokenId);
    }

    /**
     * Gets recent completed token IDs.
     */
    public Object[] getRecentHistory(int count) {
        return completedHistory.getRecent(count);
    }

    // ===== Counter Management (Array + Greedy) =====

    /**
     * Initializes counter tracking for a location.
     */
    public void initializeCounters(Long locationId, int numCounters) {
        CounterArray counterArray = new CounterArray(numCounters);
        counterArrays.put(locationId, counterArray);
        greedyAllocators.put(locationId, new CounterAllocationGreedy(counterArray));
    }

    /**
     * Allocates the best counter using greedy algorithm.
     * Returns the 0-based counter index, or -1 if no counter available.
     */
    public int allocateCounter(Long locationId, int priority) {
        CounterAllocationGreedy allocator = greedyAllocators.get(locationId);
        if (allocator == null) return -1;
        return allocator.allocateCounterWithPriority(priority);
    }

    /**
     * Releases a counter after a token is completed.
     */
    public void releaseCounter(Long locationId, int counterIndex) {
        CounterAllocationGreedy allocator = greedyAllocators.get(locationId);
        if (allocator != null) {
            allocator.releaseCounter(counterIndex);
        }
    }

    /**
     * Estimates wait time using greedy allocator.
     */
    public int estimateWaitTime(Long locationId, int avgServiceTime) {
        CounterAllocationGreedy allocator = greedyAllocators.get(locationId);
        if (allocator == null) return -1;
        return allocator.estimateMinWaitTime(avgServiceTime);
    }

    /**
     * Gets counter loads for a location.
     */
    public int[] getCounterLoads(Long locationId) {
        CounterArray array = counterArrays.get(locationId);
        if (array == null) return new int[0];
        return array.getAllLoads();
    }

    /**
     * Gets counter status info.
     */
    public CounterArray getCounterArray(Long locationId) {
        return counterArrays.get(locationId);
    }

    // ===== DP Optimization =====

    /**
     * Runs DP optimization for counter allocation.
     */
    public SlotOptimizationDP.OptimizationResult optimizeAllocation(
            int totalTokens, int numCounters, int avgServiceTime) {
        return dpOptimizer.optimizeAllocation(totalTokens, numCounters, avgServiceTime);
    }

    /**
     * Compares greedy vs DP allocation.
     */
    public String compareAllocations(Long locationId, int totalTokens, int avgServiceTime) {
        CounterArray array = counterArrays.get(locationId);
        if (array == null) return "No counter data available.";

        return dpOptimizer.compareGreedyVsDP(
            totalTokens,
            array.getNumCounters(),
            avgServiceTime,
            array.getAllLoads()
        );
    }

    // ===== Hash Table Info =====

    public int getHashTableSize() {
        return tokenLookup.size();
    }

    public boolean isTokenRegistered(String tokenNumber) {
        return tokenLookup.contains(tokenNumber);
    }
}
