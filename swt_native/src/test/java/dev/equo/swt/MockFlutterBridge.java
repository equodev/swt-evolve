package dev.equo.swt;

import org.eclipse.swt.widgets.DartWidget;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class MockFlutterBridge extends FlutterBridge {

    @Override
    public void destroy(DartWidget control) {
    }

    static public class Extension implements BeforeEachCallback, AfterEachCallback {
        @Override
        public void beforeEach(ExtensionContext context) throws Exception {
            FlutterBridge.set(new MockFlutterBridge());
        }

        @Override
        public void afterEach(ExtensionContext context) throws Exception {
            FlutterBridge.set(null);
        }
    }
}
