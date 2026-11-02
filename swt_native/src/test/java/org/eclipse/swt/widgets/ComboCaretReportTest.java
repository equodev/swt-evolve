package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.Serializer;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An editable Combo answers getSelection() and getCaretPosition() from the caret and anchor the client reports, as
 * Text does; a range set from code keeps the caret at its start.
 */
@ExtendWith(Mocks.class)
class ComboCaretReportTest {

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

    private static Combo combo(int style) {
        Combo combo = new Combo(Mocks.shell(), style);
        combo.setItems(new String[] {"Initial", "Other"});
        combo.setText("Initial");
        return combo;
    }

    @Test
    void aSelectAllTheClientReportsHasTheCaretAtItsEnd() {
        Combo combo = combo(SWT.DROP_DOWN);

        ComboHelper.clientCaretMoved((DartCombo) combo.getImpl(), 7, 0);

        assertThat(combo.getSelection()).isEqualTo(new Point(0, 7));
        assertThat(combo.getCaretPosition()).isEqualTo(7);
    }

    @Test
    void aCaretReportedAfterTypingFollowsTheNewText() {
        Combo combo = combo(SWT.DROP_DOWN);
        DartCombo impl = (DartCombo) combo.getImpl();
        ComboHelper.clientCaretMoved(impl, 7, 0);

        Event typed = new Event();
        typed.text = "x";
        ComboHelper.handleModify(impl, typed);
        ComboHelper.clientCaretMoved(impl, 1, 1);

        assertThat(combo.getText()).isEqualTo("x");
        assertThat(combo.getSelection()).isEqualTo(new Point(1, 1));
        assertThat(combo.getCaretPosition()).isEqualTo(1);
    }

    @Test
    void aSelectionBackwardsFromTheEndHasTheCaretAtItsStart() {
        Combo combo = combo(SWT.DROP_DOWN);

        ComboHelper.clientCaretMoved((DartCombo) combo.getImpl(), 2, 7);

        assertThat(combo.getSelection()).isEqualTo(new Point(2, 7));
        assertThat(combo.getCaretPosition()).isEqualTo(2);
    }

    @Test
    void aRangeSetFromCodeKeepsTheCaretAtItsStart() {
        Combo combo = combo(SWT.DROP_DOWN);
        ComboHelper.clientCaretMoved((DartCombo) combo.getImpl(), 7, 0);

        combo.setSelection(new Point(2, 7));

        assertThat(combo.getCaretPosition()).isEqualTo(2);
    }

    @Test
    void aReadOnlyComboIgnoresACaretReport() {
        Combo combo = combo(SWT.READ_ONLY);
        DartCombo impl = (DartCombo) combo.getImpl();

        ComboHelper.clientCaretMoved(impl, 3, 3);

        assertThat(combo.getSelection()).isEqualTo(new Point(0, 7));
        assertThat(impl._caretPosition()).isZero();
    }
}
