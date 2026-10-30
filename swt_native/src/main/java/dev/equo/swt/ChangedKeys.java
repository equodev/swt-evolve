package dev.equo.swt;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The numbers of the properties a value has changed since it was last sent, in the order they
 * changed. A value changes a handful of properties between sends, so a short array searched front to
 * back beats any hashed set; the hot path uses {@link #get} and {@link #size}, the {@code Set} view is
 * for everything else.
 */
public final class ChangedKeys extends AbstractSet<Integer> {

    private int[] keys = new int[8];
    private int size;

    public boolean add(int key) {
        if (indexOf(key) >= 0) return false;
        if (size == keys.length) keys = Arrays.copyOf(keys, size * 2);
        keys[size++] = key;
        return true;
    }

    @Override
    public boolean add(Integer key) {
        return add(key.intValue());
    }

    public boolean contains(int key) {
        return indexOf(key) >= 0;
    }

    @Override
    public boolean contains(Object key) {
        return key instanceof Integer && indexOf((Integer) key) >= 0;
    }

    /** The {@code i}-th number, in the order the properties changed. */
    public int get(int i) {
        return keys[i];
    }

    @Override
    public boolean remove(Object key) {
        if (!(key instanceof Integer)) return false;
        int i = indexOf((Integer) key);
        if (i < 0) return false;
        removeAt(i);
        return true;
    }

    /** Forgets every number in {@code written}. */
    public void removeAll(int[] written) {
        for (int key : written) {
            int i = indexOf(key);
            if (i >= 0) removeAt(i);
        }
    }

    /** The numbers, in order, as an array of their own. */
    public int[] toIntArray() {
        return Arrays.copyOf(keys, size);
    }

    private void removeAt(int i) {
        System.arraycopy(keys, i + 1, keys, i, size - i - 1);
        size--;
    }

    private int indexOf(int key) {
        for (int i = 0; i < size; i++) {
            if (keys[i] == key) return i;
        }
        return -1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        size = 0;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private int next;
            private boolean removable;

            @Override
            public boolean hasNext() {
                return next < size;
            }

            @Override
            public Integer next() {
                if (next >= size) throw new NoSuchElementException();
                removable = true;
                return keys[next++];
            }

            @Override
            public void remove() {
                if (!removable) throw new IllegalStateException();
                removable = false;
                removeAt(--next);
            }
        };
    }
}
