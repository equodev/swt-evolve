package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Which tab a folder shows: the page of the selected item is the only one showing, however the
 * selection was set, an empty array leaves no tab selected, and an item that goes takes the selection with it.
 */
@Tag("native-unit")
class TabFolderSelectionNativeTest {

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

    private TabFolder folder;
    private TabItem[] items;
    private Composite[] pages;

    private void folderOfThree() {
        folder = new TabFolder(DartMocks.dartShell(), SWT.NONE);
        folder.setBounds(0, 0, 400, 200);
        items = new TabItem[3];
        pages = new Composite[3];
        for (int i = 0; i < 3; i++) {
            items[i] = new TabItem(folder, SWT.NONE);
            pages[i] = new Composite(folder, SWT.NONE);
            items[i].setControl(pages[i]);
        }
    }

    private String shown() {
        StringBuilder out = new StringBuilder();
        for (Composite page : pages)
            out.append(page.getVisible() ? '1' : '0');
        return out.toString();
    }

    @Test
    void selectingAnItemShowsItsPageAndHidesTheOthers() {
        folderOfThree();
        assertThat(shown()).isEqualTo("100");
        folder.setSelection(items[2]);
        assertThat(folder.getSelectionIndex()).isEqualTo(2);
        assertThat(shown()).isEqualTo("001");
    }

    @Test
    void selectingFromAnArrayShowsThePageOfItsFirstItem() {
        folderOfThree();
        folder.setSelection(new TabItem[] { items[1], items[2] });
        assertThat(folder.getSelectionIndex()).isEqualTo(1);
        assertThat(shown()).isEqualTo("010");
    }

    @Test
    void anEmptyArrayClearsTheSelectionAndHidesItsPage() {
        folderOfThree();
        folder.setSelection(1);
        folder.setSelection(new TabItem[0]);
        assertThat(folder.getSelectionIndex()).isEqualTo(-1);
        assertThat(folder.getSelection()).isEmpty();
        assertThat(shown()).isEqualTo("000");
        folder.setSelection(2);
        assertThat(shown()).isEqualTo("001");
    }

    @Test
    void aTabAddedAfterTheSelectionWasClearedLeavesItEmpty() {
        folderOfThree();
        folder.setSelection(new TabItem[0]);
        new TabItem(folder, SWT.NONE);
        assertThat(folder.getSelectionIndex()).isEqualTo(-1);
        assertThat(folder.getSelection()).isEmpty();
    }

    @Test
    void anIndexOutOfRangeLeavesTheSelectionAlone() {
        folderOfThree();
        folder.setSelection(1);
        folder.setSelection(-1);
        folder.setSelection(3);
        assertThat(folder.getSelectionIndex()).isEqualTo(1);
        assertThat(shown()).isEqualTo("010");
    }

    @Test
    void anItemOfAnotherFolderLeavesTheSelectionAlone() {
        folderOfThree();
        TabItem foreign = new TabItem(new TabFolder(folder.getParent(), SWT.NONE), SWT.NONE);
        folder.setSelection(foreign);
        assertThat(folder.getSelectionIndex()).isEqualTo(0);
        assertThat(shown()).isEqualTo("100");
    }

    @Test
    void aRefusedArrayLeavesTheSelectionAlone() {
        folderOfThree();
        assertThatThrownBy(() -> folder.setSelection(new TabItem[] { items[2], null }))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(folder.getSelectionIndex()).isEqualTo(0);
        assertThat(folder.getSelection()).containsExactly(items[0]);
    }

    @Test
    void removingTheSelectedItemSelectsTheOneBeforeIt() {
        folderOfThree();
        folder.setSelection(2);
        items[2].dispose();
        assertThat(folder.getSelection()).containsExactly(items[1]);
        assertThat(pages[1].getVisible()).isTrue();
        assertThat(pages[0].getVisible()).isFalse();
    }

    @Test
    void removingTheSelectedItemAnnouncesTheNewSelection() {
        folderOfThree();
        folder.setSelection(2);
        List<Widget> chosen = new ArrayList<>();
        folder.addListener(SWT.Selection, e -> chosen.add(e.item));
        try (Mocks.Delivery delivery = Mocks.deliverEvents(folder.getDisplay())) {
            items[2].dispose();
            assertThat(chosen).containsExactly(items[1]);
            items[0].dispose();
            assertThat(chosen).as("an unselected item goes quietly").containsExactly(items[1]);
        }
    }

    @Test
    void removingTheSelectedFirstItemSelectsTheNewFirst() {
        folderOfThree();
        items[0].dispose();
        assertThat(folder.getSelection()).containsExactly(items[1]);
        assertThat(pages[1].getVisible()).isTrue();
        assertThat(pages[2].getVisible()).isFalse();
    }

    @Test
    void theItemBeforeTheRemovedSelectedItemIsShown() {
        folderOfThree();
        folder.setSelection(1);
        items[1].dispose();
        assertThat(folder.getSelection()).containsExactly(items[0]);
        assertThat(pages[0].getVisible()).isTrue();
        assertThat(pages[2].getVisible()).isFalse();
    }

    @Test
    void disposingTheLastItemClearsTheSelectionAndTheNextItemTakesIt() {
        folder = new TabFolder(DartMocks.dartShell(), SWT.NONE);
        TabItem only = new TabItem(folder, SWT.NONE);
        only.dispose();
        assertThat(folder.getSelectionIndex()).isEqualTo(-1);
        assertThat(folder.getSelection()).isEmpty();
        TabItem again = new TabItem(folder, SWT.NONE);
        assertThat(folder.getSelection()).containsExactly(again);
    }
}
