package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.accessibility.SwtAccessible;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Mocks;
import org.eclipse.swt.widgets.Widget;
import org.instancio.Instancio;
import org.instancio.InstancioObjectApi;
import org.instancio.Select;
import org.instancio.settings.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

public class SerializeTestBase {
    private final Settings settings;
    Serializer serializer = new Serializer();
    private MockedStatic<SwtAccessible> mockedStatic;

    protected  SerializeTestBase() {
        settings = Settings.defaults()
                .set(Keys.ASSIGNMENT_TYPE, AssignmentType.METHOD)
                .set(Keys.ON_SET_METHOD_UNMATCHED, OnSetMethodUnmatched.INVOKE)
                .set(Keys.ARRAY_ELEMENTS_NULLABLE, false)
                .set(Keys.BOOLEAN_NULLABLE, false)
                .set(Keys.STRING_NULLABLE, false);
    }

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void mocks() {
        FlutterBridge.set(new MockFlutterBridge());

        Accessible mockAcc = Mockito.mock(Accessible.class);
        mockedStatic = Mockito.mockStatic(SwtAccessible.class);
        mockedStatic.when(() -> SwtAccessible.internal_new_Accessible(Mockito.any())).thenReturn(mockAcc);
    }

    @AfterEach
    void resetStatic() {
        mockedStatic.close();
    }

    protected <T> String serialize(Object p) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            serializer.to(p, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return out.toString(StandardCharsets.UTF_8);
    }

    protected void setAll(Widget w) {
        InstancioObjectApi<Widget> inst = Instancio.ofObject(w)
                .withSettings(settings)
                .ignore(Select.setter(Widget.class, "setImpl"))
                .ignore(Select.field(Widget.class, "state"))
                .ignore(Select.field(Widget.class, "style"));
        try {
            inst
                    .ignore(Select.types().of(Class.forName("org.eclipse.swt.internal.cocoa.NSObject")));
        } catch (ClassNotFoundException e) {}
        inst = inst
                .withFillType(FillType.POPULATE_NULLS_AND_DEFAULT_PRIMITIVES)
                .generate(Select.all(boolean.class), gen -> gen.booleans().probability(1.0)) // Always true
                .generate(Select.all(int.class), gen -> gen.ints().range(1, Integer.MAX_VALUE))
                .generate(Select.all(Color.class), gen -> gen.oneOf(new Color(Mocks.red(), Mocks.green(), Mocks.blue())));
        if (w instanceof Canvas c)
            inst.generate(Select.all(Caret.class), gen -> gen.oneOf(new Caret(c, SWT.NONE)));
        inst.fill();
    }

    protected void setAll(Color w) {
        // no setters
    }

    protected AssertConsumer node(String field) {
        return new AssertConsumer(field);
    }

    public static class AssertConsumer {
        private final String field;

        public AssertConsumer(String field) {
            this.field = field;
        }

        public Consumer<? super Object> equalsTo(Object value, Object def) {
            return n -> {
                if (value == def)
                    assertThatJson(n).node(field).isAbsent();
                else
                    assertThatJson(n).node(field).isEqualTo(value);
            };
        }

        public Consumer<? super Object> equalsTo(Color value, Object def) {
            return n -> {
                if (value == def)
                    assertThatJson(n).node(field).isAbsent();
                else
                    assertThatJson(n).node(field).isObject()
                            .containsEntry("red", value.getRed())
                            .containsEntry("green", value.getGreen())
                            .containsEntry("blue", value.getBlue())
                            .containsEntry("alpha", value.getAlpha());
            };
        }

        public Consumer<? super Object> equalsTo(Caret value, Object def) {
            return n -> {
                if (value == def)
                    assertThatJson(n).node(field).isAbsent();
                else
                    assertThatJson(n).node(field).isObject()
                            .containsEntry("visible", Boolean.TRUE);
            };
        }
    }

    static final protected Object orAbsentIfNull = null;
    static final protected int orAbsentIf0 = 0;
    static final protected boolean orAbsentIfFalse = false;

}
