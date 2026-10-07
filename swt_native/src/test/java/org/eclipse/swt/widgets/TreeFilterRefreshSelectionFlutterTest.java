package org.eclipse.swt.widgets;

import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A click names the row the user picked, not the position it was shown at: a filter refresh can
 * rebuild the tree between the client reporting the click and the Display handling it.
 */
@Tag("flutter-it")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TreeFilterRefreshSelectionFlutterTest {

    private static final Map<String, List<String>> CATEGORIES = new java.util.LinkedHashMap<>();
    static {
        CATEGORIES.put("General", List.of("File", "Folder", "Project", "Untitled Text File"));
        CATEGORIES.put("Eclipse 4", List.of("Application Model"));
        CATEGORIES.put("Git", List.of("Git Repository"));
        CATEGORIES.put("Gradle", List.of("Gradle Project"));
        CATEGORIES.put("Java", List.of("Java Project", "Package"));
    }

    private final WebDisplayHarness flutter = new WebDisplayHarness();
    private Display display;
    private Shell shell;

    @BeforeAll
    void boot() {
        display = flutter.boot();
        shell = new Shell(display);
        shell.setBounds(0, 0, 400, 400);
        shell.open();
    }

    @AfterAll
    void shutdown() {
        flutter.teardown();
    }

    private void settle() {
        int quiet = 0;
        for (int round = 0; round < 40 && quiet < 3; round++) {
            long before = flutter.dispatched();
            flutter.drain();
            flutter.flush();
            flutter.drain();
            quiet = flutter.dispatched() == before ? quiet + 1 : 0;
        }
    }

    @Test
    void the_picked_row_stays_selected_through_a_filter_refresh() {
        TreeViewer viewer = new TreeViewer(shell, SWT.SINGLE | SWT.BORDER);
        viewer.getTree().setBounds(0, 0, 380, 380);
        viewer.setContentProvider(new ITreeContentProvider() {
            public Object[] getElements(Object input) {
                return CATEGORIES.keySet().toArray();
            }

            public Object[] getChildren(Object parent) {
                List<String> children = CATEGORIES.get(parent);
                return children == null ? new Object[0] : children.toArray();
            }

            public Object getParent(Object element) {
                for (Map.Entry<String, List<String>> e : CATEGORIES.entrySet())
                    if (e.getValue().contains(element)) return e.getKey();
                return null;
            }

            public boolean hasChildren(Object element) {
                return CATEGORIES.containsKey(element);
            }
        });
        viewer.setLabelProvider(new LabelProvider());
        viewer.setInput(CATEGORIES);
        viewer.expandToLevel("General", 1);
        settle();

        List<String> selectionEvents = new ArrayList<>();
        viewer.getTree().addListener(SWT.Selection, e -> selectionEvents.add(((TreeItem) e.item).getText()));
        TreeItem project = viewer.getTree().getItem(0).getItem(2);
        assertThat(project.getText()).isEqualTo("Project");
        double[] rect = flutter.renderedRect(project);
        flutter.input().click(rect[0] + 20, rect[1] + rect[3] / 2);
        // The click is in flight while the filter's refresh rebuilds the tree, as when a user picks
        // a row the moment the filter's refresh job lands: the client reported the row it showed.
        pauseWithoutDispatching();

        viewer.addFilter(new ViewerFilter() {
            public boolean select(Viewer v, Object parent, Object element) {
                if (CATEGORIES.containsKey(element))
                    return CATEGORIES.get(element).stream().anyMatch(c -> c.contains("Project"));
                return ((String) element).contains("Project");
            }
        });
        viewer.expandAll();
        settle();

        assertThat(((IStructuredSelection) viewer.getSelection()).toList()).containsExactly("Project");
        assertThat(Arrays.stream(viewer.getTree().getSelection()).map(TreeItem::getText)).containsExactly("Project");
        assertThat(selectionEvents).as("no row was picked but the user's").containsExactly("Project");
    }

    private static void pauseWithoutDispatching() {
        try {
            Thread.sleep(1_500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
