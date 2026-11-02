package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.harness.WireFrames;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/** A frame carries each widget once, however many times it is named or described while being written. */
@ExtendWith(Mocks.class)
class TapeDescribesOnceTest {

    private static final int CLIENT = 1;

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
        Serializer.enterConnection(CLIENT);
    }

    @AfterEach
    void tearDown() {
        Serializer.exitConnection();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a widget named twice and then described has one record in the frame")
    void oneRecordPerWidget() {
        Shell shell = new Shell(Display.getDefault());
        Button button = new Button(shell, SWT.PUSH);
        Serializer.Tape tape = new Serializer.Tape(CLIENT);
        try {
            tape.widget(button);
            tape.widget(button);
            tape.describe((DartWidget) shell.getImpl());
            tape.describe((DartWidget) button.getImpl());
            Serializer.discardWritten();
            long records = WireFrames.records(tape.frame().payload()).stream()
                    .filter(record -> record.channel().endsWith("/" + button.hashCode()))
                    .count();
            assertThat(records).isEqualTo(1);
        } finally {
            tape.release();
            shell.dispose();
        }
    }

    @Test
    @DisplayName("a second frame describes again a widget the first one described")
    void eachFrameDescribesItsOwn() {
        Shell shell = new Shell(Display.getDefault());
        Button button = new Button(shell, SWT.PUSH);
        try {
            for (int frame = 0; frame < 2; frame++) {
                Serializer.Tape tape = new Serializer.Tape(CLIENT);
                try {
                    tape.describe((DartWidget) button.getImpl());
                    Serializer.discardWritten();
                    assertThat(WireFrames.records(tape.frame().payload()))
                            .anyMatch(record -> record.channel().endsWith("/" + button.hashCode()));
                } finally {
                    tape.release();
                }
            }
        } finally {
            shell.dispose();
        }
    }
}
