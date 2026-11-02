package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import dev.equo.swt.size.TabFolderTheme;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Where the tab strip sits and what it takes: the tabs run from the left edge in one row, the strip is
 * above the page for TOP and below it for BOTTOM, the selected page fills the client area, and an empty
 * folder measures like an empty Composite.
 */
@Tag("native-unit")
class TabFolderTabsNativeTest {

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

    private TabFolder folder(int style) {
        TabFolder folder = new TabFolder(DartMocks.dartShell(), style);
        folder.setBounds(0, 0, 400, 200);
        return folder;
    }

    private TabItem tab(TabFolder folder, String text) {
        TabItem item = new TabItem(folder, SWT.NONE);
        item.setText(text);
        return item;
    }

    private static TabFolderTheme theme() {
        return TabFolderTheme.get();
    }

    @Test
    void tabsRunLeftToRightInOneRowAndANarrowerTextMakesANarrowerTab() {
        TabFolder folder = folder(SWT.TOP);
        TabItem a = tab(folder, "a");
        TabItem b = tab(folder, "A much longer caption");
        TabItem c = tab(folder, "c");
        assertThat(a.getBounds().x).isZero();
        assertThat(b.getBounds().x).isEqualTo(a.getBounds().x + a.getBounds().width);
        assertThat(c.getBounds().x).isEqualTo(b.getBounds().x + b.getBounds().width);
        assertThat(b.getBounds().width).isGreaterThan(a.getBounds().width);
        assertThat(a.getBounds().y).isEqualTo(b.getBounds().y);
        assertThat(a.getBounds().height).isEqualTo(theme().tabHeight());
        assertThat(a.getBounds().height + a.getBounds().y).isLessThanOrEqualTo(folder.getClientArea().y);
    }

    @Test
    void anImageWidensItsTab() {
        TabFolder folder = folder(SWT.TOP);
        TabItem plain = tab(folder, "same");
        TabItem withImage = tab(folder, "same");
        Image image = new Image(DartMocks.dartDisplay(), 16, 16);
        withImage.setImage(image);
        assertThat(withImage.getBounds().width - plain.getBounds().width).isEqualTo(theme().tabImageExtra());
    }

    @Test
    void anImageMakesEveryTabAndTheStripAsTallAsTheTabThatHoldsIt() {
        TabFolder folder = folder(SWT.TOP);
        TabItem plain = tab(folder, "plain");
        int stripWithoutImage = folder.getClientArea().y;
        tab(folder, "image").setImage(new Image(DartMocks.dartDisplay(), 16, 16));
        int grown = theme().tabHeightWithImage() - theme().tabHeight();
        assertThat(plain.getBounds().height).isEqualTo(theme().tabHeightWithImage());
        assertThat(folder.getClientArea().y).isEqualTo(stripWithoutImage + grown);
    }

    @Test
    void theTabAtAPointIsTheOneWhoseBoundsHoldIt() {
        TabFolder folder = folder(SWT.TOP);
        TabItem a = tab(folder, "First");
        TabItem b = tab(folder, "Second");
        Rectangle ra = a.getBounds();
        Rectangle rb = b.getBounds();
        assertThat(folder.getItem(new Point(ra.x + 1, ra.y + 1))).isSameAs(a);
        assertThat(folder.getItem(new Point(rb.x + rb.width - 1, rb.y + rb.height - 1))).isSameAs(b);
        assertThat(folder.getItem(new Point(rb.x + rb.width + 50, rb.y + 1))).isNull();
        assertThat(folder.getItem(new Point(5, folder.getClientArea().y + 20))).isNull();
    }

    @Test
    void bottomTabsSitBelowThePageAndTheClientAreaStartsAtTheTop() {
        TabFolder bottom = folder(SWT.BOTTOM);
        TabItem item = tab(bottom, "Bottom");
        Rectangle client = bottom.getClientArea();
        int border = theme().bodyBorder();
        assertThat(client).isEqualTo(new Rectangle(border, border, 400 - 2 * border, 200 - theme().tabStripHeight() - border));
        assertThat(item.getBounds().y).isGreaterThanOrEqualTo(client.y + client.height);
        assertThat(item.getBounds().y + item.getBounds().height).isEqualTo(200);
        assertThat(bottom.computeTrim(client.x, client.y, client.width, client.height)).isEqualTo(new Rectangle(0, 0, 400, 200));
        assertThat(bottom.getItem(new Point(1, 200 - 2))).isSameAs(item);
    }

    @Test
    void topTabsTakeTheRoomAboveThePage() {
        TabFolder top = folder(SWT.TOP);
        Rectangle client = top.getClientArea();
        int border = theme().bodyBorder();
        assertThat(client).isEqualTo(new Rectangle(border, theme().tabStripHeight(), 400 - 2 * border,
                200 - theme().tabStripHeight() - border));
        assertThat(top.computeTrim(client.x, client.y, client.width, client.height)).isEqualTo(new Rectangle(0, 0, 400, 200));
    }

    @Test
    void anEmptyFolderMeasuresTheDefaultPageSizeAndTheStrip() {
        TabFolder empty = folder(SWT.TOP);
        Point size = empty.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        assertThat(size).isEqualTo(new Point(DartWidget.DEFAULT_WIDTH, DartWidget.DEFAULT_HEIGHT + theme().tabStripHeight() + theme().bodyBorder()));
        assertThat(empty.computeSize(100, SWT.DEFAULT).x).isEqualTo(100);
    }

    @Test
    void theSelectedPageFillsTheClientAreaWhenItIsSet() {
        TabFolder folder = folder(SWT.TOP);
        TabItem item = new TabItem(folder, SWT.NONE);
        Composite page = new Composite(folder, SWT.NONE);
        item.setControl(page);
        Rectangle client = folder.getClientArea();
        assertThat(client.height).isGreaterThan(0);
        assertThat(page.getBounds()).isEqualTo(client);
    }
}
