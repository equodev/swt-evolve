package org.eclipse.swt.values;

import com.google.auto.value.AutoValue;
import java.util.*;

public interface ToolItemValue extends ItemValue {

    Optional<Boolean> getEnabled();

    Optional<Boolean> getSelection();

    Optional<String> getToolTipText();

    OptionalInt getWidth();

    static Builder builder() {
        return new AutoValue_ToolItemValueA.Builder().setSwt("ToolItem");
    }

    interface Builder extends ItemValue.Builder {

        Builder setEnabled(boolean enabled);

        Optional<Boolean> getEnabled();

        Builder setSelection(boolean selected);

        Optional<Boolean> getSelection();

        Builder setToolTipText(String string);

        Optional<String> getToolTipText();

        Builder setWidth(int width);

        OptionalInt getWidth();
    }
}

@AutoValue()
abstract class ToolItemValueA implements ToolItemValue {

    @AutoValue.Builder()
    abstract static class Builder implements ToolItemValue.Builder {

        abstract public ToolItemValueA build();
    }
}
