package org.eclipse.swt.values;

import com.google.auto.value.AutoValue;
import java.util.*;

public interface TreeItemValue extends ItemValue {

    Optional<Boolean> getChecked();

    Optional<Boolean> getExpanded();

    Optional<Boolean> getGrayed();

    OptionalInt getItemCount();

    Optional<String[]> getTexts();

    Optional<Boolean> getSelected();

    Optional<String> getImage();

    static Builder builder() {
        return new AutoValue_TreeItemValueA.Builder().setSwt("TreeItem");
    }

    interface Builder extends ItemValue.Builder {

        Builder setChecked(boolean checked);

        Optional<Boolean> getChecked();

        Builder setExpanded(boolean expanded);

        Optional<Boolean> getExpanded();

        Builder setGrayed(boolean grayed);

        Optional<Boolean> getGrayed();

        Builder setItemCount(int count);

        OptionalInt getItemCount();

        Builder setTexts(String[] strings);

        Optional<String[]> getTexts();

        Builder setSelected(boolean selected);

        Optional<Boolean> getSelected();

        Optional<String> getImage();

        Builder setImage(String string);
    }
}

@AutoValue()
abstract class TreeItemValueA implements TreeItemValue {

    @AutoValue.Builder()
    abstract static class Builder implements TreeItemValue.Builder {

        abstract public TreeItemValueA build();
    }
}
