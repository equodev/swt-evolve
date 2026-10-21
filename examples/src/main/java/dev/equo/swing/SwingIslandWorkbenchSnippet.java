package dev.equo.swing;

import dev.equo.swt.Config;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A workbench the size of a snippet: two tab stacks side by side, a Swing island per tab, and a
 * tab you can drag from one stack to the other. It exists to reproduce, on this machine, the
 * defects measured on an application we cannot run here — each of which needed a shape no
 * existing snippet had.
 *
 * <p><b>What each piece is here to reproduce</b>, with what was measured:
 * <ul>
 *   <li><b>Tabs, one island visible at a time.</b> Selecting a tab moved the tab strip's highlight
 *       and nothing else: the pane kept the previous tab's pixels, a click and sixteen wheel
 *       events inside it were answered by the <em>previous</em> tab's window — which emitted
 *       frames and visibly zoomed — and the newly selected window emitted nothing at all, its
 *       island reporting a 0x0 box, never laid out.</li>
 *   <li><b>Two stacks and a drag between them.</b> With two panes side by side exactly one had
 *       content at any moment, and which one changed with a layout change. The blank one's window
 *       was <em>not</em> silent: it emitted op-lists throughout, and they landed — an overlay
 *       painted into the empty pane — while the view's own imagery never arrived. Dragged back
 *       into one stack, it rendered completely.</li>
 *   <li><b>SWT chrome outside the embedded composite</b> ({@link #chrome}). On the application the
 *       island's box was the canvas only, with a colour-map panel and an icon toolbar beside it at
 *       window-x 10..260 — outside the island entirely. They kept rendering while the canvas did
 *       not, which is how we learned they were never in it.</li>
 *   <li><b>A tab that is never shown</b> (the last one). One island there mounted, answered 0x0
 *       below the window's bottom edge, emitted a single empty frame and was never heard from
 *       again.</li>
 * </ul>
 *
 * <p>The views themselves carry the rest: {@link MapLikeView} for Java2D content and the XOR
 * rubber band that the black ghosts were reported on, {@link SectionLikeView} for a canvas with
 * the ruler, scale bar, scrollbars and zoom controls that went missing together, and
 * {@link GlLikeCanvas} for the heavyweight, expose-driven, off-screen-image canvas the 3D view
 * looks like from our side.
 *
 * <pre>
 * ./gradlew :swing-island:runIslandDeskExample -PmainClass=dev.equo.swing.SwingIslandWorkbenchSnippet
 * </pre>
 *
 * <p>Add {@code -Ddev.equo.swing.traceFrames=true} to the JVM for the per-window frame trace, which
 * is what turns "the pane is blank" into "window 2 emitted three op-lists and none of them was the
 * map". Without the island launch this runs as an ordinary SWT_AWT application, which is the
 * comparison every one of these defects is measured against.
 */
public class SwingIslandWorkbenchSnippet {

    public static void main(String[] args) {
        // -Dworkbench.plainSwing=true runs this as an ordinary SWT_AWT application instead, which
        // is the oracle every defect reproduced here is measured against: a behaviour that is the
        // same in both is Swing's, not ours, and three hypotheses have died on that distinction.
        if (!Boolean.getBoolean("workbench.plainSwing")) {
            Config.forceEquo();
        } else {
            System.out.println("[workbench] plain SWT_AWT — this run is the oracle, not the engine");
        }

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SwingIslandWorkbenchSnippet");
        shell.setLayout(new GridLayout(1, false));

        SashForm sash = new SashForm(shell, SWT.HORIZONTAL);
        sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        CTabFolder left = stack(sash, "left");
        CTabFolder right = stack(sash, "right");

        tab(left, "Map 1", () -> new MapLikeView("Map 1", 0));
        tab(left, "Section 2", () -> new SectionLikeView("Section 2"));
        tab(left, "Cube 3", () -> glPanel("Cube 3"));
        // Never selected, and that is the point: the island it holds is the one that mounts,
        // answers a box it was never laid out into, emits one empty frame and goes quiet.
        tab(left, "Map 4 (never shown)", () -> new MapLikeView("Map 4", 7));

        left.setSelection(0);
        // The right-hand stack starts empty: dragging a tab into it is the split.
        sash.setWeights(new int[] {1, 1});
        right.setSelection(-1);

        shell.setSize(1100, 700);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    private static CTabFolder stack(Composite parent, String side) {
        CTabFolder folder = new CTabFolder(parent, SWT.BORDER);
        folder.setData("side", side);
        // The only line that can say whether a selection made on the client reached Java at all,
        // which is the question the application left open: its tab strip changed and nothing else.
        folder.addListener(SWT.Selection, e -> {
            CTabItem item = folder.getSelection();
            System.out.println("[workbench] Java sees a selection in the " + side + " stack: "
                    + (item == null ? "none" : item.getText()));
        });
        dragAndDrop(folder, side);
        return folder;
    }

    /**
     * A tab: SWT chrome on the left, the embedded composite beside it, and one AWT frame inside.
     *
     * <p>The chrome is outside the embedded composite on purpose — see the class comment. It is
     * also how you tell, by looking, whether a pane has lost its island or lost everything.
     */
    private static void tab(CTabFolder folder, String name, java.util.function.Supplier<?> view) {
        CTabItem item = new CTabItem(folder, SWT.NONE);
        item.setText(name);

        Composite page = new Composite(folder, SWT.NONE);
        page.setLayout(new GridLayout(2, false));
        item.setControl(page);
        chrome(page, name);

        Composite embedded = new Composite(page, SWT.EMBEDDED);
        embedded.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        embedded.addListener(SWT.Resize, e -> System.out.println("[workbench] " + name
                + " island laid out at " + embedded.getBounds()));

        Frame frame = SWT_AWT_newFrame(embedded);
        Object built = view.get();
        if (built instanceof GlLikeCanvas) {
            frame.add((GlLikeCanvas) built);
        } else if (built instanceof MapLikeView map) {
            // The status line is a component of its own, below the map and inside the same AWT
            // window — the arrangement the application has, and the one that found the defect this
            // was built for. Swing erases immediate drawing per COMPONENT, so a label repainting
            // leaves the canvas's pixels where they are; the engine used to drop the whole window's
            // accumulated overlay for any repaint in it, so the band went with the label and the
            // next XOR erase had nothing left to cancel against. It now decides by what the
            // repaint's clip actually covers, so this arrangement is the regression case: drag a
            // band, let the status line update under it, and the band must stay.
            JPanel withStatus = new JPanel(new BorderLayout());
            javax.swing.JLabel status = new javax.swing.JLabel(" ");
            status.setOpaque(true);
            map.statusGoesTo(text -> {
                status.setText(text);
                status.repaint(); // the label only — never the canvas
            });
            withStatus.add(map, BorderLayout.CENTER);
            withStatus.add(status, BorderLayout.SOUTH);
            frame.add(withStatus);
            // The strip says when it is given a press or a release, because once it was given one
            // that belonged to the canvas: a release posted below the window's edge went to
            // whatever was under the pointer, and the gesture the canvas had started never ended.
            listen(status, name + " status strip");
        } else {
            frame.add((javax.swing.JComponent) built);
        }
        frame.validate();
    }

    /** Says when a component receives a press or a release, and at what point. */
    private static void listen(java.awt.Component c, String who) {
        c.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                System.out.println("[who] " + who + " PRESSED at " + e.getX() + "," + e.getY());
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                System.out.println("[who] " + who + " RELEASED at " + e.getX() + "," + e.getY());
            }
        });
    }

    /** The colour-map panel and the icon toolbar, in SWT, outside the island. */
    private static void chrome(Composite page, String name) {
        Composite column = new Composite(page, SWT.NONE);
        column.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));
        column.setLayout(new GridLayout(1, false));
        Label title = new Label(column, SWT.NONE);
        title.setText("Default ColorMap");
        ToolBar bar = new ToolBar(column, SWT.VERTICAL | SWT.FLAT);
        for (String icon : new String[] {"⌖", "⤢", "⟳", "◧", "▦", "✎"}) {
            ToolItem toolItem = new ToolItem(bar, SWT.PUSH);
            toolItem.setText(icon);
            toolItem.addListener(SWT.Selection, e -> System.out.println(
                    "[workbench] " + name + " chrome tool " + icon + " pressed"));
        }
    }

    /**
     * A {@code GlLikeCanvas} in a panel, because a heavyweight canvas inside a Swing container is
     * the arrangement the application uses and the one the adoption path was written for.
     */
    private static Object glPanel(String name) {
        return new GlLikeCanvasPanel(name);
    }

    /**
     * Dragging a tab from one stack to the other, which re-parents its page — the gesture that
     * produced the side-by-side state where exactly one pane had content.
     */
    private static void dragAndDrop(CTabFolder folder, String side) {
        Transfer[] types = {TextTransfer.getInstance()};
        DragSource source = new DragSource(folder, DND.DROP_MOVE);
        source.setTransfer(types);
        source.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent event) {
                CTabItem item = folder.getSelection();
                event.data = item == null ? "" : item.getText();
            }
        });

        DropTarget target = new DropTarget(folder, DND.DROP_MOVE);
        target.setTransfer(types);
        target.addDropListener(new DropTargetAdapter() {
            @Override
            public void drop(DropTargetEvent event) {
                String text = String.valueOf(event.data);
                CTabFolder from = otherStack(folder);
                if (from == null) {
                    return;
                }
                for (CTabItem item : from.getItems()) {
                    if (!item.getText().equals(text)) {
                        continue;
                    }
                    Composite page = (Composite) item.getControl();
                    System.out.println("[workbench] moving " + text + " into the " + side
                            + " stack: re-parenting its page");
                    item.setControl(null);
                    item.dispose();
                    page.setParent(folder);
                    CTabItem moved = new CTabItem(folder, SWT.NONE);
                    moved.setText(text);
                    moved.setControl(page);
                    folder.setSelection(moved);
                    folder.getParent().layout(true, true);
                    return;
                }
            }
        });
    }

    /** The other stack in the same {@link SashForm}. */
    private static CTabFolder otherStack(CTabFolder folder) {
        for (org.eclipse.swt.widgets.Control sibling : folder.getParent().getChildren()) {
            if (sibling != folder && sibling instanceof CTabFolder other) {
                return other;
            }
        }
        return null;
    }

    /** Indirect so the import stays out of the class's own namespace in generated javadoc. */
    private static Frame SWT_AWT_newFrame(Composite embedded) {
        return org.eclipse.swt.awt.SWT_AWT.new_Frame(embedded);
    }
}
