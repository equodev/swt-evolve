package dev.equo.swt;

import dev.equo.swt.comm.CommService;
import dev.equo.swt.comm.CommServiceProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CommTransportSelectionTest {

    @Test
    void theProviderNamedByCommImplCreatesTheComm() {
        CommService provided = mock(CommService.class);
        CommService comm = FlutterBridge.newTransport("other", List.of(
                provider("unused", mock(CommService.class)),
                provider("other", provided)));
        assertThat(comm).isSameAs(provided);
    }

    private static CommServiceProvider provider(String name, CommService comm) {
        return new CommServiceProvider() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public CommService create() {
                return comm;
            }
        };
    }
}
