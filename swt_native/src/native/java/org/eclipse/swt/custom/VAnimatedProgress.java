package org.eclipse.swt.custom;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VAnimatedProgress extends VCanvas {

    protected VAnimatedProgress() {
    }

    protected VAnimatedProgress(DartAnimatedProgress impl) {
        super(impl);
    }

    @JsonConverter(target = AnimatedProgress.class)
    public static class AnimatedProgressJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartAnimatedProgress.class, (JsonWriter.WriteObject<DartAnimatedProgress>) (writer, impl) -> {
                Serializer.writeWithId(json, writer, impl);
            });
            json.registerReader(DartAnimatedProgress.class, (JsonReader.ReadObject<DartAnimatedProgress>) reader -> {
                return null;
            });
        }

        public static AnimatedProgress read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, AnimatedProgress api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
