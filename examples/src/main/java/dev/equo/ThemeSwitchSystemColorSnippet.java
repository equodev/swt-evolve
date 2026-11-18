package dev.equo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Three labels across a runtime theme switch: one with no colours of its own, one coloured from
 * {@link Display#getSystemColor} when it was created, and one made by a {@link FormToolkit}, which
 * takes its colours from the same system colours. Pick the other variant in the theme palette and
 * every label should follow it; a section title in the Forms title colour, blended for the first
 * variant, should still read. Every two seconds it prints the colours each label holds.
 *
 * <pre>
 * JAVA_TOOL_OPTIONS="-Dswt.evolve.show_theme_color_palette=true -Dswt.evolve.force_theme=light" \
 *   ./gradlew :ee-flutter:runWebExample -PmainClass=dev.equo.ThemeSwitchSystemColorSnippet
 * </pre>
 */
public class ThemeSwitchSystemColorSnippet {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Theme switch and system colours");
        shell.setLayout(new GridLayout(1, false));

        Label plain = new Label(shell, SWT.NONE);
        plain.setText("No colours of its own");

        Label system = new Label(shell, SWT.NONE);
        system.setText("Coloured from getSystemColor(COLOR_LIST_*)");
        system.setBackground(display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        system.setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));

        FormToolkit toolkit = new FormToolkit(display);
        Composite form = toolkit.createComposite(shell);
        form.setLayout(new GridLayout(1, false));
        form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Label forms = toolkit.createLabel(form, "Made by a FormToolkit");
        toolkit.createHyperlink(form, "A FormToolkit hyperlink", SWT.NONE);
        Text text = toolkit.createText(form, "A multi-line FormToolkit text", SWT.MULTI | SWT.WRAP);
        text.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        ExpandableComposite section = toolkit.createExpandableComposite(form,
                ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
        section.setText("An expandable section");
        section.setClient(toolkit.createLabel(section, "Its client"));
        ExpandableComposite blended = toolkit.createExpandableComposite(form,
                ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
        blended.setText("A title in the Forms title colour");
        blended.setTitleBarForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        blended.setClient(toolkit.createLabel(blended, "Its client"));
        Control blendedTitle = titleLabel(blended);
        Section titled = toolkit.createSection(form, Section.TITLE_BAR | Section.EXPANDED);
        titled.setText("A section with a title bar");
        titled.setClient(toolkit.createLabel(titled, "Its client"));

        shell.setSize(460, 420);
        shell.open();
        display.timerExec(2000, new Runnable() {
            public void run() {
                if (shell.isDisposed()) return;
                System.out.println("theme=" + System.getProperty("swt.evolve.force_theme")
                        + " plain=" + plain.getBackground().getRGB() + "/" + plain.getForeground().getRGB()
                        + " system=" + system.getBackground().getRGB() + "/" + system.getForeground().getRGB()
                        + " forms=" + forms.getBackground().getRGB() + "/" + forms.getForeground().getRGB()
                        + " title=" + blendedTitle.getBackground().getRGB() + "/" + blendedTitle.getForeground().getRGB()
                        + " LIST_BACKGROUND now=" + display.getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB());
                display.timerExec(2000, this);
            }
        });
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        toolkit.dispose();
        display.dispose();
    }

    /** The control an ExpandableComposite draws its title with. */
    private static Control titleLabel(ExpandableComposite section) {
        for (Control child : section.getChildren()) {
            if (child instanceof Label) return child;
        }
        return section;
    }
}
