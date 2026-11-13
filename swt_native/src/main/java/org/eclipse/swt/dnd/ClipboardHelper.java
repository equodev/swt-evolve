package org.eclipse.swt.dnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The clipboards of a Flutter-backed Display, one set per session: per clipboard, every type name of every transfer
 * set, in order. Only text also reaches the system clipboard. They behave the same on every OS:
 * <ul>
 * <li>a clipboard keeps the writer's data and converts it each time it is read, as on Win32 and GTK; the javadoc
 * leaves it open, and Cocoa converts it when it is set;</li>
 * <li>only the clipboard that wrote the data clears it, as the javadoc says; Cocoa clears it for anyone;</li>
 * <li>{@link DND#SELECTION_CLIPBOARD} is a clipboard of its own, as the javadoc describes it; only GTK has one
 * natively;</li>
 * <li>a clipboard offers the types set and no others; a macOS pasteboard also derives some from them.</li>
 * </ul>
 */
final class ClipboardHelper {

    private static final int[] CLIPBOARDS = {DND.CLIPBOARD, DND.SELECTION_CLIPBOARD};

    /** One type on a clipboard: the transfer and the datum to make its native form from. */
    private static final class Entry {
        final Transfer transfer;
        final Object datum;

        Entry(Transfer transfer, Object datum) {
            this.transfer = transfer;
            this.datum = datum;
        }

        TransferData data(String name) {
            TransferData data = typed(name);
            transfer.javaToNative(datum, data);
            return data;
        }
    }

    private static final class Board {
        final Object owner;
        final Map<String, Entry> types = new LinkedHashMap<>();

        Board(Object owner) {
            this.owner = owner;
        }

        void put(Transfer transfer, Object datum) {
            for (String name : transfer.getTypeNames())
                types.put(name, new Entry(transfer, datum));
        }
    }

    private final Map<Integer, Board> boards = new HashMap<>();
    /** The text this session last put on, or took from, the system clipboard. */
    private String systemText;

    private static TransferData typed(String name) {
        TransferData data = new TransferData();
        data.type = Transfer.registerType(name);
        return data;
    }

    /**
     * Replaces what the clipboards hold. The arguments are checked by the caller, so the datum of a TextTransfer is
     * a string. Returns the text the system clipboard holds now, empty when the data has none, or null when
     * {@link DND#CLIPBOARD} was not written.
     */
    synchronized String setContents(Object owner, Object[] data, Transfer[] dataTypes, int clipboards) {
        Board board = new Board(owner);
        String text = "";
        for (int i = 0; i < dataTypes.length; i++) {
            board.put(dataTypes[i], data[i]);
            if (text.isEmpty() && dataTypes[i] instanceof TextTransfer)
                text = (String) data[i];
        }
        for (int clipboard : CLIPBOARDS) {
            if ((clipboards & clipboard) != 0)
                boards.put(clipboard, board);
        }
        if ((clipboards & DND.CLIPBOARD) == 0)
            return null;
        systemText = text;
        return text;
    }

    /** The datum of the first type of the transfer the clipboards hold, converted by the transfer; null when none. */
    synchronized Object getContents(Transfer transfer, int clipboards) {
        for (String name : transfer.getTypeNames()) {
            for (int clipboard : CLIPBOARDS) {
                Board board = (clipboards & clipboard) != 0 ? boards.get(clipboard) : null;
                Entry entry = board != null ? board.types.get(name) : null;
                if (entry != null)
                    return transfer.nativeToJava(entry.data(name));
            }
        }
        return null;
    }

    synchronized TransferData[] getAvailableTypes(int clipboards) {
        List<TransferData> types = new ArrayList<>();
        for (int clipboard : CLIPBOARDS) {
            Board board = (clipboards & clipboard) != 0 ? boards.get(clipboard) : null;
            if (board != null) {
                for (String name : board.types.keySet())
                    types.add(typed(name));
            }
        }
        return types.toArray(new TransferData[0]);
    }

    /** The type names {@link DND#CLIPBOARD}, the system clipboard, holds. */
    synchronized String[] getAvailableTypeNames() {
        Board board = boards.get(DND.CLIPBOARD);
        return board == null ? new String[0] : board.types.keySet().toArray(new String[0]);
    }

    /**
     * Empties the clipboards {@code owner} may clear. Returns the text the system clipboard holds now, empty,
     * or null when {@link DND#CLIPBOARD} was left as it was.
     */
    synchronized String clearContents(Object owner, int clipboards) {
        String text = null;
        for (int clipboard : CLIPBOARDS) {
            Board board = (clipboards & clipboard) != 0 ? boards.get(clipboard) : null;
            if (board == null || board.owner != owner)
                continue;
            boards.remove(clipboard);
            if (clipboard == DND.CLIPBOARD)
                text = systemText = "";
        }
        return text;
    }

    /**
     * Takes text another application put on the system clipboard: it replaces what {@link DND#CLIPBOARD} holds.
     * Text this session wrote, and a system clipboard with no text or one that cannot be read, change nothing.
     */
    synchronized void importSystemText(String text) {
        if (text == null || text.isEmpty() || text.equals(systemText))
            return;
        Board board = new Board(null);
        board.put(TextTransfer.getInstance(), text);
        boards.put(DND.CLIPBOARD, board);
        systemText = text;
    }
}
