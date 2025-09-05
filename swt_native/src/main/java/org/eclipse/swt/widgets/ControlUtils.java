package org.eclipse.swt.widgets;

import java.util.Arrays;

public class ControlUtils {
    static void reparent(DartControl control, Composite newParent) {
        removeFromParentChildren(control);
        control.parent = newParent;
        addToNewParentChildren(control);
    }

    private static void addToNewParentChildren(DartControl obj) {
        if (obj.parent.getImpl() instanceof DartComposite p ) {
            Control[] newArray = p.children != null ? Arrays.copyOf(p.children, p.children.length + 1) : new Control[1];
            newArray[newArray.length - 1] = obj.getApi();
            p.children = newArray;
            obj.getBridge().dirty(p);
        }
    }

    private static void removeFromParentChildren(DartControl obj) {
        if (obj.parent.getImpl() instanceof DartComposite p) {
            for (int i = 0; i < p.children.length; i++) {
                if (p.children[i] == obj.getApi()) {
                    Control[] newChildren = new Control[p.children.length - 1];
                    System.arraycopy(p.children, 0, newChildren, 0, i);
                    System.arraycopy(p.children, i + 1, newChildren, i, p.children.length - i - 1);
                    p.children = newChildren;
                    break;
                }
            }
            obj.getBridge().dirty(p);
        }
    }
}