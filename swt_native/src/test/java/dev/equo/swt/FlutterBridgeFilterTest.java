package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.composite;

public class FlutterBridgeFilterTest extends SerializeTestBase {

    @Test
    void shouldFilterChildWhenParentIsAlsoDirty() {
        // Setup: create parent -> child hierarchy
        Composite parent = new Composite(composite(), SWT.NONE);
        Button child = new Button(parent, SWT.PUSH);

        Set<Object> dirty = new HashSet<>();
        dirty.add(parent.getImpl());
        dirty.add(child.getImpl());

        // Act
        Set<Object> filtered = FlutterBridge.filterWidgetsWithDirtyAncestors(dirty);

        // Assert: only parent should be in the filtered list
        assertThat(filtered).containsOnly(parent.getImpl());
        assertThat(filtered).doesNotContain(child.getImpl());
    }

    @Test
    void shouldKeepChildWhenParentIsNotDirty() {
        // Setup: create parent -> child hierarchy, but only child is dirty
        Composite parent = new Composite(composite(), SWT.NONE);
        Button child = new Button(parent, SWT.PUSH);

        Set<Object> dirty = new HashSet<>();
        dirty.add(child.getImpl());

        // Act
        Set<Object> filtered = FlutterBridge.filterWidgetsWithDirtyAncestors(dirty);

        // Assert: child should be in the filtered list
        assertThat(filtered).containsOnly(child.getImpl());
    }

    @Test
    void shouldKeepParentWhenOnlyParentIsDirty() {
        // Setup: create parent -> child hierarchy, but only parent is dirty
        Composite parent = new Composite(composite(), SWT.NONE);
        Button child = new Button(parent, SWT.PUSH);

        Set<Object> dirty = new HashSet<>();
        dirty.add(parent.getImpl());

        // Act
        Set<Object> filtered = FlutterBridge.filterWidgetsWithDirtyAncestors(dirty);

        // Assert: parent should be in the filtered list
        assertThat(filtered).containsOnly(parent.getImpl());
    }

    @Test
    void shouldFilterMultipleLevels() {
        // Setup: create grandparent -> parent -> child hierarchy
        Composite grandparent = new Composite(composite(), SWT.NONE);
        Composite parent = new Composite(grandparent, SWT.NONE);
        Button child = new Button(parent, SWT.PUSH);

        Set<Object> dirty = new HashSet<>();
        dirty.add(grandparent.getImpl());
        dirty.add(parent.getImpl());
        dirty.add(child.getImpl());

        // Act
        Set<Object> filtered = FlutterBridge.filterWidgetsWithDirtyAncestors(dirty);

        // Assert: only grandparent should be in the filtered list
        assertThat(filtered).containsOnly(grandparent.getImpl());
        assertThat(filtered).doesNotContain(parent.getImpl(), child.getImpl());
    }

    @Test
    void shouldKeepMultipleIndependentWidgets() {
        // Setup: create two independent hierarchies
        Composite parent1 = new Composite(composite(), SWT.NONE);
        Button child1 = new Button(parent1, SWT.PUSH);

        Composite parent2 = new Composite(composite(), SWT.NONE);
        Button child2 = new Button(parent2, SWT.PUSH);

        Set<Object> dirty = new HashSet<>();
        dirty.add(parent1.getImpl());
        dirty.add(parent2.getImpl());

        // Act
        Set<Object> filtered = FlutterBridge.filterWidgetsWithDirtyAncestors(dirty);

        // Assert: both parents should be in the filtered list
        assertThat(filtered).containsExactlyInAnyOrder(parent1.getImpl(), parent2.getImpl());
    }

    @Test
    void shouldFilterMixedScenario() {
        // Setup: complex scenario
        // Tree 1: grandparent1 -> parent1 -> child1 (all dirty)
        Composite grandparent1 = new Composite(composite(), SWT.NONE);
        Composite parent1 = new Composite(grandparent1, SWT.NONE);
        Button child1 = new Button(parent1, SWT.PUSH);

        // Tree 2: parent2 -> child2 (only child2 dirty)
        Composite parent2 = new Composite(composite(), SWT.NONE);
        Button child2 = new Button(parent2, SWT.PUSH);

        // Tree 3: parent3 (only parent3 dirty)
        Composite parent3 = new Composite(composite(), SWT.NONE);

        Set<Object> dirty = new HashSet<>();
        dirty.add(grandparent1.getImpl());
        dirty.add(parent1.getImpl());
        dirty.add(child1.getImpl());
        dirty.add(child2.getImpl());
        dirty.add(parent3.getImpl());

        // Act
        Set<Object> filtered = FlutterBridge.filterWidgetsWithDirtyAncestors(dirty);

        // Assert: only grandparent1, child2, and parent3 should be in the filtered list
        assertThat(filtered).containsExactlyInAnyOrder(
                grandparent1.getImpl(),
                child2.getImpl(),
                parent3.getImpl()
        );
    }

    @Test
    void shouldHandleEmptySet() {
        Set<Object> dirty = new HashSet<>();

        Set<Object> filtered = FlutterBridge.filterWidgetsWithDirtyAncestors(dirty);

        assertThat(filtered).isEmpty();
    }

    @Test
    void shouldHandleWidgetWithoutParent() {
        // Setup: widget without parent (top-level composite)
        Composite widget = new Composite(composite(), SWT.NONE);

        Set<Object> dirty = new HashSet<>();
        dirty.add(widget.getImpl());

        // Act
        Set<Object> filtered = FlutterBridge.filterWidgetsWithDirtyAncestors(dirty);

        // Assert: widget without parent should be in the filtered list
        assertThat(filtered).containsOnly(widget.getImpl());
    }

    @Test
    void shouldFilterWithDifferentWidgetTypes() {
        // Setup: hierarchy with different widget types
        Composite parent = new Composite(composite(), SWT.NONE);
        Label label = new Label(parent, SWT.NONE);
        Text text = new Text(parent, SWT.SINGLE);
        Button button = new Button(parent, SWT.PUSH);

        Set<Object> dirty = new HashSet<>();
        dirty.add(parent.getImpl());
        dirty.add(label.getImpl());
        dirty.add(text.getImpl());
        dirty.add(button.getImpl());

        // Act
        Set<Object> filtered = FlutterBridge.filterWidgetsWithDirtyAncestors(dirty);

        // Assert: only parent should be in the filtered list
        assertThat(filtered).containsOnly(parent.getImpl());
        assertThat(filtered).doesNotContain(label.getImpl(), text.getImpl(), button.getImpl());
    }

    @Test
    void shouldMarkAsDirtyWhenSettingDifferentText() {
        // Setup: create a button and set initial text
        Button button = new Button(composite(), SWT.PUSH);
        button.setText("Hello");

        // Clear the dirty list
        FlutterBridge.clearDirty();

        // Verify button is not dirty after clearing
        assertThat(FlutterBridge.isDirty(button.getImpl())).isFalse();

        // Act: set different text
        button.setText("World");

        // Assert: button SHOULD be marked as dirty since the text changed
        assertThat(FlutterBridge.isDirty(button.getImpl())).isTrue();
    }
}