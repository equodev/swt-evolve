package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.eclipse.swt.widgets.Mocks.swtShell;

/**
 * Regression tests for the bf395f11 / b4c8e97c dispose-NPE incident.
 *
 * Root cause: DartComposite.releaseChildren disposed each child via release(false),
 * which sets DISPOSED + nulls display but never calls releaseParent → removeControl.
 * The disposed children stayed in parent.children as zombies. The receiver's own
 * releaseParent → invalidateVisibleRegion → invalidateChildrenVisibleRegion walk
 * then NPEd dereferencing display on a zombie.
 *
 * Fix: releaseChildren snapshots children, nulls the field, then
 * iterates the snapshot. After releaseChildren returns, parent.children is null —
 * the invalidate walk sees no zombies and the NPE path is structurally impossible.
 */
class CompositeChildrenDuringDisposeTest extends SerializeTestBase {

    private static Control[] childrenOf(Composite c) throws Exception {
        Field f = DartComposite.class.getDeclaredField("children");
        f.setAccessible(true);
        return (Control[]) f.get(c.getImpl());
    }

    @Test
    void releaseChildren_clearsChildrenField() throws Exception {
        Composite parent = new Composite(swtShell(), SWT.NONE);
        new Button(parent, SWT.PUSH);
        new Button(parent, SWT.PUSH);
        new Button(parent, SWT.PUSH);

        assertThat(childrenOf(parent)).as("precondition: children populated").hasSize(3);

        ((DartComposite) parent.getImpl()).releaseChildren(true);

        assertThat(childrenOf(parent))
                .as("after releaseChildren, children must be null or empty so the dispose walk sees no zombies")
                .satisfiesAnyOf(
                        children -> assertThat(children).isNull(),
                        children -> assertThat(children).isEmpty()
                );
    }

    @Test
    void disposeNestedComposites_doesNotNPE() {
        Composite outer = new Composite(swtShell(), SWT.NONE);
        Composite inner1 = new Composite(outer, SWT.NONE);
        new Button(inner1, SWT.PUSH);
        Composite inner2 = new Composite(outer, SWT.NONE);
        new Button(inner2, SWT.PUSH);

        assertThatNoException()
                .as("disposing a nested composite tree must not NPE on a zombie sibling/child")
                .isThrownBy(outer::dispose);
    }

    /**
     * Regression test for #706: disposing a single child (while its parent stays
     * alive) must remove it from the parent's children array via
     * Composite.removeControl. If it doesn't, the disposed child lingers as a
     * zombie and later _getChildren() walks (reskinChildren,
     * CTabFolder.paintChildrenRecursively) hit it and throw "Widget is disposed".
     *
     * The macOS manifestation was a generator bug: the removeControl children-array
     * removal insert was version-gated AtLeast("3.130.0"), so SWT < 3.130 (e.g. the
     * downgraded 3.122 web build) generated a gutted removeControl that only called
     * fixTabList — never pruning the array.
     */
    @Test
    void disposeSingleChild_removedFromParentChildren() throws Exception {
        Composite parent = new Composite(swtShell(), SWT.NONE);
        Button keep = new Button(parent, SWT.PUSH);
        Button remove = new Button(parent, SWT.PUSH);

        assertThat(childrenOf(parent)).as("precondition: both children present").hasSize(2);

        remove.dispose();

        assertThat(childrenOf(parent))
                .as("disposed child must be pruned from parent.children, leaving only the live one")
                .containsExactly(keep);
    }
}
