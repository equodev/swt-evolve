package dev.equo.swing;

import dev.equo.swt.Config;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Every widget twice, side by side: on the left as SWT Evolve draws it, on the right its Swing
 * equivalent as a Swing island draws it — one tab per widget, one row per variant, the two halves
 * of a row level with each other. Next to each other the two must be indistinguishable: the same
 * outline or none, the same corner radius and outline width, the same colours, the same font.
 *
 * <pre>
 * ./gradlew :swing-island:runIslandWebExample -PmainClass=dev.equo.swing.SwingIslandParitySnippet \
 *     -Dswt.evolve.theme_name=blackandred -Dswt.evolve.force_theme=light
 * </pre>
 *
 * <p>{@code -Dparity.tab=<n>} opens a tab other than the first. Without the island launch the
 * right-hand column is stock Swing in SWT_AWT frames, which is the comparison this exists to end.
 *
 * <p>The menus are compared two ways: the shell's menu bar against a {@code JMenuBar} on the Menus
 * tab, and the same items as a context menu on each side — right-click the target. Their rows are
 * tall because an island's popup is drawn inside the island: a shorter one cuts the menu off. The Icons tab
 * draws one icon, pixel for pixel the same on both sides, on the widgets that carry one. The Color
 * tab is a colour preference as Eclipse shows one: a swatch, and a button that opens each side's
 * chooser — ColorDialog and JColorChooser — seeded with it.
 */
public class SwingIslandParitySnippet {

    /** One row per variant, on both sides, so the two halves of a row sit at the same height. */
    private static final int ROW = 44;

    /** One variant: its caption, how SWT builds it, how Swing builds the equivalent. */
    private record Variant(String caption, Consumer<Composite> swt, Supplier<JComponent> swing, int rows) {
        Variant(String caption, Consumer<Composite> swt, Supplier<JComponent> swing) {
            this(caption, swt, swing, 1);
        }
    }

    public static void main(String[] args) {
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SwingIslandParitySnippet");
        shell.setLayout(new FillLayout());

        TabFolder folder = new TabFolder(shell, SWT.NONE);
        tab(folder, "Button", new Variant[] {
                new Variant("push", p -> push(p, "Button", true), () -> new JButton("Button")),
                new Variant("disabled", p -> push(p, "Disabled", false), () -> disabled(new JButton("Disabled"))),
                new Variant("default", p -> p.getShell().setDefaultButton(push(p, "Default", true)),
                        () -> {
                            JButton b = new JButton("Default");
                            b.putClientProperty(DEFAULT, Boolean.TRUE);
                            return b;
                        }),
        });
        tab(folder, "Toggle", new Variant[] {
                new Variant("off", p -> toggle(p, "Toggle", false, true), () -> new JToggleButton("Toggle")),
                new Variant("on", p -> toggle(p, "Selected", true, true), () -> new JToggleButton("Selected", true)),
                new Variant("disabled", p -> toggle(p, "Disabled", false, false),
                        () -> disabled(new JToggleButton("Disabled"))),
        });
        tab(folder, "Check", new Variant[] {
                new Variant("off", p -> check(p, SWT.CHECK, "Check", false, true), () -> new JCheckBox("Check")),
                new Variant("on", p -> check(p, SWT.CHECK, "Checked", true, true), () -> new JCheckBox("Checked", true)),
                new Variant("disabled", p -> check(p, SWT.CHECK, "Disabled", false, false),
                        () -> disabled(new JCheckBox("Disabled"))),
                new Variant("disabled on", p -> check(p, SWT.CHECK, "Disabled checked", true, false),
                        () -> disabled(new JCheckBox("Disabled checked", true))),
        });
        tab(folder, "Radio", new Variant[] {
                new Variant("off", p -> check(p, SWT.RADIO, "Radio", false, true), () -> new JRadioButton("Radio")),
                new Variant("on", p -> check(p, SWT.RADIO, "Selected", true, true), () -> new JRadioButton("Selected", true)),
                new Variant("disabled", p -> check(p, SWT.RADIO, "Disabled", false, false),
                        () -> disabled(new JRadioButton("Disabled"))),
                new Variant("disabled on", p -> check(p, SWT.RADIO, "Disabled selected", true, false),
                        () -> disabled(new JRadioButton("Disabled selected", true))),
        });
        tab(folder, "Text", new Variant[] {
                new Variant("single", p -> text(p, SWT.BORDER, "Text", true, true), () -> new JTextField("Text", 16)),
                new Variant("disabled", p -> text(p, SWT.BORDER, "Disabled", false, true),
                        () -> disabled(new JTextField("Disabled", 16))),
                new Variant("read only", p -> text(p, SWT.BORDER | SWT.READ_ONLY, "Read only", true, false), () -> {
                    JTextField f = new JTextField("Read only", 16);
                    f.setEditable(false);
                    return f;
                }),
                new Variant("password", p -> text(p, SWT.BORDER | SWT.PASSWORD, "secret", true, true),
                        () -> new JPasswordField("secret", 16)),
                new Variant("multi", p -> {
                    Text t = new Text(p, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
                    t.setText("Line one\nLine two\nLine three");
                    t.setLayoutData(new GridData(220, ROW * 2 - 8));
                }, () -> {
                    JScrollPane s = new JScrollPane(new JTextArea("Line one\nLine two\nLine three"));
                    s.setPreferredSize(new Dimension(220, ROW * 2 - 8));
                    return s;
                }, 2),
        });
        tab(folder, "Combo", new Variant[] {
                new Variant("editable", p -> combo(p, SWT.DROP_DOWN, true), () -> {
                    JComboBox<String> c = new JComboBox<>(ITEMS);
                    c.setEditable(true);
                    return c;
                }),
                new Variant("read only", p -> combo(p, SWT.DROP_DOWN | SWT.READ_ONLY, true), () -> new JComboBox<>(ITEMS)),
                new Variant("disabled", p -> combo(p, SWT.DROP_DOWN | SWT.READ_ONLY, false),
                        () -> disabled(new JComboBox<>(ITEMS))),
        });
        tab(folder, "Spinner", new Variant[] {
                new Variant("spinner", p -> spinner(p, true), () -> new JSpinner(new SpinnerNumberModel(5, 0, 10, 1))),
                new Variant("disabled", p -> spinner(p, false),
                        () -> disabled(new JSpinner(new SpinnerNumberModel(5, 0, 10, 1)))),
        });
        tab(folder, "Range", new Variant[] {
                new Variant("scale", p -> scale(p, true), () -> slider(true)),
                new Variant("scale disabled", p -> scale(p, false), () -> slider(false)),
                new Variant("progress", p -> progress(p, true), () -> {
                    JProgressBar b = new JProgressBar(0, 100);
                    b.setValue(40);
                    // SWT's ProgressBar is 20 high by default (ProgressBarThemeExtension.defaultHeight).
                    return sized(b, 220, 20);
                }),
                new Variant("progress disabled", p -> progress(p, false), () -> {
                    JProgressBar b = new JProgressBar(0, 100);
                    b.setValue(40);
                    return disabled(sized(b, 220, 20));
                }),
        });
        // Five tables do not fit a window, so this one tab scrolls.
        tab(folder, "Table", new Variant[] {
                new Variant("lines", p -> table(p, true), () -> table(true), 4),
                new Variant("no lines", p -> table(p, false), () -> table(false), 4),
                new Variant("header icons", SwingIslandParitySnippet::iconHeaderTable,
                        SwingIslandParitySnippet::iconHeaderTable, 4),
                new Variant("buttons", SwingIslandParitySnippet::buttonTable,
                        SwingIslandParitySnippet::buttonTable, 4),
                new Variant("checks", SwingIslandParitySnippet::choiceTable,
                        SwingIslandParitySnippet::choiceTable, 4),
        }, true);
        tab(folder, "Tree", new Variant[] {
                new Variant("tree", SwingIslandParitySnippet::tree, SwingIslandParitySnippet::tree, 4),
        });
        tab(folder, "List", new Variant[] {
                new Variant("list", p -> {
                    List l = new List(p, SWT.BORDER | SWT.SINGLE);
                    for (String item : ITEMS) {
                        l.add(item);
                    }
                    l.setSelection(1);
                    l.setLayoutData(new GridData(220, ROW * 3 - 8));
                }, () -> {
                    JList<String> l = new JList<>(ITEMS);
                    l.setSelectedIndex(1);
                    return sized(new JScrollPane(l), 220, ROW * 3 - 8);
                }, 3),
        });
        tab(folder, "Tabs", new Variant[] {
                new Variant("tabs", p -> {
                    TabFolder f = new TabFolder(p, SWT.NONE);
                    for (String name : new String[] {"First", "Second", "Third"}) {
                        TabItem item = new TabItem(f, SWT.NONE);
                        item.setText(name);
                        Label page = new Label(f, SWT.NONE);
                        page.setText("Page " + name.toLowerCase());
                        item.setControl(page);
                    }
                    f.setLayoutData(new GridData(260, ROW * 3 - 8));
                }, () -> {
                    JTabbedPane t = new JTabbedPane();
                    for (String name : new String[] {"First", "Second", "Third"}) {
                        t.addTab(name, new JLabel("Page " + name.toLowerCase()));
                    }
                    return sized(t, 260, ROW * 3 - 8);
                }, 3),
        });
        tab(folder, "Labels", new Variant[] {
                new Variant("label", p -> label(p, "Label", true), () -> new JLabel("Label")),
                new Variant("disabled", p -> label(p, "Disabled label", false), () -> disabled(new JLabel("Disabled label"))),
                new Variant("separator", p -> {
                    Label s = new Label(p, SWT.SEPARATOR | SWT.HORIZONTAL);
                    s.setLayoutData(new GridData(220, SWT.DEFAULT));
                }, () -> sized(new JSeparator(), 220, 2)),
                new Variant("group", p -> {
                    Group g = new Group(p, SWT.NONE);
                    g.setText("Group");
                    g.setLayout(new FillLayout());
                    new Label(g, SWT.NONE).setText("Inside the group");
                    g.setLayoutData(new GridData(220, ROW * 2 - 8));
                }, () -> {
                    JPanel g = new JPanel(new BorderLayout());
                    g.setBorder(BorderFactory.createTitledBorder("Group"));
                    g.add(new JLabel("Inside the group"));
                    return sized(g, 220, ROW * 2 - 8);
                }, 2),
        });

        tab(folder, "Menus", new Variant[] {
                new Variant("menu bar", p -> {
                    Label l = new Label(p, SWT.NONE);
                    l.setText("The shell's, above the tabs");
                }, () -> sized(swingMenuBar(), 300, 26), 6),
                new Variant("context", p -> {
                    Label target = new Label(p, SWT.NONE);
                    target.setText("Right-click here");
                    Menu popup = new Menu(target);
                    swtItems(popup, swtIcon(display));
                    target.setMenu(popup);
                }, () -> {
                    JLabel target = new JLabel("Right-click here");
                    JPopupMenu popup = new JPopupMenu();
                    swingItems(popup::add, popup::addSeparator);
                    target.setComponentPopupMenu(popup);
                    return target;
                }, 6),
        });
        Image icon = swtIcon(display);
        tab(folder, "Icons", new Variant[] {
                new Variant("push", p -> push(p, "Add", true).setImage(icon), () -> new JButton("Add", ICON)),
                new Variant("icon only", p -> push(p, "", true).setImage(icon), () -> new JButton(ICON)),
                new Variant("disabled", p -> push(p, "Add", false).setImage(icon),
                        () -> disabled(new JButton("Add", ICON))),
                new Variant("toggle", p -> {
                    Button b = new Button(p, SWT.TOGGLE);
                    b.setText("Pin");
                    b.setImage(icon);
                }, () -> new JToggleButton("Pin", ICON)),
                new Variant("label", p -> {
                    CLabel l = new CLabel(p, SWT.NONE);
                    l.setText("Label");
                    l.setImage(icon);
                }, () -> new JLabel("Label", ICON, SwingConstants.LEADING)),
                new Variant("tabs", p -> {
                    TabFolder f = new TabFolder(p, SWT.NONE);
                    for (String name : new String[] {"First", "Second"}) {
                        TabItem item = new TabItem(f, SWT.NONE);
                        item.setText(name);
                        item.setImage(icon);
                        Label page = new Label(f, SWT.NONE);
                        page.setText("Page " + name.toLowerCase());
                        item.setControl(page);
                    }
                    f.setLayoutData(new GridData(260, ROW * 2 - 8));
                }, () -> {
                    JTabbedPane t = new JTabbedPane();
                    for (String name : new String[] {"First", "Second"}) {
                        t.addTab(name, ICON, new JLabel("Page " + name.toLowerCase()));
                    }
                    return sized(t, 260, ROW * 2 - 8);
                }, 2),
                new Variant("tree", p -> {
                    Tree t = new Tree(p, SWT.BORDER);
                    TreeItem root = new TreeItem(t, SWT.NONE);
                    root.setText("Root");
                    root.setImage(icon);
                    for (String name : new String[] {"Alpha", "Beta"}) {
                        TreeItem item = new TreeItem(root, SWT.NONE);
                        item.setText(name);
                        item.setImage(icon);
                    }
                    root.setExpanded(true);
                    t.setLayoutData(new GridData(220, ROW * 3 - 8));
                }, () -> {
                    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
                    for (String name : new String[] {"Alpha", "Beta"}) {
                        root.add(new DefaultMutableTreeNode(name));
                    }
                    JTree t = new JTree(root);
                    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
                    renderer.setOpenIcon(ICON);
                    renderer.setClosedIcon(ICON);
                    renderer.setLeafIcon(ICON);
                    t.setCellRenderer(renderer);
                    return sized(new JScrollPane(t), 220, ROW * 3 - 8);
                }, 3),
        });
        // What an Eclipse preference page shows for a colour (JFace's ColorSelector): the colour in
        // effect, and a button that opens the platform's chooser seeded with it. SWT's chooser is
        // the ColorDialog, Swing's is JColorChooser; both open as dialogs of their own.
        tab(folder, "Color", new Variant[] {
                new Variant("selector", p -> colorSelector(p, true), () -> colorSelector(true)),
                new Variant("disabled", p -> colorSelector(p, false), () -> colorSelector(false)),
        });

        // The menu bar the Menus tab compares a JMenuBar with: a shell has one, and it is the
        // shell's, so it sits above every tab.
        Menu bar = new Menu(shell, SWT.BAR);
        MenuItem file = new MenuItem(bar, SWT.CASCADE);
        file.setText("File");
        Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
        swtItems(fileMenu, icon);
        file.setMenu(fileMenu);
        MenuItem edit = new MenuItem(bar, SWT.CASCADE);
        edit.setText("Edit");
        Menu editMenu = new Menu(shell, SWT.DROP_DOWN);
        for (String name : new String[] {"Cut", "Copy", "Paste"}) {
            new MenuItem(editMenu, SWT.PUSH).setText(name);
        }
        edit.setMenu(editMenu);
        shell.setMenuBar(bar);

        int open = Math.max(0, Math.min(folder.getItemCount() - 1, Integer.getInteger("parity.tab", 0)));
        folder.setSelection(open);
        shell.setSize(820, 640);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    /** The colour both selectors start from. */
    private static final int SEED = 0x2E6FD8;

    /**
     * A swatch of the colour in effect and an "Edit..." button that opens ColorDialog on it. The
     * swatch is painted: a widget's own background is the theme's, not the application's, on both
     * sides, and the colour is this swatch's content.
     */
    private static void colorSelector(Composite p, boolean enabled) {
        Composite row = new Composite(p, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 8;
        row.setLayout(layout);
        RGB[] current = {new RGB(SEED >> 16 & 0xFF, SEED >> 8 & 0xFF, SEED & 0xFF)};
        Canvas swatch = new Canvas(row, SWT.NONE);
        swatch.setLayoutData(new GridData(48, 20));
        swatch.addPaintListener(e -> {
            org.eclipse.swt.graphics.Color fill = new org.eclipse.swt.graphics.Color(e.display, current[0]);
            org.eclipse.swt.graphics.Color line = new org.eclipse.swt.graphics.Color(e.display, 0x80, 0x80, 0x80);
            org.eclipse.swt.graphics.Rectangle area = swatch.getClientArea();
            e.gc.setBackground(fill);
            e.gc.fillRectangle(area);
            e.gc.setForeground(line);
            e.gc.drawRectangle(area.x, area.y, area.width - 1, area.height - 1);
            fill.dispose();
            line.dispose();
        });
        Button edit = new Button(row, SWT.PUSH);
        edit.setText("Edit...");
        edit.setEnabled(enabled);
        edit.addListener(SWT.Selection, e -> {
            ColorDialog dialog = new ColorDialog(p.getShell());
            dialog.setText("Color");
            dialog.setRGB(current[0]);
            RGB picked = dialog.open();
            if (picked == null) {
                return; // cancelled
            }
            current[0] = picked;
            swatch.redraw();
        });
    }

    /** The same: a swatch and an "Edit..." button that opens JColorChooser's dialog on it. */
    private static JComponent colorSelector(boolean enabled) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        Color[] current = {new Color(SEED)};
        JComponent swatch = new JComponent() {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                g.setColor(current[0]);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.GRAY);
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
        };
        swatch.setPreferredSize(new Dimension(48, 20));
        JButton edit = new JButton("Edit...");
        edit.setEnabled(enabled);
        edit.addActionListener(e -> {
            Color picked = JColorChooser.showDialog(edit, "Color", current[0]);
            if (picked != null) {
                current[0] = picked;
                swatch.repaint();
            }
        });
        row.add(swatch);
        row.add(edit);
        return row;
    }

    private static final String[] ITEMS = {"Alpha", "Beta", "Gamma", "Delta"};

    /** Marks the Swing button its tab's root pane makes the default one, as the shell does SWT's. */
    private static final String DEFAULT = "parity.default";

    /** One tab: the captions, the SWT column, the Swing column, each row the same height. */
    private static void tab(TabFolder folder, String name, Variant[] variants) {
        tab(folder, name, variants, false);
    }

    /** The same, in a page that scrolls when [scrolls]: for a tab taller than the window. */
    private static void tab(TabFolder folder, String name, Variant[] variants, boolean scrolls) {
        TabItem item = new TabItem(folder, SWT.NONE);
        item.setText(name);
        ScrolledComposite scroller = scrolls ? new ScrolledComposite(folder, SWT.V_SCROLL) : null;
        Composite page = new Composite(scrolls ? scroller : folder, SWT.NONE);
        GridLayout grid = new GridLayout(3, false);
        grid.horizontalSpacing = 24;
        page.setLayout(grid);
        if (scrolls) {
            scroller.setContent(page);
            scroller.setExpandHorizontal(true);
            scroller.setExpandVertical(true);
            item.setControl(scroller);
        } else {
            item.setControl(page);
        }

        header(page, "");
        header(page, "SWT");
        header(page, "Swing");

        Composite captions = column(page, 90);
        Composite swt = column(page, 300);
        Composite embedded = new Composite(page, SWT.EMBEDDED);
        int totalRows = 0;
        for (Variant v : variants) {
            totalRows += v.rows();
        }
        GridData embeddedData = new GridData(SWT.FILL, SWT.TOP, true, false);
        embeddedData.heightHint = totalRows * ROW;
        embeddedData.widthHint = 320;
        embedded.setLayoutData(embeddedData);

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        for (Variant v : variants) {
            Label caption = new Label(captions, SWT.NONE);
            caption.setText(v.caption());
            caption.setLayoutData(cell(v.rows()));

            Composite cell = new Composite(swt, SWT.NONE);
            GridLayout cellLayout = new GridLayout(1, false);
            cellLayout.marginWidth = 0;
            cellLayout.marginHeight = 4;
            cell.setLayout(cellLayout);
            cell.setLayoutData(cell(v.rows()));
            v.swt().accept(cell);

            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
            Dimension size = new Dimension(320, v.rows() * ROW);
            row.setPreferredSize(size);
            row.setMaximumSize(size);
            row.setMinimumSize(size);
            row.add(v.swing().get());
            rows.add(row);
        }
        // A root pane, because an SWT_AWT frame is a plain Frame: without one Swing has nowhere to
        // keep a default button, and the "default" row would compare a default SWT button with an
        // ordinary Swing one.
        JButton defaultButton = null;
        for (java.awt.Component row : rows.getComponents()) {
            for (java.awt.Component c : ((JPanel) row).getComponents()) {
                if (c instanceof JButton b && Boolean.TRUE.equals(b.getClientProperty(DEFAULT))) {
                    defaultButton = b;
                }
            }
        }
        Frame frame = SWT_AWT.new_Frame(embedded);
        if (defaultButton != null) {
            JRootPane root = new JRootPane();
            root.getContentPane().add(rows);
            root.setDefaultButton(defaultButton);
            frame.add(root);
        } else {
            frame.add(rows);
        }
        frame.validate();
        if (scrolls) {
            scroller.setMinSize(page.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        }
    }

    private static void header(Composite page, String text) {
        Label l = new Label(page, SWT.NONE);
        l.setText(text);
    }

    private static Composite column(Composite page, int width) {
        Composite c = new Composite(page, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        c.setLayout(layout);
        GridData data = new GridData(SWT.LEFT, SWT.TOP, false, false);
        data.widthHint = width;
        c.setLayoutData(data);
        return c;
    }

    private static GridData cell(int rows) {
        GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
        data.heightHint = rows * ROW;
        return data;
    }

    private static Button push(Composite p, String text, boolean enabled) {
        Button b = new Button(p, SWT.PUSH);
        b.setText(text);
        b.setEnabled(enabled);
        return b;
    }

    private static void toggle(Composite p, String text, boolean selected, boolean enabled) {
        Button b = new Button(p, SWT.TOGGLE);
        b.setText(text);
        b.setSelection(selected);
        b.setEnabled(enabled);
    }

    private static void check(Composite p, int style, String text, boolean selected, boolean enabled) {
        Button b = new Button(p, style);
        b.setText(text);
        b.setSelection(selected);
        b.setEnabled(enabled);
    }

    private static void text(Composite p, int style, String value, boolean enabled, boolean editable) {
        Text t = new Text(p, style);
        t.setText(value);
        t.setEnabled(enabled);
        t.setEditable(editable);
        t.setLayoutData(new GridData(220, SWT.DEFAULT));
    }

    private static void combo(Composite p, int style, boolean enabled) {
        Combo c = new Combo(p, style);
        c.setItems(ITEMS);
        c.select(0);
        c.setEnabled(enabled);
        c.setLayoutData(new GridData(160, SWT.DEFAULT));
    }

    private static void spinner(Composite p, boolean enabled) {
        Spinner s = new Spinner(p, SWT.BORDER);
        s.setValues(5, 0, 10, 0, 1, 1);
        s.setEnabled(enabled);
    }

    /** A Scale at its defaults: its page increment, 10, is where it draws a tick. */
    private static void scale(Composite p, boolean enabled) {
        Scale s = new Scale(p, SWT.HORIZONTAL);
        s.setMaximum(100);
        s.setSelection(40);
        s.setEnabled(enabled);
        s.setLayoutData(new GridData(220, SWT.DEFAULT));
    }

    /** The same slider, its ticks at the Scale's page increment so the two have the same marks. */
    private static JComponent slider(boolean enabled) {
        JSlider s = new JSlider(0, 100, 40);
        s.setMajorTickSpacing(10);
        s.setPaintTicks(true);
        s.setEnabled(enabled);
        return sized(s, 220, 30);
    }

    private static void progress(Composite p, boolean enabled) {
        ProgressBar b = new ProgressBar(p, SWT.SMOOTH);
        b.setMaximum(100);
        b.setSelection(40);
        b.setEnabled(enabled);
        b.setLayoutData(new GridData(220, SWT.DEFAULT));
    }

    private static void label(Composite p, String text, boolean enabled) {
        Label l = new Label(p, SWT.NONE);
        l.setText(text);
        l.setEnabled(enabled);
    }

    private static final String[] COLUMNS = {"Name", "Value", "State"};
    private static final String[][] ROWS = {{"Alpha", "1", "On"}, {"Beta", "2", "Off"}, {"Gamma", "3", "On"}};

    private static void table(Composite p, boolean lines) {
        Table t = new Table(p, SWT.BORDER | SWT.FULL_SELECTION);
        t.setHeaderVisible(true);
        t.setLinesVisible(lines);
        for (String name : COLUMNS) {
            TableColumn c = new TableColumn(t, SWT.NONE);
            c.setText(name);
            c.setWidth(80);
        }
        for (String[] row : ROWS) {
            new TableItem(t, SWT.NONE).setText(row);
        }
        t.setSelection(1);
        t.setLayoutData(new GridData(260, ROW * 4 - 8));
    }

    private static JComponent table(boolean lines) {
        JTable t = new JTable(ROWS, COLUMNS);
        t.setShowGrid(lines);
        t.setRowSelectionInterval(1, 1);
        return sized(new JScrollPane(t), 260, ROW * 4 - 8);
    }

    /** The table again, each column's title after an icon. */
    private static void iconHeaderTable(Composite p) {
        Image icon = swtIcon(p.getDisplay());
        Table t = new Table(p, SWT.BORDER | SWT.FULL_SELECTION);
        t.setHeaderVisible(true);
        for (String name : COLUMNS) {
            TableColumn c = new TableColumn(t, SWT.NONE);
            c.setText(name);
            c.setImage(icon);
            c.setWidth(80);
        }
        for (String[] row : ROWS) {
            new TableItem(t, SWT.NONE).setText(row);
        }
        t.setSelection(1);
        t.setLayoutData(new GridData(260, ROW * 4 - 8));
    }

    /** Swing's own header renderer, with the icon set on the label it hands back. */
    private static JComponent iconHeaderTable() {
        JTable t = new JTable(ROWS, COLUMNS);
        TableCellRenderer stock = t.getTableHeader().getDefaultRenderer();
        t.getTableHeader().setDefaultRenderer((table, value, selected, focused, row, column) -> {
            java.awt.Component c = stock.getTableCellRendererComponent(table, value, selected, focused, row, column);
            if (c instanceof JLabel l) {
                l.setIcon(ICON);
                l.setHorizontalTextPosition(SwingConstants.TRAILING);
            }
            return c;
        });
        t.setRowSelectionInterval(1, 1);
        return sized(new JScrollPane(t), 260, ROW * 4 - 8);
    }

    private static final String[] ACTION_COLUMNS = {"Name", "Value", "Action"};

    /** A push button with an icon in every row's last cell, held there by a TableEditor. */
    private static void buttonTable(Composite p) {
        Image icon = swtIcon(p.getDisplay());
        Table t = new Table(p, SWT.BORDER | SWT.FULL_SELECTION);
        t.setHeaderVisible(true);
        for (String name : ACTION_COLUMNS) {
            TableColumn c = new TableColumn(t, SWT.NONE);
            c.setText(name);
            c.setWidth(80);
        }
        for (String[] row : ROWS) {
            TableItem item = new TableItem(t, SWT.NONE);
            item.setText(new String[] {row[0], row[1]});
            Button b = new Button(t, SWT.PUSH);
            b.setText("Open");
            b.setImage(icon);
            TableEditor editor = new TableEditor(t);
            editor.grabHorizontal = true;
            editor.grabVertical = true;
            editor.setEditor(b, item, 2);
        }
        t.setLayoutData(new GridData(260, ROW * 4 - 8));
    }

    /** The same: a JButton renders the last column and, so a click reaches it, edits it too. */
    private static JComponent buttonTable() {
        Object[][] rows = new Object[ROWS.length][];
        for (int i = 0; i < ROWS.length; i++) {
            rows[i] = new Object[] {ROWS[i][0], ROWS[i][1], "Open"};
        }
        JTable t = new JTable(new DefaultTableModel(rows, ACTION_COLUMNS) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        });
        t.getColumnModel().getColumn(2).setCellRenderer(
                (table, value, selected, focused, row, column) -> new JButton(String.valueOf(value), ICON));
        t.getColumnModel().getColumn(2).setCellEditor(new ButtonEditor());
        return sized(new JScrollPane(t), 260, ROW * 4 - 8);
    }

    /** A cell's button that is pressed rather than edited: the click ends the edit at once. */
    private static final class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private Object value;

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean selected,
                int row, int column) {
            this.value = value;
            JButton b = new JButton(String.valueOf(value), ICON);
            b.addActionListener(e -> stopCellEditing());
            return b;
        }

        @Override
        public Object getCellEditorValue() {
            return value;
        }
    }

    private static final String[] CHOICE_COLUMNS = {"Name", "Value", "Pick"};

    /** A check box per row, SWT's own (SWT.CHECK), and a radio per row: one picked of the three. */
    private static void choiceTable(Composite p) {
        Table t = new Table(p, SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
        t.setHeaderVisible(true);
        for (String name : CHOICE_COLUMNS) {
            TableColumn c = new TableColumn(t, SWT.NONE);
            c.setText(name);
            c.setWidth(80);
        }
        for (int i = 0; i < ROWS.length; i++) {
            TableItem item = new TableItem(t, SWT.NONE);
            item.setText(new String[] {ROWS[i][0], ROWS[i][1]});
            item.setChecked(i != 1);
            // Siblings in the same parent: SWT's radios are one group already.
            Button radio = new Button(t, SWT.RADIO);
            radio.setSelection(i == 1);
            TableEditor editor = new TableEditor(t);
            editor.grabHorizontal = true;
            editor.grabVertical = true;
            editor.setEditor(radio, item, 2);
        }
        t.setLayoutData(new GridData(260, ROW * 4 - 8));
    }

    /**
     * The same in Swing's terms: a Boolean column takes JTable's own check box renderer and
     * editor, which Swing keeps in a column of its own rather than beside the name, and the picks
     * are radios the model keeps to one.
     */
    private static JComponent choiceTable() {
        String[] columns = {"", "Name", "Value", "Pick"};
        Object[][] rows = new Object[ROWS.length][];
        for (int i = 0; i < ROWS.length; i++) {
            rows[i] = new Object[] {i != 1, ROWS[i][0], ROWS[i][1], i == 1};
        }
        JTable t = new JTable(new DefaultTableModel(rows, columns) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 || column == 3 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 3;
            }

            @Override
            public void setValueAt(Object value, int row, int column) {
                if (column == 3 && Boolean.TRUE.equals(value)) {
                    for (int r = 0; r < getRowCount(); r++) {
                        super.setValueAt(r == row, r, 3);
                    }
                } else if (column != 3) {
                    super.setValueAt(value, row, column);
                }
            }
        });
        t.getColumnModel().getColumn(0).setMaxWidth(28);
        t.getColumnModel().getColumn(3).setCellRenderer((table, value, selected, focused, row, column) -> {
            JRadioButton r = new JRadioButton();
            r.setSelected(Boolean.TRUE.equals(value));
            r.setHorizontalAlignment(SwingConstants.CENTER);
            // On the ground the row's text cells are painted on, stripe and selection included:
            // a radio's own background would make its cell the one of a different colour.
            r.setBackground(table.getDefaultRenderer(Object.class)
                    .getTableCellRendererComponent(table, "", selected, false, row, column).getBackground());
            return r;
        });
        t.getColumnModel().getColumn(3).setCellEditor(new RadioEditor());
        return sized(new JScrollPane(t), 260, ROW * 4 - 8);
    }

    /** A radio cell: a click picks its row, which the model turns into one pick of all of them. */
    private static final class RadioEditor extends AbstractCellEditor implements TableCellEditor {
        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean selected,
                int row, int column) {
            JRadioButton r = new JRadioButton();
            r.setSelected(true);
            r.setHorizontalAlignment(SwingConstants.CENTER);
            r.addActionListener(e -> stopCellEditing());
            return r;
        }

        @Override
        public Object getCellEditorValue() {
            return Boolean.TRUE;
        }
    }

    private static void tree(Composite p) {
        Tree t = new Tree(p, SWT.BORDER);
        TreeItem root = new TreeItem(t, SWT.NONE);
        root.setText("Root");
        for (String name : new String[] {"Alpha", "Beta", "Gamma"}) {
            new TreeItem(root, SWT.NONE).setText(name);
        }
        root.setExpanded(true);
        t.setSelection(root.getItem(1));
        t.setLayoutData(new GridData(220, ROW * 4 - 8));
    }

    private static JComponent tree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        for (String name : new String[] {"Alpha", "Beta", "Gamma"}) {
            root.add(new DefaultMutableTreeNode(name));
        }
        JTree t = new JTree(root);
        t.setSelectionRow(2);
        return sized(new JScrollPane(t), 220, ROW * 4 - 8);
    }

    private static <T extends JComponent> T disabled(T c) {
        c.setEnabled(false);
        return c;
    }

    private static <T extends JComponent> T sized(T c, int w, int h) {
        c.setPreferredSize(new Dimension(w, h));
        return c;
    }

    /** One 16 px icon for both sides: an accent tile with a white plus, drawn once. */
    private static final BufferedImage ICON_PIXELS = icon();
    private static final Icon ICON = new ImageIcon(ICON_PIXELS);

    private static BufferedImage icon() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(0x5D, 0x4D, 0x68));
            g.fillRoundRect(1, 1, 14, 14, 5, 5);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2f));
            g.drawLine(8, 4, 8, 12);
            g.drawLine(4, 8, 12, 8);
        } finally {
            g.dispose();
        }
        return image;
    }

    /** {@link #ICON_PIXELS} as an SWT image, alpha and all. */
    private static Image swtIcon(Display display) {
        int w = ICON_PIXELS.getWidth();
        int h = ICON_PIXELS.getHeight();
        ImageData data = new ImageData(w, h, 24, new PaletteData(0xFF0000, 0x00FF00, 0x0000FF));
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = ICON_PIXELS.getRGB(x, y);
                data.setPixel(x, y, argb & 0xFFFFFF);
                data.setAlpha(x, y, argb >>> 24);
            }
        }
        return new Image(display, data);
    }

    /** The items both menus hold, SWT's: every kind a menu has, one icon, one accelerator. */
    private static void swtItems(Menu menu, Image icon) {
        MenuItem add = new MenuItem(menu, SWT.PUSH);
        add.setText("New\tCtrl+N");
        add.setAccelerator(SWT.MOD1 | 'N');
        add.setImage(icon);
        new MenuItem(menu, SWT.PUSH).setText("Open...");
        new MenuItem(menu, SWT.SEPARATOR);
        MenuItem grid = new MenuItem(menu, SWT.CHECK);
        grid.setText("Show grid");
        grid.setSelection(true);
        new MenuItem(menu, SWT.RADIO).setText("Small");
        MenuItem large = new MenuItem(menu, SWT.RADIO);
        large.setText("Large");
        large.setSelection(true);
        new MenuItem(menu, SWT.SEPARATOR);
        MenuItem export = new MenuItem(menu, SWT.CASCADE);
        export.setText("Export");
        Menu formats = new Menu(export);
        new MenuItem(formats, SWT.PUSH).setText("PNG");
        new MenuItem(formats, SWT.PUSH).setText("SVG");
        export.setMenu(formats);
        MenuItem off = new MenuItem(menu, SWT.PUSH);
        off.setText("Disabled");
        off.setEnabled(false);
    }

    /** The same items, Swing's, into a popup or a menu alike. */
    private static void swingItems(Consumer<JMenuItem> add, Runnable separator) {
        JMenuItem item = new JMenuItem("New", ICON);
        // Ctrl on every platform, as the SWT item's label spells it.
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        add.accept(item);
        add.accept(new JMenuItem("Open..."));
        separator.run();
        add.accept(new JCheckBoxMenuItem("Show grid", true));
        ButtonGroup sizes = new ButtonGroup();
        JRadioButtonMenuItem small = new JRadioButtonMenuItem("Small");
        JRadioButtonMenuItem large = new JRadioButtonMenuItem("Large", true);
        sizes.add(small);
        sizes.add(large);
        add.accept(small);
        add.accept(large);
        separator.run();
        JMenu export = new JMenu("Export");
        export.add(new JMenuItem("PNG"));
        export.add(new JMenuItem("SVG"));
        add.accept(export);
        JMenuItem off = new JMenuItem("Disabled");
        off.setEnabled(false);
        add.accept(off);
    }

    private static JMenuBar swingMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        swingItems(file::add, file::addSeparator);
        bar.add(file);
        JMenu edit = new JMenu("Edit");
        for (String name : new String[] {"Cut", "Copy", "Paste"}) {
            edit.add(new JMenuItem(name));
        }
        bar.add(edit);
        return bar;
    }
}
