package org.eclipse.swt.dnd;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.WidgetWire;
import dev.equo.swt.WireObject;
import org.eclipse.swt.widgets.DartWidget;

/**
 * What a drop target last told the client during a drag: the operation, feedback and data type
 * agreed. Written as the client's event carries them rather than as a whole Event: an Event writes
 * fields the client's event does not have, and the client cannot read past one it does not know.
 */
public final class DropAnswer implements WireObject {

    private final int detailKey;
    private final int feedbackKey;
    private final int dataTypeKey;
    private final int typeId;

    private boolean answered;
    private int detail;
    private int feedback;
    private int dataType;

    /** The keys and the type number are the generated ones of the client's Event. */
    public DropAnswer(int detailKey, int feedbackKey, int dataTypeKey, int typeId) {
        this.detailKey = detailKey;
        this.feedbackKey = feedbackKey;
        this.dataTypeKey = dataTypeKey;
        this.typeId = typeId;
    }

    /** Forgets the last answer, so the next one is sent whatever it is. */
    public void reset() {
        answered = false;
    }

    /** Tells the client the answer through {@code target}; nothing when it is the one the client keeps. */
    public void sendIfChanged(DartWidget target, int detail, int feedback, int dataType) {
        if (answered && this.detail == detail && this.feedback == feedback && this.dataType == dataType)
            return;
        answered = true;
        this.detail = detail;
        this.feedback = feedback;
        this.dataType = dataType;
        FlutterBridge.sendFields(target, "Drop/negotiationResult", this);
    }

    @Override
    public void writeWireFields(WidgetWire w) {
        w.key(detailKey);
        w.write(detail);
        w.key(feedbackKey);
        w.write(feedback);
        w.key(dataTypeKey);
        w.write(dataType);
    }

    @Override
    public int wireTypeId() {
        return typeId;
    }
}
