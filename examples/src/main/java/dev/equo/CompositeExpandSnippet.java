package dev.equo;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * Demonstrates an expandable/collapsible section with a header (icon + label)
 * and a details panel that shows or hides on click (issue #625).
 *
 * Structural aspects that matter for the stale-parent-rebuild bug:
 * - 4 levels of nesting: compositeMain -> compositeExecution (the one that must
 *   actually resize) -> [header (icon + label), compositeExecutionDetails] as siblings.
 * - Two separate MouseDown listeners trigger the same toggle: one on the small
 *   icon-only Composite, one on the Label.
 * - The actual resize runs deferred via Display.timerExec(10, ...), not inline.
 * - Relayout only walks ONE level up (parentComposite.getParent().layout()), not
 *   all the way to the Shell.
 * - On collapse, parentComposite.setSize(x, 0) is forced before relayout.
 */
public class CompositeExpandSnippet {

    static class ExpandableButtonComposite {
        final Composite button; // icon-only Composite, no children
        final Runnable layoutCallback;
        boolean isExpanded;

        ExpandableButtonComposite(Composite parent, Runnable layoutCallback, boolean initialState) {
            this.layoutCallback = layoutCallback;
            this.isExpanded = initialState;

            this.button = new Composite(parent, SWT.NONE);
            this.button.setLayoutData(new GridData(18, 18));
            this.button.setToolTipText("Expand/Collapse Execution Information");
            updateButtonImage();

            Listener toggleListener = event -> {
                isExpanded = !isExpanded;
                updateButtonImage();
                if (layoutCallback != null) {
                    layoutCallback.run();
                }
            };
            button.addListener(SWT.MouseDown, toggleListener);
        }

        void updateButtonImage() {
            button.setBackground(button.getDisplay().getSystemColor(
                    isExpanded ? SWT.COLOR_DARK_GREEN : SWT.COLOR_GRAY));
        }

        boolean isExpanded() {
            return isExpanded;
        }

        void setExpanded(boolean expanded) {
            this.isExpanded = expanded;
            updateButtonImage();
            if (layoutCallback != null) {
                layoutCallback.run();
            }
        }

        void animateLayout(Composite detailsComposite, Composite parentComposite) {
            Display.getDefault().timerExec(10, () -> {
                if (detailsComposite.isDisposed() || parentComposite.isDisposed()) {
                    return;
                }
                detailsComposite.setVisible(isExpanded);
                Object layoutData = detailsComposite.getLayoutData();
                if (layoutData instanceof GridData) {
                    ((GridData) layoutData).exclude = !isExpanded;
                }
                if (!isExpanded) {
                    parentComposite.setSize(parentComposite.getSize().x, 0);
                }
                parentComposite.layout(true, true);
                parentComposite.getParent().layout();
            });
        }
    }

    static Composite compositeExecution;
    static Composite compositeExecutionDetails;
    static ExpandableButtonComposite expandableButton;

    static void layoutExecutionInfo() {
        expandableButton.animateLayout(compositeExecutionDetails, compositeExecution);
    }

    public static void main(String[] args) {
        ConfigFlags configFlags = new ConfigFlags();
        configFlags.use_swt_colors = true;
        Config.setConfigFlags(configFlags);
        Display display = new Display();
        Shell shell = new Shell(display);

        shell.setText("Expandable section repro (issue #625)");
        shell.setLayout(new GridLayout());
        shell.setSize(420, 320);

        // compositeMain
        Composite compositeMain = new Composite(shell, SWT.NONE);
        compositeMain.setLayout(new GridLayout(1, false));
        compositeMain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // compositeExecution: the parentComposite passed to animateLayout(). Non-grab
        // vertically; its height is whatever its children need.
        compositeExecution = new Composite(compositeMain, SWT.NONE);
        GridLayout glExecution = new GridLayout(1, true);
        glExecution.verticalSpacing = 0;
        glExecution.marginHeight = 0;
        glExecution.marginWidth = 0;
        compositeExecution.setLayout(glExecution);
        compositeExecution.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        compositeExecution.setBackground(display.getSystemColor(SWT.COLOR_RED));

        // header: sibling of compositeExecutionDetails, holds the icon + the label.
        Composite header = new Composite(compositeExecution, SWT.NONE);
        GridLayout glHeader = new GridLayout(2, false);
        glHeader.marginHeight = 0;
        glHeader.marginWidth = 0;
        header.setLayout(glHeader);
        header.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        header.setCursor(display.getSystemCursor(SWT.CURSOR_HAND));
        header.setBackground(display.getSystemColor(SWT.COLOR_RED));

        expandableButton = new ExpandableButtonComposite(header, CompositeExpandSnippet::layoutExecutionInfo, false);

        Label label = new Label(header, SWT.NONE);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        label.setText("Section Title");
        label.setBackground(display.getSystemColor(SWT.COLOR_RED));
        // Separate MouseDown listener on the Label — independent from the one on the
        // icon Composite — both call the same toggle.
        label.addListener(SWT.MouseDown, event -> expandableButton.setExpanded(!expandableButton.isExpanded()));

        // compositeExecutionDetails: sibling of header, the panel that should expand/collapse.
        compositeExecutionDetails = new Composite(compositeExecution, SWT.NONE);
        compositeExecutionDetails.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        compositeExecutionDetails.setLayout(new GridLayout(1, false));
        compositeExecutionDetails.setBackground(display.getSystemColor(SWT.COLOR_YELLOW));
        compositeExecutionDetails.setVisible(false);
        ((GridData) compositeExecutionDetails.getLayoutData()).exclude = true;

        Button detailButton = new Button(compositeExecutionDetails, SWT.PUSH);
        detailButton.setText("Implicit timeout setting");

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.dispose();
    }
}
