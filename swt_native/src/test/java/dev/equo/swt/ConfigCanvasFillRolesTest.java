package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code -Dswt.evolve.canvas_fill_roles_<Class>=RRGGBB:role,...}, keyed by the application's class
 * name as written, normalized to the entries a client can use.
 */
class ConfigCanvasFillRolesTest {

    private static final String GRID = "swt.evolve.canvas_fill_roles_a.Grid";
    private static final String BLANK = "swt.evolve.canvas_fill_roles_c.Blank";

    @AfterEach
    void clearProperties() {
        System.clearProperty(GRID);
        System.clearProperty(BLANK);
    }

    @Test
    void eachClassKeepsItsNormalizedRoles() {
        System.setProperty(GRID, " #f6e6d6 : Hover , DBC7B4:selection ");
        System.setProperty(BLANK, " ");

        assertThat(Config.canvasFillRolesByClassFromProperties())
                .containsExactly(Map.entry("a.Grid", "F6E6D6:hover,DBC7B4:selection"));
    }

    @Test
    void entriesThatAreNotAColorAndAKnownRoleAreDropped() {
        assertThat(Config.normalizeFillRoles("F6E6D6:hover,FFF:hover,DBC7B4:focus,ZZZZZZ:selection,AABBCC"))
                .isEqualTo("F6E6D6:hover");
        assertThat(Config.normalizeFillRoles("FFF:hover")).isNull();
    }

    @Test
    void theMapStaysInJava() throws Exception {
        ConfigFlags flags = new ConfigFlags();
        flags.canvas_fill_roles_by_class = Map.of("a.Grid", "F6E6D6:hover");

        String json = new String(new Serializer().to(flags), java.nio.charset.StandardCharsets.UTF_8);

        assertThat(json)
                .as("the client gets the roles per GC, never the application's class names")
                .doesNotContain("canvas_fill_roles_by_class")
                .doesNotContain("a.Grid");
    }
}
