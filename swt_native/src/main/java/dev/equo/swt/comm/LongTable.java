package dev.equo.swt.comm;

import java.util.concurrent.locks.StampedLock;
import java.util.function.UnaryOperator;

/**
 * Values by a {@code long} key, with no boxing: reads take no lock (an optimistic read, retried
 * under the lock only when a write raced it), writes take one. Null is not a value.
 */
public final class LongTable<V> {

    /** One generation of the table: a write that grows it publishes a new one. */
    private static final class Slots {
        final long[] keys;
        final Object[] values;

        Slots(int capacity) {
            keys = new long[capacity];
            values = new Object[capacity];
        }
    }

    private final StampedLock lock = new StampedLock();
    private volatile Slots slots = new Slots(64);
    private int size;
    /** The value under key 0, which marks a free slot in the table. */
    private volatile Object zero;

    @SuppressWarnings("unchecked")
    public V get(long key) {
        if (key == 0) return (V) zero;
        long stamp = lock.tryOptimisticRead();
        V value = find(slots, key);
        if (lock.validate(stamp)) return value;
        stamp = lock.readLock();
        try {
            return find(slots, key);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    public boolean containsKey(long key) {
        return get(key) != null;
    }

    @SuppressWarnings("unchecked")
    private static <V> V find(Slots s, long key) {
        long[] keys = s.keys;
        int mask = keys.length - 1;
        for (int i = slot(key, mask), probes = 0; probes <= mask; i = (i + 1) & mask, probes++) {
            long k = keys[i];
            if (k == key) return (V) s.values[i];
            if (k == 0) return null;
        }
        return null;
    }

    private static int slot(long key, int mask) {
        return (int) ((key * 0x9E3779B97F4A7C15L) >>> 40) & mask;
    }

    public void put(long key, V value) {
        compute(key, old -> value);
    }

    public V remove(long key) {
        long stamp = lock.writeLock();
        try {
            if (key == 0) {
                V old = get(0);
                zero = null;
                return old;
            }
            V old = find(slots, key);
            if (old != null) delete(key);
            return old;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * Replaces the value under {@code key} with what {@code update} makes of it (null when absent),
     * removing the entry when that is null. Runs under the write lock, so it must not touch this table.
     */
    public V compute(long key, UnaryOperator<V> update) {
        long stamp = lock.writeLock();
        try {
            if (key == 0) {
                V value = update.apply(get(0));
                zero = value;
                return value;
            }
            V old = find(slots, key);
            V value = update.apply(old);
            if (value == null) {
                if (old != null) delete(key);
            } else if (old == null) {
                insert(key, value);
            } else {
                replace(key, value);
            }
            return value;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    private void replace(long key, Object value) {
        Slots s = slots;
        int mask = s.keys.length - 1;
        for (int i = slot(key, mask); ; i = (i + 1) & mask) {
            if (s.keys[i] == key) {
                s.values[i] = value;
                return;
            }
        }
    }

    private void insert(long key, Object value) {
        if ((size + 1) * 2 > slots.keys.length) grow();
        Slots s = slots;
        int mask = s.keys.length - 1;
        int i = slot(key, mask);
        while (s.keys[i] != 0) i = (i + 1) & mask;
        s.values[i] = value;
        s.keys[i] = key;
        size++;
    }

    /** Removes {@code key}, shifting back the entries probed past it, so a lookup never needs a tombstone. */
    private void delete(long key) {
        Slots s = slots;
        long[] keys = s.keys;
        Object[] values = s.values;
        int mask = keys.length - 1;
        int hole = slot(key, mask);
        while (keys[hole] != key) hole = (hole + 1) & mask;
        for (int i = (hole + 1) & mask; keys[i] != 0; i = (i + 1) & mask) {
            int home = slot(keys[i], mask);
            // Moves the entry at i into the hole when its home slot does not lie in (hole, i].
            if (((i - home) & mask) >= ((i - hole) & mask)) {
                keys[hole] = keys[i];
                values[hole] = values[i];
                hole = i;
            }
        }
        keys[hole] = 0;
        values[hole] = null;
        size--;
    }

    private void grow() {
        Slots old = slots;
        Slots s = new Slots(old.keys.length * 2);
        int mask = s.keys.length - 1;
        for (int j = 0; j < old.keys.length; j++) {
            long key = old.keys[j];
            if (key == 0) continue;
            int i = slot(key, mask);
            while (s.keys[i] != 0) i = (i + 1) & mask;
            s.keys[i] = key;
            s.values[i] = old.values[j];
        }
        slots = s;
    }
}
