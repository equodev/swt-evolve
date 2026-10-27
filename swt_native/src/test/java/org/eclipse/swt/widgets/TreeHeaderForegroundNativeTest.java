package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/** Tree.setHeaderForeground, which SWT has had since 3.106. */
@Tag("native-unit")
class TreeHeaderForegroundNativeTest extends SerializeTestBase {

    @Test
    @DisplayName("the header foreground is named for the client when it changes")
    void headerForegroundIsNamed() {
        Tree tree = new Tree(shell(), SWT.NONE);
        new TreeColumn(tree, SWT.NONE);
        DartTree impl = (DartTree) tree.getImpl();
        impl.getValue().clearDirty();

        tree.setHeaderForeground(new Color(tree.getDisplay(), 1, 2, 3));
        assertThat(impl.getValue().changedKeys()).contains(VTree.HEADER_FOREGROUND);
    }
}
