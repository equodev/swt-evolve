package dev.equo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageFileNameProvider;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * A collapsed {@link ExpandableComposite} on a {@link ScrolledForm}, expanded at runtime, whose client
 * is an adapted composite holding a bar of two image buttons over an empty bordered {@link List} sized
 * to four lines. The add and the disabled remove buttons load their images from the application's own
 * files {@code add.png} and {@code remove.ico}, through an {@link ImageFileNameProvider} as JFace's URL
 * image descriptors do.
 *
 * <p>Native draws the button bar on the default background, since only the outer client is adapted to
 * the toolkit, each button with its own image, and the list as a box under the bar.
 *
 * <pre>
 * ./gradlew :swt-evolve:examples:runExample -PmainClass=dev.equo.SectionButtonBarListSnippet
 * ./gradlew :swt-evolve:examples:runDeskExample -PmainClass=dev.equo.SectionButtonBarListSnippet
 * </pre>
 */
public class SectionButtonBarListSnippet {

    public static void main(String[] args) throws IOException {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Section button bar list");
        shell.setLayout(new GridLayout());

        FormToolkit toolkit = new FormToolkit(display);
        ScrolledForm form = toolkit.createScrolledForm(shell);
        form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Composite body = form.getBody();
        body.setLayout(new GridLayout());

        toolkit.createLabel(body, "Details");

        ExpandableComposite attachments = toolkit.createExpandableComposite(body, ExpandableComposite.TWISTIE);
        attachments.setText("Attachments");
        attachments.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                form.reflow(true);
            }
        });

        Composite client = new Composite(attachments, SWT.NONE);
        GridLayout clientLayout = new GridLayout(1, false);
        clientLayout.verticalSpacing = 0;
        clientLayout.marginHeight = 0;
        clientLayout.marginWidth = 0;
        client.setLayout(clientLayout);

        Composite buttonBar = new Composite(client, SWT.NONE);
        GridLayout barLayout = new GridLayout(2, false);
        barLayout.marginHeight = 0;
        buttonBar.setLayout(barLayout);
        buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        String addPng = copyResourceAs("view_menu.png", "add.png");
        Image addImage = new Image(display, (ImageFileNameProvider) zoom -> zoom == 100 ? addPng : null);
        String removeIco = copyResourceAs("remove.ico", "remove.ico");
        Image removeImage = new Image(display, (ImageFileNameProvider) zoom -> zoom == 100 ? removeIco : null);
        Button add = new Button(buttonBar, SWT.NONE);
        add.setImage(addImage);
        Button remove = new Button(buttonBar, SWT.NONE);
        remove.setImage(removeImage);
        remove.setEnabled(false);

        List list = new List(client, SWT.BORDER | SWT.V_SCROLL);
        int fontHeight = Math.round(list.getFont().getFontData()[0].height);
        GridData listData = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
        listData.heightHint = 4 * (fontHeight + 2);
        listData.widthHint = 300;
        list.setLayoutData(listData);

        toolkit.adapt(client, true, true);
        attachments.setClient(client);

        ExpandableComposite more = toolkit.createExpandableComposite(body, ExpandableComposite.TWISTIE);
        more.setText("More");
        more.setClient(toolkit.createLabel(more, "Nothing here"));

        shell.setSize(500, 400);
        shell.open();
        display.timerExec(1500, () -> {
            attachments.setExpanded(true);
            form.reflow(true);
        });
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        addImage.dispose();
        removeImage.dispose();
        toolkit.dispose();
        display.dispose();
    }

    /** Copies the bundled resource {@code resource} to a temporary file named {@code fileName}. */
    private static String copyResourceAs(String resource, String fileName) throws IOException {
        try (InputStream is = SectionButtonBarListSnippet.class.getResourceAsStream("/" + resource)) {
            if (is == null) throw new IllegalArgumentException("Resource not found: " + resource);
            Path file = Files.createTempDirectory("swt-evolve-images-").resolve(fileName);
            Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
            file.toFile().deleteOnExit();
            return file.toAbsolutePath().toString();
        }
    }
}
