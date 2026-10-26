package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A control moved by setLocation or setBounds hears a Move, as the view's frame-origin callback sends it
 * upstream, before the Resize of the same call; a call that leaves the position alone sends none.
 */
@ExtendWith(Mocks.class)
class ControlMoveEventTest {

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

    private static Composite listened(List<String> heard) {
        Shell shell = Mocks.shell();
        DartDisplay displayImpl = (DartDisplay) shell.getDisplay().getImpl();
        doAnswer(inv -> {
            Event event = inv.getArgument(1);
            if (event != null && (event.type == SWT.Move || event.type == SWT.Resize))
                return inv.callRealMethod();
            return null;
        }).when(displayImpl).sendEvent(any(EventTable.class), any(Event.class));
        Composite composite = new Composite(shell, SWT.NONE);
        composite.addListener(SWT.Move, e -> heard.add("Move"));
        composite.addListener(SWT.Resize, e -> heard.add("Resize"));
        return composite;
    }

    @Test
    @DisplayName("a new position is a Move, a new size a Resize, and the Move comes first")
    void aMoveComesBeforeTheResize() {
        List<String> heard = new ArrayList<>();
        Composite composite = listened(heard);
        composite.setLocation(5, 6);
        assertThat(heard).containsExactly("Move");
        heard.clear();
        composite.setBounds(7, 8, 30, 40);
        assertThat(heard).containsExactly("Move", "Resize");
        heard.clear();
        composite.setSize(10, 10);
        assertThat(heard).containsExactly("Resize");
    }

    @Test
    @DisplayName("the same position again sends no Move")
    void theSamePositionSendsNothing() {
        List<String> heard = new ArrayList<>();
        Composite composite = listened(heard);
        composite.setBounds(5, 6, 10, 10);
        heard.clear();
        composite.setLocation(5, 6);
        composite.setBounds(5, 6, 10, 10);
        assertThat(heard).isEmpty();
    }

    @Test
    @DisplayName("a control disposed by its Move listener hears no Resize")
    void aControlDisposedOnMoveHearsNoResize() {
        List<String> heard = new ArrayList<>();
        Composite composite = listened(heard);
        composite.addListener(SWT.Move, e -> composite.dispose());
        composite.setBounds(5, 6, 10, 10);
        assertThat(heard).containsExactly("Move");
        assertThat(composite.isDisposed()).isTrue();
    }
}
