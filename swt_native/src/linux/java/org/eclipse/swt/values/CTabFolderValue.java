package org.eclipse.swt.values;

import com.google.auto.value.AutoValue;
import java.util.*;

public interface CTabFolderValue extends CompositeValue {

    Optional<Boolean> getBorderVisible();

    Optional<Boolean> getMinimized();

    Optional<Boolean> getMinimizeVisible();

    OptionalInt getMinimumCharacters();

    Optional<Boolean> getMaximized();

    Optional<Boolean> getMaximizeVisible();

    Optional<Boolean> getMRUVisible();

    OptionalInt getSelectionIndex();

    Optional<Boolean> getSimple();

    Optional<Boolean> getSingle();

    OptionalInt getStyle();

    OptionalInt getTabHeight();

    OptionalInt getTabPosition();

    Optional<Boolean> getUnselectedCloseVisible();

    Optional<Boolean> getUnselectedImageVisible();

    Optional<Boolean> getHighlightEnabled();

    static Builder builder() {
        return new AutoValue_CTabFolderValueA.Builder().setSwt("CTabFolder");
    }

    interface Builder extends CompositeValue.Builder {

        Builder setBorderVisible(boolean show);

        Optional<Boolean> getBorderVisible();

        Builder setMinimized(boolean minimize);

        Optional<Boolean> getMinimized();

        Builder setMinimizeVisible(boolean visible);

        Optional<Boolean> getMinimizeVisible();

        Builder setMinimumCharacters(int count);

        OptionalInt getMinimumCharacters();

        Builder setMaximized(boolean maximize);

        Optional<Boolean> getMaximized();

        Builder setMaximizeVisible(boolean visible);

        Optional<Boolean> getMaximizeVisible();

        Builder setMRUVisible(boolean show);

        Optional<Boolean> getMRUVisible();

        Builder setSelectionIndex(int value);

        OptionalInt getSelectionIndex();

        Builder setSimple(boolean simple);

        Optional<Boolean> getSimple();

        Builder setSingle(boolean single);

        Optional<Boolean> getSingle();

        Builder setStyle(int value);

        OptionalInt getStyle();

        Builder setTabHeight(int height);

        OptionalInt getTabHeight();

        Builder setTabPosition(int position);

        OptionalInt getTabPosition();

        Builder setUnselectedCloseVisible(boolean visible);

        Optional<Boolean> getUnselectedCloseVisible();

        Builder setUnselectedImageVisible(boolean visible);

        Optional<Boolean> getUnselectedImageVisible();

        Builder setHighlightEnabled(boolean enabled);

        Optional<Boolean> getHighlightEnabled();
    }
}

@AutoValue()
abstract class CTabFolderValueA implements CTabFolderValue {

    @AutoValue.Builder()
    abstract static class Builder implements CTabFolderValue.Builder {

        abstract public CTabFolderValueA build();
    }
}
