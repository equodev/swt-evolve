package org.eclipse.swt.custom;

import dev.equo.swt.Config;
import dev.equo.swt.SerializeTestBase;
import net.javacrumbs.jsonunit.assertj.JsonMapAssert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static org.eclipse.swt.widgets.Mocks.*;

class CTabFolderColorSerializeTest extends SerializeTestBase {

    @AfterEach
    void useEquoColor() {
        Config.useEquo(Color.class);
        System.clearProperty("dev.equo.Color");
    }

    @Test
    void should_serialize_CTabFolder_with_swtColor() {
        CTabFolder w = new CTabFolder(composite(), SWT.NONE);
        Config.useEclipse(Color.class);
        Color color = new Color(red(), green(), blue());
        w.setSelectionBackground(color);
        String json = serialize(w);
        JsonMapAssert assertJ = assertThatJson(json).isObject();
        assertJ.containsEntry("id", w.hashCode())
               .containsEntry("swt", "CTabFolder")
               .containsEntry("style", w.getStyle())
               .node("selectionBackground").isObject()
                    .containsEntry("red", color.getRed())
                    .containsEntry("green", color.getGreen())
                    .containsEntry("blue", color.getBlue())
                    .containsEntry("alpha", color.getAlpha());
    }

}
