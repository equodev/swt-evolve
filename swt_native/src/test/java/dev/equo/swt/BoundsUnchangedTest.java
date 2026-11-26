package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * A control's bounds are sent only when they changed since the last send: a layout that moves a
 * control away and back between two sends (a workbench's trim, a CTabFolder's toolbars, on every
 * resize) leaves nothing to send, and the client rebuilds a control named as moved. Something else
 * that named the bounds keeps them named.
 */
@ExtendWith(Mocks.class)
@ExtendWith(MockFlutterBridge.Extension.class)
public class BoundsUnchangedTest {

    private Composite control;

    @BeforeEach
    void setup() {
        Config.defaultToEquo();
        Config.useEquo(Composite.class);
        control = new Composite(shell(), SWT.NONE);
        control.setBounds(10, 10, 100, 40);
        value().clearDirty(); // as a send leaves it
    }

    @AfterEach
    void reset() {
        System.clearProperty("dev.equo.swt.Composite");
    }

    private VComposite value() {
        return ((DartComposite) control.getImpl()).getValue();
    }

    private boolean boundsNamed() {
        return value().changedKeys().contains(VControl.BOUNDS);
    }

    @Test
    public void aMoveAwayAndBackSendsNoBounds() {
        control.setBounds(0, 0, 100, 40);
        control.setBounds(10, 10, 100, 40);

        assertThat(boundsNamed()).isFalse();
    }

    @Test
    public void aResizeAwayAndBackThroughOtherSettersSendsNoBounds() {
        control.setSize(80, 40);
        control.setLocation(12, 10);
        control.setBounds(new Rectangle(10, 10, 100, 40));

        assertThat(boundsNamed()).isFalse();
    }

    @Test
    public void aMoveThatStaysIsSent() {
        control.setBounds(0, 0, 100, 40);
        control.setBounds(20, 10, 100, 40);

        assertThat(boundsNamed()).isTrue();
        assertThat(value().getBounds()).isEqualTo(new Rectangle(20, 10, 100, 40));
    }

    @Test
    public void boundsNamedByOtherMeansStayNamed() {
        value().markDirty(VControl.BOUNDS);

        control.setBounds(0, 0, 100, 40);
        control.setBounds(10, 10, 100, 40);

        assertThat(boundsNamed()).isTrue();
    }

    // What the client holds is what the last send carried: a move remembered before it says nothing
    // about a mark made after it.
    @Test
    public void aMarkAfterASendIsNotTakenBackByAnEarlierMove() {
        control.setBounds(0, 0, 100, 40);
        value().clearDirty(); // sent at (0, 0)
        value().markDirty(VControl.BOUNDS);

        control.setBounds(10, 10, 100, 40);

        assertThat(boundsNamed()).isTrue();
    }

    @Test
    public void aMoveBackAfterASendIsSent() {
        control.setBounds(0, 0, 100, 40);
        value().clearDirty(); // sent at (0, 0)

        control.setBounds(10, 10, 100, 40);

        assertThat(boundsNamed()).isTrue();
    }
}
