package org.eclipse.swt.values;

import com.google.auto.value.AutoValue;
import java.util.*;

public interface ItemValue extends WidgetValue {

    Optional<String> getText();
    Optional<String> getImage();
    Optional<byte[]> getImageData();

    static Builder builder() {

        return new AutoValue_ItemValueA.Builder().setSwt("Item");
    }

    interface Builder extends WidgetValue.Builder {

        Builder setText(String string);

        Optional<String> getText();
        Optional<String> getImage();
        Optional<byte[]> getImageData();
        Builder setImage(String string);
        Builder setImageData(byte[] data);
    }
}

@AutoValue()
abstract class ItemValueA implements ItemValue {

    @AutoValue.Builder()
    abstract static class Builder implements ItemValue.Builder {

        abstract public ItemValueA build();
    }
}
