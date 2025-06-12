package org.eclipse.swt.values;

import com.google.auto.value.AutoValue;
import java.util.*;

public interface ScrollableValue extends ControlValue {

    static Builder builder() {
        return new AutoValue_ScrollableValueA.Builder().setSwt("Scrollable");
    }

    interface Builder extends ControlValue.Builder {
    }
}

@AutoValue()
abstract class ScrollableValueA implements ScrollableValue {

    @AutoValue.Builder()
    abstract static class Builder implements ScrollableValue.Builder {

        abstract public ScrollableValueA build();
    }
}
