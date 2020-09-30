package datastructures.concrete;

import datastructures.concrete.dictionaries.ChainedHashDictionary;
import datastructures.interfaces.IDictionary;
import datastructures.interfaces.IDisjointSet;



/**
 * @see IDisjointSet for more details.
 */
public class ArrayDisjointSet<T> implements IDisjointSet<T> {
    private int[] pointers;
    private int size;
    private IDictionary<T, Integer> indices;

    public ArrayDisjointSet() {
        pointers = new int[10];
        size = 0;
        indices = new ChainedHashDictionary<>();
    }

    @Override
    public void makeSet(T item) {
        if (indices.containsKey(item)) {
            throw new IllegalArgumentException();
        }
        if (size == pointers.length) {
            int[] temp = new int[size * 2];
            for (int i = 0; i < size; i++) {
                temp[i] = pointers[i];
            }
            pointers = temp;
        }
        pointers[size] = -1; // rank = 0, rank * -1 - 1
        indices.put(item, size);
        size++;
    }

    @Override
    public int findSet(T item) {
        if (!indices.containsKey(item)) {
            throw new IllegalArgumentException();
        }
        int index = indices.get(item);
        int result = 0;
        while (index >= 0) {
            result = index;
            index = pointers[index];
        } //end of day, index will be value of parent, result is the index of the parent
        index = indices.get(item);
        while (index >= 0) {
            if (pointers[index] >= 0) {
                pointers[index] = result;
            }
            index = pointers[index];
        }
        return result;
    }

    @Override
    public void union(T item1, T item2) {
        if (!indices.containsKey(item1) || !indices.containsKey(item2)) {
            throw new IllegalArgumentException();
        }
        int parent1 = findSet(item1);
        int parent2 = findSet(item2);
        int rank1 = pointers[parent1];
        int rank2 = pointers[parent2];
        if (parent1 != parent2) {
            if (rank1 >= rank2) {
                pointers[parent1] = parent2;
                if (rank1 == rank2) {
                    pointers[parent2]--;
                }
            } else {
                pointers[parent2] = parent1;
            }
        }
    }
}
