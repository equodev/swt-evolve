package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A spinner is laid out through {@code computeSize}, and its up/down stepper is drawn at the right
 * edge of whatever width that returns. A width that ignores the hint therefore does not merely
 * mis-size the control: it pushes the stepper outside the row it was laid out in, where it is
 * clipped away and the increment/decrement affordance is lost.
 */
@ExtendWith(Mocks.class)
class SpinnerWidthHintTest {

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
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    void widthHintSizesTheNumberAndTheStepperIsAddedOutsideIt() {
        Shell shell = Mocks.shell();
        Spinner spinner = new Spinner(shell, SWT.BORDER);
        spinner.setMaximum(1000);

        int hinted = spinner.computeSize(40, SWT.DEFAULT).x;

        assertThat(hinted).isGreaterThan(40);
        assertThat(hinted).isLessThan(spinner.computeSize(80, SWT.DEFAULT).x);
    }

    @Test
    void naturalWidthFollowsTheWidestValueTheRangeCanShow() {
        Shell shell = Mocks.shell();
        Spinner narrow = new Spinner(shell, SWT.BORDER);
        narrow.setMaximum(9);
        Spinner wide = new Spinner(shell, SWT.BORDER);
        wide.setMaximum(1000000);

        assertThat(wide.computeSize(SWT.DEFAULT, SWT.DEFAULT).x)
                .isGreaterThan(narrow.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
    }

    /**
     * The shape a dialog row takes around a spinner: a label and the spinner in a two-column grid,
     * the spinner constrained by {@code GridData.widthHint}. The dialog is sized from the row's
     * preferred width, so a spinner laid out at a constant width regardless of its hint takes the
     * row - and with it the dialog - wider than the hint asked for.
     * <p>
     * Asserted against the spinner alone. The row's own geometry is not a safe reference here:
     * {@code Sizes.getClientArea(DartScrollable)} deducts a visible scroll bar that
     * {@code computeTrim} never added, so a composite's client area and its preferred size
     * disagree by the bar width wherever one happens to exist.
     */
    @Test
    void aNarrowerWidthHintLaysTheSpinnerOutNarrower() {
        assertThat(layOutHintedSpinner(40)).isLessThan(layOutHintedSpinner(80));
    }

    private static int layOutHintedSpinner(int widthHint) {
        Shell shell = Mocks.shell();
        Composite row = new Composite(shell, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        row.setLayout(layout);

        new Label(row, SWT.NONE).setText("Items per batch");

        Spinner spinner = new Spinner(row, SWT.BORDER);
        GridData data = new GridData();
        data.widthHint = widthHint;
        spinner.setLayoutData(data);
        spinner.setTextLimit(4);
        spinner.setMaximum(1000);

        row.setSize(row.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        row.layout();

        Rectangle bounds = spinner.getBounds();
        assertThat(bounds.width).isEqualTo(spinner.computeSize(widthHint, SWT.DEFAULT).x);
        return bounds.width;
    }
}
