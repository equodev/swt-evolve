package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The macOS application menu labels its items from the {@code SWT_*} message keys, which
 * {@code SWTMessages} only defines from SWT 3.119 on. Asking an older bundle for them returns the
 * key, so the menu read "SWT_About Eclipse", "SWT_Preferences", "SWT_HideOthers" and so on.
 *
 * <p>The bundle ships inside the platform jar and is packaged into ours at assembly time, so it is
 * absent from the test classpath. The baselines are reproduced here from their published contents
 * instead.
 */
class AppMenuMessagesTest {

    /** SWTMessages up to 3.118: none of the seven app-menu keys, under either name. */
    private static final String PRE_3_119 = "SWT_Yes=Yes\nSWT_No=No\nSWT_OK=OK\n";

    /** The seven entries 3.119 added. */
    private static final String FROM_3_119 = PRE_3_119
            + "SWT_About=About\nSWT_Preferences=Preferences...\nSWT_Services=Services\n"
            + "SWT_Hide=Hide\nSWT_HideOthers=Hide Others\nSWT_ShowAll=Show All\nSWT_Quit=Quit\n";

    @Test
    @DisplayName("a pre-3.119 bundle still labels every item")
    void preThreeOneNineBundleStillLabelsEveryItem() throws IOException {
        assertLabelled(bundle(PRE_3_119));
    }

    @Test
    @DisplayName("no bundle at all still labels every item")
    void noBundleStillLabelsEveryItem() {
        assertLabelled(null);
    }

    @Test
    @DisplayName("a 3.119+ bundle is what the labels come from")
    void bundleFromThreeOneNineOnwardsSuppliesTheLabels() throws IOException {
        assertLabelled(bundle(FROM_3_119));
    }

    @Test
    @DisplayName("a translation wins under either key name")
    void translationWinsUnderEitherKeyName() throws IOException {
        assertThat(AppMenuMessages.label("SWT_HideOthers", bundle("SWT_HideOthers=Ocultar otros\n")))
                .isEqualTo("Ocultar otros");
        // Pre-3.119 translations key off the English label, which is the name upstream passed then.
        // A space is a key/value separator in .properties, so such a key is written escaped.
        assertThat(AppMenuMessages.label("SWT_HideOthers", bundle("Hide\\ Others=Ocultar otros\n")))
                .isEqualTo("Ocultar otros");
    }

    private static void assertLabelled(ResourceBundle bundle) {
        for (String key : AppMenuMessages.keys()) {
            assertThat(AppMenuMessages.label(key, bundle))
                    .as("application menu label for %s", key)
                    .isNotNull()
                    .isNotEqualTo(key)
                    .doesNotStartWith("SWT_");
        }
        assertThat(AppMenuMessages.label("SWT_About", bundle)).isEqualTo("About");
        assertThat(AppMenuMessages.label("SWT_Preferences", bundle)).isEqualTo("Preferences...");
        assertThat(AppMenuMessages.label("SWT_Services", bundle)).isEqualTo("Services");
        assertThat(AppMenuMessages.label("SWT_Hide", bundle)).isEqualTo("Hide");
        assertThat(AppMenuMessages.label("SWT_HideOthers", bundle)).isEqualTo("Hide Others");
        assertThat(AppMenuMessages.label("SWT_ShowAll", bundle)).isEqualTo("Show All");
        assertThat(AppMenuMessages.label("SWT_Quit", bundle)).isEqualTo("Quit");
    }

    private static ResourceBundle bundle(String contents) throws IOException {
        return new PropertyResourceBundle(new StringReader(contents));
    }
}
