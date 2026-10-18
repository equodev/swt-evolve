package dev.equo.swt;

import dev.equo.swt.harness.RecordingComm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
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

    /** The properties a pick is written to, which the flags are rebuilt from. */
    private static final String[] PICKED_PROPERTIES = {
            "swt.evolve.force_theme", "swt.evolve.theme_name", "swt.evolve.theme_color"};

    private final RecordingComm comm = new RecordingComm();
    private final Map<String, String> savedProperties = new HashMap<>();
    private ConfigFlags savedFlags;

    @BeforeEach
    void startFromTheDefaults() {
        savedFlags = Config.getConfigFlags();
        for (String key : PICKED_PROPERTIES) {
            savedProperties.put(key, System.getProperty(key));
            System.clearProperty(key);
        }
        Config.setConfigFlags(null);
    }

    @AfterEach
    void restore() {
        savedProperties.forEach((key, value) -> {
            if (value == null) System.clearProperty(key);
            else System.setProperty(key, value);
        });
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void a_pick_is_kept_and_echoed_back() throws Exception {
        pick(flags -> {
            flags.theme_name = "cursor";
            flags.force_theme = "light";
        });

        List<ConfigFlags> echoes = echoes();
        assertThat(echoes).hasSize(1);
        assertThat(echoes.get(0).theme_name).isEqualTo("cursor");
        assertThat(echoes.get(0).force_theme).isEqualTo("light");
        assertThat(Config.getConfigFlags().theme_name).isEqualTo("cursor");
    }

    @Test
    void picking_the_same_theme_again_is_not_echoed() throws Exception {
        pick(flags -> flags.theme_name = "cursor");
        pick(flags -> flags.theme_name = "cursor");

        assertThat(echoes()).hasSize(1);
    }

    private void pick(Consumer<ConfigFlags> set) {
        ConfigFlags flags = new ConfigFlags();
        set.accept(flags);
        FlutterBridge.handlePropertySetFromFlutter(comm, flags);
    }

    private List<ConfigFlags> echoes() throws Exception {
        Serializer serializer = new Serializer();
        List<ConfigFlags> echoes = new ArrayList<>();
        for (RecordingComm.Frame frame : comm.sent) {
            if (FlutterBridge.PROPERTIES_CHANNEL.equals(frame.event))
                echoes.add(serializer.from(ConfigFlags.class, frame.json.getBytes(StandardCharsets.UTF_8)));
        }
        return echoes;
    }
}
