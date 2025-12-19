package org.eclipse.swt.graphics;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.device;

public class GraphicsUtilsTest extends SerializeTestBase {

    public static final String imageName = "imageName";

    private static Stream<Object[]> providePaths() {
        if ("win32".equals(org.eclipse.swt.SWT.getPlatform())) {
            return Stream.of(
                    new Object[]{"C:\\Users\\myuser\\Documents\\" + imageName + ".png"},
                    new Object[]{"file:/C:/Users/myuser/Documents/" + imageName + ".png"},
                    new Object[]{"file:\\C:\\Users\\myuser\\Documents\\" + imageName + ".png"},
                    new Object[]{"file:/C:/myapp/build/libs/myapp.jar!/common/" + imageName + ".png"},
                    new Object[]{"file:\\C:\\myapp\\build\\libs\\myapp.jar!\\common\\" + imageName + ".png"},
                    new Object[]{imageName + ".png"}
            );
        } else {
            return Stream.of(
                    new Object[]{"/Users/myuser/Documents/" + imageName + ".png"},
                    new Object[]{"file://Users/myuser/Documents/" + imageName + ".png"},
                    new Object[]{"file://myapp/build/libs/myapp.jar!/common/" + imageName + ".png"},
                    new Object[]{"file:///myapp/build/libs/myapp.jar!/common/" + imageName + ".png"},
                    new Object[]{imageName + ".png"}
            );
        }
    }

    @ParameterizedTest
    @MethodSource("providePaths")
    public void should_parse_filename(String inputPath) {
        String filename = GraphicsUtils.getFilename(inputPath);
        assertThat(filename).isEqualTo(imageName);
    }

    @Test
    public void should_copy_fontdata() {
        // Create a FontData
        FontData original = new FontData("Arial", 12, SWT.BOLD);

        // Copy it
        FontData copy = GraphicsUtils.copyFontData(original);

        // Verify properties are the same
        assertThat(copy).isNotNull();
        assertThat(copy.getName()).isEqualTo(original.getName());
        assertThat(copy.getHeight()).isEqualTo(original.getHeight());
        assertThat(copy.getStyle()).isEqualTo(original.getStyle());
    }

    @Test
    public void should_return_null_when_fontdata_is_null() {
        FontData result = GraphicsUtils.copyFontData(null);
        assertThat(result).isNull();
    }

    @Test
    public void should_copy_font() {
        // Create a Font from FontData
        FontData fontData = new FontData("Courier", 14, SWT.ITALIC);
        Font original = new Font(device(), fontData);

        // Copy it (device is obtained from original font)
        Font copy = GraphicsUtils.copyFont(original);

        // Verify properties are the same
        assertThat(copy).isNotNull();
        FontData[] copiedFontDataArray = copy.getFontData();
        assertThat(copiedFontDataArray).isNotNull();
        assertThat(copiedFontDataArray.length).isGreaterThan(0);

        FontData copiedFontData = copiedFontDataArray[0];
        assertThat(copiedFontData.getName()).isEqualTo(fontData.getName());
        assertThat(copiedFontData.getHeight()).isEqualTo(fontData.getHeight());
        assertThat(copiedFontData.getStyle()).isEqualTo(fontData.getStyle());

        // Cleanup
        original.dispose();
        copy.dispose();
    }

    @Test
    public void should_return_null_when_font_is_null() {
        Font result = GraphicsUtils.copyFont(null);
        assertThat(result).isNull();
    }
}
