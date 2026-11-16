package dev.equo.swt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The locale a POSIX process writes dates in, as GTK's calendar reads it from the environment. */
class ConfigPosixTimeLocaleTest {

    @Test
    void lcTimeWinsOverLangWithoutItsEncoding() {
        assertThat(Config.posixTimeLocaleTag(null, "de_DE.UTF-8", "en_US.UTF-8")).isEqualTo("de-DE");
    }

    @Test
    void lcAllWinsOverLcTime() {
        assertThat(Config.posixTimeLocaleTag("es_AR.UTF-8", "de_DE.UTF-8", "en_US.UTF-8")).isEqualTo("es-AR");
    }

    @Test
    void langWhenNoCategoryIsSetWithoutItsModifier() {
        assertThat(Config.posixTimeLocaleTag("", null, "de_DE@euro")).isEqualTo("de-DE");
    }

    @Test
    void theCLocaleOrNoneIsUsEnglish() {
        assertThat(Config.posixTimeLocaleTag(null, "C.UTF-8", null)).isEqualTo("en-US");
        assertThat(Config.posixTimeLocaleTag(null, null, "POSIX")).isEqualTo("en-US");
        assertThat(Config.posixTimeLocaleTag(null, null, null)).isEqualTo("en-US");
    }
}
