package dev.equo.swt.size;

import dev.equo.swt.ImageMetricUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisabledOnOs(OS.LINUX)
@Tag("metal")
public class ImageSizeTest extends SizeAssert {

    @RegisterExtension()
    private static final ImageSizeBridge staticBridge = new ImageSizeBridge();

    public static Stream<String> imagePaths() {
        String[] formats = {"png", "jpg", "gif", "bmp"};
        int[] sizes = {16, 48};

        return Stream.of(formats)
            .flatMap(format ->
                Arrays.stream(sizes)
                    .mapToObj(size -> "/images/" + size + "x" + size + "." + format)
            );
    }

    @ParameterizedTest
    @MethodSource("imagePaths")
    void image_size_should_equal_flutter(String imagePath) throws Exception {
        InputStream inputStream = ImageSizeTest.class.getResourceAsStream(imagePath);
        assertThat(inputStream).as("Image not found: " + imagePath).isNotNull();
        byte[] imageBytes = inputStream.readAllBytes();
        inputStream.close();

        InputStream javaInputStream = new ByteArrayInputStream(imageBytes);
        PointD javaSize = ImageMetricUtil.getImageSize(javaInputStream);

        CompletableFuture<PointD> result = staticBridge.measure(imageBytes);

        PointD flutterSize = assertCompletes(result);

        assertThat(flutterSize.x()).as("Width for " + imagePath).isEqualTo(javaSize.x());
        assertThat(flutterSize.y()).as("Height for " + imagePath).isEqualTo(javaSize.y());

        System.out.println("PASS: " + imagePath + " Java: " + javaSize + " Flutter: " + flutterSize);
    }

    static class ImageSizeBridge extends GenericSizeBridge<Map<String, String>, double[], PointD> {

        public ImageSizeBridge() {
            super("imageSize", double[].class);
        }

        public CompletableFuture<PointD> measure(byte[] imageBytes) {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            Map<String, String> request = Map.of("imageData", base64Image);
            return super.measure(request);
        }

        @Override
        protected PointD convertResult(double[] serialized) {
            return new PointD(serialized[0], serialized[1]);
        }
    }
}
