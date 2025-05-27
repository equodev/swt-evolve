package org.eclipse.swt.values;

import com.google.auto.value.AutoValue;
import java.util.*;

public interface MenuValue extends WidgetValue {

    Optional<Boolean> getEnabled();

    OptionalInt getOrientation();

    Optional<Boolean> getVisible();

    static Builder builder() {
        return new AutoValue_MenuValueA.Builder().setSwt("Menu");
    }

    interface Builder extends WidgetValue.Builder {

        Builder setEnabled(boolean enabled);

        Optional<Boolean> getEnabled();

        Builder setOrientation(int orientation);

        OptionalInt getOrientation();

        Builder setVisible(boolean visible);

        Optional<Boolean> getVisible();
    }
}

@AutoValue()
abstract class MenuValueA implements MenuValue {

    @AutoValue.Builder()
    abstract static class Builder implements MenuValue.Builder {

        abstract public MenuValueA build();
    }
}
