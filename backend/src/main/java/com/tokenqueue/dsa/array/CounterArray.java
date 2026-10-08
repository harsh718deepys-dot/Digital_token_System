package com.tokenqueue.dsa.array;

/**
 * CounterArray - Custom Array-Based Data Structure for Counter Management
 * ========================================================================
 * 
 * This class manages service counters using a fixed-size array.
 * Each element in the array represents one counter and stores
 * its current workload (number of tokens being handled).
 * 
 * WHY AN ARRAY?
 * Counters are a fixed resource (e.g., a bank has 5 counters).
 * Arrays are ideal for fixed-size, index-accessible data.
 * Direct access by counter number gives O(1) performance.
 * 
 * ARRAY OPERATIONS DEMONSTRATED:
 *   - Traversal: Finding the counter with minimum/maximum load
 *   - Insertion: Adding workload to a counter
 *   - Deletion: Removing workload from a counter
 *   - Searching: Finding a specific counter's status
 *   - Updating: Changing counter capacity/load
 * 
 * Example array for 5 counters:
 *   Index:    [0]  [1]  [2]  [3]  [4]
 *   Load:      8    4   12    2    6
 *   Counter:   1    2    3    4    5
 *   
 *   Counter 4 has the least load (2 tokens) → assign next token here.
 * 
 * Time Complexity Summary:
 *   getLoad()        → O(1)  (direct index access)
 *   setLoad()        → O(1)
 *   increment()      → O(1)
 *   decrement()      → O(1)
 *   findMinLoad()    → O(n)  (must check all counters)
 *   findMaxLoad()    → O(n)
 *   getTotalLoad()   → O(n)
 *   getAverageLoad() → O(n)
 * 
 * Space Complexity: O(n) where n = number of counters.
 * 
 * @author TokenQueue DSA Project
 */
public class CounterArray {

    // Array storing the workload (number of tokens) for each counter
    private int[] loads;

    // Array storing the active/inactive status of each counter
    private boolean[] active;

    // Array storing the capacity (max tokens) for each counter
    private int[] capacities;

    // The total number of counters
    private final int numCounters;

    // Default capacity per counter
    private static final int DEFAULT_CAPACITY = 20;

    /**
     * Constructor: creates an array of counters.
     * All counters start with zero load and default capacity.
     * 
     * @param numCounters the fixed number of counters
     */
    public CounterArray(int numCounters) {
        this.numCounters = numCounters;
        this.loads = new int[numCounters];
        this.active = new boolean[numCounters];
        this.capacities = new int[numCounters];

        // Initialize all counters as active with default capacity and zero load
        for (int i = 0; i < numCounters; i++) {
            loads[i] = 0;
            active[i] = true;
            capacities[i] = DEFAULT_CAPACITY;
        }
    }

    // ========== ARRAY ACCESS OPERATIONS ==========

    /**
     * GET LOAD (Direct Array Access):
     * Returns the current workload of a specific counter.
     * 
     * Uses direct index access: loads[counterIndex]
     * This is why arrays are powerful — O(1) random access.
     * 
     * Time Complexity: O(1) — direct index access
     * 
     * @param counterIndex the 0-based index of the counter
     * @return the current load of the counter
     */
    public int getLoad(int counterIndex) {
        validateIndex(counterIndex);
        return loads[counterIndex];
    }

    /**
     * SET LOAD (Array Update):
     * Sets the workload of a specific counter.
     * 
     * Time Complexity: O(1)
     */
    public void setLoad(int counterIndex, int load) {
        validateIndex(counterIndex);
        loads[counterIndex] = load;
    }

    /**
     * INCREMENT LOAD (Array Update):
     * Increases the workload of a counter by 1.
     * Called when a new token is assigned to this counter.
     * 
     * Time Complexity: O(1)
     */
    public void incrementLoad(int counterIndex) {
        validateIndex(counterIndex);
        loads[counterIndex]++;
    }

    /**
     * DECREMENT LOAD (Array Update):
     * Decreases the workload of a counter by 1.
     * Called when a token is completed at this counter.
     * 
     * Time Complexity: O(1)
     */
    public void decrementLoad(int counterIndex) {
        validateIndex(counterIndex);
        if (loads[counterIndex] > 0) {
            loads[counterIndex]--;
        }
    }

    // ========== ARRAY TRAVERSAL OPERATIONS ==========

    /**
     * FIND MINIMUM LOAD (Linear Search/Traversal):
     * Traverses the entire array to find the counter with the LEAST workload.
     * This is used by the Greedy algorithm to assign new tokens.
     * 
     * Algorithm:
     * 1. Start with the first active counter as the minimum.
     * 2. Compare each subsequent active counter.
     * 3. If a counter has less load, it becomes the new minimum.
     * 4. Return the index of the counter with minimum load.
     * 
     * Time Complexity: O(n) — must check every counter.
     * Space Complexity: O(1) — only stores the current minimum.
     * 
     * @return the 0-based index of the counter with minimum load, or -1 if none active
     */
    public int findMinLoadCounter() {
        int minIndex = -1;
        int minLoad = Integer.MAX_VALUE;

        // TRAVERSAL: Check each counter's load
        for (int i = 0; i < numCounters; i++) {
            // Only consider active counters that aren't at capacity
            if (active[i] && loads[i] < minLoad && loads[i] < capacities[i]) {
                minLoad = loads[i];
                minIndex = i;
            }
        }

        return minIndex;
    }

    /**
     * FIND MAXIMUM LOAD (Linear Search/Traversal):
     * Finds the counter with the MOST workload.
     * Useful for monitoring and load balancing.
     * 
     * Time Complexity: O(n)
     * 
     * @return the index of the busiest counter
     */
    public int findMaxLoadCounter() {
        int maxIndex = -1;
        int maxLoad = Integer.MIN_VALUE;

        for (int i = 0; i < numCounters; i++) {
            if (active[i] && loads[i] > maxLoad) {
                maxLoad = loads[i];
                maxIndex = i;
            }
        }

        return maxIndex;
    }

    /**
     * GET TOTAL LOAD (Array Summation):
     * Traverses the entire array to sum all counter loads.
     * Gives the total number of tokens across all counters.
     * 
     * Time Complexity: O(n)
     */
    public int getTotalLoad() {
        int total = 0;
        for (int i = 0; i < numCounters; i++) {
            if (active[i]) {
                total += loads[i];
            }
        }
        return total;
    }

    /**
     * GET AVERAGE LOAD:
     * Computes the average workload across all active counters.
     * 
     * Time Complexity: O(n)
     */
    public double getAverageLoad() {
        int activeCount = getActiveCount();
        if (activeCount == 0) return 0;
        return (double) getTotalLoad() / activeCount;
    }

    /**
     * GET ACTIVE COUNT:
     * Counts the number of currently active counters.
     * 
     * Time Complexity: O(n)
     */
    public int getActiveCount() {
        int count = 0;
        for (int i = 0; i < numCounters; i++) {
            if (active[i]) count++;
        }
        return count;
    }

    // ========== COUNTER STATUS OPERATIONS ==========

    /**
     * SET ACTIVE (Counter Status Update):
     * Activates or deactivates a counter.
     * 
     * Time Complexity: O(1)
     */
    public void setActive(int counterIndex, boolean isActive) {
        validateIndex(counterIndex);
        active[counterIndex] = isActive;
    }

    /**
     * IS ACTIVE:
     * Checks if a counter is currently active.
     * 
     * Time Complexity: O(1)
     */
    public boolean isActive(int counterIndex) {
        validateIndex(counterIndex);
        return active[counterIndex];
    }

    /**
     * SET CAPACITY:
     * Sets the maximum token capacity for a counter.
     * 
     * Time Complexity: O(1)
     */
    public void setCapacity(int counterIndex, int capacity) {
        validateIndex(counterIndex);
        capacities[counterIndex] = capacity;
    }

    /**
     * GET CAPACITY:
     * Time Complexity: O(1)
     */
    public int getCapacity(int counterIndex) {
        validateIndex(counterIndex);
        return capacities[counterIndex];
    }

    /**
     * IS COUNTER AVAILABLE:
     * Checks if a counter can accept more tokens.
     * 
     * Time Complexity: O(1)
     */
    public boolean isAvailable(int counterIndex) {
        validateIndex(counterIndex);
        return active[counterIndex] && loads[counterIndex] < capacities[counterIndex];
    }

    /**
     * GET NUMBER OF COUNTERS:
     * Time Complexity: O(1)
     */
    public int getNumCounters() {
        return numCounters;
    }

    /**
     * GET ALL LOADS:
     * Returns a copy of the loads array.
     * Returns a copy so external code cannot modify the internal array.
     * 
     * Time Complexity: O(n) for the copy
     */
    public int[] getAllLoads() {
        int[] copy = new int[numCounters];
        System.arraycopy(loads, 0, copy, 0, numCounters);
        return copy;
    }

    // ========== VALIDATION ==========

    /**
     * VALIDATE INDEX:
     * Ensures the counter index is within valid bounds.
     * Throws an exception for invalid indices.
     */
    private void validateIndex(int index) {
        if (index < 0 || index >= numCounters) {
            throw new IndexOutOfBoundsException(
                "Counter index " + index + " is out of bounds. Valid range: 0 to " + (numCounters - 1)
            );
        }
    }

    /**
     * DISPLAY:
     * Returns a formatted string showing all counter statuses.
     * 
     * Time Complexity: O(n)
     */
    public String display() {
        StringBuilder sb = new StringBuilder("Counter Array Status:\n");
        for (int i = 0; i < numCounters; i++) {
            sb.append("  Counter ").append(i + 1)
              .append(": Load=").append(loads[i])
              .append("/").append(capacities[i])
              .append(active[i] ? " [Active]" : " [Inactive]")
              .append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return display();
    }
}
