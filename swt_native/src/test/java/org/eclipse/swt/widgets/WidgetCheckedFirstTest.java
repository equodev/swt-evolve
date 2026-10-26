package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Color;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A disposed widget is refused with ERROR_WIDGET_DISPOSED by the methods that check it upstream, before any
 * argument is looked at: getClientArea, and the setters that copy the colour they are given.
 */
@Tag("native-unit")
class WidgetCheckedFirstTest {

    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        shell = DartMocks.dartShell();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static List<String> notRefusedAsDisposed(Map<String, Executable> calls) {
        List<String> wrong = new ArrayList<>();
        calls.forEach((name, call) -> {
            try {
                call.execute();
                wrong.add(name + ": no exception");
            } catch (SWTException e) {
                if (e.code != SWT.ERROR_WIDGET_DISPOSED)
                    wrong.add(name + ": code " + e.code);
            } catch (Throwable t) {
                wrong.add(name + ": " + t);
            }
        });
        return wrong;
    }

    private <W extends Widget> W disposed(Function<Composite, W> factory) {
        W widget = factory.apply(shell);
        widget.dispose();
        return widget;
    }

    @Test
    void getClientAreaRefusesADisposedWidget() {
        Map<String, Executable> calls = new LinkedHashMap<>();
        Composite composite = disposed(p -> new Composite(p, SWT.H_SCROLL | SWT.V_SCROLL));
        Group group = disposed(p -> new Group(p, SWT.NONE));
        TabFolder tabFolder = disposed(p -> new TabFolder(p, SWT.NONE));
        Table table = disposed(p -> new Table(p, SWT.NONE));
        Tree tree = disposed(p -> new Tree(p, SWT.NONE));
        CTabFolder cTabFolder = disposed(p -> new CTabFolder(p, SWT.NONE));
        ViewForm viewForm = disposed(p -> new ViewForm(p, SWT.NONE));
        calls.put("Composite", composite::getClientArea);
        calls.put("Group", group::getClientArea);
        calls.put("TabFolder", tabFolder::getClientArea);
        calls.put("Table", table::getClientArea);
        calls.put("Tree", tree::getClientArea);
        calls.put("CTabFolder", cTabFolder::getClientArea);
        calls.put("ViewForm", viewForm::getClientArea);
        assertThat(notRefusedAsDisposed(calls)).isEmpty();
    }

    @Test
    void aDisposedWidgetIsRefusedBeforeTheColourItIsGiven() {
        Color color = new Color(shell.getDisplay(), 1, 2, 3);
        color.dispose();
        Canvas canvas = disposed(p -> new Canvas(p, SWT.NONE));
        Table table = new Table(shell, SWT.NONE);
        TableItem tableItem = new TableItem(table, SWT.NONE);
        Tree tree = new Tree(shell, SWT.NONE);
        TreeItem treeItem = new TreeItem(tree, SWT.NONE);
        tableItem.dispose();
        treeItem.dispose();
        table.dispose();
        tree.dispose();
        Table otherTable = disposed(p -> new Table(p, SWT.NONE));
        Tree otherTree = disposed(p -> new Tree(p, SWT.NONE));
        Link link = disposed(p -> new Link(p, SWT.NONE));
        StyledText styledText = disposed(p -> new StyledText(p, SWT.NONE));
        CTabFolder folder = disposed(p -> new CTabFolder(p, SWT.NONE));
        Map<String, Executable> calls = new LinkedHashMap<>();
        calls.put("Control.setForeground", () -> canvas.setForeground(color));
        calls.put("Table.setHeaderBackground", () -> otherTable.setHeaderBackground(color));
        calls.put("Tree.setHeaderForeground", () -> otherTree.setHeaderForeground(color));
        calls.put("TableItem.setBackground", () -> tableItem.setBackground(color));
        calls.put("TreeItem.setForeground", () -> treeItem.setForeground(color));
        calls.put("Link.setLinkForeground", () -> link.setLinkForeground(color));
        calls.put("StyledText.setMarginColor", () -> styledText.setMarginColor(color));
        calls.put("CTabFolder.setSelectionForeground", () -> folder.setSelectionForeground(color));
        assertThat(notRefusedAsDisposed(calls)).isEmpty();
    }
}
