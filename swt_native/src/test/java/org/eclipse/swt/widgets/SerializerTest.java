package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.*;

import dev.equo.swt.Config;
import dev.equo.swt.Serializer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.cocoa.NSObject;
import org.eclipse.swt.internal.cocoa.NSView;
import org.instancio.Instancio;
import org.instancio.Select;
import org.instancio.settings.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SerializerTest {

    Serializer serializer = new Serializer();

    @BeforeAll
    static void useEquo() {
        Config.defaultToEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

//    @Test
//    void should_serialize_point() throws IOException {
//        Point p = new Point(10, 20);
//
//        String json = serialize(p);
//
//        assertThat(json).isEqualTo("{\"x\": 10, \"y\": 20}");
//    }

    @Test
    void should_serialize_button() throws IOException {
        Display display = Mockito.mock(Display.class);
        SwtDisplay swtDisplay = Mockito.mock(SwtDisplay.class);
        Mockito.when(display.getImpl()).thenReturn(swtDisplay);

        Shell shell = Mockito.mock(Shell.class);
        SwtShell swtShell = Mockito.mock(SwtShell.class);
        Mockito.when(shell.getImpl()).thenReturn(swtShell);
        swtShell.display = display;

        Button w = new Button(shell, SWT.PUSH);
        w.setBounds(10, 20, 30, 40);
        w.setText("l alal a");

        String json = serialize(w);

        assertThat(json).isEqualTo("{\"text\":\"l alal a\",\"bounds\":{\"x\":10,\"y\":20,\"width\":30,\"height\":40}}");

        Button b = new Button(shell, SWT.PUSH);
        Settings settings = Settings.defaults()
                .set(Keys.ASSIGNMENT_TYPE, AssignmentType.METHOD)
                .set(Keys.ON_SET_METHOD_UNMATCHED, OnSetMethodUnmatched.INVOKE);
        Instancio.ofObject(b)
                .withSettings(settings)
//                .ignore(Select.setter(Button::setParent))
//                .ignore(Select.setter(Button::setImpl))
                .ignore(Select.types().of(NSObject.class))
               .fill();
        json = serialize(b);

        assertThat(json).isEqualTo("{\"text\":\"l alal a\",\"bounds\":{\"x\":10,\"y\":20,\"width\":30,\"height\":40}}");}

    private <T> String serialize(Button p) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.to(p, out);

        return out.toString(StandardCharsets.UTF_8);
    }

}
