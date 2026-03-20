package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.*;

public class CustomSashLayoutExample {

    static class SimpleSashLayout extends Layout {

        private int sashWidth = 5;
        private int weight = 30;

        @Override
        protected void layout(Composite parent, boolean flushCache) {
            Control[] children = parent.getChildren();
            if (children.length < 3) return;

            Control left = children[0];
            Sash sash = (Sash) children[1];
            Control right = children[2];

            int width = parent.getClientArea().width;
            int height = parent.getClientArea().height;

            int leftWidth = width * weight / 100;
            int rightWidth = width - leftWidth - sashWidth;

            left.setBounds(0, 0, leftWidth, height);
            sash.setBounds(leftWidth, 0, sashWidth, height);
            right.setBounds(leftWidth + sashWidth, 0, rightWidth, height);
        }

        @Override
        protected org.eclipse.swt.graphics.Point computeSize(Composite parent, int wHint, int hHint, boolean flushCache) {
            return new org.eclipse.swt.graphics.Point(600, 400);
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }
    }

    public static void main(String[] args) {
        System.setProperty("dev.equo.swt.debug", "true");
//        Config.forceEclipse();
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Custom SashLayout Simulation");
        shell.setSize(800, 600);
        shell.setLayout(new FillLayout()); // 🔥 clave

        Composite root = new Composite(shell, SWT.NONE);
        SimpleSashLayout layout = new SimpleSashLayout();
        root.setLayout(layout);

        Text left = new Text(root, SWT.MULTI | SWT.BORDER);
        left.setText("Left panel");

        Sash sash = new Sash(root, SWT.VERTICAL);

        Text right = new Text(root, SWT.MULTI | SWT.BORDER);
        right.setText("Right panel");

        sash.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int width = root.getClientArea().width;
                int newWeight = (int) ((e.x / (double) width) * 100);
                layout.setWeight(newWeight);
                root.layout();
            }
        });

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}