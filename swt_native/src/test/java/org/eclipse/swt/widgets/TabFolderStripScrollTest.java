package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;

/**
 * A strip narrower than its tabs scrolls on the render side and shows its arrows past the tabs'
 * viewport: the tabs' bounds, the hit test and the press focus follow what the render side reports.
 */
@Tag("native-unit")
class TabFolderStripScrollTest {

    private static final int SCROLL = 120;
    private static final int VIEWPORT = 340;

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
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static TabFolder overflowingFolder() {
        TabFolder folder = new TabFolder(DartMocks.dartShell(), SWT.TOP);
        folder.setBounds(0, 0, 400, 200);
        for (int i = 1; i <= 12; i++) {
            new TabItem(folder, SWT.NONE).setText(String.format("Page %02d", i));
        }
        return folder;
    }

    private static void reportStrip(TabFolder folder, String text) {
        Event e = new Event();
        e.text = text;
        TabFolderHelper.handleStripLaidOut((DartTabFolder) folder.getImpl(), e);
    }

    private static Event press(int x, int y) {
        Event e = new Event();
        e.button = 1;
        e.x = x;
        e.y = y;
        return e;
    }

    private static boolean pressFocuses(TabFolder folder, int x, int y) {
        return ControlHelper.takesFocusOnPress((DartControl) folder.getImpl(), press(x, y));
    }

    @Test
    void aScrolledStripMovesTheTabsBoundsByTheScroll() {
        TabFolder folder = overflowingFolder();
        Rectangle[] unscrolled = new Rectangle[folder.getItemCount()];
        for (int i = 0; i < unscrolled.length; i++) unscrolled[i] = folder.getItem(i).getBounds();
        assertThat(unscrolled[unscrolled.length - 1].x).as("sanity: the tabs overflow the folder").isGreaterThan(400);

        reportStrip(folder, SCROLL + "," + VIEWPORT);

        for (int i = 0; i < unscrolled.length; i++) {
            assertThat(folder.getItem(i).getBounds())
                    .isEqualTo(new Rectangle(unscrolled[i].x - SCROLL, unscrolled[i].y, unscrolled[i].width, unscrolled[i].height));
        }
    }

    @Test
    void aPointOnADrawnTabFindsThatTab() {
        TabFolder folder = overflowingFolder();
        TabItem shown = null;
        Point centre = null;
        for (TabItem item : folder.getItems()) {
            Rectangle b = item.getBounds();
            if (b.x - SCROLL >= 0 && b.x - SCROLL + b.width <= VIEWPORT) {
                shown = item;
                centre = new Point(b.x - SCROLL + b.width / 2, b.y + b.height / 2);
                break;
            }
        }
        assertThat(shown).as("sanity: a tab lies wholly in the scrolled viewport").isNotNull();

        reportStrip(folder, SCROLL + "," + VIEWPORT);

        assertThat(folder.getItem(centre)).isSameAs(shown);
        assertThat(pressFocuses(folder, centre.x, centre.y)).as("a press on a tab focuses the folder").isTrue();
    }

    @Test
    void aPressOnTheArrowsFindsNoTabAndLeavesTheFocus() {
        TabFolder folder = overflowingFolder();
        Rectangle first = folder.getItem(0).getBounds();
        int y = first.y + first.height / 2;
        int onArrows = VIEWPORT + 10;
        assertThat(folder.getItem(new Point(onArrows, y))).as("sanity: an unscrolled tab reaches under the arrows").isNotNull();

        reportStrip(folder, SCROLL + "," + VIEWPORT);

        assertThat(folder.getItem(new Point(onArrows, y))).isNull();
        assertThat(pressFocuses(folder, onArrows, y)).isFalse();
    }

    @Test
    void aStripWhoseTabsFitAgainLaysThemOutFromTheLeftEdge() {
        TabFolder folder = overflowingFolder();
        int unscrolled = folder.getItem(3).getBounds().x;
        reportStrip(folder, SCROLL + "," + VIEWPORT);
        reportStrip(folder, "");
        assertThat(folder.getItem(3).getBounds().x).isEqualTo(unscrolled);
    }

    @Test
    void aReportThatIsNotTwoNumbersForgetsTheScroll() {
        TabFolder folder = overflowingFolder();
        int unscrolled = folder.getItem(3).getBounds().x;
        for (String report : new String[] {null, "120", "a,340", "120,340,9"}) {
            reportStrip(folder, SCROLL + "," + VIEWPORT);
            reportStrip(folder, report);
            assertThat(folder.getItem(3).getBounds().x).as("after the report %s", report).isEqualTo(unscrolled);
        }
    }

    @Test
    void aPointLeftOfTheStripFindsNoTab() {
        TabFolder folder = overflowingFolder();
        Rectangle first = folder.getItem(0).getBounds();
        reportStrip(folder, SCROLL + "," + VIEWPORT);
        assertThat(folder.getItem(new Point(-1, first.y + first.height / 2))).isNull();
    }

    @Test
    void aReportForADisposedFolderIsIgnored() {
        TabFolder folder = overflowingFolder();
        DartTabFolder impl = (DartTabFolder) folder.getImpl();
        folder.dispose();
        reportStrip(folder, SCROLL + "," + VIEWPORT);
        assertThat(impl.shownStrip).isNull();
    }

    @Test
    void theRenderSidesReportReachesTheFolderOnTheDisplayThread() {
        TabFolder folder = overflowingFolder();
        DartTabFolder impl = (DartTabFolder) folder.getImpl();
        Display display = folder.getDisplay();
        List<Runnable> handed = new ArrayList<>();
        doAnswer(inv -> handed.add(inv.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        try {
            int unscrolled = folder.getItem(3).getBounds().x;
            Event e = new Event();
            e.text = SCROLL + "," + VIEWPORT;
            impl._onClientEvent("TabFolder/stripLaidOut", e);
            impl._onClientEvent("TabFolder/stripLaidOut", e);
            assertThat(handed).as("each report is handed to the display").hasSize(2);

            handed.get(0).run();
            assertThat(folder.getItem(3).getBounds().x).isEqualTo(unscrolled - SCROLL);

            impl.shownStrip = null;
            folder.dispose();
            handed.get(1).run();
            assertThat(impl.shownStrip).as("a report that arrives after the folder went is dropped").isNull();
        } finally {
            doNothing().when(display).asyncExec(any(Runnable.class));
        }
    }
}
