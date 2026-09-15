package org.eclipse.swt.graphics;

import java.lang.ref.*;
import java.lang.ref.Cleaner.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

public class VResource {

    protected VResource() {
    }

    protected VResource(IResource impl) {
        this.impl = impl;
    }

    protected IResource impl;

    /**
     * Wire names of the properties changed since the last send.
     */
    private final java.util.Set<String> changed = new java.util.LinkedHashSet<>();

    /**
     * Records that {@code key} changed and schedules the widget for the next flush. Both, always: a flag without a flush never ships, and a flush without a flag ships an update that claims nothing changed.
     */
    public void markDirty(String key) {
        changed.add(key);
        if (impl instanceof DartResource d)
            d.dirty();
    }

    /**
     * The properties changed since the last send, in the order they changed.
     */
    public java.util.Set<String> changedKeys() {
        return changed;
    }

    public boolean anyDirty() {
        return !changed.isEmpty();
    }

    /**
     * Called once the widget has been sent; what follows is a fresh set of changes.
     */
    public void clearDirty() {
        changed.clear();
    }

    /**
     * Write stamp of the last state sent for this value. A partial update names it, so the far side can tell whether the update fits the state it holds.
     */
    private long sentSeq_ = 0L;

    /**
     * Which connection {@link #sentSeq_} was sent to. Delivery is a fact about one client, not about this object: a frame written for one connection tells a second nothing, and naming a widget it was never given costs it a round trip to ask for the widget again. One id rather than a set because a value is almost always written for one client; a second client re-describes it, which is the safe direction to be wrong in.
     */
    private int sentConn_ = 0;

    /**
     * The stamp this value carries for {@code connection}, or zero when that connection has not been given it.
     */
    public long sentSeq(int connection) {
        return sentConn_ == connection ? sentSeq_ : 0L;
    }

    /**
     * Records that the state stamped {@code seq} has been sent whole to {@code connection}, so nothing is outstanding for it and its next update is measured from here.
     */
    public void sent(int connection, long seq) {
        sentConn_ = connection;
        sentSeq_ = seq;
        changed.clear();
    }

    /**
     * Writes the changed properties, and only those. Each pair leaves a trailing comma for the caller to overwrite with the closing brace, as the full writer does.
     */
    public void writeDiff(JsonWriter writer) {
        for (String key : changed) writeProperty(writer, key);
    }

    /**
     * Writes one property by its wire name. Each class handles the properties it declares and passes the rest up, so a subclass never has to know what it inherited. A name nothing recognises writes nothing: it can only have come from a version that knows a property this one does not.
     */
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
        }
    }

    @JsonConverter(target = Resource.class)
    public static class ResourceJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartResource.class, (JsonWriter.WriteObject<DartResource>) (writer, impl) -> {
                if (impl == null || impl.isDisposed())
                    writer.writeNull();
                else
                    writer.serializeObject(impl.getValue());
            });
            json.registerReader(DartResource.class, (JsonReader.ReadObject<DartResource>) reader -> {
                return null;
            });
        }

        public static Resource read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, Resource api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
