package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A check tree whose boxes are owned by the application, not by the user's click: a plain
 * {@code Tree} with {@code SWT.CHECK}, a Selection listener that reacts to {@code detail == SWT.CHECK},
 * and raw {@code TreeItem.setChecked/setGrayed} calls driven by a model.
 *
 * <p>Each item's check mirrors its element's {@code visible} flag. A child is shown grayed + checked
 * while its parent is hidden or grayed, a folder is always grayed + checked, and a click on a grayed
 * item is reverted. So one click changes other items, and some clicks must change nothing at all.
 *
 * <p>Every row carries a colour square, so a row that is rebuilt rather than updated shows as a
 * flicker of its image.
 *
 * <p>The pane on the right prints the state the Java side holds for every item.
 *
 * <p>Run: {@code ./gradlew :examples:runDeskExample -PmainClass=dev.equo.TreeCheckAppDrivenSnippet}
 * <br>Swap Config.forceEquo() for Config.forceEclipse() (runEmbedExample) for the native baseline.
 */
public class TreeCheckAppDrivenSnippet {

    private static final class Element {
        final String label;
        final Element parent;
        final boolean folder;
        final boolean changeable;
        final List<Element> children = new ArrayList<>();
        boolean visible = true;

        Element(Element parent, String label, boolean folder, boolean changeable) {
            this.parent = parent;
            this.label = label;
            this.folder = folder;
            this.changeable = changeable;
            if (parent != null) parent.children.add(this);
        }

        boolean isRootOrFolder() {
            return parent == null || folder;
        }

        /** The ancestor sitting directly below a folder or the root. */
        Element typeAncestor() {
            Element e = this;
            while (e.parent != null && !e.parent.isRootOrFolder()) e = e.parent;
            return e;
        }
    }

    private static Text state;
    private static Label message;
    private static Tree tree;
    private static int colorIndex;

    public static void main(String[] args) {
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("TreeCheckAppDrivenSnippet — check state set by the application");
        shell.setLayout(new GridLayout(2, true));

        tree = new Tree(shell, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK | SWT.MULTI);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        state = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL);
        state.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        message = new Label(shell, SWT.NONE);
        message.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        for (Element e : buildModel().children) createItem(null, e);
        tree.addListener(SWT.Selection, event -> {
            if (event.detail == SWT.CHECK) checkStateChanged((TreeItem) event.item);
        });
        tree.addListener(SWT.Expand, event -> display.asyncExec(() -> {
            if (!event.item.isDisposed()) updateItem((TreeItem) event.item);
        }));

        updateAll();
        shell.setSize(900, 560);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static Element buildModel() {
        Element root = new Element(null, "root", false, false);
        Element wells = new Element(root, "Wells (folder)", true, true);
        Element wellA = new Element(wells, "Well A", false, true);
        new Element(wellA, "Log 1", false, true);
        new Element(wellA, "Log 2", false, true);
        Element wellB = new Element(wells, "Well B", false, true);
        new Element(wellB, "Log 3", false, true);
        Element surfaces = new Element(root, "Surfaces", false, true);
        new Element(surfaces, "Horizon 1", false, true);
        new Element(surfaces, "Horizon 2 (not displayable)", false, false);
        return root;
    }

    private static void createItem(TreeItem parentItem, Element element) {
        TreeItem item = parentItem == null ? new TreeItem(tree, SWT.NONE) : new TreeItem(parentItem, SWT.NONE);
        item.setText(element.label);
        item.setData(element);
        item.setImage(colorSquare(tree.getDisplay(), SQUARE_COLORS[colorIndex++ % SQUARE_COLORS.length]));
        for (Element child : element.children) createItem(item, child);
        item.setExpanded(true);
    }

    private static final RGB[] SQUARE_COLORS = {
            new RGB(220, 30, 30), new RGB(60, 60, 60), new RGB(30, 120, 220), new RGB(40, 160, 70)};

    /** A plain colour square, like the legend swatch an inventory row shows next to its name. */
    private static Image colorSquare(Display display, RGB rgb) {
        ImageData data = new ImageData(12, 12, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
        int pixel = data.palette.getPixel(rgb);
        for (int y = 0; y < data.height; y++) {
            for (int x = 0; x < data.width; x++) data.setPixel(x, y, pixel);
        }
        return new Image(display, data);
    }

    private static void checkStateChanged(TreeItem item) {
        String error = validate(item);
        if (error != null) {
            message.setText(error);
        } else {
            Element element = (Element) item.getData();
            element.visible = item.getChecked();
            message.setText(element.label + " -> " + (element.visible ? "visible" : "hidden"));
            updateItem(item);
        }
        printState();
    }

    /** Reverts a click the model does not allow; returns why, or null when the click stands. */
    private static String validate(TreeItem item) {
        Element element = (Element) item.getData();
        boolean checked = item.getChecked();
        boolean grayed = item.getGrayed();
        Element parent = element.parent;
        Element type = element.typeAncestor();
        boolean parentVisible = parent.isRootOrFolder() || parent.visible;
        boolean typeVisible = type == element || type.visible;
        if (grayed || !parentVisible || !typeVisible) {
            item.setChecked(!checked);
            item.setGrayed(item.getGrayed());
            if (grayed && (!element.changeable || element.folder)) {
                return "Reverted: \"" + element.label + "\" is not displayable";
            }
            return "Reverted: \"" + (parentVisible ? type.label : parent.label) + "\" is hidden";
        }
        return null;
    }

    private static void updateAll() {
        for (TreeItem item : tree.getItems()) updateItem(item);
        printState();
    }

    private static void updateItem(TreeItem item) {
        Element element = (Element) item.getData();
        Element parent = element.parent;
        TreeItem parentItem = item.getParentItem();
        boolean rootOrFolderParent = parent.isRootOrFolder();
        boolean parentGrayed = !rootOrFolderParent && parentItem.getGrayed();
        boolean parentVisible = rootOrFolderParent || parent.visible;
        boolean typeVisible = element.typeAncestor().visible;

        if (!element.changeable) {
            item.setGrayed(true);
        } else if (element.visible && parent.changeable) {
            item.setGrayed(parentGrayed || !typeVisible || !parentVisible);
        } else {
            item.setGrayed(false);
        }

        for (TreeItem child : item.getItems()) updateItem(child);

        if (item.getGrayed()) {
            item.setChecked(true);
        } else {
            item.setChecked(element.visible);
            if (element.folder) {
                item.setGrayed(true);
                item.setChecked(true);
            }
        }
    }

    private static void printState() {
        StringBuilder sb = new StringBuilder("Java state (expected appearance)\n\n");
        for (TreeItem item : tree.getItems()) describe(item, 0, sb);
        state.setText(sb.toString());
    }

    private static void describe(TreeItem item, int depth, StringBuilder sb) {
        String expected = !item.getChecked() ? "empty box" : item.getGrayed() ? "partial mark" : "checkmark";
        sb.append("  ".repeat(depth)).append(item.getText()).append(": ").append(expected).append('\n');
        for (TreeItem child : item.getItems()) describe(child, depth + 1, sb);
    }
}
