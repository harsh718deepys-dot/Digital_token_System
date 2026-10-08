package com.tokenqueue.dsa.stack;

/**
 * TokenHistoryStack - Custom Stack Implementation (LIFO)
 * =======================================================
 * 
 * This stack stores the history of recently completed/processed tokens.
 * It follows the LIFO principle: Last In, First Out.
 * The most recently completed token is always at the top.
 * 
 * WHY A STACK FOR HISTORY?
 * When viewing recent activity, we want the LATEST action first.
 * A stack naturally provides this: push completed tokens on top,
 * and the most recent one is always accessible via peek().
 * 
 * INTERNAL STRUCTURE:
 * Uses a singly linked list. The top of the stack is the head of the list.
 * 
 * Example:
 *   Top → T108 Completed (most recent)
 *         T107 Completed
 *         T106 Completed
 *         T105 Completed (oldest)
 * 
 * Time Complexity Summary:
 *   push()    → O(1)
 *   pop()     → O(1)
 *   peek()    → O(1)
 *   isEmpty() → O(1)
 *   size()    → O(1)
 * 
 * Space Complexity: O(n) where n is the number of elements.
 * 
 * @author TokenQueue DSA Project
 */
public class TokenHistoryStack<T> {

    /**
     * Node class for the linked list-based stack.
     * Each node holds data and points to the node below it.
     */
    private static class Node<T> {
        T data;       // The completed token data
        Node<T> next; // Pointer to the next node (below in the stack)

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    // TOP pointer: always points to the topmost element
    private Node<T> top;

    // Size counter
    private int size;

    // Maximum capacity (to prevent unbounded growth)
    private final int maxSize;

    private static final int DEFAULT_MAX_SIZE = 100;

    /**
     * Constructor: creates an empty stack with default max size.
     */
    public TokenHistoryStack() {
        this.top = null;
        this.size = 0;
        this.maxSize = DEFAULT_MAX_SIZE;
    }

    /**
     * Constructor with custom max size.
     */
    public TokenHistoryStack(int maxSize) {
        this.top = null;
        this.size = 0;
        this.maxSize = maxSize;
    }

    /**
     * PUSH (Add to Stack):
     * Pushes a completed token onto the top of the stack.
     * This is like placing a new plate on top of a stack of plates.
     * 
     * Steps:
     * 1. Create a new node with the token data.
     * 2. Point the new node's next to the current top.
     * 3. Update top to point to the new node.
     * 4. If stack exceeds max size, remove the bottom element.
     * 
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     * 
     * @param data the completed token to push
     */
    public void push(T data) {
        // Step 1: Create new node
        Node<T> newNode = new Node<>(data);

        // Step 2: Link new node to current top
        newNode.next = top;

        // Step 3: Update top pointer
        top = newNode;

        // Step 4: Increment size
        size++;

        // If exceeding max size, remove the oldest (bottom) element
        // This prevents unbounded memory growth for history
        if (size > maxSize) {
            removeBottom();
        }
    }

    /**
     * POP (Remove from Stack):
     * Removes and returns the most recently added token (top of stack).
     * LIFO: Last In, First Out.
     * 
     * Steps:
     * 1. Check if stack is empty.
     * 2. Store the top element's data.
     * 3. Move top to the next element.
     * 4. Return the stored data.
     * 
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     * 
     * @return the most recently pushed token
     * @throws IllegalStateException if the stack is empty
     */
    public T pop() {
        // Step 1: Check empty
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty. No tokens in history.");
        }

        // Step 2: Save top data
        T data = top.data;

        // Step 3: Move top down
        top = top.next;

        // Step 4: Decrement size
        size--;

        return data;
    }

    /**
     * PEEK (View Top Element):
     * Returns the most recent token WITHOUT removing it.
     * 
     * Time Complexity: O(1)
     * 
     * @return the token at the top of the stack
     * @throws IllegalStateException if the stack is empty
     */
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty. Nothing to peek.");
        }
        return top.data;
    }

    /**
     * IS EMPTY:
     * Checks if the stack has any elements.
     * 
     * Time Complexity: O(1)
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * SIZE:
     * Returns the current number of elements in the stack.
     * 
     * Time Complexity: O(1)
     */
    public int size() {
        return size;
    }

    /**
     * IS FULL:
     * Checks if the stack has reached its maximum capacity.
     * 
     * Time Complexity: O(1)
     */
    public boolean isFull() {
        return size >= maxSize;
    }

    /**
     * CLEAR:
     * Removes all elements from the stack.
     * 
     * Time Complexity: O(1) — we just reset the top pointer.
     * (Java's garbage collector handles the abandoned nodes.)
     */
    public void clear() {
        top = null;
        size = 0;
    }

    /**
     * TO ARRAY:
     * Converts the stack to an array (top element at index 0).
     * Useful for displaying history in the UI.
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(n)
     */
    public Object[] toArray() {
        Object[] array = new Object[size];
        Node<T> current = top;
        int index = 0;
        while (current != null) {
            array[index++] = current.data;
            current = current.next;
        }
        return array;
    }

    /**
     * GET RECENT:
     * Returns the most recent 'count' elements from the stack.
     * Elements are returned in order from most recent to oldest.
     * 
     * Time Complexity: O(count)
     * Space Complexity: O(count)
     * 
     * @param count how many recent elements to return
     */
    public Object[] getRecent(int count) {
        int actualCount = Math.min(count, size);
        Object[] recent = new Object[actualCount];
        Node<T> current = top;
        for (int i = 0; i < actualCount; i++) {
            recent[i] = current.data;
            current = current.next;
        }
        return recent;
    }

    /**
     * REMOVE BOTTOM:
     * Removes the oldest element (bottom of stack) when max size is exceeded.
     * Must traverse to the second-to-last node.
     * 
     * Time Complexity: O(n) — must traverse entire stack.
     */
    private void removeBottom() {
        if (size <= 1) {
            top = null;
            size = 0;
            return;
        }

        Node<T> current = top;
        // Traverse to the second-to-last node
        while (current.next != null && current.next.next != null) {
            current = current.next;
        }
        // Remove the last node
        current.next = null;
        size--;
    }

    /**
     * DISPLAY:
     * Returns a string showing the stack contents from top to bottom.
     * 
     * Time Complexity: O(n)
     */
    public String display() {
        if (isEmpty()) {
            return "[Empty Stack]";
        }

        StringBuilder sb = new StringBuilder("Top\n ↓\n");
        Node<T> current = top;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append("\n ↓\n");
            }
            current = current.next;
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return display();
    }
}
