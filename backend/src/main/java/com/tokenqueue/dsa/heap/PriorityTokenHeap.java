package com.tokenqueue.dsa.heap;

import java.util.ArrayList;
import java.util.List;

/**
 * PriorityTokenHeap - Custom Min-Heap Implementation for Priority Queue
 * ======================================================================
 * 
 * This heap manages priority-based tokens so that higher-priority visitors
 * (lower priority number) are served before lower-priority visitors.
 * 
 * PRIORITY CATEGORIES:
 *   1 = Emergency        (highest priority, served first)
 *   2 = Senior Citizen
 *   3 = Differently-Abled
 *   4 = Pregnant Woman
 *   5 = Normal            (lowest priority, served last among priorities)
 * 
 * HOW A MIN-HEAP WORKS:
 * - A min-heap is a complete binary tree where the parent node is always
 *   SMALLER than or equal to its children.
 * - The SMALLEST element (highest priority) is always at the root (index 0).
 * - We use an ArrayList internally to represent the binary tree.
 * 
 * ARRAY REPRESENTATION OF BINARY TREE:
 *   Parent of node at index i     → (i - 1) / 2
 *   Left child of node at index i → 2 * i + 1
 *   Right child of node at index i → 2 * i + 2
 * 
 * Example heap (by priority):
 *          1(Emergency)
 *         /            \
 *   2(Senior)     3(Disabled)
 *    /      \
 * 5(Normal) 5(Normal)
 * 
 * Time Complexity Summary:
 *   insert()       → O(log n)  (heapify up)
 *   extractMin()   → O(log n)  (heapify down)
 *   peek()         → O(1)
 *   size()         → O(1)
 *   isEmpty()      → O(1)
 * 
 * Space Complexity: O(n) where n is the number of tokens.
 * 
 * @author TokenQueue DSA Project
 */
public class PriorityTokenHeap<T> {

    /**
     * HeapNode wraps each token with its priority value and arrival timestamp.
     * If two tokens have the same priority, the one that arrived first is served first.
     */
    public static class HeapNode<T> {
        private T data;          // The token data
        private int priority;    // Priority level (1=highest, 5=lowest)
        private long timestamp;  // When the token was inserted (for tie-breaking)

        public HeapNode(T data, int priority) {
            this.data = data;
            this.priority = priority;
            this.timestamp = System.nanoTime(); // Capture arrival time
        }

        public T getData() { return data; }
        public int getPriority() { return priority; }
        public long getTimestamp() { return timestamp; }
        public void setPriority(int priority) { this.priority = priority; }

        @Override
        public String toString() {
            return "HeapNode{data=" + data + ", priority=" + priority + "}";
        }
    }

    // The internal array that stores the heap nodes
    // We use ArrayList for dynamic resizing
    private List<HeapNode<T>> heap;

    /**
     * Constructor: creates an empty min-heap.
     */
    public PriorityTokenHeap() {
        this.heap = new ArrayList<>();
    }

    // ========== HELPER METHODS FOR ARRAY-BASED TREE NAVIGATION ==========

    /**
     * PARENT INDEX:
     * Given a node at index i, its parent is at (i - 1) / 2.
     * Example: node at index 3 → parent at index 1
     */
    private int parent(int index) {
        return (index - 1) / 2;
    }

    /**
     * LEFT CHILD INDEX:
     * Given a node at index i, its left child is at 2*i + 1.
     * Example: node at index 1 → left child at index 3
     */
    private int leftChild(int index) {
        return 2 * index + 1;
    }

    /**
     * RIGHT CHILD INDEX:
     * Given a node at index i, its right child is at 2*i + 2.
     * Example: node at index 1 → right child at index 4
     */
    private int rightChild(int index) {
        return 2 * index + 2;
    }

    /**
     * SWAP:
     * Exchanges two elements in the heap array.
     * Used during heapify operations.
     */
    private void swap(int i, int j) {
        HeapNode<T> temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    /**
     * COMPARE:
     * Compares two heap nodes to determine ordering.
     * First compares by priority (lower number = higher priority).
     * If priorities are equal, compares by timestamp (earlier = first).
     * 
     * Returns negative if a should come before b.
     */
    private int compare(HeapNode<T> a, HeapNode<T> b) {
        if (a.priority != b.priority) {
            return Integer.compare(a.priority, b.priority);
        }
        // Same priority: whoever arrived first gets served first (FIFO within same priority)
        return Long.compare(a.timestamp, b.timestamp);
    }

    // ========== CORE HEAP OPERATIONS ==========

    /**
     * INSERT (Add to Heap):
     * Adds a new priority token to the heap.
     * 
     * Steps:
     * 1. Add the new node at the END of the array (last position in tree).
     * 2. HEAPIFY UP: Move the node upward until the heap property is restored.
     * 
     * WHY HEAPIFY UP?
     * The new node might have a higher priority than its parent.
     * We keep swapping it with its parent until it reaches the correct position.
     * 
     * Time Complexity: O(log n) — at most, we traverse the height of the tree.
     * Space Complexity: O(1) additional space.
     * 
     * @param data     the token to insert
     * @param priority the priority level (1=Emergency to 5=Normal)
     */
    public void insert(T data, int priority) {
        // Step 1: Create a new heap node and add it at the end
        HeapNode<T> newNode = new HeapNode<>(data, priority);
        heap.add(newNode);

        // Step 2: Heapify up to maintain the min-heap property
        heapifyUp(heap.size() - 1);
    }

    /**
     * HEAPIFY UP (Bubble Up / Sift Up):
     * After inserting at the bottom, move the node UP the tree
     * until the heap property is satisfied.
     * 
     * The heap property for a min-heap:
     *   parent.priority <= child.priority
     * 
     * Algorithm:
     * 1. Compare the node with its parent.
     * 2. If the node has higher priority (smaller number) than parent, swap them.
     * 3. Repeat until the node is at the root or heap property is satisfied.
     * 
     * Visual example:
     *   Before insert(Emergency, 1):      After heapifyUp:
     *        3                                  1
     *       / \                                / \
     *      5   5                              5   3
     *     /                                  /
     *    1  ← new                           5
     * 
     * Time Complexity: O(log n) — height of the tree.
     */
    private void heapifyUp(int index) {
        // Keep moving up while node has higher priority than its parent
        while (index > 0 && compare(heap.get(index), heap.get(parent(index))) < 0) {
            // Swap current node with its parent
            swap(index, parent(index));
            // Move to the parent's position and check again
            index = parent(index);
        }
    }

    /**
     * EXTRACT MIN (Remove Highest Priority Token):
     * Removes and returns the token with the highest priority (smallest number).
     * This is the visitor who should be served next.
     * 
     * Steps:
     * 1. Save the root element (highest priority).
     * 2. Move the LAST element to the root position.
     * 3. Remove the last position.
     * 4. HEAPIFY DOWN from the root to restore heap property.
     * 
     * WHY MOVE LAST TO ROOT?
     * Removing from the middle of an array is expensive.
     * By moving the last element to the root and heapifying down,
     * we maintain the complete tree structure.
     * 
     * Time Complexity: O(log n) — heapify down traverses the tree height.
     * Space Complexity: O(1)
     * 
     * @return the token data with the highest priority
     * @throws IllegalStateException if the heap is empty
     */
    public T extractMin() {
        if (isEmpty()) {
            throw new IllegalStateException("Priority heap is empty. No tokens to extract.");
        }

        // Step 1: Save the root (highest priority token)
        T minData = heap.get(0).getData();

        // Step 2: Move the last element to the root
        int lastIndex = heap.size() - 1;
        heap.set(0, heap.get(lastIndex));

        // Step 3: Remove the last element
        heap.remove(lastIndex);

        // Step 4: Heapify down from the root to restore order
        if (!isEmpty()) {
            heapifyDown(0);
        }

        return minData;
    }

    /**
     * HEAPIFY DOWN (Bubble Down / Sift Down):
     * After extracting the root and placing the last element there,
     * move it DOWN the tree until the heap property is restored.
     * 
     * Algorithm:
     * 1. Compare the node with its left and right children.
     * 2. Find the child with the smallest priority.
     * 3. If that child has higher priority (smaller number), swap.
     * 4. Repeat from the child's position.
     * 
     * Visual example:
     *   Before heapifyDown:       After heapifyDown:
     *        5                         2
     *       / \                       / \
     *      2   3                     5   3
     * 
     * Time Complexity: O(log n) — height of the tree.
     */
    private void heapifyDown(int index) {
        int smallest = index;

        int left = leftChild(index);
        int right = rightChild(index);

        // Check if left child has higher priority (smaller value)
        if (left < heap.size() && compare(heap.get(left), heap.get(smallest)) < 0) {
            smallest = left;
        }

        // Check if right child has even higher priority
        if (right < heap.size() && compare(heap.get(right), heap.get(smallest)) < 0) {
            smallest = right;
        }

        // If the smallest is not the current node, swap and continue
        if (smallest != index) {
            swap(index, smallest);
            // Recursively heapify down from the new position
            heapifyDown(smallest);
        }
    }

    /**
     * PEEK (View Highest Priority Token):
     * Returns the highest-priority token WITHOUT removing it.
     * 
     * Time Complexity: O(1) — the root is always at index 0.
     * 
     * @return the token data at the root (highest priority)
     */
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Priority heap is empty.");
        }
        return heap.get(0).getData();
    }

    /**
     * PEEK NODE:
     * Returns the full HeapNode (with priority info) without removing.
     */
    public HeapNode<T> peekNode() {
        if (isEmpty()) {
            throw new IllegalStateException("Priority heap is empty.");
        }
        return heap.get(0);
    }

    /**
     * UPDATE PRIORITY:
     * Changes the priority of an existing token in the heap.
     * After changing the priority, we need to restore the heap property.
     * 
     * Steps:
     * 1. Find the token in the heap (linear search).
     * 2. Update its priority.
     * 3. If priority increased (number decreased), heapify up.
     * 4. If priority decreased (number increased), heapify down.
     * 
     * Time Complexity: O(n) for search + O(log n) for heapify = O(n)
     * 
     * @param data        the token to find
     * @param newPriority the new priority value
     * @return true if the token was found and updated
     */
    public boolean updatePriority(T data, int newPriority) {
        // Step 1: Linear search for the token
        for (int i = 0; i < heap.size(); i++) {
            if (heap.get(i).getData().equals(data)) {
                int oldPriority = heap.get(i).getPriority();

                // Step 2: Update the priority
                heap.get(i).setPriority(newPriority);

                // Step 3: Restore heap property
                if (newPriority < oldPriority) {
                    // Priority increased (smaller number) → might need to go UP
                    heapifyUp(i);
                } else if (newPriority > oldPriority) {
                    // Priority decreased (larger number) → might need to go DOWN
                    heapifyDown(i);
                }

                return true;
            }
        }
        return false; // Token not found
    }

    /**
     * REMOVE:
     * Removes a specific token from the heap.
     * Used when a visitor cancels their priority token.
     * 
     * Steps:
     * 1. Find the token.
     * 2. Replace it with the last element.
     * 3. Remove the last element.
     * 4. Heapify up or down as needed.
     * 
     * Time Complexity: O(n) for search + O(log n) for heapify = O(n)
     */
    public boolean remove(T data) {
        for (int i = 0; i < heap.size(); i++) {
            if (heap.get(i).getData().equals(data)) {
                int lastIndex = heap.size() - 1;

                if (i == lastIndex) {
                    heap.remove(lastIndex);
                } else {
                    heap.set(i, heap.get(lastIndex));
                    heap.remove(lastIndex);
                    heapifyDown(i);
                    heapifyUp(i);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * IS EMPTY:
     * Time Complexity: O(1)
     */
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    /**
     * SIZE:
     * Time Complexity: O(1)
     */
    public int size() {
        return heap.size();
    }

    /**
     * CONTAINS:
     * Checks if a token exists in the heap.
     * Time Complexity: O(n) — must search linearly.
     */
    public boolean contains(T data) {
        for (HeapNode<T> node : heap) {
            if (node.getData().equals(data)) {
                return true;
            }
        }
        return false;
    }

    /**
     * GET ALL SORTED:
     * Returns all tokens sorted by priority (for display purposes).
     * Creates a copy to avoid destroying the heap.
     * 
     * Time Complexity: O(n log n) — extracting all elements.
     */
    public List<T> getAllSorted() {
        // Create a temporary copy of the heap
        PriorityTokenHeap<T> tempHeap = new PriorityTokenHeap<>();
        for (HeapNode<T> node : heap) {
            tempHeap.insert(node.getData(), node.getPriority());
        }

        List<T> sorted = new ArrayList<>();
        while (!tempHeap.isEmpty()) {
            sorted.add(tempHeap.extractMin());
        }
        return sorted;
    }

    /**
     * GET POSITION:
     * Gets the position of a token in priority order.
     * Uses the sorted list to determine position.
     * 
     * Time Complexity: O(n log n)
     */
    public int getPosition(T data) {
        List<T> sorted = getAllSorted();
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).equals(data)) {
                return i + 1; // 1-based position
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return "PriorityTokenHeap{size=" + size() + ", heap=" + heap + "}";
    }
}
