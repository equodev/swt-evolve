package dev.equo.swt;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

public final class LatestEvent {

    private final AtomicReference<Event> pending = new AtomicReference<>();

    public void post(Display display, Event event, Consumer<Event> handler) {
        if (pending.getAndSet(event) != null)
            return;
        display.asyncExec(() -> {
            Event latest = pending.getAndSet(null);
            if (latest != null)
                handler.accept(latest);
        });
    }
}
