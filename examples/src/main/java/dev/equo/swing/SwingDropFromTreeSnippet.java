package dev.equo.swing;

import dev.equo.swt.Config;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Drags a Tree item onto embedded Swing views. The left view receives the drop through a Swing
 * {@link DropTarget} inside the embedded frame; the right one through an SWT
 * {@link org.eclipse.swt.dnd.DropTarget} on the {@code SWT.EMBEDDED} composite. Pass
 * {@code eclipse} as the first argument to compare against native SWT.
 */
public class SwingDropFromTreeSnippet {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("eclipse")) Config.forceEclipse();
        else Config.forceEquo();

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SwingDropFromTreeSnippet");
        shell.setLayout(new FillLayout());

        Tree tree = new Tree(shell, SWT.BORDER | SWT.SINGLE);
        for (String name : new String[] {"Item A", "Item B", "Item C"}) {
            new TreeItem(tree, SWT.NONE).setText(name);
        }
        DragSource source = new DragSource(tree, DND.DROP_COPY | DND.DROP_MOVE);
        source.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        source.addDragListener(new DragSourceAdapter() {
            @Override
            public void dragSetData(DragSourceEvent e) {
                TreeItem[] sel = tree.getSelection();
                e.data = sel.length > 0 ? sel[0].getText() : "";
            }
        });

        Composite awtHost = new Composite(shell, SWT.EMBEDDED);
        Frame awtFrame = SWT_AWT.new_Frame(awtHost);
        JLabel awtLabel = view(awtFrame, "AWT DropTarget: drop here");
        new DropTarget(awtLabel, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent e) {
                System.out.println("AWT dragEnter " + e.getCurrentDataFlavorsAsList());
                e.acceptDrag(DnDConstants.ACTION_COPY);
            }

            @Override
            public void drop(DropTargetDropEvent e) {
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    Object text = e.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    System.out.println("AWT drop " + text);
                    awtLabel.setText("AWT dropped: " + text);
                    e.dropComplete(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    e.dropComplete(false);
                }
            }
        }, true);

        Composite swtHost = new Composite(shell, SWT.EMBEDDED);
        Frame swtFrame = SWT_AWT.new_Frame(swtHost);
        JLabel swtLabel = view(swtFrame, "SWT DropTarget: drop here");
        org.eclipse.swt.dnd.DropTarget target = new org.eclipse.swt.dnd.DropTarget(swtHost, DND.DROP_COPY | DND.DROP_MOVE);
        target.setTransfer(new Transfer[] {TextTransfer.getInstance()});
        target.addDropListener(new org.eclipse.swt.dnd.DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetEvent e) {
                System.out.println("SWT dragEnter");
                e.detail = DND.DROP_COPY;
            }

            @Override
            public void drop(DropTargetEvent e) {
                System.out.println("SWT drop " + e.data);
                SwingUtilities.invokeLater(() -> swtLabel.setText("SWT dropped: " + e.data));
            }
        });

        shell.setSize(900, 300);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static JLabel view(Frame frame, String text) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);
        frame.add(panel);
        frame.validate();
        return label;
    }
}
