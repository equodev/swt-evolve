package org.eclipse.swt.values;

import com.google.auto.value.AutoValue;
import java.util.*;

public interface TabFolderValue extends CompositeValue {

    OptionalInt getSelectionIndex();

    static Builder builder() {
        return new AutoValue_TabFolderValueA.Builder().setSwt("TabFolder");
    }

    interface Builder extends CompositeValue.Builder {

        Builder setSelectionIndex(int value);

        OptionalInt getSelectionIndex();
    }
}

@AutoValue()
abstract class TabFolderValueA implements TabFolderValue {

    @AutoValue.Builder()
    abstract static class Builder implements TabFolderValue.Builder {

        abstract public TabFolderValueA build();
    }
}
