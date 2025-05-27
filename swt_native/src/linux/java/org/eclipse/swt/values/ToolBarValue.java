package org.eclipse.swt.values;

import com.google.auto.value.AutoValue;
import java.util.*;

public interface ToolBarValue extends CompositeValue {

    static Builder builder() {
        return new AutoValue_ToolBarValueA.Builder().setSwt("ToolBar");
    }

    interface Builder extends CompositeValue.Builder {
    }
}

@AutoValue()
abstract class ToolBarValueA implements ToolBarValue {

    @AutoValue.Builder()
    abstract static class Builder implements ToolBarValue.Builder {

        abstract public ToolBarValueA build();
    }
}
