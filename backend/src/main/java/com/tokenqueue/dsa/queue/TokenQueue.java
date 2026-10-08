package com.tokenqueue.dsa.queue;

/**
 * TokenQueue - Custom Queue Implementation (FIFO)
 * ================================================
 * 
 * This queue manages the order of normal (non-priority) tokens.
 * It follows the FIFO principle: First In, First Out.
 * The visitor who receives the token first is served first.
 * 
 * INTERNAL STRUCTURE:
 * We use a singly linked list to implement the queue.
 * - 'front' points to the first element (next to be served).
 * - 'rear' points to the last element (most recently added).
 * 
 * WHY LINKED LIST?
 * Arrays would require shifting elements on dequeue → O(n).
 * Linked list gives O(1) enqueue and dequeue.
 * 
 * Time Complexity Summary:
 *   enqueue()  → O(1)
 *   dequeue()  → O(1)
 *   peek()     → O(1)
 *   isEmpty()  → O(1)
 *   size()     → O(1)
 *   contains() → O(n)
 *   remove()   → O(n)
 *   display()  → O(n)
 * 
 * Space Complexity: O(n) where n is the number of tokens in the queue.
 * 
 * @author TokenQueue DSA Project
 */
public class TokenQueue<T> {

    /**
     * Node class represents each element in the queue.
     * Each node holds data and a reference to the next node.
     */
    private static class Node<T> {
        T data;       // The token data stored in this node
        Node<T> next; // Pointer to the next node in the queue

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    // FRONT pointer: points to the first element in the queue (next to be served)
    private Node<T> front;

    // REAR pointer: points to the last element in the queue (most recently added)
    private Node<T> rear;

    // SIZE: tracks the number of elements currently in the queue
    private int size;

    /**
     * Constructor: creates an empty queue.
     * Initially, both front and rear are null (no elements).
     */
    public TokenQueue() {
        this.front = null;
        this.rear = null;
        this.size = 0;
    }

    /**
     * ENQUEUE (Add to Queue):
     * Adds a new token at the rear (end) of the queue.
     * This represents a new visitor joining the queue.
     * 
     * Steps:
     * 1. Create a new node with the token data.
     * 2. If the queue is empty, both front and rear point to the new node.
     * 3. Otherwise, link the current rear to the new node, then update rear.
     * 
     * Time Complexity: O(1) — direct insertion at the rear.
     * Space Complexity: O(1) — only one new node is created.
     * 
     * @param data the token to add to the queue
     */
    public void enqueue(T data) {
        // Step 1: Create a new node for this token
        Node<T> newNode = new Node<>(data);

        if (isEmpty()) {
            // Step 2a: If queue is empty, this is both the first and last element
            front = newNode;
            rear = newNode;
        } else {
            // Step 2b: Link current rear's next to the new node
            rear.next = newNode;
            // Step 3: Move the rear pointer to the new node
            rear = newNode;
        }

        // Step 4: Increment the size counter
        size++;
    }

    /**
     * DEQUEUE (Remove from Queue):
     * Removes and returns the token from the front of the queue.
     * This represents the next visitor being called for service.
     * 
     * Steps:
     * 1. Check if the queue is empty (nothing to dequeue).
     * 2. Store the front data to return.
     * 3. Move front to the next node.
     * 4. If queue becomes empty, also set rear to null.
     * 
     * Time Complexity: O(1) — direct removal from the front.
     * Space Complexity: O(1) — no extra space needed.
     * 
     * @return the token at the front of the queue
     * @throws IllegalStateException if the queue is empty
     */
    public T dequeue() {
        // Step 1: Check if the queue is empty
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty. No tokens to dequeue.");
        }

        // Step 2: Store the front element's data
        T data = front.data;

        // Step 3: Move front pointer to the next node
        front = front.next;

        // Step 4: If queue is now empty, set rear to null too
        if (front == null) {
            rear = null;
        }

        // Step 5: Decrement size
        size--;

        return data;
    }

    /**
     * PEEK (View Front Element):
     * Returns the front token WITHOUT removing it.
     * Useful to see who is next in line without actually serving them.
     * 
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     * 
     * @return the token at the front of the queue
     * @throws IllegalStateException if the queue is empty
     */
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty. Nothing to peek.");
        }
        return front.data;
    }

    /**
     * IS EMPTY:
     * Checks whether the queue has any tokens.
     * 
     * Time Complexity: O(1)
     * 
     * @return true if the queue has no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * SIZE:
     * Returns the current number of tokens in the queue.
     * 
     * Time Complexity: O(1)
     * 
     * @return the number of elements in the queue
     */
    public int size() {
        return size;
    }

    /**
     * CONTAINS:
     * Checks whether a specific token exists in the queue.
     * Must traverse the queue from front to rear to find it.
     * 
     * Time Complexity: O(n) — worst case, must check every element.
     * Space Complexity: O(1)
     * 
     * @param data the token to search for
     * @return true if the token is found in the queue
     */
    public boolean contains(T data) {
        // Traverse from front to rear, checking each node
        Node<T> current = front;
        while (current != null) {
            if (current.data.equals(data)) {
                return true; // Found!
            }
            current = current.next;
        }
        return false; // Not found
    }

    /**
     * REMOVE (Remove Specific Element):
     * Removes a specific token from anywhere in the queue.
     * Used when a visitor cancels their token.
     * 
     * Steps:
     * 1. Handle special case: removing the front element.
     * 2. Otherwise, traverse to find the node just before the target.
     * 3. Re-link the previous node to skip the target node.
     * 
     * Time Complexity: O(n) — must search for the element.
     * Space Complexity: O(1)
     * 
     * @param data the token to remove
     * @return true if the token was found and removed
     */
    public boolean remove(T data) {
        if (isEmpty()) {
            return false;
        }

        // Special case: target is at the front
        if (front.data.equals(data)) {
            dequeue();
            return true;
        }

        // Traverse to find the node BEFORE the one to remove
        Node<T> current = front;
        while (current.next != null) {
            if (current.next.data.equals(data)) {
                // Found it! Remove by skipping the node
                if (current.next == rear) {
                    // If removing the rear, update rear pointer
                    rear = current;
                }
                current.next = current.next.next;
                size--;
                return true;
            }
            current = current.next;
        }

        return false; // Element not found
    }

    /**
     * GET POSITION:
     * Returns the 1-based position of a token in the queue.
     * Position 1 means the token is next to be served.
     * 
     * Time Complexity: O(n) — must traverse to find position.
     * Space Complexity: O(1)
     * 
     * @param data the token to find
     * @return the 1-based position, or -1 if not found
     */
    public int getPosition(T data) {
        Node<T> current = front;
        int position = 1;
        while (current != null) {
            if (current.data.equals(data)) {
                return position;
            }
            current = current.next;
            position++;
        }
        return -1; // Not found
    }

    /**
     * DISPLAY (Print Queue):
     * Returns a string representation of all tokens in the queue.
     * Shows the order from front (next to serve) to rear (last to serve).
     * 
     * Example output: [T101 → T102 → T103 → T104]
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(n) for the string
     */
    public String display() {
        if (isEmpty()) {
            return "[Empty Queue]";
        }

        StringBuilder sb = new StringBuilder("[");
        Node<T> current = front;
        while (current != null) {
            sb.append(current.data);
            if (current.next != null) {
                sb.append(" → ");
            }
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * TO ARRAY:
     * Converts the queue to an array for display or iteration purposes.
     * 
     * Time Complexity: O(n)
     * Space Complexity: O(n) for the new array
     * 
     * @return an Object array of all elements in queue order
     */
    public Object[] toArray() {
        Object[] array = new Object[size];
        Node<T> current = front;
        int index = 0;
        while (current != null) {
            array[index++] = current.data;
            current = current.next;
        }
        return array;
    }

    @Override
    public String toString() {
        return display();
    }
}
