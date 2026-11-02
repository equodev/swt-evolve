package dev.equo.swt.comm;

/**
 * A handler of a frame's payload where it lies, {@code data[offset, offset + length)}: lent for the
 * length of the call, so a handler that keeps any of it copies that part.
 */
@FunctionalInterface
public interface FrameBytes {
    void accept(byte[] data, int offset, int length);
}
