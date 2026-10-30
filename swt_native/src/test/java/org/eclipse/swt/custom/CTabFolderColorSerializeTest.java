package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.SerializeTestBase;
import dev.equo.swt.harness.WireAssert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static dev.equo.swt.harness.WireAssert.assertThatWire;
import static org.eclipse.swt.widgets.Mocks.*;

class CTabFolderColorSerializeTest extends SerializeTestBase {

    @AfterEach
    void useEquoColor() {
        Config.useEquo(Color.class);
        System.clearProperty("dev.equo.Color");
    }

    @Test
    void should_serialize_CTabFolder_with_swtColor() {
        CTabFolder w = new CTabFolder(shell(), SWT.NONE);
        Config.useEclipse(Color.class);
        Color color = new Color(device(), red(), green(), blue());
        w.setSelectionBackground(color);
        java.util.Map<String, Object> state = serialize(w);
        WireAssert assertJ = assertThatWire(state).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CTabFolder")
               .containsEntry("style", w.getStyle())
               .node("selectionBackground").isEqualTo(String.format("#%02x%02x%02x%02x", color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()));
    }

}
