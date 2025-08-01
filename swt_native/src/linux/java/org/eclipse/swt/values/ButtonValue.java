package org.eclipse.swt.values;

import com.google.auto.value.AutoValue;

import java.util.Optional;
import java.util.OptionalInt;

public interface ButtonValue extends ControlValue {

    OptionalInt getAlignment();

    Optional<Boolean> getGrayed();

    Optional<Boolean> getSelection();

    Optional<String> getText();

    Optional<String> getImage();

    static Builder builder() {
        return new AutoValue_ButtonValueA.Builder().setSwt("Button");
    }

    interface Builder extends ControlValue.Builder {

        Builder setAlignment(int alignment);

        OptionalInt getAlignment();

        Builder setGrayed(boolean grayed);

        Optional<Boolean> getGrayed();

        Builder setSelection(boolean selected);

        Optional<Boolean> getSelection();

        Optional<String> getImage();

        Builder setText(String string);

        Builder setImage(String string);

        Optional<String> getText();
    }
}

@AutoValue()
abstract class ButtonValueA implements ButtonValue {

    @AutoValue.Builder()
    abstract static class Builder implements ButtonValue.Builder {

        abstract public ButtonValueA build();
    }
}
