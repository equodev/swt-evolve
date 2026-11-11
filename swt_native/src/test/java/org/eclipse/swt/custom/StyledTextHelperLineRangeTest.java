package org.eclipse.swt.custom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("native-unit")
class StyledTextHelperLineRangeTest {

    @Test
    @DisplayName("a range holds exactly its lines, in order")
    void range() {
        Set<Integer> lines = StyledTextHelper.lineRange(3, 4, 10);
        List<Integer> iterated = new ArrayList<>(lines);
        assertThat(iterated).containsExactly(3, 4, 5, 6);
        assertThat(lines).hasSize(4);
        assertThat(lines.contains(3)).isTrue();
        assertThat(lines.contains(6)).isTrue();
        assertThat(lines.contains(2)).isFalse();
        assertThat(lines.contains(7)).isFalse();
        assertThat(lines.contains("3")).isFalse();
    }

    @Test
    @DisplayName("a range that does not fit, or holds no line, is empty")
    void empty() {
        assertThat(StyledTextHelper.lineRange(-1, 2, 10)).isEmpty();
        assertThat(StyledTextHelper.lineRange(8, 3, 10)).isEmpty();
        assertThat(StyledTextHelper.lineRange(2, 0, 10)).isEmpty();
        assertThat(StyledTextHelper.lineRange(0, 10, 10)).hasSize(10);
    }

    @Test
    @DisplayName("a bullet line is found where it is, and -1 where it is not")
    void bulletIndexOf() {
        int[] lines = {2, 5, 9, 14, 0, 0};
        assertThat(StyledTextHelper.bulletIndexOf(lines, 4, 2)).isEqualTo(0);
        assertThat(StyledTextHelper.bulletIndexOf(lines, 4, 9)).isEqualTo(2);
        assertThat(StyledTextHelper.bulletIndexOf(lines, 4, 14)).isEqualTo(3);
        assertThat(StyledTextHelper.bulletIndexOf(lines, 4, 6)).isEqualTo(-1);
        assertThat(StyledTextHelper.bulletIndexOf(lines, 4, 0)).isEqualTo(-1);
        assertThat(StyledTextHelper.bulletIndexOf(lines, 0, 2)).isEqualTo(-1);
    }
}
