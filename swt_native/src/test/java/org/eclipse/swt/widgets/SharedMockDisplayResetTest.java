package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;

/** What one test stubs on the JVM-wide mock display does not reach the next. */
@ExtendWith(Mocks.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SharedMockDisplayResetTest {

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUpBridge() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDownBridge() {
        FlutterBridge.set(null);
    }

    @Test
    @Order(1)
    void aTestLeavesItsStubsOnTheDisplay() {
        Display display = Mocks.display();
        DartDisplay impl = (DartDisplay) display.getImpl();
        doCallRealMethod().when(impl).sendEvent(any(EventTable.class), any(Event.class));
        Map<String, Object> data = new HashMap<>();
        doAnswer(inv -> data.put(inv.getArgument(0), inv.getArgument(1))).when(display).setData(anyString(), any());
        doAnswer(inv -> data.get(inv.getArgument(0))).when(display).getData(anyString());
        display.setData("key", "left over");
        doReturn(new Rectangle(0, 0, 1920, 1080)).when(display).getBounds();
        doReturn(new Point(10, 20)).when(display).getCursorLocation();
    }

    @Test
    @Order(2)
    void theNextFindsTheDisplayAsTheFixtureBuiltIt() {
        Shell shell = Mocks.shell();
        Composite composite = new Composite(shell, SWT.NONE);
        composite.addListener(SWT.Resize, e -> {
            throw new IllegalStateException("a listener ran");
        });

        assertThatCode(() -> composite.setSize(100, 50)).doesNotThrowAnyException();
        assertThat(shell.getDisplay().getData("key")).isNull();
        assertThat(shell.getDisplay().getBounds()).isNull();
        assertThat(shell.getDisplay().getCursorLocation()).isNull();
    }
}
