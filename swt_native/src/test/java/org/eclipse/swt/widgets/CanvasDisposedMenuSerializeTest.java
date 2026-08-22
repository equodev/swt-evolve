package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import static org.eclipse.swt.widgets.Mocks.*;
import org.junit.jupiter.api.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;

class CanvasDisposedMenuSerializeTest extends SerializeTestBase {

    // SWT only clears Control.menu when the control itself is disposed, so a Menu disposed on its
    // own stays reachable from the Canvas payload. It travels as the identity stub, not as null:
    // the same path also feeds widget arrays, where a null element is undecodable on the client.
    @Test
    void should_serialize_a_disposed_Menu_as_an_identity_stub() {
        Canvas w = new Canvas(swtShell(), SWT.NONE);
        Menu menu = new Menu(w);
        w.setMenu(menu);
        menu.dispose();

        String json = serialize(w);

        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "Canvas");
        assertThatJson(json).node("menu").isObject()
               .containsOnlyKeys("id", "swt", "seq", "style")
               .containsEntry("swt", "Menu");
    }
}
