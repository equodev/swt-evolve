package org.eclipse.swt.values;

import com.google.auto.value.AutoValue;
import java.util.*;

public interface WidgetValue {

    OptionalInt getStyle();

    String getSwt();

    long getId();

    Optional<List<WidgetValue>> getChildren();

    static Builder builder() {
        return new AutoValue_WidgetValueA.Builder().setSwt("Widget");
    }

    interface Builder {

        Builder setSwt(String type);

        Builder setId(long id);

        Builder setChildren(List<WidgetValue> children);

        WidgetValue build();

        Builder setStyle(int value);

        OptionalInt getStyle();
    }
}

@AutoValue()
abstract class WidgetValueA implements WidgetValue {

    @AutoValue.Builder()
    abstract static class Builder implements WidgetValue.Builder {

        abstract public WidgetValueA build();
    }
}
