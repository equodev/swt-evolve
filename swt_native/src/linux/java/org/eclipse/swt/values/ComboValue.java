package org.eclipse.swt.values;

import com.google.auto.value.AutoValue;
import java.util.*;

public interface ComboValue extends CompositeValue {

    Optional<String[]> getItems();

    Optional<Boolean> getListVisible();

    OptionalInt getOrientation();

    OptionalInt getSelectionIndex();

    Optional<String> getText();

    OptionalInt getTextLimit();

    OptionalInt getVisibleItemCount();

    static Builder builder() {
        return new AutoValue_ComboValueA.Builder().setSwt("Combo");
    }

    interface Builder extends CompositeValue.Builder {

        Builder setItems(String... items);

        Optional<String[]> getItems();

        Builder setListVisible(boolean visible);

        Optional<Boolean> getListVisible();

        Builder setOrientation(int orientation);

        OptionalInt getOrientation();

        Builder setSelectionIndex(int value);

        OptionalInt getSelectionIndex();

        Builder setText(String string);

        Optional<String> getText();

        Builder setTextLimit(int limit);

        OptionalInt getTextLimit();

        Builder setVisibleItemCount(int count);

        OptionalInt getVisibleItemCount();
    }
}

@AutoValue()
abstract class ComboValueA implements ComboValue {

    @AutoValue.Builder()
    abstract static class Builder implements ComboValue.Builder {

        abstract public ComboValueA build();
    }
}
