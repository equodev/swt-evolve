package dev.equo.swt.delivery;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DartWidget;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.VComposite;
import org.eclipse.swt.widgets.Widget;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A resize must send the tab folder as a change: a frame naming none ({@code _d}) carries the
 * folder's whole subtree.
 */
@ExtendWith(Mocks.class)
class CTabFolderResizeCensusTest {

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
        FlutterBridge.setSendObserver(null);
    }

    @Test
    void resizingATabFolderSendsWhatChangedRatherThanTheFolder() {
        Shell shell = Mocks.shell();
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        CTabItem item = new CTabItem(folder, SWT.NONE);
        Composite page = new Composite(folder, SWT.NONE);
        StyledText text = new StyledText(page, SWT.MULTI | SWT.V_SCROLL);
        text.setText("one\ntwo\nthree");
        item.setControl(page);
        folder.setSelection(item);
        folder.setSize(400, 300);
        settle(shell);

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            // A vertical resize, the way a sash drag delivers one.
            for (int height = 299; height > 289; height--) {
                folder.setSize(400, height);
                FlutterBridge.update();
            }

            List<DeliveryAudit.Frame> folderFrames = audit.framesOn(channelOf(folder));
            assertThat(folderFrames).as("the folder said nothing about being resized").isNotEmpty();

            long whole = folderFrames.stream().skip(1).filter(f -> !names(f)).count();
            System.out.println("PROBE CTabFolder frames: " + folderFrames.size()
                    + ", of which whole: " + whole
                    + ", bytes: " + folderFrames.stream().mapToInt(f -> f.json().length()).sum());
            for (DeliveryAudit.Frame frame : folderFrames) {
                JsonObject o = JsonParser.parseString(frame.json()).getAsJsonObject();
                System.out.println("   " + frame.json().length() + " B  _d="
                        + (o.has("_d") ? o.get("_d") : "(none — whole)"));
            }

            assertThat(whole)
                    .as("after the first delivery, resizing sent the whole folder and all under it")
                    .isZero();
        }
    }


    @Test
    void resizingWithAChangedChildStillSendsWhatChanged() {
        Shell shell = Mocks.shell();
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        CTabItem item = new CTabItem(folder, SWT.NONE);
        Composite page = new Composite(folder, SWT.NONE);
        StyledText text = new StyledText(page, SWT.MULTI | SWT.V_SCROLL);
        text.setText("one\ntwo\nthree");
        item.setControl(page);
        folder.setSelection(item);
        folder.setSize(400, 300);
        settle(shell);
        FlutterBridge.update();

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            // Everything under the folder is laid out too, so descendants are dirty in the same flush.
            for (int height = 299; height > 289; height--) {
                folder.setSize(400, height);
                text.setTopPixel(height % 7);
                FlutterBridge.update();
            }

            List<DeliveryAudit.Frame> folderFrames = audit.framesOn(channelOf(folder));
            long whole = folderFrames.stream().skip(1).filter(f -> !names(f)).count();
            int bytes = folderFrames.stream().mapToInt(f -> f.json().length()).sum();
            System.out.println("PROBE with a changed child: " + folderFrames.size()
                    + " folder frames, whole: " + whole + ", bytes: " + bytes);

            assertThat(whole)
                    .as("a resize with a changed child sent the whole folder, subtree and all")
                    .isZero();
        }
    }


    @Test
    void resizingWithAChangedTabItemStillSendsWhatChanged() {
        Shell shell = Mocks.shell();
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        CTabItem item = new CTabItem(folder, SWT.NONE);
        Composite page = new Composite(folder, SWT.NONE);
        StyledText text = new StyledText(page, SWT.MULTI | SWT.V_SCROLL);
        text.setText("one\ntwo\nthree");
        item.setControl(page);
        folder.setSelection(item);
        folder.setSize(400, 300);
        settle(shell);
        FlutterBridge.update();

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            // A CTabItem has no channel of its own, so its change travels in the folder's payload.
            for (int height = 299; height > 289; height--) {
                folder.setSize(400, height);
                item.setToolTipText("h" + height);
                FlutterBridge.update();
            }

            List<DeliveryAudit.Frame> folderFrames = audit.framesOn(channelOf(folder));
            long whole = folderFrames.stream().skip(1).filter(f -> !names(f)).count();
            int bytes = folderFrames.stream().mapToInt(f -> f.json().length()).sum();
            System.out.println("PROBE with a changed tab item: " + folderFrames.size()
                    + " folder frames, whole: " + whole + ", bytes: " + bytes);
            for (DeliveryAudit.Frame frame : folderFrames) {
                JsonObject o = JsonParser.parseString(frame.json()).getAsJsonObject();
                System.out.println("   " + frame.json().length() + " B  _d="
                        + (o.has("_d") ? o.get("_d") : "(none — whole)"));
            }

            assertThat(whole)
                    .as("a changed tab item sent the whole folder, subtree and all")
                    .isZero();
        }
    }


    @Test
    void aChangedChildUnderAnAncestorNamingChildrenTravelsOnItsOwnChannel() {
        Shell shell = Mocks.shell();
        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        CTabItem item = new CTabItem(folder, SWT.NONE);
        Composite page = new Composite(folder, SWT.NONE);
        StyledText text = new StyledText(page, SWT.MULTI | SWT.V_SCROLL);
        text.setText("one\ntwo\nthree");
        item.setControl(page);
        folder.setSelection(item);
        folder.setSize(400, 300);
        settle(shell);
        FlutterBridge.update();

        try (DeliveryAudit audit = DeliveryAudit.install()) {
            for (int height = 299; height > 289; height--) {
                folder.setSize(400, height);
                text.setTopPixel(height % 7);
                // The folder names its children among its own changes; that must not send it whole.
                ((DartWidget) folder.getImpl()).getValue().markDirty(VComposite.CHILDREN);
                FlutterBridge.update();
            }

            List<DeliveryAudit.Frame> folderFrames = audit.framesOn(channelOf(folder));
            List<DeliveryAudit.Frame> textFrames = audit.framesOn(channelOf(text));
            long whole = folderFrames.stream().skip(1).filter(f -> !names(f)).count();
            System.out.println("PROBE naming children: folder " + folderFrames.size() + " frames ("
                    + folderFrames.stream().mapToInt(f -> f.json().length()).sum() + " B), whole "
                    + whole + "; text " + textFrames.size() + " frames ("
                    + textFrames.stream().mapToInt(f -> f.json().length()).sum() + " B)");

            assertThat(textFrames)
                    .as("the changed text had no frame of its own, so an ancestor had to carry it")
                    .isNotEmpty();
            assertThat(whole)
                    .as("the folder was sent whole to carry a child that could describe itself")
                    .isZero();
        }
    }

    private static boolean names(DeliveryAudit.Frame frame) {
        return JsonParser.parseString(frame.json()).getAsJsonObject().has("_d");
    }

    private static String channelOf(Widget widget) {
        return widget.getClass().getSimpleName() + "/" + widget.hashCode();
    }

    private void settle(Widget root) {
        FlutterBridge.update();
        markSent(root);
        bridge.comm.sent.clear();
    }

    private void markSent(Widget widget) {
        widget.setData("dev.equo.swt.new", false);
        if (widget instanceof Composite composite) {
            Control[] children = composite.getChildren();
            if (children == null) return;
            for (Control child : children) markSent(child);
        }
    }
}
