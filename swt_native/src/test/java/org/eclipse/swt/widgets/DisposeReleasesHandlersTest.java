package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A disposed widget takes the handlers it registered for client events with it. A handler left
 * in the comm's table keeps its widget reachable, and an item keeps its parent and everything the
 * parent holds: a tree created and disposed over and over grows the heap by every item it had.
 */
@ExtendWith(Mocks.class)
class DisposeReleasesHandlersTest {

    private RecordingBridge bridge;

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
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private List<String> handlersOf(Widget widget) {
        String owner = FlutterBridge.event(widget);
        return bridge.comm.handlers.keySet().stream()
                .filter(name -> name.startsWith(owner + "/"))
                .collect(Collectors.toList());
    }

    @Test
    void aDisposedTreeLeavesNoHandlersForItselfOrItsItems() {
        Shell shell = Mocks.shell();
        Tree tree = new Tree(shell, SWT.NONE);
        TreeItem root = new TreeItem(tree, SWT.NONE);
        TreeItem child = new TreeItem(root, SWT.NONE);
        assertThat(handlersOf(child)).as("an item registers for its client events").isNotEmpty();

        tree.dispose();

        assertThat(handlersOf(tree)).isEmpty();
        assertThat(handlersOf(root)).isEmpty();
        assertThat(handlersOf(child)).isEmpty();
    }

    @Test
    void aDisposedTreeLeavesNoEntriesForRefreshRequests() {
        Shell shell = Mocks.shell();
        Tree tree = new Tree(shell, SWT.NONE);
        TreeItem item = new TreeItem(tree, SWT.NONE);
        // Entered by a change made once the widget is built.
        tree.setHeaderVisible(true);
        item.setText("item");
        assertThat(FlutterBridge.holdsId(tree.hashCode())).as("a changed widget is entered").isTrue();
        assertThat(FlutterBridge.holdsId(item.hashCode())).as("a changed item is entered").isTrue();

        tree.dispose();

        assertThat(FlutterBridge.holdsId(tree.hashCode())).isFalse();
        assertThat(FlutterBridge.holdsId(item.hashCode())).isFalse();
    }

    @Test
    void aDisposedItemLeavesNoHandlers() {
        Shell shell = Mocks.shell();
        Tree tree = new Tree(shell, SWT.NONE);
        TreeItem item = new TreeItem(tree, SWT.NONE);

        item.dispose();

        assertThat(handlersOf(item)).isEmpty();
    }
}
