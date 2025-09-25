package dev.equo;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    private String name;
    private List<TreeNode> children = new ArrayList<>();
    private TreeNode parent;

    public TreeNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addChild(TreeNode child) {
        children.add(child);
        child.setParent(this);
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    @Override
    public String toString() {
        return getName();
    }
}