package org.eclipse.swt.custom;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.*;
import static org.eclipse.swt.widgets.Mocks.*;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;

/**
 * Regression test for issue #610. Per-item tooltips handed to CCombo via
 * setData("org.eclipse.swt.custom.CCombo.itemToolTips", String[]) must reach the serialized value
 * (itemTooltips) the Flutter dropdown renders. Fails before the fix, where setData does not
 * intercept the key and itemTooltips is not a serialized property.
 */
class CComboTooltipSerializeTest extends SerializeTestBase {

    private static final String ITEM_TOOLTIPS_KEY = "org.eclipse.swt.custom.CCombo.itemToolTips";

    @Test
    void setData_populates_serialized_itemTooltips() {
        CCombo w = new CCombo(swtShell(), SWT.NONE);
        w.setItems(new String[] {"click", "sendKeys"});
        String[] tooltips = {"<h4>click</h4><p>Clicks it.</p>", "Types keys."};
        w.setData(ITEM_TOOLTIPS_KEY, tooltips);

        String json = serialize(w);
        assertThatJson(json).node("itemTooltips").isArray().hasSize(2);
        assertThatJson(json).node("itemTooltips[0]").isEqualTo(tooltips[0]);
        assertThatJson(json).node("itemTooltips[1]").isEqualTo(tooltips[1]);
    }

    @Test
    void setData_updates_serialized_itemTooltips() {
        CCombo w = new CCombo(swtShell(), SWT.NONE);
        w.setData(ITEM_TOOLTIPS_KEY, new String[] {"first"});
        w.setData(ITEM_TOOLTIPS_KEY, new String[] {"second", "third"});

        String json = serialize(w);
        assertThatJson(json).node("itemTooltips").isArray().hasSize(2);
        assertThatJson(json).node("itemTooltips[0]").isEqualTo("second");
    }
}
