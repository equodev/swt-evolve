package dev.equo.swt;

/** 64-bit FNV-1a, the running hash the GC wire names paints and renders by. */
final class Fnv {
    /** The offset basis: the hash of nothing. */
    static final long EMPTY = 0xcbf29ce484222325L;

    private static final long PRIME = 0x100000001b3L;

    private Fnv() {
    }

    /** Folds {@code value} into {@code hash} as one step. */
    static long mix(long hash, long value) {
        return (hash ^ value) * PRIME;
    }

    /** Folds the eight bytes of {@code value} into {@code hash}, low byte first. */
    static long ofLong(long hash, long value) {
        for (int i = 0; i < 8; i++) {
            hash = mix(hash, value & 0xFF);
            value >>>= 8;
        }
        return hash;
    }

    /** Folds {@code length} bytes of {@code bytes} from {@code offset} into {@code hash}. */
    static long of(long hash, byte[] bytes, int offset, int length) {
        for (int i = offset; i < offset + length; i++) hash = mix(hash, bytes[i] & 0xFF);
        return hash;
    }

    /** Folds {@code bytes} and their length into {@code hash}; a null array hashes as length -1. */
    static long of(long hash, byte[] bytes) {
        if (bytes == null) return ofLong(hash, -1);
        return of(ofLong(hash, bytes.length), bytes, 0, bytes.length);
    }
}
