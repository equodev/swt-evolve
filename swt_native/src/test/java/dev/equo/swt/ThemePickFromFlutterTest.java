package dev.equo.swt;

import dev.equo.swt.harness.RecordingComm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The theme picker in the main toolbar sends what the user picked on
 * {@link FlutterBridge#PROPERTY_SET_CHANNEL}; Java keeps it and echoes the properties back.
 */
class ThemePickFromFlutterTest {

    /** A pick is written to a swt.evolve.* property, which the flags are rebuilt from. */
    private static final String PICKED_PREFIX = "swt.evolve.";

    private final RecordingComm comm = new RecordingComm();
    private final Map<String, String> savedProperties = new HashMap<>();
    private ConfigFlags savedFlags;

    @BeforeEach
    void startFromTheDefaults() {
        savedFlags = Config.getConfigFlags();
        System.getProperties().forEach((k, v) -> {
            if (k.toString().startsWith(PICKED_PREFIX)) savedProperties.put(k.toString(), v.toString());
        });
        clearPicked();
        Config.setConfigFlags(null);
    }

    @AfterEach
    void restore() {
        clearPicked();
        savedProperties.forEach(System::setProperty);
        Config.setConfigFlags(savedFlags);
    }

    private static void clearPicked() {
        for (Object key : System.getProperties().keySet().toArray())
            if (key.toString().startsWith(PICKED_PREFIX)) System.clearProperty(key.toString());
    }

    @Test
    void a_pick_is_kept_and_echoed_back() throws Exception {
        pick(flags -> {
            flags.theme_name = "blackandred";
            flags.force_theme = "light";
        });

        List<Map<String, Object>> echoes = echoes();
        assertThat(echoes).hasSize(1);
        assertThat(echoes.get(0).get("theme_name")).isEqualTo("blackandred");
        assertThat(echoes.get(0).get("force_theme")).isEqualTo("light");
        assertThat(Config.getConfigFlags().theme_name).isEqualTo("blackandred");
    }

    @Test
    void picking_the_same_theme_again_is_not_echoed() throws Exception {
        Consumer<ConfigFlags> greyAndBlackDark = flags -> {
            flags.theme_name = "greyandblack";
            flags.force_theme = "dark";
        };
        pick(greyAndBlackDark);
        pick(greyAndBlackDark);

        assertThat(echoes()).hasSize(1);
    }

    private void pick(Consumer<ConfigFlags> set) {
        ConfigFlags flags = new ConfigFlags();
        set.accept(flags);
        FlutterBridge.handlePropertySetFromFlutter(comm, flags);
    }

    /** The flags Java echoed, each as its fields by name. */
    private List<Map<String, Object>> echoes() throws Exception {
        List<Map<String, Object>> echoes = new ArrayList<>();
        for (RecordingComm.Frame frame : comm.sent) {
            if (FlutterBridge.PROPERTIES_CHANNEL.equals(frame.event))
                echoes.add(WireDump.fields(frame.bytes, 0, frame.bytes.length));
        }
        return echoes;
    }
}
