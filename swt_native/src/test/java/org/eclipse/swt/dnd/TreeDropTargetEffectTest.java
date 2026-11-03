package org.eclipse.swt.dnd;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** A drag held over a Tree scrolls and expands it as TreeDropTargetEffect does natively. */
@Tag("native-unit")
class TreeDropTargetEffectTest {

    private static final int SCROLL_AND_EXPAND = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND;

    private Mocks.Delivery delivery;
    private Tree tree;
    private DropTarget target;
    private long now = 10_000;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        Display display = Mocks.display();
        delivery = Mocks.deliverEvents(display);
        tree = new Tree(Mocks.shell(display), SWT.SINGLE);
        tree.setBounds(0, 0, 200, 5 * tree.getItemHeight() + tree.getItemHeight() / 2);
        target = new DropTarget(tree, DND.DROP_MOVE);
    }

    @AfterEach
    void tearDown() {
        delivery.close();
        FlutterBridge.set(null);
    }

    private TreeItem[] roots(int count) {
        TreeItem[] items = new TreeItem[count];
        for (int i = 0; i < count; i++) {
            items[i] = new TreeItem(tree, SWT.NONE);
            items[i].setText("item " + i);
        }
        return items;
    }

    private DropTargetEvent over(TreeItem item, int feedback) {
        DNDEvent e = new DNDEvent();
        e.widget = target;
        e.item = item;
        e.feedback = feedback;
        return new DropTargetEvent(e);
    }

    private TreeDropTargetEffectHelper helper() {
        TreeDropTargetEffectHelper helper = new TreeDropTargetEffectHelper(tree, () -> now);
        helper.dragEnter();
        return helper;
    }

    private List<Event> expands() {
        List<Event> events = new ArrayList<>();
        tree.addListener(SWT.Expand, events::add);
        return events;
    }

    @Test
    @DisplayName("a drag held a second over a collapsed item expands it, then sends SWT.Expand")
    void expandsAfterTheHysteresis() {
        TreeItem folder = roots(1)[0];
        new TreeItem(folder, SWT.NONE);
        List<Event> expands = expands();
        List<Boolean> expandedWhenSent = new ArrayList<>();
        tree.addListener(SWT.Expand, e -> expandedWhenSent.add(((TreeItem) e.item).getExpanded()));
        TreeDropTargetEffectHelper helper = helper();

        helper.dragOver(over(folder, SCROLL_AND_EXPAND));
        now += TreeDropTargetEffectHelper.EXPAND_HYSTERESIS - 1;
        helper.dragOver(over(folder, SCROLL_AND_EXPAND));
        assertThat(folder.getExpanded()).isFalse();
        assertThat(expands).isEmpty();

        now += 1;
        helper.dragOver(over(folder, SCROLL_AND_EXPAND));
        assertThat(folder.getExpanded()).isTrue();
        assertThat(expands).extracting(e -> e.item).containsExactly(folder);
        assertThat(expandedWhenSent).containsExactly(true);
    }

    @Test
    @DisplayName("moving to another item, or a feedback without EXPAND, restarts the wait")
    void anotherItemRestartsTheExpandWait() {
        TreeItem[] items = roots(2);
        new TreeItem(items[0], SWT.NONE);
        TreeDropTargetEffectHelper helper = helper();

        helper.dragOver(over(items[0], SCROLL_AND_EXPAND));
        now += 600;
        helper.dragOver(over(items[1], SCROLL_AND_EXPAND));
        now += 600;
        helper.dragOver(over(items[0], SCROLL_AND_EXPAND));
        now += 600;
        helper.dragOver(over(items[0], DND.FEEDBACK_SELECT));
        now += 1000;
        helper.dragOver(over(items[0], SCROLL_AND_EXPAND));

        assertThat(items[0].getExpanded()).isFalse();
    }

    @Test
    @DisplayName("a drag held over the last row shown scrolls the tree one row, not before 200 ms")
    void scrollsDownFromTheLastRow() {
        TreeItem[] items = roots(20);
        tree.setTopItem(items[0]);
        TreeDropTargetEffectHelper helper = helper();

        helper.dragOver(over(items[4], SCROLL_AND_EXPAND));
        now += TreeDropTargetEffectHelper.SCROLL_HYSTERESIS - 1;
        helper.dragOver(over(items[4], SCROLL_AND_EXPAND));
        assertThat(tree.getTopItem()).isSameAs(items[0]);

        now += 1;
        helper.dragOver(over(items[4], SCROLL_AND_EXPAND));
        assertThat(tree.getTopItem()).isSameAs(items[1]);
    }

    @Test
    @DisplayName("a drag held over the top row scrolls up a row; over a middle row it does not scroll")
    void scrollsUpFromTheTopRowOnly() {
        TreeItem[] items = roots(20);
        tree.setTopItem(items[3]);
        TreeDropTargetEffectHelper helper = helper();

        helper.dragOver(over(items[3], SCROLL_AND_EXPAND));
        now += TreeDropTargetEffectHelper.SCROLL_HYSTERESIS;
        helper.dragOver(over(items[3], SCROLL_AND_EXPAND));
        assertThat(tree.getTopItem()).isSameAs(items[2]);

        helper.dragOver(over(items[3], DND.FEEDBACK_SCROLL));
        now += TreeDropTargetEffectHelper.SCROLL_HYSTERESIS;
        helper.dragOver(over(items[3], DND.FEEDBACK_SCROLL));
        assertThat(tree.getTopItem()).isSameAs(items[2]);
    }

    @Test
    @DisplayName("a Tree's DropTargetEffect expands the item a drag is held over")
    void theTreesEffectExpands() throws InterruptedException {
        TreeItem folder = roots(1)[0];
        new TreeItem(folder, SWT.NONE);
        List<Event> expands = expands();
        DropTargetEffect effect = target.getDropTargetEffect();

        effect.dragEnter(over(folder, SCROLL_AND_EXPAND));
        effect.dragOver(over(folder, SCROLL_AND_EXPAND));
        Thread.sleep(TreeDropTargetEffectHelper.EXPAND_HYSTERESIS + 50);
        effect.dragOver(over(folder, SCROLL_AND_EXPAND));

        assertThat(folder.getExpanded()).isTrue();
        assertThat(expands).extracting(e -> e.item).containsExactly(folder);
    }
}
