package dev.equo;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
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
//            super.onReady(this);
            context = InitializeFlutterWindow(client.getPort(), getHandle(parent), id(this), widgetName(this), "", 0, 0);
        }

        @Override
        protected long getHandle(Control control) {
            return super.getHandle(control);
        }

        @Override
        protected void setHandle(DartControl control, long view) {
            super.setHandle(control, view);
        }

        @Override
        public Object container(DartComposite parent) {
            return super.container(parent);
        }

        @Override
        public void setBounds(DartControl dartControl, Rectangle bounds) {
            super.setBounds(dartControl, bounds);
        }

        @Override
        public void setFocus(DartControl dartControl) {
            super.setFocus(dartControl);
        }

        @Override
        public boolean hasFocus(DartControl control) {
            return super.hasFocus(control);
        }

        @Override
        public void destroy(DartWidget wid) {
            Dispose(context);
        }

        @Override
        protected void destroyHandle(DartControl control) {
            super.destroyHandle(control);
        }

        @Override
        public Point getWindowOrigin(DartControl control) {
            return super.getWindowOrigin(control);
        }

        @Override
        public void setCursor(DartControl control, long cursor) {
            super.setCursor(control, cursor);
        }
    }

}