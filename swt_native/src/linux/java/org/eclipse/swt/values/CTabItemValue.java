package org.eclipse.swt.values;

import com.google.auto.value.AutoValue;
import java.util.*;

public interface CTabItemValue extends ItemValue {

    Optional<Boolean> getShowClose();

    Optional<String> getToolTipText();

    static Builder builder() {
        return new AutoValue_CTabItemValueA.Builder().setSwt("CTabItem");
    }

    interface Builder extends ItemValue.Builder {

        Builder setShowClose(boolean close);

        Optional<Boolean> getShowClose();

        Builder setToolTipText(String string);

        Optional<String> getToolTipText();
    }
}

@AutoValue()
abstract class CTabItemValueA implements CTabItemValue {

    @AutoValue.Builder()
    abstract static class Builder implements CTabItemValue.Builder {

        abstract public CTabItemValueA build();
    }
}
