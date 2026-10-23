package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.NoopBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A {@link SWT#SHEET} is a dialog here, as on Win32 and GTK: it gets the dialog trim, and when it is shown
 * without having been placed it is centred on its parent's client area. Only Cocoa attaches a sheet to its
 * parent's window.
 */
@Tag("flutter-it")
class ShellSheetFlutterTest {

    private Display display;
    private Shell parent;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new NoopBridge());
        display = new Display();
        FlutterBridge.set(null);
        DartDisplay dd = (DartDisplay) display.getImpl();
        TestWebBridge bridge = new TestWebBridge(dd);
        dd.setBridge(bridge);
        bridge.start(dd);
        new Shell(display).open();
        parent = new Shell(display, SWT.SHELL_TRIM);
        parent.setBounds(100, 120, 600, 400);
        parent.open();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed())
            display.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void aSheetIsAPrimaryModalDialog() {
        int style = new Shell(parent, SWT.SHEET).getStyle();
        assertThat(style & SWT.SHEET).isZero();
        assertThat(style & SWT.DIALOG_TRIM).isEqualTo(SWT.DIALOG_TRIM);
        assertThat(style & SWT.PRIMARY_MODAL).isEqualTo(SWT.PRIMARY_MODAL);
    }

    @Test
    void aSheetOpensCentredOnItsParentsClientArea() {
        Shell sheet = new Shell(parent, SWT.SHEET);
        sheet.setSize(200, 100);
        sheet.open();
        Rectangle client = display.map(parent, null, parent.getClientArea());
        assertThat(sheet.getLocation()).isEqualTo(new Point(
                client.x + (client.width - 200) / 2,
                client.y + (client.height - 100) / 2));
    }

    @Test
    void aSheetThatWasPlacedKeepsItsLocation() {
        Shell sheet = new Shell(parent, SWT.SHEET);
        sheet.setBounds(30, 40, 200, 100);
        sheet.open();
        assertThat(sheet.getLocation()).isEqualTo(new Point(30, 40));
    }

    @Test
    void aDialogIsNotCentred() {
        Shell dialog = new Shell(parent, SWT.DIALOG_TRIM);
        dialog.setSize(200, 100);
        Point before = dialog.getLocation();
        dialog.open();
        assertThat(dialog.getLocation()).isEqualTo(before);
    }
}
