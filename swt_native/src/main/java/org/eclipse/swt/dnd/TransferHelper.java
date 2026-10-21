package org.eclipse.swt.dnd;

/**
 * What a drop target outside SWT needs from a {@link Transfer}: its native format names and its
 * data in native form. Both are protected on {@code Transfer}, for the platform code of this
 * package; the AWT drop bridge stands in for that platform.
 */
public final class TransferHelper {

    private TransferHelper() {
    }

    public static String[] typeNames(Transfer transfer) {
        return transfer.getTypeNames();
    }

    /** Converts {@code data} into the transfer's first native type; null if it has none. */
    public static TransferData toNative(Transfer transfer, Object data) {
        TransferData[] types = transfer.getSupportedTypes();
        if (types.length == 0) return null;
        transfer.javaToNative(data, types[0]);
        return types[0];
    }
}
