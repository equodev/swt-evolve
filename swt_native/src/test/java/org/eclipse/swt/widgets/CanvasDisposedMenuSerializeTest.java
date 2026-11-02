package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static dev.equo.swt.harness.WireAssert.assertThatWire;
import java.util.Map;

class CanvasDisposedMenuSerializeTest extends SerializeTestBase {

    // SWT only clears Control.menu when the control itself is disposed, so a Menu disposed on its
    // own stays reachable from the Canvas payload. It travels as the identity stub, not as null:
    // the same path also feeds widget arrays, where a null element is undecodable on the client.
    @Test
    void should_serialize_a_disposed_Menu_as_an_identity_stub() {
        Canvas w = new Canvas(shell(), SWT.NONE);
        Menu menu = new Menu(w);
        w.setMenu(menu);
        menu.dispose();

        Map<String, Object> wire = serialize(w);

        assertThatWire(wire).isObject()
               .containsEntry("id", w.hashCode())
               .containsEntry("swt", "Canvas");
        // Named by id, as any widget is; there is nothing of a disposed one to describe.
        assertThatWire(wire).node("menu").isObject()
               .containsEntry("id", menu.hashCode());
    }
}
