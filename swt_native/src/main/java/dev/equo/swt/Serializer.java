package dev.equo.swt;

import com.dslplatform.json.DslJson;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DartButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Serializer {
    private final DslJson<Object> dsl;

    public Serializer() {
        DslJson.Settings<Object> settings = new DslJson.Settings<>();
        settings.includeServiceLoader();
        settings.skipDefaultValues(true);
        dsl = new DslJson<>(settings);
    }

    public void to(Object p, ByteArrayOutputStream out) throws IOException {
        dsl.serialize(p, out);
    }
}
