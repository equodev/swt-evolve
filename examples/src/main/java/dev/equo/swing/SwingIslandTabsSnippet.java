package dev.equo.swing;

import dev.equo.swt.Config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Swing islands in a {@link CTabFolder}: one AWT frame per tab, only one of them shown at a time.
 *
 * <p>{@link SwingIslandSnippet} stacks its islands in a {@code GridLayout}, where all of them are
 * laid out and all of them are visible, so it cannot see what this is for: <b>what happens to an
 * island when its tab is not the selected one, and what happens when it becomes selected.</b> That
 * is the shape an Eclipse editor area has, and the shape the defect this was written for lives in.
 *
 * <p>Measured on an application with three editor parts, each hosting an island, driven through
 * the client: selecting a tab moved the tab strip's highlight and nothing else. The pane kept
 * showing the previous tab's pixels; a click and sixteen wheel events inside it were answered by
 * the <em>previous</em> tab's window, which emitted frames and visibly zoomed; the newly selected
 * window emitted nothing at all, before or after, and its island reported a 0x0 box — never laid
 * out. Each tab here paints a flat colour and its own name so that "which window owns this pane"
 * is answerable by looking, and each logs its paints so it is answerable from the console too.
 *
 * <pre>
 * ./gradlew :swing-island:runIslandDeskExample -PmainClass=dev.equo.swing.SwingIslandTabsSnippet
 * </pre>
 *
 * <p>{@code -Dtabs=N} for a different number. Without the island launch this runs as an ordinary
 * SWT_AWT application and the tabs behave the way the toolkit has always made them behave, which
 * is the comparison that matters.
 */
public class SwingIslandTabsSnippet {

    private static final Color[] COLOURS = {
        new Color(0xB0, 0x30, 0x30), new Color(0x30, 0x70, 0xB0), new Color(0x30, 0x90, 0x50),
        new Color(0xA0, 0x70, 0x20), new Color(0x70, 0x40, 0x90),
    };

    public static void main(String[] args) {
        Config.forceEquo();

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SwingIslandTabsSnippet");
        shell.setLayout(new FillLayout());

        CTabFolder folder = new CTabFolder(shell, SWT.BORDER);
        int tabs = Math.max(2, Integer.getInteger("tabs", 3));
        for (int i = 0; i < tabs; i++) {
            addTab(folder, i);
        }
        folder.setSelection(0);
        // Said out loud: the question is whether a selection on the client reaches Java at all,
        // and this is the only line that can answer it from the host's side.
        folder.addListener(SWT.Selection, e -> {
            CTabItem item = folder.getSelection();
            System.out.println("[tabs] Java sees a selection: "
                    + (item == null ? "none" : item.getText()));
        });

        shell.setSize(720, 520);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static void addTab(CTabFolder folder, int index) {
        String name = "Island " + (index + 1);
        CTabItem item = new CTabItem(folder, SWT.NONE);
        item.setText(name);

        Composite embedded = new Composite(folder, SWT.EMBEDDED);
        item.setControl(embedded);
        // The box the island is given, printed whenever it changes: an island that is never laid
        // out reports 0x0, and that is the difference between "not selected" and "not working".
        embedded.addListener(SWT.Resize, e -> System.out.println("[tabs] " + name
                + " laid out at " + embedded.getBounds()));

        Frame frame = SWT_AWT.new_Frame(embedded);
        frame.add(new Flat(name, COLOURS[index % COLOURS.length]));
        frame.validate();
    }

    /** A panel that is nothing but its own colour and its own name, and says when it paints. */
    private static final class Flat extends JPanel {
        private final String name;
        private int paints;

        Flat(String name, Color colour) {
            super(new BorderLayout());
            this.name = name;
            setBackground(colour);
            JLabel label = new JLabel(name, SwingConstants.CENTER);
            label.setForeground(Color.WHITE);
            label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
            add(label, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            paints++;
            System.out.println("[tabs] " + name + " painted " + paints + " time(s) at "
                    + getWidth() + "x" + getHeight());
        }
    }
}
