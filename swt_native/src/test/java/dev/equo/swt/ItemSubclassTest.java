package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.eclipse.swt.widgets.Mocks.swtShell;

/**
 * {@link Item} is one of the few SWT widgets that permits subclassing outside SWT — its
 * {@code checkSubclass()} is a documented no-op — and Eclipse relies on that (JDT's editor
 * breadcrumb builds its items that way). SWT's own item classes never reach the public
 * {@code Item(Widget, int)} constructor; they route through the protected impl constructor, so
 * this constructor exists solely for such subclasses and has to install an implementation.
 */
@ExtendWith(Mocks.class)
@ExtendWith(MockFlutterBridge.Extension.class)
public class ItemSubclassTest {

    /** Shaped like JDT's {@code BreadcrumbItem}: extends Item, parented to a Composite. */
    private static final class SubclassedItem extends Item {
        SubclassedItem(Composite parent) {
            super(parent, SWT.NONE);
        }
    }

    @BeforeEach
    void setup() {
        Config.defaultToEquo();
        Config.useEquo(Composite.class);
    }

    @AfterEach
    void reset() {
        System.clearProperty("dev.equo.swt.Composite");
    }

    @Test
    public void subclassedItem_hasAnImplementation() {
        SubclassedItem item = new SubclassedItem(new Composite(swtShell(), SWT.NONE));

        assertThat(item.getImpl()).as("impl of an application subclass of Item").isNotNull();
    }

    @Test
    public void subclassedItem_carriesData() {
        SubclassedItem item = new SubclassedItem(new Composite(swtShell(), SWT.NONE));

        assertThatCode(() -> item.setData("element")).doesNotThrowAnyException();
        assertThat(item.getData()).isEqualTo("element");
    }

    @Test
    public void subclassedItem_carriesText() {
        SubclassedItem item = new SubclassedItem(new Composite(swtShell(), SWT.NONE));

        item.setText("sample.Common");
        assertThat(item.getText()).isEqualTo("sample.Common");
    }
}
