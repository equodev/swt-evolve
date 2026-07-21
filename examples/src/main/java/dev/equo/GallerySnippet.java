package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

/**
 * One page with a representative instance of every Semantics-tagged widget, for visual
 * inspection and as a single stable Playwright target. Config.forceEquo() renders
 * everything through Flutter, including widgets not in the default equoEnabled set
 * (Group, TabFolder, ExpandBar, CoolBar).
 *
 * Laid out as a 2-column grid (not a ScrolledComposite) so the whole thing fits a normal
 * browser window without needing scroll — a ScrolledComposite's content sizing currently
 * breaks when the Display resizes down to a real (smaller than requested) browser viewport;
 * content disappears instead of becoming scrollable. That's a real bug worth fixing
 * separately; this snippet just sidesteps it.
 *
 * Deliberately has no shell.setSize(): an SWT Shell with x=0,y=0 tracks the Display's bounds
 * (see SwtFlutterBridgeWeb#resizeMainShells), which on web *is* the real browser viewport — so
 * the Shell already sizes itself to whatever window it's opened in. Each section instead caps
 * its own width (SECTION_WIDTH) since GridLayout would otherwise size every column to its
 * content's uncompressed preferred width, which easily exceeds the actual window.
 */
public class GallerySnippet {
    public static void main(String[] args) {
        Config.forceEquo();

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Equo SWT Gallery");

        Composite root = shell;
        root.setLayout(new GridLayout(2, true));

        GridData fillH = new GridData(SWT.FILL, SWT.CENTER, true, false);

        // Status — the only observable signal for widgets below that have no other Dart-side
        // state of their own to check (a plain push Button, a ToolItem, a column header): each
        // of their listeners updates this text, which the E2E suite reads back instead of just
        // asserting "didn't throw".
        Label status = new Label(root, SWT.NONE);
        status.setText("Status: (no interaction yet)");
        GridData statusData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        statusData.horizontalSpan = 2;
        status.setLayoutData(statusData);

        // Button
        Group buttonGroup = section(root, "Button");
        buttonGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
        Button push = new Button(buttonGroup, SWT.PUSH);
        push.setText("Push");
        push.addSelectionListener(widgetSelectedAdapter(e -> {
            System.out.println("Push clicked");
            status.setText("Push clicked");
        }));
        Button check = new Button(buttonGroup, SWT.CHECK);
        check.setText("Check");
        check.addSelectionListener(widgetSelectedAdapter(e -> System.out.println("Check: " + check.getSelection())));
        Button radioA = new Button(buttonGroup, SWT.RADIO);
        radioA.setText("Radio A");
        Button radioB = new Button(buttonGroup, SWT.RADIO);
        radioB.setText("Radio B");
        Button toggle = new Button(buttonGroup, SWT.TOGGLE);
        toggle.setText("Toggle");

        // Label / Link
        Group labelGroup = section(root, "Label / Link");
        labelGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
        Label label = new Label(labelGroup, SWT.NONE);
        label.setText("A plain label");
        // The anchor spans the whole label so a click anywhere on the widget hits it (the
        // E2E suite clicks the Semantics node's center; only the anchor span is tappable).
        Link link = new Link(labelGroup, SWT.NONE);
        link.setText("<a href=\"https://equo.dev\">A link to equo.dev</a>");
        link.addSelectionListener(widgetSelectedAdapter(e -> status.setText("Link clicked: " + e.text)));

        // Text / StyledText
        Group textGroup = section(root, "Text / StyledText");
        textGroup.setLayout(new GridLayout(1, false));
        Text text = new Text(textGroup, SWT.BORDER);
        text.setText("Editable text field");
        text.setLayoutData(fillH);
        StyledText styledText = new StyledText(textGroup, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        styledText.setText("Multi-line\nstyled text\nwith several lines.");
        styledText.addModifyListener(e -> status.setText("StyledText modified"));
        GridData styledTextData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        styledTextData.heightHint = 60;
        styledText.setLayoutData(styledTextData);

        // Combo / CCombo / List
        Group selectGroup = section(root, "Combo / CCombo / List");
        selectGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
        Combo combo = new Combo(selectGroup, SWT.READ_ONLY);
        combo.setItems("Combo A", "Combo B", "Combo C");
        combo.select(0);
        CCombo ccombo = new CCombo(selectGroup, SWT.BORDER);
        ccombo.setItems(new String[] {"CCombo A", "CCombo B", "CCombo C"});
        ccombo.select(0);
        List list = new List(selectGroup, SWT.BORDER | SWT.SINGLE);
        list.setItems(new String[] {"List item 1", "List item 2", "List item 3"});

        // Table
        Group tableGroup = section(root, "Table");
        tableGroup.setLayout(new FillLayout());
        GridData tableData = new GridData(SWT.FILL, SWT.CENTER, false, false);
        tableData.widthHint = SECTION_WIDTH;
        tableData.heightHint = 100;
        tableGroup.setLayoutData(tableData);
        Table table = new Table(tableGroup, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        for (String col : new String[] {"Name", "Role"}) {
            TableColumn c = new TableColumn(table, SWT.NONE);
            c.setText(col);
            c.setWidth(150);
            c.addSelectionListener(widgetSelectedAdapter(e -> status.setText(col + " column clicked")));
        }
        String[][] rows = {{"Ada", "Engineer"}, {"Grace", "Admiral"}, {"Linus", "Maintainer"}};
        for (String[] row : rows) {
            TableItem ti = new TableItem(table, SWT.NONE);
            ti.setText(row);
        }

        // Tree
        Group treeGroup = section(root, "Tree");
        treeGroup.setLayout(new FillLayout());
        GridData treeData = new GridData(SWT.FILL, SWT.CENTER, false, false);
        treeData.widthHint = SECTION_WIDTH;
        treeData.heightHint = 120;
        treeGroup.setLayoutData(treeData);
        Tree tree = new Tree(treeGroup, SWT.BORDER);
        tree.setHeaderVisible(true);
        for (String col : new String[] {"Item", "Detail"}) {
            TreeColumn c = new TreeColumn(tree, SWT.NONE);
            c.setText(col);
            c.setWidth(150);
            c.addSelectionListener(widgetSelectedAdapter(e -> status.setText(col + " column clicked")));
        }
        for (int i = 0; i < 3; i++) {
            TreeItem parent = new TreeItem(tree, SWT.NONE);
            parent.setText(new String[] {"Branch " + i, "—"});
            for (int j = 0; j < 2; j++) {
                TreeItem child = new TreeItem(parent, SWT.NONE);
                child.setText(new String[] {"Leaf " + i + "." + j, "detail"});
            }
        }
        tree.getItem(0).setExpanded(true);
        tree.addSelectionListener(widgetSelectedAdapter(e ->
                status.setText("Tree item selected: " + tree.getSelection()[0].getText())));

        // CTabFolder
        Group ctabGroup = section(root, "CTabFolder");
        ctabGroup.setLayout(new FillLayout());
        GridData ctabData = new GridData(SWT.FILL, SWT.CENTER, false, false);
        ctabData.widthHint = SECTION_WIDTH;
        ctabData.heightHint = 100;
        ctabGroup.setLayoutData(ctabData);
        CTabFolder ctab = new CTabFolder(ctabGroup, SWT.BORDER);
        for (int i = 0; i < 3; i++) {
            CTabItem item = new CTabItem(ctab, SWT.NONE);
            item.setText("CTab " + i);
            Text body = new Text(ctab, SWT.MULTI);
            body.setText("Content for CTab " + i);
            item.setControl(body);
        }
        ctab.setSelection(0);

        // TabFolder
        Group tabGroup = section(root, "TabFolder");
        tabGroup.setLayout(new FillLayout());
        GridData tabData = new GridData(SWT.FILL, SWT.CENTER, false, false);
        tabData.widthHint = SECTION_WIDTH;
        tabData.heightHint = 100;
        tabGroup.setLayoutData(tabData);
        TabFolder tabFolder = new TabFolder(tabGroup, SWT.NONE);
        for (int i = 0; i < 3; i++) {
            TabItem item = new TabItem(tabFolder, SWT.NONE);
            item.setText("Tab " + i);
            Text body = new Text(tabFolder, SWT.MULTI);
            body.setText("Content for Tab " + i);
            item.setControl(body);
        }

        // ToolBar
        Group toolGroup = section(root, "ToolBar");
        toolGroup.setLayout(new FillLayout());
        ToolBar toolBar = new ToolBar(toolGroup, SWT.BORDER);
        for (int i = 0; i < 4; i++) {
            ToolItem item = new ToolItem(toolBar, SWT.PUSH);
            item.setText("Tool " + i);
            int toolIndex = i;
            item.addSelectionListener(widgetSelectedAdapter(e -> status.setText("Tool " + toolIndex + " clicked")));
        }

        // ExpandBar
        Group expandGroup = section(root, "ExpandBar");
        expandGroup.setLayout(new FillLayout());
        GridData expandData = new GridData(SWT.FILL, SWT.CENTER, false, false);
        expandData.widthHint = SECTION_WIDTH;
        expandData.heightHint = 140;
        expandGroup.setLayoutData(expandData);
        ExpandBar expandBar = new ExpandBar(expandGroup, SWT.NONE);
        expandBar.addExpandListener(new org.eclipse.swt.events.ExpandAdapter() {
            @Override
            public void itemExpanded(org.eclipse.swt.events.ExpandEvent e) {
                status.setText(((ExpandItem) e.item).getText() + " expanded");
            }

            @Override
            public void itemCollapsed(org.eclipse.swt.events.ExpandEvent e) {
                status.setText(((ExpandItem) e.item).getText() + " collapsed");
            }
        });
        for (int i = 0; i < 2; i++) {
            ExpandItem item = new ExpandItem(expandBar, SWT.NONE);
            item.setText("Expand " + i);
            Composite content = new Composite(expandBar, SWT.NONE);
            content.setLayout(new RowLayout());
            Button inner = new Button(content, SWT.PUSH);
            inner.setText("Inside " + i);
            content.pack();
            item.setControl(content);
            item.setHeight(content.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
            item.setExpanded(i == 0);
        }

        // CoolBar
        Group coolGroup = section(root, "CoolBar");
        coolGroup.setLayout(new FillLayout());
        CoolBar coolBar = new CoolBar(coolGroup, SWT.FLAT | SWT.BORDER);
        for (String toolLabel : new String[] {"New", "Open", "Save"}) {
            CoolItem item = new CoolItem(coolBar, SWT.NONE);
            Button b = new Button(coolBar, SWT.PUSH);
            b.setText(toolLabel);
            b.addSelectionListener(widgetSelectedAdapter(e -> status.setText(toolLabel + " cool button clicked")));
            Point size = b.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            item.setControl(b);
            item.setPreferredSize(item.computeSize(size.x, size.y));
        }

        // Slider / Scale / Spinner — the value controls. Each posts its current value to the
        // status Label so the suite can assert the Java round-trip, and each carries live
        // `selection` state to assert directly.
        Group valueGroup = section(root, "Slider / Scale / Spinner");
        valueGroup.setLayout(new GridLayout(1, false));
        Slider slider = new Slider(valueGroup, SWT.HORIZONTAL);
        slider.setMinimum(0);
        slider.setMaximum(100);
        slider.setSelection(25);
        slider.setLayoutData(fillH);
        slider.addSelectionListener(widgetSelectedAdapter(e -> status.setText("Slider at " + slider.getSelection())));
        Scale scale = new Scale(valueGroup, SWT.HORIZONTAL);
        scale.setMinimum(0);
        scale.setMaximum(100);
        scale.setSelection(25);
        scale.setLayoutData(fillH);
        scale.addSelectionListener(widgetSelectedAdapter(e -> status.setText("Scale at " + scale.getSelection())));
        Spinner spinner = new Spinner(valueGroup, SWT.BORDER);
        spinner.setMinimum(0);
        spinner.setMaximum(100);
        spinner.setSelection(25);
        spinner.addSelectionListener(widgetSelectedAdapter(e -> status.setText("Spinner at " + spinner.getSelection())));

        // ProgressBar / DateTime — the ProgressBar is display-only, so a Button advances it from
        // the Java side and the suite asserts the state render; the DateTime starts on a fixed
        // date so its state is deterministic.
        Group progressGroup = section(root, "ProgressBar / DateTime");
        progressGroup.setLayout(new GridLayout(2, false));
        ProgressBar progressBar = new ProgressBar(progressGroup, SWT.HORIZONTAL);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setSelection(30);
        progressBar.setLayoutData(fillH);
        Button advanceProgress = new Button(progressGroup, SWT.PUSH);
        advanceProgress.setText("Advance Progress");
        advanceProgress.addSelectionListener(widgetSelectedAdapter(e -> {
            progressBar.setSelection(progressBar.getSelection() + 10);
            status.setText("Progress at " + progressBar.getSelection());
        }));
        DateTime date = new DateTime(progressGroup, SWT.DATE | SWT.BORDER);
        date.setDate(2026, 6, 15); // months are 0-based: July 15, 2026
        date.addSelectionListener(widgetSelectedAdapter(e -> status.setText("Date changed")));

        // Menu / CLabel / Canvas — the CLabel carries the popup menu (each item posts to the
        // status Label) and the Canvas reports its mouse events the same way. The Canvas gets
        // NO menu: attaching one swallowed its mouse events (documented product gap).
        Group menuGroup = section(root, "Menu / CLabel / Canvas");
        menuGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
        CLabel clabel = new CLabel(menuGroup, SWT.NONE);
        clabel.setText("Right-click me");
        Menu popup = new Menu(clabel);
        for (String action : new String[] {"Menu Action", "Other Action"}) {
            MenuItem mi = new MenuItem(popup, SWT.PUSH);
            mi.setText(action);
            mi.addSelectionListener(widgetSelectedAdapter(e -> {
                // Web gap: an item click doesn't close/sync the menu (its modal barrier then
                // swallows every later click) — close it from Java, BEFORE the status update so
                // the status change guarantees the close was already delivered.
                popup.setVisible(false);
                status.setText(action + " clicked");
            }));
        }
        clabel.setMenu(popup);
        // Programmatic opener for MANUAL exploration: right-click (MenuDetect) does not open a
        // control's popup on web yet, and closing a popup can leave an intercepting semantics
        // node — both documented product gaps, which is why no automated scenario drives this
        // menu (see the coverage table in e2e/README.md).
        Button openMenu = new Button(menuGroup, SWT.PUSH);
        openMenu.setText("Open Menu");
        openMenu.addSelectionListener(widgetSelectedAdapter(e -> popup.setVisible(true)));
        Canvas canvas = new Canvas(menuGroup, SWT.BORDER);
        canvas.setLayoutData(new org.eclipse.swt.layout.RowData(120, 60));
        canvas.addPaintListener(e -> {
            e.gc.setBackground(e.display.getSystemColor(SWT.COLOR_DARK_CYAN));
            e.gc.fillRectangle(10, 10, 100, 40);
        });
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                status.setText("Canvas clicked");
            }
        });

        // SashForm / Sash — the SashForm holds two panes (its internal sash resizes them); the
        // standalone Sash reports its drag to Java, which is the observable event.
        Group sashGroup = section(root, "SashForm / Sash");
        sashGroup.setLayout(new GridLayout(1, false));
        SashForm sashForm = new SashForm(sashGroup, SWT.HORIZONTAL);
        GridData sashFormData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        sashFormData.heightHint = 50;
        sashForm.setLayoutData(sashFormData);
        Text leftPane = new Text(sashForm, SWT.BORDER | SWT.MULTI);
        leftPane.setText("Left pane");
        Text rightPane = new Text(sashForm, SWT.BORDER | SWT.MULTI);
        rightPane.setText("Right pane");
        sashForm.setWeights(new int[] {1, 1});
        Sash sash = new Sash(sashGroup, SWT.VERTICAL);
        GridData sashData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        sashData.widthHint = 8;
        sashData.heightHint = 30;
        sash.setLayoutData(sashData);
        sash.addSelectionListener(widgetSelectedAdapter(e -> status.setText("Sash dragged")));

        // ToolTip / ScrolledComposite — the balloon ToolTip is shown programmatically (its
        // observable state is VToolTip.visible/text); the ScrolledComposite hosts content taller
        // than its viewport.
        Group tipGroup = section(root, "ToolTip / ScrolledComposite");
        tipGroup.setLayout(new GridLayout(2, false));
        ToolTip tip = new ToolTip(shell, SWT.BALLOON | SWT.ICON_INFORMATION);
        tip.setText("Gallery tip");
        tip.setMessage("This balloon came from Java");
        Button showTip = new Button(tipGroup, SWT.PUSH);
        showTip.setText("Show Tooltip");
        showTip.addSelectionListener(widgetSelectedAdapter(e -> tip.setVisible(true)));
        ScrolledComposite scrolled = new ScrolledComposite(tipGroup, SWT.V_SCROLL | SWT.BORDER);
        GridData scrolledData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        scrolledData.heightHint = 60;
        scrolled.setLayoutData(scrolledData);
        Composite scrolledContent = new Composite(scrolled, SWT.NONE);
        scrolledContent.setLayout(new GridLayout(1, false));
        Text scrolledTop = new Text(scrolledContent, SWT.READ_ONLY);
        scrolledTop.setText("Scrolled content top");
        for (int i = 1; i <= 5; i++) {
            Label filler = new Label(scrolledContent, SWT.NONE);
            filler.setText("Scrolled row " + i);
        }
        scrolledContent.pack();
        scrolled.setContent(scrolledContent);
        scrolled.setMinSize(scrolledContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        scrolled.setExpandHorizontal(true);
        scrolled.setExpandVertical(false);

        // Dialog — a secondary (child) Shell with its own Text input, opened modally on
        // demand. Unlike MessageBox (a one-off Flutter showDialog()), this exercises a real
        // DartShell: it renders as a Positioned overlay inside the same Display Stack (see
        // display_evolve.dart), not a separate browser tab/window.
        Group dialogGroup = section(root, "Dialog");
        dialogGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
        Button openDialog = new Button(dialogGroup, SWT.PUSH);
        openDialog.setText("Open Dialog");
        openDialog.addSelectionListener(widgetSelectedAdapter(e -> {
            Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
            dialog.setText("Enter your name");
            dialog.setLayout(new GridLayout(1, false));

            Label prompt = new Label(dialog, SWT.NONE);
            prompt.setText("Your name:");

            Text nameInput = new Text(dialog, SWT.BORDER);
            GridData inputData = new GridData(SWT.FILL, SWT.CENTER, true, false);
            inputData.widthHint = 220;
            nameInput.setLayoutData(inputData);

            Button ok = new Button(dialog, SWT.PUSH);
            ok.setText("OK");
            ok.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
            ok.addSelectionListener(widgetSelectedAdapter(okEvent -> {
                System.out.println("Dialog input: " + nameInput.getText());
                dialog.dispose();
            }));

            dialog.pack();
            // Center it on the main shell instead of leaving it wherever SWT's default
            // placement lands — at the gallery's size, the default position can overlap
            // visually with whatever section happens to be underneath it.
            org.eclipse.swt.graphics.Rectangle parentBounds = shell.getBounds();
            org.eclipse.swt.graphics.Point dialogSize = dialog.getSize();
            dialog.setLocation(
                parentBounds.x + (parentBounds.width - dialogSize.x) / 2,
                parentBounds.y + (parentBounds.height - dialogSize.y) / 2);
            dialog.open();
        }));

        // Reset affordance for the E2E suite: one live Display serves every scenario (Scope.SUITE),
        // so between tests the widgets are restored to baseline in place — no JVM/server restart.
        // The status Label is set back to baseline *last*: WebSocket delivery and Flutter processing
        // both preserve order, so once the suite sees the status at baseline every earlier restore
        // has already been applied — that's the signal it waits on (see GalleryInstance.reset).
        Button resetGallery = new Button(root, SWT.PUSH);
        resetGallery.setText("Reset Gallery");
        GridData resetData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        resetData.horizontalSpan = 2;
        resetGallery.setLayoutData(resetData);
        resetGallery.addSelectionListener(widgetSelectedAdapter(e -> {
            for (Shell child : shell.getShells()) {
                child.dispose();
            }
            check.setSelection(false);
            radioA.setSelection(false);
            radioB.setSelection(false);
            toggle.setSelection(false);
            combo.select(0);
            ccombo.select(0);
            list.deselectAll();
            text.setText("Editable text field");
            // Only touch the StyledText when a scenario actually changed it: setText fires its
            // ModifyListener, whose web round-trip echo can land AFTER the baseline status below
            // and overwrite the next scenario's status assert with "StyledText modified".
            String styledBaseline = "Multi-line\nstyled text\nwith several lines.";
            if (!styledBaseline.equals(styledText.getText())) {
                styledText.setText(styledBaseline);
            }
            table.deselectAll();
            tree.deselectAll();
            for (TreeItem branch : tree.getItems()) {
                branch.setExpanded(false);
            }
            tree.getItem(0).setExpanded(true);
            ctab.setSelection(0);
            tabFolder.setSelection(0);
            ExpandItem[] barItems = expandBar.getItems();
            for (int i = 0; i < barItems.length; i++) {
                barItems[i].setExpanded(i == 0);
            }
            slider.setSelection(25);
            scale.setSelection(25);
            spinner.setSelection(25);
            progressBar.setSelection(30);
            date.setDate(2026, 6, 15);
            popup.setVisible(false); // a failed scenario may leave the context menu up
            tip.setVisible(false);
            sashForm.setWeights(new int[] {1, 1});
            status.setText("Status: (no interaction yet)");
        }));

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    // Without an explicit widthHint, GridLayout sizes each column to its content's natural
    // (uncompressed) preferred width — with 2 equal columns that easily exceeds a normal
    // browser window. Capping every section to the same width keeps the whole gallery inside
    // ~1000px regardless of how wide any one widget's content wants to be.
    private static final int SECTION_WIDTH = 460;

    private static Group section(Composite parent, String title) {
        Group group = new Group(parent, SWT.NONE);
        group.setText(title);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        data.widthHint = SECTION_WIDTH;
        group.setLayoutData(data);
        return group;
    }
}
