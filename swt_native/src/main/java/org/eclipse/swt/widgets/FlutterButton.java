package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.util.ButtonSwtSizingConstants;
import org.eclipse.swt.values.ButtonValue;

public class FlutterButton extends FlutterControl implements IButton {

    public FlutterButton(IComposite parent, int style) {
        super(parent, style);
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

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        checkWidget(); // Standard SWT check to ensure the widget isn't disposed

        int style = getStyle();
        String text = getText(); // Get the button's text
        int textLength = text != null ? text.length() : 0;
        // Check if the button currently has an image set
        boolean hasImage = getImage() != null;

        double calculatedWidth = 0;
        double calculatedHeight = 0;

        // Determine size based on button style (PUSH, CHECK, RADIO, TOGGLE)
        if ((style & SWT.PUSH) != 0) {
            // Calculate size for a standard Push Button
            calculatedWidth = ButtonSwtSizingConstants.calculatePushWidth(textLength); //
            calculatedHeight = ButtonSwtSizingConstants.PUSH_FIXED_HEIGHT; //
        } else if ((style & SWT.CHECK) != 0) {
            // Calculate size for a Checkbox Button
            calculatedWidth = ButtonSwtSizingConstants.calculateCheckWidth(textLength); //
            calculatedHeight = ButtonSwtSizingConstants.CHECK_FIXED_HEIGHT; //
        } else if ((style & SWT.RADIO) != 0) {
            // Calculate size for a Radio Button
            calculatedWidth = ButtonSwtSizingConstants.calculateRadioWidth(textLength); //
            calculatedHeight = ButtonSwtSizingConstants.RADIO_FIXED_HEIGHT; //
        } else if ((style & SWT.TOGGLE) != 0) {
            // Calculate size for a Toggle Button
            calculatedHeight = ButtonSwtSizingConstants.TOGGLE_FIXED_HEIGHT; // Height is fixed for all toggle types
            if (hasImage && textLength == 0) {
                // Toggle Button with Icon only
                calculatedWidth = ButtonSwtSizingConstants.calculateToggleIconOnlyWidth(); //
            } else if (hasImage && textLength > 0) {
                // Toggle Button with Text and Icon
                calculatedWidth = ButtonSwtSizingConstants.calculateToggleTextAndIconWidth(textLength); //
            } else {
                // Toggle Button with Text only (or neither, fallback to text only calculation)
                calculatedWidth = ButtonSwtSizingConstants.calculateToggleTextOnlyWidth(textLength); //
            }
        } else {
            // --- Fallback Case ---
            // If the style is none of the above (or SWT.FLAT without other types?),
            // default to PUSH button sizing as a reasonable fallback.
            // Alternatively, you could call super.computeSize() if appropriate,
            // or throw an exception for unsupported styles.
            System.err.println("Warning: Unsupported Button style encountered in computeSize. Falling back to PUSH sizing.");
            calculatedWidth = ButtonSwtSizingConstants.calculatePushWidth(textLength); //
            calculatedHeight = ButtonSwtSizingConstants.PUSH_FIXED_HEIGHT; //
        }

        // --- Apply Hints ---
        // If a specific width or height hint is provided (and it's not SWT.DEFAULT),
        // use the hint value instead of the calculated value.
        if (wHint != SWT.DEFAULT) {
            calculatedWidth = wHint;
        }
        if (hHint != SWT.DEFAULT) {
            calculatedHeight = hHint;
        }

        int trimWidth = 0;
        int trimHeight = 0;
        // Calculate the trim based on the style and OS. Example values:
        if ((style & SWT.BORDER) != 0) {
//             Simplified: Actual trim depends on OS theme. Use computeTrim for accuracy.
            trimWidth = 4; // e.g., 2px border on each side
            trimHeight = 4; // e.g., 2px border top/bottom
        }

        // --- Final Dimensions ---
        // Convert double calculations to integers (rounding up) and add trim.
        int finalWidth = (int) Math.ceil(calculatedWidth) + trimWidth;
        int finalHeight = (int) Math.ceil(calculatedHeight) + trimHeight;

        // --- Apply SWT Constraints ---
        // Ensure the size isn't negative or unreasonably small (though minWidth in constants helps).
        finalWidth = Math.max(0, finalWidth);
        finalHeight = Math.max(0, finalHeight);

        return new Point(finalWidth, finalHeight);
    }

}
