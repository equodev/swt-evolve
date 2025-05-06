package org.eclipse.swt.values;

import com.google.auto.value.AutoValue;
import java.util.*;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;

public interface ControlValue extends WidgetValue {

    OptionalInt getOrientation();

    OptionalInt getTextDirection();

    Optional<Rectangle> getBounds();

    Optional<Boolean> getDragDetect();

    Optional<Boolean> getEnabled();

    Optional<Object> getLayoutData();

    Optional<MenuValue> getMenu();

    Optional<String> getToolTipText();

    Optional<Boolean> getTouchEnabled();

    Optional<Boolean> getVisible();

    static Builder builder() {
        return new AutoValue_ControlValueA.Builder().setSwt("Control");
    }

    interface Builder extends WidgetValue.Builder {

        Builder setOrientation(int orientation);

        OptionalInt getOrientation();

        Builder setTextDirection(int textDirection);

        OptionalInt getTextDirection();

        Builder setBounds(Rectangle rect);

        Optional<Rectangle> getBounds();

        Builder setDragDetect(boolean dragDetect);

        Optional<Boolean> getDragDetect();

        Builder setEnabled(boolean enabled);

        Optional<Boolean> getEnabled();

        Builder setLayoutData(Object layoutData);

        Optional<Object> getLayoutData();

        Builder setMenu(MenuValue menu);

        Optional<MenuValue> getMenu();

        Builder setToolTipText(String string);

        Optional<String> getToolTipText();

        Builder setTouchEnabled(boolean enabled);

        Optional<Boolean> getTouchEnabled();

        Builder setVisible(boolean visible);

        Optional<Boolean> getVisible();
    }
}

@AutoValue()
abstract class ControlValueA implements ControlValue {

    @AutoValue.Builder()
    abstract static class Builder implements ControlValue.Builder {

        abstract public ControlValueA build();
    }
}
