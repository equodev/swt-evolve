package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.swtShell;

/**
 * Re-applying a value a widget already has must not mark it dirty: every dirty() re-serializes the
 * widget's whole subtree to Flutter and rebuilds it there. The E4 CSS engine and JFace managers
 * re-apply unchanged backgrounds/fonts/texts on every relayout pass, so an ungated setter turns a
 * tree-item double-click into a re-render storm of the surrounding widgets.
 */
@ExtendWith(Mocks.class)
public class IdempotentSetterDirtyTest extends SerializeTestBase {

    @Test
    void idempotentCTabFolderBackgroundDoesNotDirty() {
        CTabFolder folder = new CTabFolder(swtShell(), SWT.BORDER);
        folder.setBackground(new Color(10, 20, 30));

        FlutterBridge.clearDirty();
        folder.setBackground(new Color(10, 20, 30));

        assertThat(FlutterBridge.isDirty(folder.getImpl())).isFalse();
    }

    @Test
    void changedCTabFolderBackgroundStillDirties() {
        CTabFolder folder = new CTabFolder(swtShell(), SWT.BORDER);
        folder.setBackground(new Color(10, 20, 30));

        FlutterBridge.clearDirty();
        folder.setBackground(new Color(40, 50, 60));

        assertThat(FlutterBridge.isDirty(folder.getImpl())).isTrue();
    }

    @Test
    void idempotentToolItemTextDoesNotDirty() {
        ToolBar bar = new ToolBar(swtShell(), SWT.FLAT);
        ToolItem item = new ToolItem(bar, SWT.PUSH);
        item.setText("Run");

        FlutterBridge.clearDirty();
        item.setText("Run");

        assertThat(FlutterBridge.isDirty(item.getImpl())).isFalse();
    }

    @Test
    void changedToolItemTextStillDirties() {
        ToolBar bar = new ToolBar(swtShell(), SWT.FLAT);
        ToolItem item = new ToolItem(bar, SWT.PUSH);
        item.setText("Run");

        FlutterBridge.clearDirty();
        item.setText("Stop");

        assertThat(FlutterBridge.isDirty(item.getImpl())).isTrue();
    }

    @Test
    void idempotentToolItemToolTipDoesNotDirty() {
        ToolBar bar = new ToolBar(swtShell(), SWT.FLAT);
        ToolItem item = new ToolItem(bar, SWT.PUSH);
        item.setToolTipText("Runs the suite");

        FlutterBridge.clearDirty();
        item.setToolTipText("Runs the suite");

        assertThat(FlutterBridge.isDirty(item.getImpl())).isFalse();
    }

    @Test
    void moveAboveKeepingOrderDoesNotDirtyParent() {
        Composite parent = new Composite(swtShell(), SWT.NONE);
        Button a = new Button(parent, SWT.PUSH);
        Button b = new Button(parent, SWT.PUSH);

        FlutterBridge.clearDirty();
        a.moveAbove(b);

        assertThat(FlutterBridge.isDirty(parent.getImpl())).isFalse();
    }

    @Test
    void moveAboveChangingOrderStillDirtiesParent() {
        Composite parent = new Composite(swtShell(), SWT.NONE);
        Button a = new Button(parent, SWT.PUSH);
        Button b = new Button(parent, SWT.PUSH);

        FlutterBridge.clearDirty();
        b.moveAbove(a);

        assertThat(FlutterBridge.isDirty(parent.getImpl())).isTrue();
    }
}
