package dev.equo;

import org.eclipse.jface.viewers.LabelProvider;

public class ViewLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        if (element instanceof TreeNode) {
            return ((TreeNode) element).getName();
        }
        return super.getText(element);
    }
}