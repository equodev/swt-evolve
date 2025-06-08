package dev.equo.swt;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.internal.cocoa.NSObject;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Widget;
import org.instancio.Instancio;
import org.instancio.Select;
import org.instancio.settings.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

public class SerializeTestBase {
    private final Settings settings;
    Serializer serializer = new Serializer();

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
        Config.defaultToEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void mocks() {
        FlutterBridge.set(new MockFlutterBridge());
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
        Instancio.ofObject(w)
                .withSettings(settings)
                .ignore(Select.setter(Widget.class, "setImpl"))
                .ignore(Select.field(Widget.class, "state"))
                .ignore(Select.field(Widget.class, "style"))
                .ignore(Select.types().of(NSObject.class))
                .withFillType(FillType.POPULATE_NULLS_AND_DEFAULT_PRIMITIVES)
                .generate(Select.all(boolean.class), gen -> gen.booleans().probability(1.0)) // Always true
                .generate(Select.all(int.class), gen -> gen.ints().range(1, Integer.MAX_VALUE))
                .fill();
    }

    protected void setAll(Resource w) {
        Instancio.ofObject(w)
                .withSettings(settings)
                .ignore(Select.setter(Resource.class, "setImpl"))
                .ignore(Select.field(Widget.class, "state"))
                .ignore(Select.types().of(NSObject.class))
                .withFillType(FillType.POPULATE_NULLS_AND_DEFAULT_PRIMITIVES)
                .generate(Select.all(boolean.class), gen -> gen.booleans().probability(1.0)) // Always true
                .generate(Select.all(int.class), gen -> gen.ints().range(1, Integer.MAX_VALUE))
                .fill();
    }

    protected Consumer<? super Object> isEqualTo(Object value, Object def) {
        return n -> {
            if (value == def)
                assertThatJson(n).isAbsent();
            else
                assertThatJson(n).isEqualTo(value);
        };
    }
    protected Consumer<? super Object> isEqualTo(Color value, Object def) {
        return n -> {
            if (value == def)
                assertThatJson(n).isAbsent();
            else
                assertThatJson(n).isObject().isEqualTo(value.handle);
        };
    }

    protected Object ifNotNull() {
        return null;
    }

    protected boolean orAbsentIfFalse() {
        return false;
    }

    protected int orAbsentIf0() {
        return 0;
    }

}
