package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** A PopupList answers its List's items, and what the implementation reports about what it holds. */
@Tag("native-unit")
@ExtendWith(Mocks.class)
class PopupListImplementationNativeTest extends SerializeTestBase {

    @Test
    void theItemsAreTheListsNotTheArrayGiven() {
        String[] items = {"Apple", "Banana"};
        PopupList popup = new PopupList(Mocks.shell());
        popup.setItems(items);
        assertThat(popup.getItems()).containsExactly("Apple", "Banana").isNotSameAs(items);
        items[0] = "Changed";
        assertThat(popup.getItems()).containsExactly("Apple", "Banana");
    }

    @Test
    void itemsTheListRefusesAreNotAnswered() {
        PopupList popup = new PopupList(Mocks.shell());
        popup.setItems(new String[] {"Apple"});
        assertThatThrownBy(() -> popup.setItems(new String[] {"One", null})).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> popup.setItems(null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(popup.getItems()).containsExactly("Apple");
    }

    @Test
    void anImplementationBuiltWithoutItsApiCreatesOneOnceBoundToItself() {
        DartPopupList impl = new DartPopupList(Mocks.shell(), null);
        PopupList api = impl.getApi();
        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
        assertThat(impl.getValue()).isSameAs(impl.getValue());
    }

    @Test
    void theImplementationReportsWhatItHolds() {
        PopupList popup = new PopupList(Mocks.shell(), SWT.H_SCROLL);
        DartPopupList impl = (DartPopupList) popup.getImpl();
        popup.setItems(new String[] {"Apple", "Banana"});
        popup.setMinimumWidth(40);
        popup.select("Ba");

        assertThat(impl._shell()).isNotNull();
        assertThat(impl._list().getParent()).isSameAs(impl._shell());
        assertThat(impl._minimumWidth()).isEqualTo(40);
        assertThat(impl._items()).containsExactly("Apple", "Banana");
        assertThat(impl._selection()).isEqualTo("Ba");
        assertThat(impl._font()).isNull();
    }
}
