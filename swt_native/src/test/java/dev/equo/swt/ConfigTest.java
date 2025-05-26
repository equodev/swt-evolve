package dev.equo.swt;

import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigTest {

    @BeforeEach
    void defaults_swt() {
        Config.defaultImpl = Config.Impl.eclipse;
        System.clearProperty("dev.equo.swt.Point");
    }

    @Test
    void should_default_to_swt() {
        Config.defaultToEclipse();
        assertThat(Config.defaultImpl).isEqualTo(Config.Impl.eclipse);
        assertThat(Config.isEquo(Point.class)).isFalse();
    }

    @Test
    void should_default_to_equo() {
        Config.defaultToEquo();
        assertThat(Config.defaultImpl).isEqualTo(Config.Impl.equo);
        assertThat(Config.isEquo(Point.class)).isTrue();
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
        System.setProperty("dev.equo.swt.Point", "");
        assertThat(Config.isEquo(Point.class)).isTrue();
    }

    @Test
    void class_should_default_to_swt_without_property() {
        System.clearProperty("dev.equo.swt.Point");
        assertThat(Config.isEquo(Point.class)).isFalse();
    }

}
