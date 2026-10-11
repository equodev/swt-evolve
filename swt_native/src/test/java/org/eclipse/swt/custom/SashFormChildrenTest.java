package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Sash;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The sashes of a SashForm are its children on every SWT, so getChildren() lists them. The children sent
 * to Flutter leave them out: the Flutter side draws the dividers itself.
 */
@ExtendWith(Mocks.class)
class SashFormChildrenTest {

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
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private SashForm laidOutForm() {
        SashForm form = new SashForm(Mocks.shell(), SWT.HORIZONTAL);
        form.setBounds(0, 0, 300, 200);
        new Composite(form, SWT.NONE);
        new Composite(form, SWT.NONE);
        form.layout();
        return form;
    }

    @Test
    @DisplayName("getChildren lists the panes and the sash between them")
    void getChildrenListsTheSashes() {
        SashForm form = laidOutForm();

        Control[] children = form.getChildren();

        assertThat(children).hasSize(3);
        assertThat(children).filteredOn(c -> c instanceof Sash).hasSize(1);
        assertThat(children).filteredOn(c -> !(c instanceof Sash)).hasSize(2);
    }

    @Test
    @DisplayName("the array getChildren returns is a copy")
    void getChildrenReturnsACopy() {
        SashForm form = laidOutForm();

        form.getChildren()[0] = null;

        assertThat(form.getChildren()).doesNotContainNull();
    }

    @Test
    @DisplayName("the children sent to Flutter are the panes only")
    void theValueLeavesTheSashesOut() {
        SashForm form = laidOutForm();

        Control[] sent = ((DartSashForm) form.getImpl()).getValue().getChildren();

        assertThat(sent).hasSize(2);
        assertThat(sent).noneMatch(c -> c instanceof Sash);
    }
}
