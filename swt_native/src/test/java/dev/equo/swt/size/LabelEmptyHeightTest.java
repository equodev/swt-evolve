package dev.equo.swt.size;

import dev.equo.swt.Config;
import dev.equo.swt.MockFlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * A label without text is one line tall, as on every native platform. Layouts size rows from it
 * before its text arrives: FilteredItemsSelectionDialog shares one GridData between its
 * "Matching items:" label, the empty progress label beside it and their composite, so the row takes
 * the empty label's height.
 */
@ExtendWith(Mocks.class)
@ExtendWith(MockFlutterBridge.Extension.class)
class LabelEmptyHeightTest {

    @BeforeEach
    void setup() {
        Config.defaultToEquo();
        Config.useEquo(Label.class);
    }

    @AfterEach
    void reset() {
        System.clearProperty("dev.equo.swt.Label");
    }

    @Test
    void emptyLabel_isAsTallAsOneLine() {
        Label text = new Label(shell(), SWT.NONE);
        text.setText("&Matching items:");
        Label empty = new Label(shell(), SWT.RIGHT);

        assertThat(empty.computeSize(SWT.DEFAULT, SWT.DEFAULT).y)
                .isEqualTo(text.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
    }

    @Test
    void imageLabelWithoutText_isAsTallAsItsImage() {
        Label small = new Label(shell(), SWT.NONE);
        small.setImage(new Image(shell().getDisplay(), 8, 8));
        Label large = new Label(shell(), SWT.NONE);
        large.setImage(new Image(shell().getDisplay(), 16, 16));

        // The Dart label draws only the image, so an image shorter than a text line is not
        // raised to one.
        assertThat(small.computeSize(SWT.DEFAULT, SWT.DEFAULT).y)
                .isEqualTo(large.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - 8);
    }

    @Test
    void tinyImageLabelWithoutText_isAsTallAsItsImage() {
        Label tiny = new Label(shell(), SWT.NONE);
        tiny.setImage(new Image(shell().getDisplay(), 4, 4));
        Label large = new Label(shell(), SWT.NONE);
        large.setImage(new Image(shell().getDisplay(), 16, 16));

        assertThat(tiny.computeSize(SWT.DEFAULT, SWT.DEFAULT).y)
                .isEqualTo(large.computeSize(SWT.DEFAULT, SWT.DEFAULT).y - 12);
    }

    @Test
    void labelsSharingGridDataWithAnEmptyLabel_keepTheirTextHeight() {
        Label reference = new Label(shell(), SWT.NONE);
        reference.setText("&Matching items:");
        int oneLine = reference.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

        Composite parent = new Composite(shell(), SWT.NONE);
        parent.setLayout(new GridLayout());
        Composite row = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        row.setLayout(layout);
        Label list = new Label(row, SWT.NONE);
        list.setText("&Matching items:");
        GridData shared = new GridData(GridData.FILL_HORIZONTAL);
        list.setLayoutData(shared);
        Label progress = new Label(row, SWT.RIGHT);
        progress.setLayoutData(shared);
        row.setLayoutData(shared);

        parent.setSize(578, 430);
        parent.layout(true, true);

        assertThat(list.getBounds().height).isEqualTo(oneLine);
    }
}
