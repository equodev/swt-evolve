package org.eclipse.swt.values;

import com.google.auto.value.AutoValue;
import java.util.*;

public interface TabItemValue extends ItemValue {

    Optional<String> getToolTipText();

    static Builder builder() {
        return new AutoValue_TabItemValueA.Builder().setSwt("TabItem");
    }

    interface Builder extends ItemValue.Builder {

        Builder setToolTipText(String string);

        Optional<String> getToolTipText();
    }
}

@AutoValue()
abstract class TabItemValueA implements TabItemValue {

    @AutoValue.Builder()
    abstract static class Builder implements TabItemValue.Builder {

        abstract public TabItemValueA build();
    }
}
