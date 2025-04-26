package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.values.ButtonValue;

public class FlutterButton extends FlutterControl implements IButton {

    public FlutterButton(IComposite parent, int style) {
        super(parent, style);
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {
        // Not Generated
        return new Point(100, 30);
    }

    @Override
    public void addSelectionListener(SelectionListener listener) {
        checkWidget();
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        TypedListener typedListener = new TypedListener(listener);
        addListener(SWT.Selection, typedListener);
        addListener(SWT.DefaultSelection, typedListener);
    }

    @Override
    public int getAlignment() {
        if ((style & SWT.ARROW) != 0) {
            if ((style & SWT.UP) != 0)
                return SWT.UP;
            if ((style & SWT.DOWN) != 0)
                return SWT.DOWN;
            if ((style & SWT.LEFT) != 0)
                return SWT.LEFT;
            if ((style & SWT.RIGHT) != 0)
                return SWT.RIGHT;
            return SWT.UP;
        }
        if ((style & SWT.LEFT) != 0)
            return SWT.LEFT;
        if ((style & SWT.CENTER) != 0)
            return SWT.CENTER;
        if ((style & SWT.RIGHT) != 0)
            return SWT.RIGHT;
        return SWT.LEFT;
    }

    @Override
    public boolean getGrayed() {
        return builder().getGrayed().orElse(false);
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public boolean getSelection() {
        if ((style & (SWT.CHECK | SWT.RADIO | SWT.TOGGLE)) == 0)
            return false;
        return builder().getSelection().orElse(false);
    }

    @Override
    public String getText() {
        return builder().getText().orElse(null);
    }

    @Override
    public void removeSelectionListener(SelectionListener listener) {
        if (listener == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        if (eventTable == null)
            return;
        eventTable.unhook(SWT.Selection, listener);
        eventTable.unhook(SWT.DefaultSelection, listener);
    }

    @Override
    public void setAlignment(int alignment) {
        if ((style & SWT.ARROW) != 0) {
            if ((style & (SWT.UP | SWT.DOWN | SWT.LEFT | SWT.RIGHT)) == 0)
                return;
            style &= ~(SWT.UP | SWT.DOWN | SWT.LEFT | SWT.RIGHT);
            style |= alignment & (SWT.UP | SWT.DOWN | SWT.LEFT | SWT.RIGHT);
        }
        if ((alignment & (SWT.LEFT | SWT.RIGHT | SWT.CENTER)) == 0)
            return;
        style &= ~(SWT.LEFT | SWT.RIGHT | SWT.CENTER);
        style |= alignment & (SWT.LEFT | SWT.RIGHT | SWT.CENTER);
    }

    @Override
    public void setGrayed(boolean grayed) {
        builder().setGrayed(grayed);
        FlutterSwt.dirty(this);
    }

    @Override
    public void setImage(Image image) {
        if (image != null){
            builder().setImage(image.getImageData(). getFilename());
        }
    }

    @Override
    public void setSelection(boolean selected) {
        if ((style & (SWT.CHECK | SWT.RADIO | SWT.TOGGLE)) == 0)
            return;
        builder().setSelection(selected);
        FlutterSwt.dirty(this);
    }

    @Override
    public void setText(String string) {
        if (string == null)
            error(SWT.ERROR_NULL_ARGUMENT);
        //if ((style & SWT.ARROW) != 0)
          //  return;
        builder().setText(string);
        FlutterSwt.dirty(this);
    }

    public ButtonValue.Builder builder() {
        if (builder == null)
            builder = ButtonValue.builder().setId(hashCode()).setStyle(style);
        return (ButtonValue.Builder) builder;
    }

    @Override
    protected void hookEvents() {
        super.hookEvents();
        String ev = FlutterSwt.getEvent(this);
        FlutterSwt.CLIENT.getComm().on(ev + "/Selection/Selection", p -> {
            System.out.println(ev + "/Selection/Selection event");
            builder().setSelection(!getSelection());
            display.asyncExec(() -> {
                if ((style & SWT.RADIO) != 0) {
                    if ((parent.getStyle() & SWT.NO_RADIO_GROUP) == 0) {
                        // selectRadio()
                        for (IControl child : parent.getChildren()) {
                            if (child instanceof FlutterButton button) {
                                if (this != button) {
                                    button.setRadioSelection(false);
                                }
                            } else if (child instanceof SWTControl control) {
                                control.setRadioSelection(false);
                            }
                        }
                    }
                }
                sendEvent(SWT.Selection);
            });
        });
        FlutterSwt.CLIENT.getComm().on(ev + "/Selection/DefaultSelection", p -> {
            System.out.println(ev + "/Selection/DefaultSelection event");
            display.asyncExec(() -> {
                sendEvent(SWT.DefaultSelection);
            });
        });
    }

}
