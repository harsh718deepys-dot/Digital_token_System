package com.tokenqueue.dsa.dp;

/**
 * SlotOptimizationDP - Dynamic Programming for Waiting Time Optimization
 * ========================================================================
 * 
 * This class uses DYNAMIC PROGRAMMING to optimize how tokens are distributed
 * across counters to MINIMIZE the total (or maximum) waiting time.
 * 
 * PROBLEM STATEMENT:
 * Given N tokens and K counters, each counter can handle tokens at different rates.
 * Distribute all N tokens across K counters such that the maximum waiting time
 * (across all counters) is minimized.
 * 
 * This is similar to the "Load Balancing" or "Job Scheduling on Machines" problem.
 * 
 * DYNAMIC PROGRAMMING APPROACH:
 * 
 * STATE:
 *   dp[i][j] = the minimum possible "maximum load" when distributing
 *              the first 'i' tokens across the first 'j' counters.
 * 
 * RECURRENCE:
 *   For each way to split 'i' tokens between counter 'j' and counters 1..(j-1):
 *   dp[i][j] = min over all splits k (0 ≤ k ≤ i):
 *                max(dp[k][j-1], cost(k+1, i))
 *   
 *   Where cost(k+1, i) is the time for counter j to serve tokens k+1 to i.
 *   
 *   In our simplified model:
 *   cost(k+1, i) = (i - k) * avgServiceTime
 * 
 * BASE CASE:
 *   dp[0][j] = 0 for all j (no tokens → no wait time)
 *   dp[i][1] = i * avgServiceTime (all tokens on one counter)
 * 
 * TRANSITION:
 *   We try every possible split point and take the minimum.
 * 
 * ANSWER:
 *   dp[N][K] gives the minimum possible maximum wait time.
 *   We also track the actual allocation using a separate 'allocation' array.
 * 
 * WHY DP AND NOT GREEDY?
 * Greedy assigns tokens one at a time without looking ahead.
 * DP considers ALL possible distributions to find the true optimum.
 * However, DP is more computationally expensive (O(N² × K)).
 * 
 * WHEN IS DP USED IN THIS PROJECT?
 * - Admin can trigger "Optimize Allocation" to rebalance counters.
 * - Used during batch assignment when multiple tokens arrive at once.
 * - Provides an optimal baseline to compare against greedy allocation.
 * 
 * Time Complexity: O(N² × K) where N = tokens, K = counters
 * Space Complexity: O(N × K) for the DP table
 * 
 * @author TokenQueue DSA Project
 */
public class SlotOptimizationDP {

    /**
     * OptimizationResult holds the output of the DP optimization.
     */
    public static class OptimizationResult {
        private int[] allocation;          // Number of tokens allocated to each counter
        private int minimizedMaxWaitTime;  // The minimized maximum wait time
        private int totalWaitTime;         // Total wait time across all counters

        public OptimizationResult(int[] allocation, int minimizedMaxWaitTime, int totalWaitTime) {
            this.allocation = allocation;
            this.minimizedMaxWaitTime = minimizedMaxWaitTime;
            this.totalWaitTime = totalWaitTime;
        }

        public int[] getAllocation() { return allocation; }
        public int getMinimizedMaxWaitTime() { return minimizedMaxWaitTime; }
        public int getTotalWaitTime() { return totalWaitTime; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("DP Optimization Result:\n");
            sb.append("  Minimized Max Wait Time: ").append(minimizedMaxWaitTime).append(" minutes\n");
            sb.append("  Total Wait Time: ").append(totalWaitTime).append(" minutes\n");
            sb.append("  Allocation: ");
            for (int i = 0; i < allocation.length; i++) {
                sb.append("Counter ").append(i + 1).append("=").append(allocation[i]);
                if (i < allocation.length - 1) sb.append(", ");
            }
            return sb.toString();
        }
    }

    /**
     * OPTIMIZE ALLOCATION (Main DP Algorithm):
     * Distributes 'totalTokens' across 'numCounters' to minimize
     * the maximum waiting time at any single counter.
     * 
     * DP TABLE:
     *   dp[i][j] represents the minimum possible maximum load when
     *   distributing i tokens across j counters.
     * 
     * ALGORITHM STEPS:
     * 1. Initialize base cases.
     * 2. Fill the DP table bottom-up (tabulation).
     * 3. Backtrack to find the actual allocation.
     * 
     * MEMOIZATION vs TABULATION:
     * We use TABULATION (bottom-up) here because:
     * - We need to fill the entire table anyway.
     * - Bottom-up avoids recursion overhead.
     * - Easier to understand and implement iteratively.
     * 
     * Time Complexity: O(N² × K)
     *   - For each cell dp[i][j], we try O(N) split points.
     *   - There are O(N × K) cells.
     * 
     * Space Complexity: O(N × K) for the DP table.
     * 
     * @param totalTokens    total number of tokens to distribute
     * @param numCounters    number of available counters
     * @param avgServiceTime average service time per token in minutes
     * @return OptimizationResult with the optimal allocation
     */
    public OptimizationResult optimizeAllocation(int totalTokens, int numCounters, int avgServiceTime) {
        // Edge cases
        if (totalTokens <= 0 || numCounters <= 0) {
            return new OptimizationResult(new int[numCounters], 0, 0);
        }
        if (numCounters == 1) {
            // Only one counter: all tokens must go there
            int[] alloc = {totalTokens};
            int waitTime = totalTokens * avgServiceTime;
            return new OptimizationResult(alloc, waitTime, waitTime);
        }
        if (totalTokens <= numCounters) {
            // Fewer tokens than counters: one token per counter
            int[] alloc = new int[numCounters];
            for (int i = 0; i < totalTokens; i++) {
                alloc[i] = 1;
            }
            return new OptimizationResult(alloc, avgServiceTime, totalTokens * avgServiceTime);
        }

        // ========================================
        // STEP 1: Initialize the DP Table
        // ========================================
        
        // dp[i][j] = min max load when distributing i tokens across j counters
        int[][] dp = new int[totalTokens + 1][numCounters + 1];
        
        // split[i][j] = the optimal split point for backtracking
        // It records how many tokens go to counters 1..(j-1) in the optimal split
        int[][] split = new int[totalTokens + 1][numCounters + 1];

        // Initialize with a large value (infinity)
        for (int i = 0; i <= totalTokens; i++) {
            for (int j = 0; j <= numCounters; j++) {
                dp[i][j] = Integer.MAX_VALUE;
            }
        }

        // ========================================
        // STEP 2: Base Cases
        // ========================================

        // BASE CASE 1: Zero tokens → zero wait time for any number of counters
        for (int j = 0; j <= numCounters; j++) {
            dp[0][j] = 0;
        }

        // BASE CASE 2: All tokens on one counter
        // dp[i][1] = i * avgServiceTime
        for (int i = 0; i <= totalTokens; i++) {
            dp[i][1] = i * avgServiceTime;
            split[i][1] = 0; // No split needed for 1 counter
        }

        // ========================================
        // STEP 3: Fill DP Table (Tabulation / Bottom-Up)
        // ========================================

        // For each number of counters j (from 2 to K)
        for (int j = 2; j <= numCounters; j++) {
            // For each number of tokens i (from 1 to N)
            for (int i = 1; i <= totalTokens; i++) {

                // TRANSITION:
                // Try every possible split point k.
                // k tokens go to the first (j-1) counters.
                // (i-k) tokens go to counter j.
                //
                // dp[i][j] = min over k: max(dp[k][j-1], (i-k) * avgServiceTime)
                //
                // We want the split that minimizes the MAXIMUM load.

                for (int k = 0; k <= i; k++) {
                    // Load on counters 1..(j-1) with k tokens = dp[k][j-1]
                    // Load on counter j with (i-k) tokens = (i-k) * avgServiceTime
                    int loadOnPrevCounters = dp[k][j - 1];
                    int loadOnCurrentCounter = (i - k) * avgServiceTime;

                    // The bottleneck is the MAX of the two
                    int maxLoad = Math.max(loadOnPrevCounters, loadOnCurrentCounter);

                    // Is this split better than what we've found so far?
                    if (maxLoad < dp[i][j]) {
                        dp[i][j] = maxLoad;
                        split[i][j] = k; // Remember the split point
                    }
                }
            }
        }

        // ========================================
        // STEP 4: Backtrack to Find Actual Allocation
        // ========================================

        // The optimal minimum maximum wait time is dp[totalTokens][numCounters]
        int minimizedMaxWait = dp[totalTokens][numCounters];

        // Backtrack through the split table to find how many tokens each counter gets
        int[] allocation = new int[numCounters];
        int remainingTokens = totalTokens;

        // Start from the last counter and work backwards
        for (int j = numCounters; j >= 2; j--) {
            int k = split[remainingTokens][j];
            allocation[j - 1] = remainingTokens - k; // Counter j gets (remaining - k) tokens
            remainingTokens = k;
        }
        allocation[0] = remainingTokens; // Counter 1 gets the rest

        // Calculate total wait time
        int totalWait = 0;
        for (int count : allocation) {
            totalWait += count * avgServiceTime;
        }

        return new OptimizationResult(allocation, minimizedMaxWait, totalWait);
    }

    /**
     * OPTIMIZE WITH VARYING SERVICE TIMES:
     * A more advanced version where each counter can have
     * a different average service time.
     * 
     * This accounts for the fact that some counters are faster than others
     * (e.g., experienced staff at one counter vs. new staff at another).
     * 
     * STATE:
     *   dp[i][j] = min max wait when distributing i tokens across counters 1..j,
     *              where each counter has its own service time.
     * 
     * RECURRENCE:
     *   dp[i][j] = min over k: max(dp[k][j-1], (i-k) * serviceTimes[j-1])
     * 
     * Time Complexity: O(N² × K)
     * Space Complexity: O(N × K)
     * 
     * @param totalTokens  total tokens to distribute
     * @param serviceTimes array of average service times per counter
     * @return OptimizationResult
     */
    public OptimizationResult optimizeWithVaryingServiceTimes(int totalTokens, int[] serviceTimes) {
        int numCounters = serviceTimes.length;

        if (totalTokens <= 0 || numCounters <= 0) {
            return new OptimizationResult(new int[numCounters], 0, 0);
        }

        int[][] dp = new int[totalTokens + 1][numCounters + 1];
        int[][] split = new int[totalTokens + 1][numCounters + 1];

        // Initialize with infinity
        for (int i = 0; i <= totalTokens; i++) {
            for (int j = 0; j <= numCounters; j++) {
                dp[i][j] = Integer.MAX_VALUE;
            }
        }

        // Base case: zero tokens
        for (int j = 0; j <= numCounters; j++) {
            dp[0][j] = 0;
        }

        // Base case: one counter
        for (int i = 0; i <= totalTokens; i++) {
            dp[i][1] = i * serviceTimes[0];
        }

        // Fill DP table
        for (int j = 2; j <= numCounters; j++) {
            for (int i = 1; i <= totalTokens; i++) {
                for (int k = 0; k <= i; k++) {
                    int loadOnPrev = dp[k][j - 1];
                    int loadOnCurrent = (i - k) * serviceTimes[j - 1];
                    int maxLoad = Math.max(loadOnPrev, loadOnCurrent);

                    if (maxLoad < dp[i][j]) {
                        dp[i][j] = maxLoad;
                        split[i][j] = k;
                    }
                }
            }
        }

        // Backtrack
        int[] allocation = new int[numCounters];
        int remaining = totalTokens;
        for (int j = numCounters; j >= 2; j--) {
            int k = split[remaining][j];
            allocation[j - 1] = remaining - k;
            remaining = k;
        }
        allocation[0] = remaining;

        // Calculate total wait
        int totalWait = 0;
        for (int i = 0; i < numCounters; i++) {
            totalWait += allocation[i] * serviceTimes[i];
        }

        return new OptimizationResult(allocation, dp[totalTokens][numCounters], totalWait);
    }

    /**
     * COMPARE GREEDY VS DP:
     * Compares the greedy allocation result with the DP optimal result.
     * This demonstrates WHY DP can be better than greedy in some cases.
     * 
     * @param totalTokens    total tokens
     * @param numCounters    number of counters
     * @param avgServiceTime average service time
     * @param greedyLoads    the current loads from greedy allocation
     * @return a comparison string
     */
    public String compareGreedyVsDP(int totalTokens, int numCounters, int avgServiceTime, int[] greedyLoads) {
        // Get DP optimal result
        OptimizationResult dpResult = optimizeAllocation(totalTokens, numCounters, avgServiceTime);

        // Calculate greedy max wait
        int greedyMaxWait = 0;
        int greedyTotalWait = 0;
        for (int load : greedyLoads) {
            int wait = load * avgServiceTime;
            greedyMaxWait = Math.max(greedyMaxWait, wait);
            greedyTotalWait += wait;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("===== Greedy vs DP Comparison =====\n\n");
        
        sb.append("GREEDY Result:\n");
        for (int i = 0; i < greedyLoads.length; i++) {
            sb.append("  Counter ").append(i + 1).append(": ").append(greedyLoads[i]).append(" tokens\n");
        }
        sb.append("  Max Wait: ").append(greedyMaxWait).append(" min\n");
        sb.append("  Total Wait: ").append(greedyTotalWait).append(" min\n\n");
        
        sb.append("DP OPTIMAL Result:\n");
        for (int i = 0; i < dpResult.getAllocation().length; i++) {
            sb.append("  Counter ").append(i + 1).append(": ")
              .append(dpResult.getAllocation()[i]).append(" tokens\n");
        }
        sb.append("  Max Wait: ").append(dpResult.getMinimizedMaxWaitTime()).append(" min\n");
        sb.append("  Total Wait: ").append(dpResult.getTotalWaitTime()).append(" min\n\n");
        
        int improvement = greedyMaxWait - dpResult.getMinimizedMaxWaitTime();
        sb.append("DP IMPROVEMENT: ").append(improvement).append(" minutes less max wait time.\n");
        
        return sb.toString();
    }
}
