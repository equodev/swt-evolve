package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ControlHelper;
import org.eclipse.swt.widgets.DartControl;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Tree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.swtShell;

/**
 * There is no OS focus machinery behind this backend, so an embedding workbench never sees the
 * activation it uses to decide which of its parts is the active one — it binds that listener to a
 * part's client Composite, not to the control the user clicked. A control taking focus therefore
 * has to announce it up its parent chain, or a view keeps rendering and reporting selection while
 * the workbench still considers another part active and every action bound to it does nothing.
 */
@ExtendWith(Mocks.class)
public class ControlActivationTest extends SerializeTestBase {

    /**
     * The walk runs from a focus callback, so it can land on a control that was disposed in the
     * meantime; it must stop rather than throw. Activation reaching the workbench is covered on the
     * real app — this harness has no event dispatch to assert it at this level.
     */
    @Test
    void activationStopsAtTheShellAndSurvivesADisposedControl() {
        Composite parent = new Composite(swtShell(), SWT.NONE);
        Tree tree = new Tree(parent, SWT.NONE);
        DartControl impl = (DartControl) tree.getImpl();
        tree.dispose();

        ControlHelper.sendActivateToAncestors(impl);

        assertThat(tree.isDisposed()).isTrue();
    }
}
