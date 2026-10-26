package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** An item takes its size from its control only for what the application has not set. */
@Tag("flutter-it")
class CoolItemSetControlNativeTest {

    private Display display;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = new Display();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) {
            display.dispose();
        }
        FlutterBridge.set(null);
    }

    private static CoolItem itemOn(CoolBar bar) {
        bar.setSize(600, 100);
        return new CoolItem(bar, SWT.NONE);
    }

    @Test
    void aPreferredSizeSetBeforeTheControlIsKept() {
        CoolBar bar = new CoolBar(new Shell(display), SWT.FLAT);
        CoolItem item = itemOn(bar);
        item.setPreferredSize(50, 30);

        item.setControl(new Button(bar, SWT.PUSH));

        assertThat(item.getPreferredSize()).isEqualTo(new Point(50, 30));
    }

    @Test
    void aMinimumSizeSetBeforeTheControlIsKept() {
        CoolBar bar = new CoolBar(new Shell(display), SWT.FLAT);
        CoolItem item = itemOn(bar);
        item.setMinimumSize(7, 5);

        item.setControl(new Button(bar, SWT.PUSH));

        assertThat(item.getMinimumSize()).isEqualTo(new Point(7, 5));
    }

    @Test
    void anItemWithNoSizeOfItsOwnTakesTheSizeOfItsControl() {
        CoolBar bar = new CoolBar(new Shell(display), SWT.FLAT);
        CoolItem item = itemOn(bar);
        Button button = new Button(bar, SWT.PUSH);
        button.setText("a button");

        item.setControl(button);

        Point wanted = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        assertThat(item.getPreferredSize().y).isEqualTo(wanted.y);
        assertThat(item.getPreferredSize().x).isEqualTo(item.computeSize(wanted.x, wanted.y).x);
        assertThat(item.getMinimumSize().y).isEqualTo(wanted.y);
    }
}
