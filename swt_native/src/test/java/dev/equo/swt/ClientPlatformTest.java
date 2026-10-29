package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;

/** The client's OS, as Flutter reports it, is named the way SWT names platforms. */
class ClientPlatformTest {

    @Test
    void appleClientsAreCocoa() {
        assertThat(ClientPlatform.fromTargetPlatform("macOS")).isEqualTo("cocoa");
        assertThat(ClientPlatform.fromTargetPlatform("iOS")).isEqualTo("cocoa");
    }

    @Test
    void windowsClientsAreWin32() {
        assertThat(ClientPlatform.fromTargetPlatform("windows")).isEqualTo("win32");
    }

    @Test
    void otherKnownClientsAreGtk() {
        assertThat(ClientPlatform.fromTargetPlatform("linux")).isEqualTo("gtk");
        assertThat(ClientPlatform.fromTargetPlatform("android")).isEqualTo("gtk");
        assertThat(ClientPlatform.fromTargetPlatform("fuchsia")).isEqualTo("gtk");
    }

    @Test
    void anUnreportedOrUnknownClientHasNone() {
        assertThat(ClientPlatform.fromTargetPlatform(null)).isNull();
        assertThat(ClientPlatform.fromTargetPlatform("ohos")).isNull();
    }

    @Test
    void withoutADisplayItIsTheApplicationPlatform() {
        assertThat(ClientPlatform.of(null)).isEqualTo(ServerPlatform.get());
        assertThat(ClientPlatform.isMac(null)).isEqualTo("cocoa".equals(ServerPlatform.get()));
    }

    @Test
    void theSwtPlatformWithoutASessionIsTheApplicationPlatform() {
        assertThat(SWT.getPlatform()).isEqualTo(ServerPlatform.get());
    }
}
