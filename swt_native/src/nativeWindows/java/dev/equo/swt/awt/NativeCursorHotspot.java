package dev.equo.swt.awt;

import java.awt.Cursor;
import java.lang.reflect.Field;

import sun.awt.AWTAccessor;

/**
 * The hotspot of a custom AWT cursor. AWT hands it to its native cursor object and keeps no Java
 * copy, so it is read back from there. Looked up by name from {@link EmbedCursor}, which is shared by
 * every platform.
 *
 * <p>The native object keeps the hotspot next to the cursor's size, as four consecutive ints
 * {@code x, y, width, height}. Their offset is not part of any contract, so the first ints of the
 * object are searched for that run, with the size the cursor is known to have and a hotspot inside
 * it; nothing matching means no answer rather than a guess.
 */
final class NativeCursorHotspot {

    private static final int INTS_SEARCHED = 64;

    private NativeCursorHotspot() {}

    static int[] of(Cursor cursor, int width, int height) {
        long awtCursor = AWTAccessor.getCursorAccessor().getPData(cursor);
        sun.misc.Unsafe unsafe = unsafe();
        if (awtCursor == 0 || unsafe == null) return null;
        int[] ints = new int[INTS_SEARCHED];
        for (int i = 0; i < ints.length; i++) ints[i] = unsafe.getInt(awtCursor + 4L * i);
        return find(ints, width, height);
    }

    static int[] find(int[] ints, int width, int height) {
        for (int i = 0; i + 3 < ints.length; i++) {
            int x = ints[i], y = ints[i + 1];
            boolean sized = ints[i + 2] == width && ints[i + 3] == height;
            if (sized && x >= 0 && x < width && y >= 0 && y < height) {
                return new int[] {x, y};
            }
        }
        return null;
    }

    private static sun.misc.Unsafe unsafe() {
        try {
            Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (sun.misc.Unsafe) field.get(null);
        } catch (ReflectiveOperationException | RuntimeException unavailable) {
            return null;
        }
    }
}
