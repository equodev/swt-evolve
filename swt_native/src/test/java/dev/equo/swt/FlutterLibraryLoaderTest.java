package dev.equo.swt;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlutterLibraryLoaderTest {

    @Test
    void firstBundleBaseDir_returnsFirstNonNull() {
        String base = FlutterLibraryLoader.firstBundleBaseDir(List.of(
            () -> "/home/u/.equo/ewt",
            () -> "/ignored"));
        assertThat(base).isEqualTo("/home/u/.equo/ewt");
    }

    @Test
    void firstBundleBaseDir_skipsNullAndBlank() {
        String base = FlutterLibraryLoader.firstBundleBaseDir(List.of(
            () -> null,
            () -> "   ",
            () -> "/home/u/.equo/ewt"));
        assertThat(base).isEqualTo("/home/u/.equo/ewt");
    }

    @Test
    void firstBundleBaseDir_returnsNullWhenNoneProvide() {
        String base = FlutterLibraryLoader.firstBundleBaseDir(List.of(
            () -> null,
            () -> ""));
        assertThat(base).isNull();
    }

    @Test
    void firstBundleBaseDir_returnsNullOnEmpty() {
        assertThat(FlutterLibraryLoader.firstBundleBaseDir(List.of())).isNull();
    }
}
