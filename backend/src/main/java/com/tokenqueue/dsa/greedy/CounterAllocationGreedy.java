package com.tokenqueue.dsa.greedy;

import com.tokenqueue.dsa.array.CounterArray;

/**
 * CounterAllocationGreedy - Greedy Algorithm for Counter Assignment
 * ==================================================================
 * 
 * This class uses a GREEDY ALGORITHM to assign incoming tokens
 * to the most suitable counter.
 * 
 * WHAT IS A GREEDY ALGORITHM?
 * A greedy algorithm makes the locally optimal choice at each step,
 * hoping that these local choices lead to a globally optimal solution.
 * 
 * GREEDY CHOICE HERE:
 * For each new token, select the counter with the SMALLEST current workload.
 * This distributes tokens as evenly as possible across counters.
 * 
 * WHY GREEDY?
 * - Simple and fast: O(n) per allocation where n = number of counters.
 * - Works well in practice for load balancing.
 * - Makes a decision immediately without considering future tokens.
 * 
 * EXAMPLE:
 *   Counter 1 → 8 tokens
 *   Counter 2 → 4 tokens
 *   Counter 3 → 12 tokens
 *   Counter 4 → 2 tokens  ← Minimum load!
 *   
 *   New token T110 arrives → Assigned to Counter 4 (greedy choice).
 *   
 *   After assignment:
 *   Counter 1 → 8 tokens
 *   Counter 2 → 4 tokens
 *   Counter 3 → 12 tokens
 *   Counter 4 → 3 tokens
 * 
 * LIMITATIONS:
 * - Greedy doesn't guarantee globally optimal solutions.
 * - It doesn't consider future token arrivals.
 * - It doesn't account for varying service times per counter.
 * - For example, a counter with 2 tokens doing long tasks may actually
 *   take longer than a counter with 5 tokens doing quick tasks.
 * 
 * Despite limitations, greedy is the most practical approach for
 * real-time counter allocation where decisions must be instant.
 * 
 * Time Complexity: O(n) per allocation (n = number of counters)
 * Space Complexity: O(1) additional space
 * 
 * @author TokenQueue DSA Project
 */
public class CounterAllocationGreedy {

    // Reference to the counter array managed by this algorithm
    private CounterArray counterArray;

    /**
     * Constructor: takes a reference to the CounterArray.
     * The greedy algorithm operates on the counter data.
     * 
     * @param counterArray the array of counters to allocate from
     */
    public CounterAllocationGreedy(CounterArray counterArray) {
        this.counterArray = counterArray;
    }

    /**
     * ALLOCATE COUNTER (Greedy Strategy):
     * Assigns a new token to the counter with the minimum current workload.
     * 
     * GREEDY CHOICE:
     * "Pick the counter with the fewest tokens right now."
     * This is the locally optimal decision — we don't consider
     * what tokens might arrive in the future.
     * 
     * Algorithm:
     * 1. Traverse all counters (using CounterArray.findMinLoadCounter).
     * 2. Find the active counter with the smallest load that is not at capacity.
     * 3. Assign the token to that counter (increment its load).
     * 4. Return the counter index.
     * 
     * Time Complexity: O(n) where n = number of counters
     *   - We must check every counter to find the minimum.
     * 
     * Space Complexity: O(1) — no extra data structures needed.
     * 
     * @return the 0-based index of the allocated counter, or -1 if no counter available
     */
    public int allocateCounter() {
        // GREEDY STEP: Find the counter with minimum load
        int minCounter = counterArray.findMinLoadCounter();

        if (minCounter == -1) {
            // No active counter is available (all at capacity or inactive)
            return -1;
        }

        // Assign the token by incrementing the counter's load
        counterArray.incrementLoad(minCounter);

        return minCounter;
    }

    /**
     * ALLOCATE COUNTER WITH PRIORITY:
     * Enhanced greedy allocation that considers priority.
     * Emergency/Priority tokens may be assigned to dedicated counters or
     * the counter with the fastest estimated service time.
     * 
     * GREEDY STRATEGY:
     * For high-priority tokens (priority 1-2):
     *   - Prefer counters with the LOWEST load (fastest service).
     * For normal tokens (priority 3-5):
     *   - Standard min-load allocation.
     * 
     * This gives priority visitors slightly better counter assignments
     * (counters with fewer people ahead of them).
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(1)
     * 
     * @param priority the priority level (1=highest to 5=lowest)
     * @return the allocated counter index, or -1 if none available
     */
    public int allocateCounterWithPriority(int priority) {
        if (priority <= 2) {
            // HIGH PRIORITY: Find the absolute minimum load counter
            // This ensures priority visitors get the fastest counter
            int bestCounter = -1;
            int bestLoad = Integer.MAX_VALUE;

            for (int i = 0; i < counterArray.getNumCounters(); i++) {
                if (counterArray.isAvailable(i)) {
                    int load = counterArray.getLoad(i);
                    if (load < bestLoad) {
                        bestLoad = load;
                        bestCounter = i;
                    }
                }
            }

            if (bestCounter != -1) {
                counterArray.incrementLoad(bestCounter);
                return bestCounter;
            }
        }

        // Normal priority or no priority counter found: standard allocation
        return allocateCounter();
    }

    /**
     * RELEASE COUNTER:
     * Called when a token is completed at a counter.
     * Decrements the counter's workload.
     * 
     * Time Complexity: O(1)
     * 
     * @param counterIndex the counter that completed a token
     */
    public void releaseCounter(int counterIndex) {
        counterArray.decrementLoad(counterIndex);
    }

    /**
     * ESTIMATE WAIT TIME:
     * Estimates the waiting time for a token at a given counter.
     * 
     * Simple estimation formula:
     *   waitTime = tokensAhead * averageServiceTime
     * 
     * We use a default average service time of 5 minutes per token.
     * 
     * Time Complexity: O(1)
     * 
     * @param counterIndex     the counter to estimate for
     * @param avgServiceTimeMin average service time in minutes per token
     * @return estimated wait time in minutes
     */
    public int estimateWaitTime(int counterIndex, int avgServiceTimeMin) {
        if (counterIndex < 0 || counterIndex >= counterArray.getNumCounters()) {
            return -1;
        }
        // Estimated wait = tokens ahead × average service time
        return counterArray.getLoad(counterIndex) * avgServiceTimeMin;
    }

    /**
     * ESTIMATE WAIT TIME (Using Best Counter):
     * Estimates the minimum possible wait time if a token were
     * assigned to the optimal counter right now.
     * 
     * Time Complexity: O(n) — must find the min load counter
     * 
     * @param avgServiceTimeMin average service time in minutes
     * @return estimated minimum wait time
     */
    public int estimateMinWaitTime(int avgServiceTimeMin) {
        int minCounter = counterArray.findMinLoadCounter();
        if (minCounter == -1) return -1;
        return counterArray.getLoad(minCounter) * avgServiceTimeMin;
    }

    /**
     * GET LOAD DISTRIBUTION:
     * Returns the current load distribution as a string.
     * Useful for monitoring and debugging.
     * 
     * Time Complexity: O(n)
     */
    public String getLoadDistribution() {
        StringBuilder sb = new StringBuilder("Load Distribution (Greedy Allocation):\n");
        for (int i = 0; i < counterArray.getNumCounters(); i++) {
            sb.append("  Counter ").append(i + 1)
              .append(": ").append(counterArray.getLoad(i))
              .append(" tokens")
              .append(counterArray.isActive(i) ? " [Active]" : " [Inactive]")
              .append("\n");
        }
        sb.append("  Total: ").append(counterArray.getTotalLoad()).append(" tokens\n");
        sb.append("  Average: ").append(String.format("%.1f", counterArray.getAverageLoad())).append(" tokens/counter\n");
        return sb.toString();
    }

    /**
     * IS BALANCED:
     * Checks if the load is reasonably balanced across counters.
     * A balanced distribution means no counter has more than 2x the average load.
     * 
     * Time Complexity: O(n)
     * 
     * @return true if load distribution is considered balanced
     */
    public boolean isBalanced() {
        double avg = counterArray.getAverageLoad();
        if (avg == 0) return true;

        for (int i = 0; i < counterArray.getNumCounters(); i++) {
            if (counterArray.isActive(i) && counterArray.getLoad(i) > 2 * avg) {
                return false;
            }
        }
        return true;
    }
}
