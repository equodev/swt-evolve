package dev.equo.swt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code -Dswt.evolve.canvas_fill_roles_<Class>=RRGGBB:role,...}, keyed by the application's class
 * name as written, parsed into the colors of each role.
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
    void eachClassKeepsTheColorsOfItsRoles() {
        System.setProperty(GRID, " #f6e6d6 : Hover , DBC7B4:selection ");
        System.setProperty(BLANK, " ");

        Map<String, CanvasFillRoles> byClass = Config.canvasFillRolesByClassFromProperties();

        assertThat(byClass).containsOnlyKeys("a.Grid");
        assertThat(byClass.get("a.Grid").hover).containsExactly(0xF6E6D6);
        assertThat(byClass.get("a.Grid").selection).containsExactly(0xDBC7B4);
    }

    @Test
    void entriesThatAreNotAColorAndAKnownRoleAreDropped() {
        CanvasFillRoles roles = CanvasFillRoles.parse("F6E6D6:hover,FFF:hover,DBC7B4:focus,ZZZZZZ:selection,AABBCC");

        assertThat(roles.hover).containsExactly(0xF6E6D6);
        assertThat(roles.selection).as("a role with no color stays off the wire").isNull();
        assertThat(CanvasFillRoles.parse("FFF:hover")).isNull();
    }

    @Test
    void theMapStaysInJava() throws Exception {
        ConfigFlags flags = new ConfigFlags();
        flags.canvas_fill_roles_by_class = Collections.singletonMap("a.Grid", CanvasFillRoles.parse("F6E6D6:hover"));

        String json = new String(new Serializer().to(flags), java.nio.charset.StandardCharsets.UTF_8);

        assertThat(json)
                .as("the client gets the roles per GC, never the application's class names")
                .doesNotContain("canvas_fill_roles_by_class")
                .doesNotContain("a.Grid");
    }
}
