package dev.equo.swt;

import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DartComposite;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The name a widget is presented under has to hold for the widget's whole life.
 *
 * <p>{@link Config#isMainSashComposite} is a structural rule - a SashLayout directly arranging
 * part-stacks - so it answers differently before and after the first part-stack is added. The
 * client files a widget under the name its first description carried and subscribes to that, so a
 * composite first described as a {@code Composite} and addressed as a {@code MainComposite}
 * afterwards is left listening on a channel nothing is sent to any more. A perspective builds its
 * sash containers empty and fills them, and a Tracker split creates one the same way.
 */
@ExtendWith(Mocks.class)
class PresentedNameIsStableTest {

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    /** Only so that building a widget has somewhere to record itself; nothing here reads it. */
    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a sash container keeps the name it was described with when its first stack arrives")
    void nameSurvivesTheFirstPartStack() {
        DartComposite sash = emptySashContainer();

        String describedAs = describe(sash);
        assertThat(describedAs)
                .as("nothing arranged by it yet, so the structural rule does not recognise it")
                .isEqualTo("Composite");

        new CTabFolder((Composite) sash.getApi(), SWT.NONE);

        assertThat(Config.presentedName(sash, "Composite"))
                .as("the client subscribed under the name it was given, and nothing tells it to "
                        + "move")
                .isEqualTo(describedAs);
    }

    @Test
    @DisplayName("one that already arranges a stack when first described keeps that name instead")
    void nameSettlesOnWhateverTheFirstDescriptionSaw() {
        DartComposite sash = emptySashContainer();
        new CTabFolder((Composite) sash.getApi(), SWT.NONE);

        assertThat(describe(sash))
                .as("described only once it is recognisable, so that is the name it settles on")
                .isEqualTo("MainComposite");
    }

    @Test
    @DisplayName("a composite with no sash layout is never renamed either way")
    void plainCompositeIsUnaffected() {
        Shell shell = Mocks.shell();
        Composite plain = new Composite(shell, SWT.NONE);
        DartComposite impl = (DartComposite) plain.getImpl();

        assertThat(describe(impl)).isEqualTo("Composite");
        new CTabFolder(plain, SWT.NONE);
        assertThat(Config.presentedName(impl, "Composite")).isEqualTo("Composite");
    }

    // ---- harness ----

    /** A SashLayout container with nothing in it yet, as a perspective and a split both build one. */
    private DartComposite emptySashContainer() {
        Shell shell = Mocks.shell();
        Composite perspective = new Composite(shell, SWT.NONE);
        Composite sash = new Composite(perspective, SWT.NONE);
        sash.setLayout(new ConfigMainSashCompositeTest.FakeSashLayout());
        return (DartComposite) sash.getImpl();
    }

    /** What one description of this widget would name it, recorded as the serializer records it. */
    private String describe(DartComposite impl) {
        String name = Config.presentedName(impl, "Composite");
        Config.describedAs(impl, name);
        return name;
    }
}
