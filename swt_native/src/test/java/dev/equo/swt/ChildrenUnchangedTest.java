package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * A composite's children are sent only when they changed since the last send: reorders that undo
 * each other between two sends (a CTabFolder brings one toolbar to the top and then the other, on
 * every layout) leave nothing to send, and a list named as changed makes the client rebuild the
 * composite and carries every changed child in full.
 */
@ExtendWith(Mocks.class)
@ExtendWith(MockFlutterBridge.Extension.class)
public class ChildrenUnchangedTest {

    private Composite composite;
    private Control a, b, c;

    @BeforeEach
    void setup() {
        Config.defaultToEquo();
        Config.useEquo(Composite.class);
        composite = new Composite(shell(), SWT.NONE);
        a = new Label(composite, SWT.NONE);
        b = new Button(composite, SWT.NONE);
        c = new Label(composite, SWT.NONE);
        value().clearDirty(); // as a send leaves it
    }

    @AfterEach
    void reset() {
        System.clearProperty("dev.equo.swt.Composite");
    }

    private VComposite value() {
        return ((DartComposite) composite.getImpl()).getValue();
    }

    @Test
    public void reordersThatUndoEachOtherSendNoChildren() {
        value().markDirty(VControl.BOUNDS);

        b.moveAbove(null);
        a.moveAbove(null);

        assertThat(composite.getChildren()).containsExactly(a, b, c);
        assertThat(value().changedKeys().contains(VComposite.CHILDREN)).as("children named").isFalse();
        assertThat(value().changedKeys().contains(VControl.BOUNDS)).as("bounds still named").isTrue();
    }

    @Test
    public void aReorderThatStaysIsSent() {
        value().markDirty(VControl.BOUNDS);

        b.moveAbove(null);
        c.moveAbove(null);

        assertThat(composite.getChildren()).containsExactly(c, b, a);
        assertThat(value().changedKeys().contains(VComposite.CHILDREN)).isTrue();
    }

    // As in a CTabFolder's layout: the reorders come before anything else it changes.
    @Test
    public void undoneReordersBeforeOtherChangesSendNoChildren() {
        b.moveAbove(null);
        a.moveAbove(null);
        value().markDirty(VControl.BOUNDS);

        assertThat(value().changedKeys().contains(VComposite.CHILDREN)).isFalse();
        assertThat(value().changedKeys().contains(VControl.BOUNDS)).isTrue();
    }

    @Test
    public void undoneReordersWithNothingElseSendNothing() {
        b.moveAbove(null);
        a.moveAbove(null);

        assertThat(value().anyDirty()).as("nothing named, so not described whole either").isFalse();
    }

    @Test
    public void aChildAddedAndDisposedAgainSendsNoChildren() {
        value().markDirty(VControl.BOUNDS);

        Label extra = new Label(composite, SWT.NONE);
        extra.dispose();

        assertThat(composite.getChildren()).containsExactly(a, b, c);
        assertThat(value().changedKeys().contains(VComposite.CHILDREN)).isFalse();
    }

    @Test
    public void afterASendTheNextChangeIsComparedWithWhatWasSent() {
        value().markDirty(VControl.BOUNDS);
        b.moveAbove(null);
        value().clearDirty(); // sent: b, a, c

        value().markDirty(VControl.BOUNDS);
        a.moveAbove(null);
        assertThat(value().changedKeys().contains(VComposite.CHILDREN)).as("a, b, c differs from what was sent").isTrue();
        b.moveAbove(null);
        assertThat(value().changedKeys().contains(VComposite.CHILDREN)).as("back to b, a, c").isFalse();
    }
}
