package org.eclipse.swt.widgets;

import java.util.Arrays;

public class ControlUtils {
    static void reparent(DartControl control, Composite newParent) {
        removeFromParentChildren(control);
        control.parent = newParent;
        addToParentChildren(control);
    }

    public static void addToParentChildren(DartControl obj) {
        if (obj.parent.getImpl() instanceof DartComposite p ) {
            Control[] newArray = p.children != null ? Arrays.copyOf(p.children, p.children.length + 1) : new Control[1];
            newArray[newArray.length - 1] = obj.getApi();
            p.children = newArray;
            obj.getBridge().dirty(p);
        }
    }
    
    public static void updateChildrenOrderOnMove(Control movedControl, Control referenceControl, boolean moveAbove) {
        if (movedControl == null) {
            return;
        }
        
        Composite parent = movedControl.getParent();
        if (parent == null) {
            return;
        }
        
        DartComposite parentImpl = (DartComposite) parent.getImpl();
        if (parentImpl == null || parentImpl.children == null) {
            return;
        }
        
        if (!(parentImpl instanceof DartMainToolbar)) {
            return;
        }
        
        Control[] children = parentImpl.children;
        int movedIndex = -1;
        int referenceIndex = -1;
        
        for (int i = 0; i < children.length; i++) {
            if (children[i] == movedControl) {
                movedIndex = i;
            }
            if (referenceControl != null && children[i] == referenceControl) {
                referenceIndex = i;
            }
        }
        
        if (movedIndex == -1) {
            return;
        }
        
        Control[] newChildren = new Control[children.length - 1];
        System.arraycopy(children, 0, newChildren, 0, movedIndex);
        System.arraycopy(children, movedIndex + 1, newChildren, movedIndex, children.length - movedIndex - 1);
        
        int adjustedReferenceIndex = referenceIndex;
        if (referenceControl != null && referenceIndex != -1 && movedIndex < referenceIndex) {
            adjustedReferenceIndex = referenceIndex - 1;
        }
        
        int newIndex;       
        if (moveAbove) {
            if (referenceControl == null) {
                newIndex = 0;
            } else if (referenceIndex == -1) {
                return;
            } else {
                newIndex = adjustedReferenceIndex;
            }
        } else {
            if (referenceControl == null) {
                newIndex = newChildren.length;
            } else if (referenceIndex == -1) {
                return;
            } else {
                newIndex = adjustedReferenceIndex + 1;
            }
        }

        Control[] finalChildren = new Control[newChildren.length + 1];
        System.arraycopy(newChildren, 0, finalChildren, 0, newIndex);
        finalChildren[newIndex] = movedControl;
        System.arraycopy(newChildren, newIndex, finalChildren, newIndex + 1, newChildren.length - newIndex);
        
        parentImpl.children = finalChildren;
        ((DartControl) movedControl.getImpl()).getBridge().dirty(parentImpl);
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