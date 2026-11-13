package dev.equo.swt.awt;

import dev.equo.swt.Config;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.SystemFlavorMap;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/** The data of an SWT drag as an AWT drop target reads it. */
class SwtTransferableTest {

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @Test
    void anRtfDragDropsItsBytesOnAFlavorThatReadsAStream() throws Exception {
        DataFlavor flavor = new DataFlavor("text/rtf;class=java.io.InputStream");
        SystemFlavorMap map = (SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap();
        for (String name : TransferHelper.typeNames(RTFTransfer.getInstance())) map.addFlavorForUnencodedNative(name, flavor);
        String rtf = "{\\rtf1 Item A}";

        AwtDropBridge.SwtTransferable dropped =
                new AwtDropBridge.SwtTransferable(new Transfer[] {RTFTransfer.getInstance()}, RTFTransfer.getInstance(), rtf);

        try (InputStream in = (InputStream) dropped.getTransferData(flavor)) {
            assertThat(in.readAllBytes()).isEqualTo(rtf.getBytes(StandardCharsets.UTF_8));
        }
    }
}
