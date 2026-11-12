package org.eclipse.swt.custom;

import dev.equo.swt.WidgetWire;
import dev.equo.swt.WireObject;

/**
 * One edit of a StyledText's content as its client is told it: the range replaced ({@code start} to
 * {@code end}), the text put there and the length the content has after it. Written as the client's
 * event carries those fields, which it reads with no map of names; each StyledText reuses one.
 */
final class TextEdit implements WireObject {

    private final int countKey;
    private final int endKey;
    private final int startKey;
    private final int textKey;
    private final int typeId;

    int count;
    int end;
    int start;
    String text;

    /** The keys and the type number are the generated ones of the client's Event. */
    TextEdit(int countKey, int endKey, int startKey, int textKey, int typeId) {
        this.countKey = countKey;
        this.endKey = endKey;
        this.startKey = startKey;
        this.textKey = textKey;
        this.typeId = typeId;
    }

    @Override
    public void writeWireFields(WidgetWire w) {
        w.key(countKey);
        w.write(count);
        w.key(endKey);
        w.write(end);
        w.key(startKey);
        w.write(start);
        w.key(textKey);
        w.write(text);
    }

    @Override
    public int wireTypeId() {
        return typeId;
    }
}
