package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** What a Selection or DefaultSelection arriving from Flutter does to a CoolItem. */
@Tag("native-unit")
class CoolItemEventsFromFlutterNativeTest extends EventsFromFlutterNativeTestBase {

    private CoolItem item() {
        CoolBar bar = new CoolBar(DartMocks.dartShell(display), SWT.NONE);
        return new CoolItem(bar, SWT.DROP_DOWN);
    }

    @Test
    @DisplayName("a Selection from Flutter reaches the item's Selection listeners")
    void selectionReachesListeners() {
        CoolItem item = item();
        List<Event> events = record(item, SWT.Selection);

        fromFlutter(item, "Selection/Selection");
        runQueued();

        assertThat(events).hasSize(1);
        assertThat(events.get(0).widget).isSameAs(item);
    }

    @Test
    @DisplayName("a DefaultSelection from Flutter reaches the item's DefaultSelection listeners")
    void defaultSelectionReachesListeners() {
        CoolItem item = item();
        List<Event> events = record(item, SWT.DefaultSelection);

        fromFlutter(item, "Selection/DefaultSelection");
        runQueued();

        assertThat(events).hasSize(1);
        assertThat(events.get(0).widget).isSameAs(item);
    }

    @Test
    @DisplayName("a Selection or DefaultSelection that arrives after the item was disposed is dropped")
    void eventsToADisposedItemAreDropped() {
        CoolItem item = item();
        List<Event> selections = record(item, SWT.Selection);
        List<Event> defaultSelections = record(item, SWT.DefaultSelection);

        fromFlutter(item, "Selection/Selection");
        fromFlutter(item, "Selection/DefaultSelection");
        item.dispose();
        runQueued();

        assertThat(selections).isEmpty();
        assertThat(defaultSelections).isEmpty();
    }
}
