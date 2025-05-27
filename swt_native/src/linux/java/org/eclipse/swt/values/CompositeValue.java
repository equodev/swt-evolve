package org.eclipse.swt.values;

import com.google.auto.value.AutoValue;
import java.util.*;
import org.eclipse.swt.widgets.Layout;

public interface CompositeValue extends ScrollableValue {

    OptionalInt getBackgroundMode();

    Optional<Layout> getLayout();

    Optional<Boolean> getLayoutDeferred();

    static Builder builder() {
        return new AutoValue_CompositeValueA.Builder().setSwt("Composite");
    }

    interface Builder extends ScrollableValue.Builder {

        Builder setBackgroundMode(int mode);

        OptionalInt getBackgroundMode();

        Builder setLayout(Layout layout);

        Optional<Layout> getLayout();

        Builder setLayoutDeferred(boolean defer);

        Optional<Boolean> getLayoutDeferred();
    }
}

@AutoValue()
abstract class CompositeValueA implements CompositeValue {

    @AutoValue.Builder()
    abstract static class Builder implements CompositeValue.Builder {

        abstract public CompositeValueA build();
    }
}
