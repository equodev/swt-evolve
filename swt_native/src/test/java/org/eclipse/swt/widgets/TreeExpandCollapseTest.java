package org.eclipse.swt.widgets; /**
 * Simple test to verify tree expand/collapse behavior for multi-level trees
 * This test creates a 3-level tree structure and verifies that expand/collapse
 * operations work correctly for all levels.
 */
import org.eclipse.swt.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class TreeExpandCollapseTest {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Tree Expand/Collapse Test");
        shell.setLayout(new FillLayout());
        
        Tree tree = new Tree(shell, SWT.BORDER);
        
        // Create a 3-level tree structure
        for (int i = 0; i < 3; i++) {
            TreeItem level1 = new TreeItem(tree, SWT.NONE);
            level1.setText("Level 1 - Item " + i);
            
            for (int j = 0; j < 3; j++) {
                TreeItem level2 = new TreeItem(level1, SWT.NONE);
                level2.setText("Level 2 - Item " + j);
                
                for (int k = 0; k < 3; k++) {
                    TreeItem level3 = new TreeItem(level2, SWT.NONE);
                    level3.setText("Level 3 - Item " + k);
                }
            }
        }
        
        // Add listeners to verify expand/collapse behavior
        tree.addTreeListener(new TreeAdapter() {
            @Override
            public void treeExpanded(TreeEvent e) {
                TreeItem item = (TreeItem) e.item;
                System.out.println("Expanded: " + item.getText() + 
                                 " (Has " + item.getItemCount() + " children)");
                printTreeState(tree);
            }
            
            @Override
            public void treeCollapsed(TreeEvent e) {
                TreeItem item = (TreeItem) e.item;
                System.out.println("Collapsed: " + item.getText() + 
                                 " (Has " + item.getItemCount() + " children)");
                printTreeState(tree);
            }
        });
        
        tree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TreeItem item = (TreeItem) e.item;
                System.out.println("Selected: " + item.getText());
            }
        });
        
        shell.setSize(400, 300);
        shell.open();
        
        System.out.println("=== Tree Expand/Collapse Test Started ===");
        System.out.println("Try expanding and collapsing items at different levels");
        printTreeState(tree);
        
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        
        display.dispose();
    }
    
    /**
     * Prints the current state of all visible items in the tree
     */
    private static void printTreeState(Tree tree) {
        System.out.println("--- Current Tree State ---");
        TreeItem[] rootItems = tree.getItems();
        int visibleIndex = 0;
        
        for (TreeItem rootItem : rootItems) {
            visibleIndex = printItemState(rootItem, 0, visibleIndex);
        }
        
        System.out.println("Total visible items: " + visibleIndex);
        System.out.println("--------------------------");
    }
    
    /**
     * Recursively prints the state of a tree item and its visible children
     */
    private static int printItemState(TreeItem item, int level, int visibleIndex) {
        String indent = "  ".repeat(level);
        String state = item.getExpanded() ? "[+]" : "[-]";
        if (item.getItemCount() == 0) {
            state = "[ ]"; // Leaf node
        }
        
        System.out.println(indent + state + " [" + visibleIndex + "] " + item.getText());
        visibleIndex++;
        
        // Only process children if the item is expanded
        if (item.getExpanded()) {
            TreeItem[] children = item.getItems();
            for (TreeItem child : children) {
                visibleIndex = printItemState(child, level + 1, visibleIndex);
            }
        }
        
        return visibleIndex;
    }
}