package dev.equo.swt;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Java8HelpersTest {

    @Test
    void aMapNeedsKeyValuePairs() {
        assertThat(Java8.map("a", 1)).containsEntry("a", 1);
        assertThatThrownBy(() -> Java8.map("a")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void repeatJoinsCopiesAndNothingForNoCount() {
        assertThat(Java8.repeat("ab", 3)).isEqualTo("ababab");
        assertThat(Java8.repeat("ab", 0)).isEmpty();
    }

    @Test
    void theDefaultPackageHasAnEmptyName() throws Exception {
        assertThat(Java8.packageName(Java8.class)).isEqualTo("dev.equo.swt");
        assertThat(Java8.packageName(Class.forName("[I"))).isEmpty();
    }

    @Test
    void writeBytesAppendsThemAll() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Java8.writeBytes(out, new byte[] {1, 2, 3});
        assertThat(out.toByteArray()).containsExactly(1, 2, 3);
    }
}
