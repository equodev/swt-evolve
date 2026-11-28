package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** A folder's topRight block floats over the tab row, shown on hover, by default only in the Eclipse IDE. */
class CTabFolderAutoHideFlagTest {

    private static final String PROPERTY = "swt.evolve.ctabfolder_topright_auto_hide";
    private static final String PRODUCT = "eclipse.product";

    private ConfigFlags savedFlags;
    private String savedProperty;
    private String savedProduct;

    @BeforeEach
    void setUp() {
        savedFlags = Config.setConfigFlags(null);
        savedProperty = System.getProperty(PROPERTY);
        savedProduct = System.getProperty(PRODUCT);
        System.clearProperty(PROPERTY);
    }

    @AfterEach
    void tearDown() {
        restore(PROPERTY, savedProperty);
        restore(PRODUCT, savedProduct);
        Config.setConfigFlags(savedFlags);
    }

    private static void restore(String key, String value) {
        if (value == null) System.clearProperty(key);
        else System.setProperty(key, value);
    }

    @Test
    void outsideTheEclipseIdeTheBlockIsAlwaysShownInTheTabRow() {
        System.setProperty(PRODUCT, "org.example.rcp.product");

        assertThat(Config.getConfigFlags().ctabfolder_topright_auto_hide).isFalse();
    }

    @Test
    void inTheEclipseIdeItFloatsAndShowsOnHover() {
        System.setProperty(PRODUCT, "org.eclipse.epp.package.java.product");

        assertThat(Config.getConfigFlags().ctabfolder_topright_auto_hide).isTrue();
    }

    @Test
    void thePropertyOverridesTheDefaultEitherWay() {
        System.setProperty(PRODUCT, "org.eclipse.epp.package.java.product");
        System.setProperty(PROPERTY, "false");
        assertThat(Config.getConfigFlags().ctabfolder_topright_auto_hide).isFalse();

        Config.setConfigFlags(null);
        System.setProperty(PRODUCT, "org.example.rcp.product");
        System.setProperty(PROPERTY, "true");
        assertThat(Config.getConfigFlags().ctabfolder_topright_auto_hide).isTrue();
    }
}
