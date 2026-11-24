package dev.equo.jface;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.MultipleHyperlinkPresenter;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * A text viewer whose word {@code target} carries two hyperlinks. Hold Ctrl/Cmd and hover it: JFace
 * shows the choice list (a Shell with a headerless Table). Clicking "Go to first line" or
 * "Go to last line" must close the list and select that line in the editor.
 *
 * The console prints each mouse event the list's Table gets, the row under the pointer against the
 * selected row (JFace opens the link only when they match), each link opened, and the editor's
 * selection afterwards.
 */
public class HyperlinkListSnippet {

    /** Names the link opened last and the text it selected, for a test to read. */
    private static Label status;

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Hyperlink list");
        shell.setLayout(new GridLayout());

        // The first word is a link too, so a test finds it at the text's top-left corner.
        StringBuilder text = new StringBuilder("target of two links\nfirst line\n");
        for (int i = 0; i < 10; i++)
            text.append("call target here ").append(i).append('\n');
        text.append("last line");
        Document document = new Document(text.toString());

        SourceViewer viewer = new SourceViewer(shell, null, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        viewer.configure(new SourceViewerConfiguration() {
            @Override
            public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
                return new IHyperlinkDetector[] {new TwoTargetDetector(document)};
            }

            @Override
            public IHyperlinkPresenter getHyperlinkPresenter(ISourceViewer sourceViewer) {
                return new MultipleHyperlinkPresenter(new RGB(0, 0, 255));
            }
        });
        viewer.setDocument(document);
        StyledText st = viewer.getTextWidget();
        st.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        status = new Label(shell, SWT.NONE);
        status.setText("No link opened");
        status.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        display.addFilter(SWT.MouseDown, e -> logTableEvent("MouseDown", e));
        display.addFilter(SWT.MouseUp, e -> logTableEvent("MouseUp", e));
        display.addFilter(SWT.Selection, e -> logTableEvent("Selection", e));
        display.addFilter(SWT.DefaultSelection, e -> logTableEvent("DefaultSelection", e));
        display.addFilter(SWT.Show, e -> {
            if (e.widget instanceof Shell s && s != shell)
                System.out.println("[popup] shown " + s.getBounds());
        });
        display.addFilter(SWT.Dispose, e -> {
            if (e.widget instanceof Shell s && s != shell)
                System.out.println("[popup] disposed");
        });
        for (int type : new int[] {SWT.FocusIn, SWT.FocusOut, SWT.Activate, SWT.Deactivate})
            display.addFilter(type, e -> System.out.println("[focus] " + typeName(e.type) + " "
                    + e.widget.getClass().getSimpleName() + " focusControl="
                    + (display.getFocusControl() == null ? "null" : display.getFocusControl().getClass().getSimpleName())));

        shell.setSize(600, 400);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
        System.out.println("[editor] final selection " + st.getSelection());
    }

    private static String typeName(int type) {
        return switch (type) {
            case SWT.FocusIn -> "FocusIn";
            case SWT.FocusOut -> "FocusOut";
            case SWT.Activate -> "Activate";
            default -> "Deactivate";
        };
    }

    private static void logTableEvent(String kind, Event e) {
        if (!(e.widget instanceof Table table))
            return;
        Point pt = new Point(e.x, e.y);
        TableItem under = table.getItem(pt);
        TableItem[] sel = table.getSelection();
        System.out.println("[table] " + kind + " button=" + e.button + " at " + pt
                + " itemHeight=" + table.getItemHeight() + " header=" + table.getHeaderHeight()
                + " under=" + (under == null ? "null" : under.getText())
                + " selected=" + (sel.length == 0 ? "none" : sel[0].getText())
                + " match=" + (under != null && sel.length > 0 && under.equals(sel[0])));
    }

    static class TwoTargetDetector extends AbstractHyperlinkDetector {
        private final Document document;

        TwoTargetDetector(Document document) {
            this.document = document;
        }

        @Override
        public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultiple) {
            String all = document.get();
            int offset = region.getOffset();
            int start = all.lastIndexOf("target", offset);
            if (start < 0 || offset > start + "target".length())
                return null;
            IRegion word = new Region(start, "target".length());
            return new IHyperlink[] {
                    new LineLink(textViewer, word, "Go to first line", all.indexOf("first line"), "first line".length()),
                    new LineLink(textViewer, word, "Go to last line", all.lastIndexOf("last line"), "last line".length())};
        }
    }

    record LineLink(ITextViewer viewer, IRegion region, String label, int offset, int length) implements IHyperlink {
        @Override
        public IRegion getHyperlinkRegion() {
            return region;
        }

        @Override
        public String getTypeLabel() {
            return null;
        }

        @Override
        public String getHyperlinkText() {
            return label;
        }

        @Override
        public void open() {
            System.out.println("[link] open " + label);
            viewer.setSelectedRange(offset, length);
            viewer.revealRange(offset, length);
            status.setText("Opened " + label + ", selected " + viewer.getTextWidget().getSelectionText());
            Display.getCurrent().asyncExec(() -> System.out.println(
                    "[editor] selection after open " + viewer.getTextWidget().getSelection()));
        }
    }
}
