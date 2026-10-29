package dev.equo.swt;

import org.eclipse.swt.program.Program;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

/**
 * A test that builds a real web Display reaches {@link WebFlutterServer#launchBrowser}. Under the
 * unit suite that has to start headless Chrome, never the developer's default browser. Both launch
 * paths are stubbed, so the test itself opens nothing either way.
 */
@Tag("native-unit")
class WebFlutterServerUnitSuiteBrowserNativeTest {

    @TempDir
    Path webDir;

    @Test
    void aWebDisplayBuiltByATestGetsHeadlessChromeNotTheDefaultBrowser() throws Exception {
        String browser = System.getProperty("equo.swt.browser");
        System.clearProperty("equo.swt.browser");
        try (MockedStatic<Program> program = mockStatic(Program.class);
             MockedStatic<HeadlessChrome> chrome = mockStatic(HeadlessChrome.class)) {
            chrome.when(() -> HeadlessChrome.resolveBinary(any())).thenReturn("chrome");
            chrome.when(() -> HeadlessChrome.launch(anyString(), anyString(), anyBoolean(), anyBoolean(), any()))
                    .thenReturn(mock(HeadlessChrome.class));

            new WebFlutterServer.Builder()
                    .webDirectory(webDir.toFile())
                    .commPort(0)
                    .widgetId(1)
                    .widgetName("Display")
                    .build()
                    .launchBrowser();

            program.verifyNoInteractions();
            chrome.verify(() -> HeadlessChrome.launch(eq("chrome"), anyString(), eq(true), anyBoolean(), any()));
        } finally {
            if (browser != null) System.setProperty("equo.swt.browser", browser);
        }
    }
}
