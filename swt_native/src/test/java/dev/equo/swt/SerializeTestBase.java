package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.accessibility.SwtAccessible;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Mocks;
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
import java.util.Base64;
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
                .generate(Select.all(Image.class), gen -> gen.oneOf(createTestImage()));
        if (!(w instanceof Caret) && !(w instanceof TreeColumn) && !(w instanceof TableColumn)) {
            inst = inst.generate(Select.all(Color.class), gen -> gen.oneOf(new Color(Mocks.red(), Mocks.green(), Mocks.blue())));
        }
        if (w instanceof Canvas c)
            inst.generate(Select.all(Caret.class), gen -> gen.oneOf(new Caret(c, SWT.NONE)));
        inst.fill();
    }

    protected void setAll(Color w) {
        // no setters
    }

    protected void setAll(GC w) {
        InstancioObjectApi<GC> inst = Instancio.ofObject(w)
                .withSettings(settings);
        try {
            inst
                    .ignore(Select.types().of(Class.forName("org.eclipse.swt.internal.cocoa.NSObject")));
        } catch (ClassNotFoundException e) {}
        inst = inst
                .withFillType(FillType.POPULATE_NULLS_AND_DEFAULT_PRIMITIVES)
                .generate(Select.all(boolean.class), gen -> gen.booleans().probability(1.0)) // Always true
                .generate(Select.all(int.class), gen -> gen.ints().range(1, 40))
                .generate(Select.all(int[].class), gen -> gen.array().length(2))
                .generate(Select.all(Color.class), gen -> gen.oneOf(new Color(Mocks.red(), Mocks.green(), Mocks.blue())));
        inst.fill();
    }

    private Image createTestImage() {
        try {
            java.io.InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("collapseall.png");
            if (resourceStream != null) {
                ImageData imageData = new ImageData(resourceStream);
                Image image = new Image(null, imageData);
                resourceStream.close();
                return image;
            }
        } catch (Exception e) {
            // Resource not available, create default image
        }
        return new Image(null, 16, 16);
    }

    protected void setAll(Image i) {
        i.setBackground(new Color(Mocks.red(), Mocks.green(), Mocks.blue()));
    }

    protected AssertConsumer node(String field) {
        return new AssertConsumer(field);
    }

    private static String serializeByteArray(byte[] value) {
        if (value == null) return null;
        return Base64.getEncoder().encodeToString(value);
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


        public Consumer<? super Object> equalsTo(Image value, Object def) {
            return n -> {
                if (value == def)
                    assertThatJson(n).node(field).isAbsent();
                else {
                    String filename = ((DartImage) value.getImpl())._filename();
                    if (filename != null) {
                        assertThatJson(n).node(field).isObject().containsEntry("filename", filename);
                    } else {
                        assertThatJson(n).node(field).isObject().node("imageData").isObject()
                                .containsEntry("width", value.getImageData().width)
                                .containsEntry("height", value.getImageData().height);

                    }
                }
            };
        }

        public Consumer<? super Object> equalsTo(ImageData value, Object def) {
            return n -> {
                if (value == def)
                    assertThatJson(n).node(field).isAbsent();
                else
                    assertThatJson(n).node(field).isObject()
                            .containsEntry("scanlinePad", value.scanlinePad)
                            .containsEntry("height", value.height)
                            .containsEntry("alphaData", serializeByteArray(value.alphaData))
                            .containsEntry("bytesPerLine", value.bytesPerLine)
                            .containsEntry("width", value.width)
                            .containsEntry("alpha", value.alpha)
                            .containsEntry("depth", value.depth)
                            .containsEntry("type", value.type)
                            .containsEntry("transparentPixel", value.transparentPixel);
            };
        }
    }

    static final protected Object orAbsentIfNull = null;
    static final protected int orAbsentIf0 = 0;
    static final protected boolean orAbsentIfFalse = false;

}
