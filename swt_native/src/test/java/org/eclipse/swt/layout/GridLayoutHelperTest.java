package org.eclipse.swt.layout;

import org.eclipse.swt.widgets.Control;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("native-unit")
class GridLayoutHelperTest {

    @Test
    @DisplayName("a full grid doubles, with no row allocated")
    void doubles() {
        Control[][] grown = GridLayoutHelper.grownGrid(new Control[16][], 16);
        assertThat(grown.length).isEqualTo(32);
        assertThat((Object[]) grown).containsOnlyNulls();
    }

    @Test
    @DisplayName("a child spanning past the doubled grid gets four rows beyond its last one")
    void tallChild() {
        assertThat(GridLayoutHelper.grownGrid(new Control[4][], 20).length).isEqualTo(24);
    }
}
