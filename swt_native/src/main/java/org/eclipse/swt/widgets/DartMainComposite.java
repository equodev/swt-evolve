package org.eclipse.swt.widgets;

public class DartMainComposite extends DartComposite {

    public DartMainComposite(Composite parent, int style, Composite composite) {
        super(parent, style, composite);
        System.out.println("[DartMainComposite] constructed parent=" + (parent != null ? parent.getImpl().getClass().getSimpleName() : "null"));
    }
}
