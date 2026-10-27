package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * A wrapping help panel as an application builds one: a {@link ScrolledForm} laid out with
 * {@link TableWrapLayout}, holding a {@link FormText}. Prints every control's bounds and where the
 * paragraph breaks.
 *
 * <p>{@code TextSegment} measures every word once and caches it for the life of the control, so a
 * scale that settles after the first layout leaves those widths behind — run it with and without
 * {@code -Dswt.autoScale=150} and the breaks must be identical.
 *
 * <pre>
 * ./gradlew :swt-evolve:examples:runDeskExample -PmainClass=dev.equo.FormTextGuideSnippet
 * </pre>
 */
public class FormTextGuideSnippet {

    private static final String PARAGRAPH =
            "This guide provides you with a dynamic listing of workflows for the currently open "
            + "perspective. The following workflows will help you get started using the modelling "
            + "tools of this release.";

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Form text guide");
        shell.setLayout(new FillLayout());

        FormToolkit toolkit = new FormToolkit(display);
        ScrolledForm form = toolkit.createScrolledForm(shell);
        TableWrapLayout layout = new TableWrapLayout();
        form.getBody().setLayout(layout);

        FormText text = toolkit.createFormText(form.getBody(), true);
        text.setText("<form><p>" + PARAGRAPH + "</p></form>", true, false);
        text.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

        shell.setSize(600, 320);
        shell.open();
        display.timerExec(3000, () -> dump(shell, text));
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        toolkit.dispose();
        display.dispose();
    }

    private static void dump(Shell shell, FormText text) {
        if (shell.isDisposed()) return;
        print(shell, 0);
        Rectangle b = text.getBounds();
        System.out.println("[guide] formText bounds=" + b + " clientArea=" + text.getClientArea()
                + " xInShell=" + shell.toControl(text.toDisplay(0, 0)).x
                + " deviceZoom=" + org.eclipse.swt.internal.DPIUtil.getDeviceZoom());
    }

    private static void print(Control c, int depth) {
        System.out.println("[guide] " + "  ".repeat(depth) + c.getClass().getName() + " " + c.getBounds());
        if (c instanceof Composite) {
            Composite composite = (Composite) c;
            System.out.println("[guide] " + "  ".repeat(depth) + "  client=" + composite.getClientArea()
                    + " layout=" + (composite.getLayout() == null ? "null"
                            : composite.getLayout().getClass().getSimpleName()));
            for (Control child : composite.getChildren()) print(child, depth + 1);
        }
    }
}
