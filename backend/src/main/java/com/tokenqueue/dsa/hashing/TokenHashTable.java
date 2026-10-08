package com.tokenqueue.dsa.hashing;

import java.util.ArrayList;
import java.util.List;

/**
 * TokenHashTable - Custom Hash Table Implementation
 * ==================================================
 * 
 * This hash table provides O(1) average-case lookup for tokens by their ID.
 * Instead of searching through the entire queue, we can instantly find
 * any token's details using its token ID (e.g., "T105").
 * 
 * HOW HASHING WORKS:
 * 1. Take the token ID (a string like "T105").
 * 2. Pass it through a HASH FUNCTION to get a numeric index.
 * 3. Store/retrieve the token at that index in the array.
 * 
 * Example:
 *   Token ID: "T105"
 *        ↓
 *   Hash Function: sum of char values % table size
 *        ↓
 *   Index: 7
 *        ↓
 *   table[7] → Token Details (name, service, priority, etc.)
 * 
 * COLLISION HANDLING: SEPARATE CHAINING
 * When two different keys hash to the same index (a "collision"),
 * we store both entries in a linked list (chain) at that index.
 * 
 * Example of collision:
 *   Index 7: [T105 → T112 → T119]  (all hashed to index 7)
 * 
 * Time Complexity Summary:
 *   insert()   → O(1) average, O(n) worst case (all keys collide)
 *   search()   → O(1) average, O(n) worst case
 *   delete()   → O(1) average, O(n) worst case
 *   contains() → O(1) average
 * 
 * Space Complexity: O(n + m) where n = number of entries, m = table size
 * 
 * LOAD FACTOR:
 * When the ratio of entries to table size exceeds a threshold (0.75),
 * we RESIZE the table to maintain O(1) performance.
 * 
 * @author TokenQueue DSA Project
 */
public class TokenHashTable<K, V> {

    /**
     * Entry class represents a key-value pair in the hash table.
     * Each entry also has a 'next' pointer for chaining (linked list).
     */
    private static class Entry<K, V> {
        K key;         // The token ID (e.g., "T105")
        V value;       // The token details
        Entry<K, V> next; // Next entry in the chain (for collision handling)

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }

    // The hash table: an array of linked list heads (chains)
    private Entry<K, V>[] table;

    // Number of key-value pairs currently stored
    private int size;

    // Initial capacity of the hash table
    private static final int INITIAL_CAPACITY = 16;

    // Load factor threshold: resize when size/capacity > this value
    // 0.75 is the standard load factor used in Java's HashMap
    private static final double LOAD_FACTOR_THRESHOLD = 0.75;

    /**
     * Constructor: creates a hash table with default initial capacity.
     * All buckets are initially null (empty).
     */
    @SuppressWarnings("unchecked")
    public TokenHashTable() {
        this.table = new Entry[INITIAL_CAPACITY];
        this.size = 0;
    }

    /**
     * Constructor with custom capacity.
     */
    @SuppressWarnings("unchecked")
    public TokenHashTable(int capacity) {
        this.table = new Entry[capacity];
        this.size = 0;
    }

    // ========== HASH FUNCTION ==========

    /**
     * HASH FUNCTION:
     * Converts a key (token ID) into an array index.
     * 
     * Steps:
     * 1. Get the key's hashCode() (Java provides this for all objects).
     * 2. Take the ABSOLUTE value (hashCode can be negative).
     * 3. Apply MODULO with table length to get a valid index.
     * 
     * Why modulo? The hashCode can be any integer, but our array
     * indices are 0 to (table.length - 1). Modulo maps any integer
     * to this range.
     * 
     * Example:
     *   key = "T105"
     *   hashCode = 84315 (Java's String.hashCode())
     *   index = 84315 % 16 = 11
     *   → Token stored at table[11]
     * 
     * Time Complexity: O(1) for the computation.
     * 
     * @param key the key to hash
     * @return the array index (bucket number)
     */
    private int hash(K key) {
        // Use Java's built-in hashCode, ensure non-negative, then modulo
        return Math.abs(key.hashCode()) % table.length;
    }

    // ========== CORE OPERATIONS ==========

    /**
     * INSERT (Put):
     * Adds a key-value pair to the hash table.
     * If the key already exists, updates its value.
     * 
     * Steps:
     * 1. Compute the hash (bucket index).
     * 2. Check if the key already exists in that bucket's chain.
     *    - If yes, UPDATE the value.
     * 3. If not, INSERT a new entry at the HEAD of the chain.
     * 4. Check load factor and RESIZE if needed.
     * 
     * Time Complexity: O(1) average case.
     * Space Complexity: O(1) for the new entry.
     * 
     * @param key   the token ID
     * @param value the token details
     */
    public void insert(K key, V value) {
        // Step 1: Compute the bucket index
        int index = hash(key);

        // Step 2: Check if key already exists in the chain
        Entry<K, V> current = table[index];
        while (current != null) {
            if (current.key.equals(key)) {
                // Key found! Update the value.
                current.value = value;
                return;
            }
            current = current.next;
        }

        // Step 3: Key not found. Create new entry and insert at HEAD of chain.
        // Inserting at head is O(1) — no need to traverse to the end.
        Entry<K, V> newEntry = new Entry<>(key, value);
        newEntry.next = table[index]; // Point new entry to current head
        table[index] = newEntry;      // Make new entry the head
        size++;

        // Step 4: Check if we need to resize
        if ((double) size / table.length > LOAD_FACTOR_THRESHOLD) {
            resize();
        }
    }

    /**
     * SEARCH (Get):
     * Retrieves the value associated with a key.
     * 
     * Steps:
     * 1. Compute the hash (bucket index).
     * 2. Traverse the chain at that bucket.
     * 3. Compare each entry's key with the target key.
     * 4. Return the value if found, null otherwise.
     * 
     * Time Complexity: O(1) average case (assuming good hash distribution).
     *                  O(n) worst case (all keys in one bucket).
     * 
     * @param key the token ID to search for
     * @return the token details, or null if not found
     */
    public V search(K key) {
        // Step 1: Compute the bucket index
        int index = hash(key);

        // Step 2-3: Traverse the chain to find the key
        Entry<K, V> current = table[index];
        while (current != null) {
            if (current.key.equals(key)) {
                return current.value; // Found!
            }
            current = current.next;
        }

        return null; // Not found
    }

    /**
     * DELETE (Remove):
     * Removes a key-value pair from the hash table.
     * 
     * Steps:
     * 1. Compute the hash (bucket index).
     * 2. Search the chain for the key.
     * 3. If found, re-link the chain to skip the removed entry.
     * 
     * Special cases:
     * - Entry is at the HEAD of the chain → update table[index].
     * - Entry is in the MIDDLE/END → update previous.next.
     * 
     * Time Complexity: O(1) average case.
     * 
     * @param key the token ID to remove
     * @return true if the key was found and removed
     */
    public boolean delete(K key) {
        int index = hash(key);

        Entry<K, V> current = table[index];
        Entry<K, V> previous = null;

        while (current != null) {
            if (current.key.equals(key)) {
                if (previous == null) {
                    // Removing the HEAD of the chain
                    table[index] = current.next;
                } else {
                    // Removing from MIDDLE or END: skip the node
                    previous.next = current.next;
                }
                size--;
                return true;
            }
            previous = current;
            current = current.next;
        }

        return false; // Key not found
    }

    /**
     * CONTAINS:
     * Checks whether a key exists in the hash table.
     * 
     * Time Complexity: O(1) average case.
     * 
     * @param key the key to check
     * @return true if the key exists
     */
    public boolean contains(K key) {
        return search(key) != null;
    }

    /**
     * SIZE:
     * Returns the number of key-value pairs in the table.
     * Time Complexity: O(1)
     */
    public int size() {
        return size;
    }

    /**
     * IS EMPTY:
     * Time Complexity: O(1)
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * GET ALL KEYS:
     * Returns a list of all keys in the hash table.
     * Must traverse all buckets and all chains.
     * 
     * Time Complexity: O(n + m) where n = entries, m = table size.
     */
    public List<K> getAllKeys() {
        List<K> keys = new ArrayList<>();
        for (Entry<K, V> entry : table) {
            Entry<K, V> current = entry;
            while (current != null) {
                keys.add(current.key);
                current = current.next;
            }
        }
        return keys;
    }

    /**
     * GET ALL VALUES:
     * Returns a list of all values in the hash table.
     * 
     * Time Complexity: O(n + m)
     */
    public List<V> getAllValues() {
        List<V> values = new ArrayList<>();
        for (Entry<K, V> entry : table) {
            Entry<K, V> current = entry;
            while (current != null) {
                values.add(current.value);
                current = current.next;
            }
        }
        return values;
    }

    // ========== RESIZE (REHASHING) ==========

    /**
     * RESIZE (Rehash):
     * When the load factor exceeds the threshold, double the table size
     * and rehash all existing entries.
     * 
     * WHY RESIZE?
     * As the table fills up, chains become longer, degrading performance
     * from O(1) towards O(n). Doubling the table and rehashing distributes
     * entries more evenly.
     * 
     * Steps:
     * 1. Create a new table with DOUBLE the capacity.
     * 2. Rehash every entry from the old table into the new table.
     *    (Hash values change because table.length changed!)
     * 
     * Time Complexity: O(n) — must rehash all entries.
     * Space Complexity: O(n) — new table.
     * 
     * This is amortized O(1) per insert operation over time.
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<K, V>[] oldTable = table;
        table = new Entry[oldTable.length * 2]; // Double the capacity
        size = 0; // Reset size (will be re-counted during re-insertion)

        // Rehash all existing entries into the new table
        for (Entry<K, V> head : oldTable) {
            Entry<K, V> current = head;
            while (current != null) {
                insert(current.key, current.value); // Re-insert with new hash
                current = current.next;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TokenHashTable{\n");
        for (int i = 0; i < table.length; i++) {
            if (table[i] != null) {
                sb.append("  Bucket ").append(i).append(": ");
                Entry<K, V> current = table[i];
                while (current != null) {
                    sb.append("[").append(current.key).append("=").append(current.value).append("]");
                    if (current.next != null) sb.append(" → ");
                    current = current.next;
                }
                sb.append("\n");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
