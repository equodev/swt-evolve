package org.eclipse.swt.widgets;

import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A dialog that has been shown, focused and closed is garbage once the application lets go of it:
 * nothing the bridge or the comm keeps for a live widget may outlive its dispose.
 */
@Tag("flutter-it")
class ClosedDialogIsCollectedFlutterTest {

    @Test
    void closedDialogsAreCollected() throws InterruptedException {
        WebDisplayHarness flutter = new WebDisplayHarness();
        Display display = flutter.boot();
        try {
            Shell parent = new Shell(display);
            parent.setBounds(0, 0, 800, 600);
            parent.open();
            flutter.settle();

            List<WeakReference<Object>> closed = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                Shell dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
                dialog.setLayout(new FillLayout());
                Composite form = new Composite(dialog, SWT.NONE);
                form.setLayout(new GridLayout(2, false));
                for (int row = 0; row < 3; row++) {
                    new Label(form, SWT.NONE).setText("Field " + row);
                    new Text(form, SWT.BORDER).setText("value " + row);
                }
                dialog.setBounds(140, 140, 400, 300);
                dialog.open();
                flutter.settle();
                dialog.close();
                flutter.settle();
                closed.add(new WeakReference<>(dialog));
                closed.add(new WeakReference<>(dialog.getImpl()));
                closed.add(new WeakReference<>(form));
            }

            for (int i = 0; i < 10 && closed.stream().anyMatch(r -> r.get() != null); i++) {
                System.gc();
                Thread.sleep(20);
            }
            assertThat(closed).allSatisfy(r -> assertThat(r.get()).as("a closed dialog's widget").isNull());
        } finally {
            flutter.teardown();
        }
    }
}
