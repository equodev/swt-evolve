package org.eclipse.swt.widgets;

import java.util.Arrays;

public class ControlUtils {
    static void reparent(DartControl control, Composite newParent) {
        removeFromParentChildren(control);
        control.parent = newParent;
        addToParentChildren(control);
        // Scheduled so the new parent describes the control instead of naming it. A name carries no
        // write stamp, and the stamp is what tells a move from a disposal - without one, the old
        // parent's frame, applied first, reads as the control having gone away.
        control.getBridge().dirty(control);
    }

    public static void addToParentChildren(DartControl obj) {
        if (obj.parent.getImpl() instanceof DartComposite ) { DartComposite p = (DartComposite) obj.parent.getImpl();
            Control[] before = p.children;
            Control[] newArray = p.children != null ? Arrays.copyOf(p.children, p.children.length + 1) : new Control[1];
            newArray[newArray.length - 1] = obj.getApi();
            p.children = newArray;
            // A child can be added while the parent is still being constructed, before it has a
            // value to record anything on. Scheduling it is all that can be done then, and all that
            // is needed: a parent that new is sent whole.
            if (p.getValue() != null) markChildren(p, obj.getApi(), before);
            else obj.getBridge().dirty(p);
        }
    }

    public static void addToParentChildren(IControl obj) {
        if (obj._parent().getImpl() instanceof DartComposite ) { DartComposite p = (DartComposite) obj._parent().getImpl();
            Control[] before = p.children;
            Control[] newArray = p.children != null ? Arrays.copyOf(p.children, p.children.length + 1) : new Control[1];
            newArray[newArray.length - 1] = obj.getApi();
            p.children = newArray;
            if (p.getValue() != null) markChildren(p, obj.getApi(), before);
        }
    }

    /** Takes [control] out of [p]'s children, as a composite does when a child is disposed. */
    public static void removeChild(DartComposite p, Control control) {
        if (p.children == null) return;
        for (int i = 0; i < p.children.length; i++) {
            if (p.children[i] == control) {
                Control[] before = p.children;
                Control[] newChildren = new Control[p.children.length - 1];
                System.arraycopy(p.children, 0, newChildren, 0, i);
                System.arraycopy(p.children, i + 1, newChildren, i, p.children.length - i - 1);
                p.children = newChildren;
                markChildren(p, control, before);
                return;
            }
        }
    }

    /**
     * Marks the children [p] sends, unless [child] is one [p] draws itself: those are left out of
     * the list it sends (a CCombo's text, list and arrow), which their arrival or order leaves as it was.
     *
     * <p>The mark is taken back when the changes since the last send add up to no change
     * ({@link dev.equo.swt.FlutterBridge#markChange}), as two reorders that undo each other do: a
     * CTabFolder brings one toolbar to the top and then the other on every layout. A list named as
     * changed is sent, with each changed child in full, and the client rebuilds the composite.
     */
    private static void markChildren(DartComposite p, Control child, Control[] before) {
        if (p._drawsItself(child)) return;
        VComposite value = p.getValue();
        // A value that keeps no record of its changes (a test's stand-in) is only marked.
        if (value.changedKeys() == null) {
            value.markDirty(VComposite.CHILDREN);
            return;
        }
        p.childrenMark = dev.equo.swt.FlutterBridge.markChange(p, VComposite.CHILDREN, before, p.children,
                p.childrenMark);
        // Still a change: it waits for the next flush, as any mark does, even one already recorded.
        if (value.changedKeys().contains(VComposite.CHILDREN)) value.markDirty(VComposite.CHILDREN);
    }

    public static void updateChildrenOrderOnMove(Control movedControl, Control referenceControl, boolean moveAbove) {
        if (movedControl == null) {
            return;
        }

        // Moving a control above/below itself is a no-op (matches SWT). It must be handled here because
        // otherwise movedControl is its own reference: it is removed from the children array yet the
        // reference index still points past the shortened array, overflowing the reindex arraycopy below.
        if (movedControl == referenceControl) {
            return;
        }

        Composite parent = movedControl.getParent();
        if (parent == null) {
            return;
        }

        if (!(parent.getImpl() instanceof DartComposite)) {
            return;
        }
        DartComposite parentImpl = (DartComposite) parent.getImpl();

        if (parentImpl.children == null) {
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

        // Re-inserting at the index the control was removed from reproduces the original order.
        // E4 layout passes re-assert an already-correct z-order on every relayout; dirtying the
        // parent for that re-serializes its whole subtree with unchanged content.
        if (newIndex == movedIndex) {
            return;
        }

        Control[] finalChildren = new Control[newChildren.length + 1];
        System.arraycopy(newChildren, 0, finalChildren, 0, newIndex);
        finalChildren[newIndex] = movedControl;
        System.arraycopy(newChildren, newIndex, finalChildren, newIndex + 1, newChildren.length - newIndex);

        parentImpl.children = finalChildren;
        if (parentImpl.getValue() != null) markChildren(parentImpl, movedControl, children);
        else ((DartControl) movedControl.getImpl()).getBridge().dirty(parentImpl);
    }

    private static void removeFromParentChildren(DartControl obj) {
        if (obj.parent.getImpl() instanceof DartComposite) { DartComposite p = (DartComposite) obj.parent.getImpl();
            Control[] before = p.children;
            for (int i = 0; i < p.children.length; i++) {
                if (p.children[i] == obj.getApi()) {
                    Control[] newChildren = new Control[p.children.length - 1];
                    System.arraycopy(p.children, 0, newChildren, 0, i);
                    System.arraycopy(p.children, i + 1, newChildren, i, p.children.length - i - 1);
                    p.children = newChildren;
                    break;
                }
            }
            if (p.getValue() != null) markChildren(p, obj.getApi(), before);
            else obj.getBridge().dirty(p);
        }
    }
}
