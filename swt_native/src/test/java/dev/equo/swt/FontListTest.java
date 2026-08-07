package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Hosts that pick a font by intersecting a CSS font-family list with what SWT says exists get
 * nothing back when the list holds only the system font.
 */
public class FontListTest {

    private static final FontData SYSTEM_FONT = new FontData("system", 11, SWT.NORMAL);

    // Names no other suite uses, so registering them cannot change what a serialized FontData
    // round-trips to — the substitution table is global and shared across the whole JVM run.
    private static final String FACE_A = "Evolve Test Face A";
    private static final String FACE_B = "Evolve Test Face B";

    @BeforeAll
    static void registerFaces() {
        FontMetricsUtil.registerFontSubstitution(FACE_A, "Liberation Sans");
        FontMetricsUtil.registerFontSubstitution(FACE_B, "Liberation Sans");
    }

    private static String[] namesOf(FontData[] fonts) {
        return Arrays.stream(fonts).map(FontData::getName).toArray(String[]::new);
    }

    @Test
    public void reports_more_than_just_the_system_font() {
        FontData[] fonts = FontMetricsUtil.getFontList(null, true, SYSTEM_FONT);

        assertThat(fonts).hasSizeGreaterThan(1);
        assertThat(namesOf(fonts)).contains("system", FACE_A, FACE_B);
    }

    @Test
    public void filters_by_face_name() {
        FontData[] fonts = FontMetricsUtil.getFontList(FACE_A, true, SYSTEM_FONT);

        assertThat(namesOf(fonts)).containsExactly(FACE_A);
    }

    @Test
    public void face_name_match_is_case_insensitive() {
        FontData[] fonts = FontMetricsUtil.getFontList(FACE_A.toUpperCase(), true, SYSTEM_FONT);

        assertThat(namesOf(fonts)).containsExactly(FACE_A);
    }

    @Test
    public void non_scalable_request_yields_nothing() {
        assertThat(FontMetricsUtil.getFontList(null, false, SYSTEM_FONT)).isEmpty();
    }

    @Test
    public void survives_a_device_with_no_system_font() {
        FontData[] fonts = FontMetricsUtil.getFontList(null, true, null);

        assertThat(fonts).isNotEmpty();
        assertThat(Arrays.stream(fonts).allMatch(f -> f.getHeight() > 0)).isTrue();
    }
}
