package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display.*;

public class VText extends VScrollable {

    protected VText() {
    }

    protected VText(DartText impl) {
        super(impl);
    }

    public int getCaretPosition() {
        return ((DartText) impl).getCaretPosition();
    }

    public void setCaretPosition(int value) {
        ((DartText) impl).caretPosition = value;
    }

    public boolean getDoubleClickEnabled() {
        return ((DartText) impl).getDoubleClickEnabled();
    }

    public void setDoubleClickEnabled(boolean value) {
        ((DartText) impl).doubleClick = value;
    }

    public int getEchoCharacter() {
        return ((DartText) impl).getEchoChar();
    }

    public void setEchoCharacter(int value) {
        ((DartText) impl).echoCharacter = (char) value;
    }

    public boolean getEditable() {
        return ((DartText) impl).getEditable();
    }

    public void setEditable(boolean value) {
        ((DartText) impl).editable = value;
    }

    public char[] getHiddenText() {
        return ((DartText) impl).hiddenText;
    }

    public void setHiddenText(char[] value) {
        ((DartText) impl).hiddenText = value;
    }

    public String getMessage() {
        return ((DartText) impl).getMessage();
    }

    public void setMessage(String value) {
        ((DartText) impl).message = value;
    }

    public Point getSelection() {
        return ((DartText) impl).getSelection();
    }

    public void setSelection(Point value) {
        ((DartText) impl).selection = value;
    }

    public int getTabs() {
        return ((DartText) impl).getTabs();
    }

    public void setTabs(int value) {
        ((DartText) impl).tabs = value;
    }

    public String getText() {
        return ((DartText) impl).getText();
    }

    public void setText(String value) {
        ((DartText) impl).text = value;
    }

    public char[] getTextChars() {
        return ((DartText) impl).textChars;
    }

    public void setTextChars(char[] value) {
        ((DartText) impl).textChars = value;
    }

    public int getTextLimit() {
        return ((DartText) impl).getTextLimit();
    }

    public void setTextLimit(int value) {
        ((DartText) impl).textLimit = value;
    }

    public int getTopIndex() {
        return ((DartText) impl).getTopIndex();
    }

    public void setTopIndex(int value) {
        ((DartText) impl).topIndex = value;
    }

    public int[] getEdits() {
        return ((DartText) impl).apiEdits.read();
    }

    public String[] getEditTexts() {
        return ((DartText) impl).apiEdits.texts();
    }

    public static final int CARET_POSITION = 576;

    public static final int ECHO_CHARACTER = 577;

    public static final int EDIT_TEXTS = 578;

    public static final int EDITABLE = 579;

    public static final int EDITS = 580;

    public static final int MESSAGE = 581;

    public static final int SELECTION = 582;

    public static final int TEXT = 583;

    public static final int TEXT_LIMIT = 584;

    @Override
    public String wireName(int key) {
        switch(key) {
            case CARET_POSITION:
                return "caretPosition";
            case ECHO_CHARACTER:
                return "echoCharacter";
            case EDITABLE:
                return "editable";
            case MESSAGE:
                return "message";
            case SELECTION:
                return "selection";
            case TEXT:
                return "text";
            case TEXT_LIMIT:
                return "textLimit";
            case EDITS:
                return "edits";
            case EDIT_TEXTS:
                return "editTexts";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 576:
                w.write((int) getCaretPosition());
                return true;
            case 577:
                w.write((int) getEchoCharacter());
                return true;
            case 579:
                w.write(getEditable());
                return true;
            case 581:
                w.write(getMessage());
                return true;
            case 582:
                w.write(getSelection());
                return true;
            case 583:
                w.write(getText());
                return true;
            case 584:
                w.write((int) getTextLimit());
                return true;
            case 580:
                w.write(getEdits());
                return true;
            case 578:
                w.write(getEditTexts());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 576:
                {
                    int v_ = getCaretPosition();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(576);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 577:
                {
                    int v_ = getEchoCharacter();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(577);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 579:
                {
                    boolean v_ = getEditable();
                    if (!v_)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(579);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 581:
                {
                    String v_ = getMessage();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(581);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 582:
                {
                    Point v_ = getSelection();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(582);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 583:
                {
                    String v_ = getText();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(583);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 584:
                {
                    int v_ = getTextLimit();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(584);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 580:
                {
                    int[] v_ = getEdits();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(580);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 578:
                {
                    String[] v_ = getEditTexts();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.length == 0)
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(578);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VScrollable.allWireKeys_(), new int[] { 576, 577, 579, 581, 582, 583, 584, 580, 578 });

    protected static int[] allWireKeys_() {
        return wireKeys_;
    }

    @Override
    public int[] wireKeys() {
        return wireKeys_;
    }

    @Override
    public boolean wireAlways(int key) {
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }
}
