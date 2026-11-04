package dev.equo.jface;

import dev.equo.swt.Config;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.LineNumberChangeRulerColumn;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A short document in a ProjectionViewer with the columns a text editor shows: annotations,
 * line numbers with quick diff, folding, and an overview ruler. Every ruler should be a plain
 * column down to its bottom edge.
 *
 * The rulers' bottom rows only go blank at a fractional device pixel ratio, such as 1.25, 1.6 or
 * 1.8 with a 2000x1127 browser window: at 1 or 2 every ruler is filled to its edge.
 *
 * Run with:
 *   ./gradlew :examples:runWebExample -PmainClass=dev.equo.jface.RulerBottomSnippet
 *
 * RULER_HEIGHT=<px> in the environment gives the editor a fixed height; RULER_RESIZE=<px> changes
 * it to that height eight seconds after opening, as an editor does when its part is laid out again.
 * RULER_MAXIMIZE=1 opens the shell maximized, so the rulers take the browser window's height.
 */
public class RulerBottomSnippet {

    public static void main(String[] args) throws ClassNotFoundException {
        Config.useEquo(StyledText.class);
        Config.useEquo(Class.forName("org.eclipse.swt.custom.StyledTextRenderer"));

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Ruler Bottom Snippet");
        GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = layout.verticalSpacing = 0;
        shell.setLayout(layout);
        shell.setSize(700, 500);

        Color rulerBg = new Color(40, 42, 54);
        IAnnotationAccess access = new IAnnotationAccess() {
            @Override public Object getType(Annotation annotation) { return annotation.getType(); }
            @Override public boolean isMultiLine(Annotation annotation) { return true; }
            @Override public boolean isTemporary(Annotation annotation) { return false; }
        };
        ISharedTextColors colors = new ISharedTextColors() {
            @Override public Color getColor(RGB rgb) { return new Color(rgb); }
            @Override public void dispose() { }
        };

        CompositeRuler ruler = new CompositeRuler();
        ruler.addDecorator(0, new AnnotationRulerColumn(new AnnotationModel(), 12, access));
        LineNumberChangeRulerColumn lineNumbers = new LineNumberChangeRulerColumn(colors);
        lineNumbers.setBackground(rulerBg);
        lineNumbers.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
        ruler.addDecorator(1, lineNumbers);

        OverviewRuler overview = new OverviewRuler(access, 14, colors);
        ProjectionViewer viewer = new ProjectionViewer(shell, ruler, overview, true,
                SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        int height = envInt("RULER_HEIGHT", SWT.DEFAULT);
        GridData editorData = new GridData(SWT.FILL, height == SWT.DEFAULT ? SWT.FILL : SWT.TOP, true, height == SWT.DEFAULT);
        editorData.heightHint = height;
        viewer.getControl().setLayoutData(editorData);
        ProjectionSupport projection = new ProjectionSupport(viewer, access, colors);
        projection.install();
        viewer.setDocument(new Document("/**\n *\n */\nmodule test {\n}\n"), new AnnotationModel());
        viewer.doOperation(ProjectionViewer.TOGGLE);

        // Dark rulers, so a block the background fill doesn't reach stands out.
        viewer.getTextWidget().setBackground(new Color(30, 31, 40));
        viewer.getTextWidget().setForeground(new Color(220, 220, 220));
        ruler.getControl().setBackground(rulerBg);
        for (Control column : ((Composite) ruler.getControl()).getChildren())
            column.setBackground(rulerBg);
        overview.getControl().setBackground(rulerBg);
        overview.getHeaderControl().setBackground(rulerBg);

        int resize = envInt("RULER_RESIZE", 0);
        if (resize != 0) {
            display.timerExec(8000, () -> {
                editorData.heightHint = resize;
                shell.layout(true, true);
            });
        }
        if (envInt("RULER_MAXIMIZE", 0) != 0) shell.setMaximized(true);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static int envInt(String name, int fallback) {
        String value = System.getenv(name);
        return value == null ? fallback : Integer.parseInt(value);
    }
}
