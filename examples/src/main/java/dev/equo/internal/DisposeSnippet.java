package dev.equo.internal;

import org.eclipse.swt.widgets.*;

public class DisposeSnippet {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Dispose test");

        TestSwtFlutterBridge bridge = new TestSwtFlutterBridge(shell);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }

        bridge.destroy(null);
        display.dispose();
    }

    static class TestSwtFlutterBridge extends SwtFlutterBridge {
        public TestSwtFlutterBridge(Shell parent) {
            super(null);
            initFlutterView(parent);
        }

        private void initFlutterView(Shell parent) {
            context = InitializeFlutterWindow(client.getPort(), getHandle(parent), 0, "", "", 0, 0);
        }

        @Override
        public void destroy(DartWidget wid) {
            Dispose(context);
        }
    }

}