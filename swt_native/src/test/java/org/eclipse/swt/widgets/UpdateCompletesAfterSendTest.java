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
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

/**
 * What {@link FlutterBridge#update()} returns completes once the widgets it flushed are on their way:
 * a focus request or a menu's shown event is sent when it completes, and must not reach the client
 * before the widget it is about.
 */
@ExtendWith(Mocks.class)
class UpdateCompletesAfterSendTest {

    private RecordingBridge bridge;

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
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        // The mock Display is shared by the whole suite: it is handed back as it was found.
        Display display = Mocks.display();
        doNothing().when(display).asyncExec(any(Runnable.class));
        doCallRealMethod().when(display).getThread();
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("an update made off the Display thread completes after the widget is sent there")
    void anUpdateFromAnotherThreadCompletesAfterTheSend() {
        Shell shell = Mocks.shell();
        Label label = new Label(shell, SWT.NONE);
        label.setData("dev.equo.swt.new", false);
        FlutterBridge.update();
        bridge.comm.sent.clear();

        // The Display runs on another thread: what is handed to it waits until it gets to it.
        Display display = shell.getDisplay();
        List<Runnable> handed = new ArrayList<>();
        doAnswer(inv -> handed.add(inv.getArgument(0))).when(display).asyncExec(any(Runnable.class));
        doReturn(new Thread()).when(display).getThread();

        label.setText("changed");
        CompletableFuture<Void> sent = FlutterBridge.update();

        assertThat(handed).as("the send is handed to the Display thread").isNotEmpty();
        assertThat(sent).as("nothing was sent yet").isNotDone();
        handed.forEach(Runnable::run);
        assertThat(sent).as("the send happened").isDone();
        assertThat(bridge.comm.sent.stream().filter(f -> f.event.equals("Label/" + label.hashCode()))).hasSize(1);
    }
}
