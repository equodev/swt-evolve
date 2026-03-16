package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.swtShell;

@ExtendWith(Mocks.class)
@ExtendWith(MockFlutterBridge.Extension.class)
public class MoveAboveBelowTest {

    @BeforeEach
    void setup() {
        Config.defaultToEquo();
        Config.useEquo(Composite.class);
    }

    @AfterEach
    void reset() {
        System.clearProperty("dev.equo.swt.Composite");
    }

    @Test
    public void moveAbove_movesControlBeforeTarget() {
        Composite composite = new Composite(swtShell(), SWT.NONE);
        Label label = new Label(composite, SWT.NONE);
        Button button = new Button(composite, SWT.NONE);
        Composite child = new Composite(composite, SWT.NONE);

        assertThat(composite.getChildren()).as("initial order").containsExactly(label, button, child);

        child.moveAbove(button);
        assertThat(composite.getChildren()).as("after child.moveAbove(button)").containsExactly(label, child, button);
    }

    @Test
    public void moveAbove_null_movesToTop() {
        Composite composite = new Composite(swtShell(), SWT.NONE);
        Label label = new Label(composite, SWT.NONE);
        Button button = new Button(composite, SWT.NONE);
        Composite child = new Composite(composite, SWT.NONE);

        child.moveAbove(null);
        assertThat(composite.getChildren()).as("after child.moveAbove(null)").containsExactly(child, label, button);
    }

    @Test
    public void moveBelow_movesControlAfterTarget() {
        Composite composite = new Composite(swtShell(), SWT.NONE);
        Label label = new Label(composite, SWT.NONE);
        Button button = new Button(composite, SWT.NONE);
        Composite child = new Composite(composite, SWT.NONE);

        label.moveBelow(button);
        assertThat(composite.getChildren()).as("after label.moveBelow(button)").containsExactly(button, label, child);
    }

    @Test
    public void moveBelow_null_movesToBottom() {
        Composite composite = new Composite(swtShell(), SWT.NONE);
        Label label = new Label(composite, SWT.NONE);
        Button button = new Button(composite, SWT.NONE);
        Composite child = new Composite(composite, SWT.NONE);

        label.moveBelow(null);
        assertThat(composite.getChildren()).as("after label.moveBelow(null)").containsExactly(button, child, label);
    }
}