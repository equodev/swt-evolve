package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Demonstrates CTabFolder with an integrated toolbar in the top-right corner.
 * This example shows how to use setTopRight() method to embed a custom toolbar with different types of tool items including icons and tooltips.
 */
public class CTabFolderWithTopRightToolBarSnippet {

    public static void main(String[] args) throws IOException {
        Config.forceEquo();
        System.setProperty("swt.evolve.ctabfolder_visible_controls", "true");
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("CTabFolder with Toolbar TopRight Snippet");
        shell.setSize(400, 100);
        shell.setLayout(new FillLayout());

        CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER);

        Composite toolbarComposite = new Composite(tabFolder, SWT.NONE);
        FillLayout toolbarLayout = new FillLayout();
        toolbarLayout.marginHeight = 2;
        toolbarLayout.marginWidth = 2;
        toolbarComposite.setLayout(toolbarLayout);

        ToolBar toolBar = new ToolBar(toolbarComposite, SWT.FLAT);

        ToolItem itemA = new ToolItem(toolBar, SWT.PUSH);
        itemA.setText("A");
        itemA.setToolTipText("Item A");

        ToolItem check = new ToolItem(toolBar, SWT.CHECK);
        check.setText("Check");
        check.setToolTipText("Check Item");
        check.setImage(new Image(display, getImagePath("synced.png")));

        ToolItem backward_forward = new ToolItem(toolBar, SWT.PUSH);
        backward_forward.setToolTipText("backwards forward");
        backward_forward.setImage(new Image(display, getImagePath("backward_nav.png")));
        backward_forward.setHotImage(new Image(display, getImagePath("forward_nav.png")));

        ToolItem view_menu = new ToolItem(toolBar, SWT.PUSH);
        view_menu.setToolTipText("view_menu");
        view_menu.setImage(new Image(display, getImagePath("view_menu.png")));

        tabFolder.setTopRight(toolbarComposite);

        CTabItem tab1 = new CTabItem(tabFolder, SWT.NONE);
        tab1.setText("Tab 1");

        CTabItem tab2 = new CTabItem(tabFolder, SWT.NONE);
        tab2.setText("Tab 2");

        tabFolder.setSelection(0);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private static String getImagePath(String imageName) throws IOException {
        URL resourceUrl = CTabFolderWithTopRightToolBarSnippet.class.getResource("/" + imageName);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + imageName);
        }

        if ("file".equals(resourceUrl.getProtocol())) {
            return Paths.get(resourceUrl.getPath()).toString();
        } else {
            InputStream is = CTabFolderWithTopRightToolBarSnippet.class.getResourceAsStream("/" + imageName);
            Path tmpDir = Files.createTempDirectory("swt-evolve-images-");
            Path tmpFile = tmpDir.resolve(imageName);
            assert is != null;
            Files.copy(is, tmpFile, StandardCopyOption.REPLACE_EXISTING);
            tmpFile.toFile().deleteOnExit();
            return tmpFile.toAbsolutePath().toString();
        }
    }


}

