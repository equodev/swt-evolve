package org.eclipse.swt.graphics;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ImageUtilsTest {

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
        String filename = ImageUtils.getFilename(inputPath);
        assertThat(filename).isEqualTo(imageName);
    }
}
