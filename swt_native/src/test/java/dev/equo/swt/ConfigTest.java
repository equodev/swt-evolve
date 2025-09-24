package dev.equo.swt;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigTest {

    @BeforeEach
    void defaults_swt() {
        Config.defaultImpl = Config.Impl.eclipse;
    }

    @AfterEach
    void reset() {
        Config.defaultImpl = Config.Impl.eclipse;
        System.clearProperty("dev.equo.swt.Point");
        System.clearProperty("dev.equo.swt.CTabItem");
        System.clearProperty("dev.equo.swt.Button");
    }

    @Test
    void should_default_to_swt() {
        System.clearProperty("dev.equo.swt.Point");
        Config.defaultToEclipse();
        assertThat(Config.defaultImpl).isEqualTo(Config.Impl.eclipse);
        assertThat(Config.isEquo(Point.class)).isFalse();
    }

    @Test
    void should_default_to_equo() {
        Config.defaultToEquo();
        assertThat(Config.defaultImpl).isEqualTo(Config.Impl.equo);
        assertThat(Config.isEquo(CTabItem.class)).isTrue();
    }

    @Test
    void class_should_default_to_swt_with_config() {
        Config.useEclipse(Point.class);
        assertThat(Config.isEquo(Point.class)).isFalse();
    }

    @Test
    void class_should_default_to_equo_with_config() {
        Config.useEquo(Point.class);
        assertThat(Config.isEquo(Point.class)).isTrue();
    }

    @Test
    void class_should_default_to_equo_with_property() {
        System.setProperty("dev.equo.swt.Point", Config.Impl.equo.name());
        assertThat(Config.isEquo(Point.class)).isTrue();
    }

    @Test
    void class_should_default_to_swt_without_property() {
        System.clearProperty("dev.equo.swt.Point");
        assertThat(Config.isEquo(Point.class)).isFalse();
    }

    @Nested
    class EquoWidgetDefaults {

        @Test
        void button_should_default_to_equo_with_equo() {
            Config.defaultToEquo();
            assertThat(Config.isEquo(Button.class)).isTrue();
        }

        @Test
        void button_should_default_to_swt_with_config() {
            Config.useEclipse(Button.class);
            assertThat(Config.isEquo(Button.class)).isFalse();
        }

        @Test
        void button_should_default_to_equo_with_config() {
            Config.useEquo(Button.class);
            assertThat(Config.isEquo(Button.class)).isTrue();
        }

        @Test
        void button_should_default_to_equo_with_property() {
            System.setProperty("dev.equo.swt.Button", Config.Impl.equo.name());
            assertThat(Config.isEquo(Button.class)).isTrue();
        }

        @Test
        void button_should_default_to_eclipse_with_property_as_eclipse() {
            System.setProperty("dev.equo.swt.Button", "eclipse");
            assertThat(Config.isEquo(Button.class)).isFalse();
        }

    }

}
