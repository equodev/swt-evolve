package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DartSideBar;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** The rails are worked out again only when a folder or side bar changes in a way their record reads. */
@ExtendWith(Mocks.class)
class ViewRailsShapeTest {

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

    private CTabFolder folderWithTabs(Shell shell) {
        CTabFolder folder = new CTabFolder(shell, SWT.NONE);
        folder.setBounds(0, 0, 300, 200);
        new CTabItem(folder, SWT.NONE).setText("Outline");
        new CTabItem(folder, SWT.NONE).setText("Problems");
        folder.setSelection(0);
        return folder;
    }

    @Test
    void choosingAnotherViewOrRenamingOneLeavesTheShapeAlone() {
        CTabFolder folder = folderWithTabs(Mocks.shell());
        List<Object> before = ViewRails.shape(folder);

        folder.setSelection(1);
        folder.getItem(0).setText("Outline (2)");

        assertThat(ViewRails.shape(folder)).isEqualTo(before);
    }

    @Test
    void hidingResizingOrEmptyingAFolderChangesItsShape() {
        CTabFolder folder = folderWithTabs(Mocks.shell());
        List<Object> listed = ViewRails.shape(folder);

        folder.setVisible(false);
        assertThat(ViewRails.shape(folder)).as("minimized").isNotEqualTo(listed);
        folder.setVisible(true);

        folder.setBounds(0, 0, 400, 200);
        assertThat(ViewRails.shape(folder)).as("its part moved").isNotEqualTo(listed);
        List<Object> moved = ViewRails.shape(folder);

        for (CTabItem item : folder.getItems()) item.dispose();
        assertThat(ViewRails.shape(folder)).as("no longer listed").isNotEqualTo(moved);
    }

    @Test
    void aSideBarChangesShapeWhenATrimBarComesOrGoes() {
        Composite bar = org.mockito.Mockito.mock(Composite.class);
        org.mockito.Mockito.doReturn(org.mockito.Mockito.mock(DartSideBar.class)).when(bar).getImpl();
        org.mockito.Mockito.doReturn(true).when(bar).getVisible();
        org.mockito.Mockito.doReturn(new Control[0]).when(bar).getChildren();
        List<Object> empty = ViewRails.shape(bar);

        org.mockito.Mockito.doReturn(new Control[] {org.mockito.Mockito.mock(Control.class)}).when(bar).getChildren();

        assertThat(ViewRails.shape(bar)).isNotEqualTo(empty);
    }
}
