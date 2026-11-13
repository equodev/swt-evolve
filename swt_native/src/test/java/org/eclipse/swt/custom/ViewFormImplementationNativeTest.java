package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** What a ViewForm does around the border it draws itself, and what it sends Flutter to draw. */
@Tag("native-unit")
public class ViewFormImplementationNativeTest extends SerializeTestBase {

    private static Composite fixed(Composite parent, int width, int height) {
        Composite child = new Composite(parent, SWT.NONE);
        child.setLayout(new Layout() {
            @Override
            protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
                return new Point(wHint == SWT.DEFAULT ? width : wHint, hHint == SWT.DEFAULT ? height : hHint);
            }

            @Override
            protected void layout(Composite composite, boolean flushCache) {
            }
        });
        return child;
    }

    /** The trim is the one-point border plus the two-point highlight on each side, and FLAT drops the highlight. */
    @Test
    void theTrimHoldsTheBorderAndTheHighlight() {
        Shell shell = Mocks.shell();
        assertThat(new ViewForm(shell, SWT.NONE).computeTrim(5, 6, 7, 8)).isEqualTo(new Rectangle(5, 6, 7, 8));
        assertThat(new ViewForm(shell, SWT.BORDER).computeTrim(5, 6, 7, 8)).isEqualTo(new Rectangle(2, 3, 13, 14));
        assertThat(new ViewForm(shell, SWT.BORDER | SWT.FLAT).computeTrim(5, 6, 7, 8)).isEqualTo(new Rectangle(4, 5, 9, 10));
        assertThat(new ViewForm(shell, SWT.BORDER).computeSize(SWT.DEFAULT, SWT.DEFAULT)).isEqualTo(new Point(6, 6));
    }

    /** The client area leaves out the border only, and the layout steps over the highlight inside it. */
    @Test
    void theClientAreaLeavesOutTheBorderAndTheContentSitsInsideTheHighlight() {
        ViewForm form = new ViewForm(Mocks.shell(), SWT.BORDER);
        form.setSize(300, 200);
        Composite content = fixed(form, 100, 50);
        form.setContent(content);
        assertThat(form.getClientArea()).isEqualTo(new Rectangle(1, 1, 298, 198));
        assertThat(content.getBounds()).isEqualTo(new Rectangle(3, 3, 294, 194));
    }

    /** setSelectionBackground has no caller in SWT; null stands for the list background once another colour was set. */
    @Test
    void theSelectionBackgroundFallsBackToTheListBackground() {
        ViewForm form = new ViewForm(Mocks.shell(), SWT.NONE);
        DartViewForm impl = (DartViewForm) form.getImpl();
        impl.setSelectionBackground(null);
        assertThat(impl._selectionBackground()).isNull();
        Color red = form.getDisplay().getSystemColor(SWT.COLOR_RED);
        impl.setSelectionBackground(red);
        impl.setSelectionBackground(red);
        assertThat(impl._selectionBackground()).isSameAs(red);
        impl.setSelectionBackground(null);
        assertThat(impl._selectionBackground()).isEqualTo(form.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    }

    /** What Flutter is sent to draw the border, the highlight and the separator, named again after each layout. */
    @Test
    void theValueCarriesWhatTheFormDraws() {
        ViewForm form = new ViewForm(Mocks.shell(), SWT.BORDER);
        form.setSize(300, 200);
        VViewForm value = ((DartViewForm) form.getImpl()).getValue();
        value.clearDirty();
        form.setTopLeft(fixed(form, 50, 20));
        form.setContent(fixed(form, 100, 50));

        assertThat(value.changedKeys()).contains(VViewForm.HIGHLIGHT, VViewForm.SEPARATOR);
        assertThat(value.getBorderVisible()).isTrue();
        assertThat(value.getHighlight()).isEqualTo(2);
        assertThat(value.getSeparator()).isEqualTo(24);

        value.clearDirty();
        form.setBorderVisible(false);
        assertThat(value.changedKeys()).contains(VViewForm.BORDER_VISIBLE, VViewForm.HIGHLIGHT, VViewForm.SEPARATOR);
        assertThat(value.getBorderVisible()).isFalse();
        assertThat(value.getHighlight()).isZero();
        assertThat(value.getSeparator()).isEqualTo(21);
        form.setContent(null);
        assertThat(value.getSeparator()).isEqualTo(-1);
    }
}
