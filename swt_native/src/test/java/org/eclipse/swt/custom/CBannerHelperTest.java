package org.eclipse.swt.custom;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("native-unit")
class CBannerHelperTest {

    @Test
    void aHiddenBannerIsLaidOutOnceItHasAWidthOrAHeight() {
        assertThat(CBannerHelper.hasRoomToLayOut(new Point(0, 0))).isFalse();
        assertThat(CBannerHelper.hasRoomToLayOut(new Point(0, 60))).isTrue();
        assertThat(CBannerHelper.hasRoomToLayOut(new Point(300, 0))).isTrue();
    }
}
