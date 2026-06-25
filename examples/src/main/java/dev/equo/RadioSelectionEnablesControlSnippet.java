package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * A Group with 3 RADIO buttons -- "No parent", "Use parent object" and "Shadow root
 * parent" -- where the latter two each have an associated "Browse" button that must
 * be enabled only while their own radio is selected.
 *
 * <p>Reported bug: clicking a radio enables the Browse button belonging to the
 * <em>previously</em> selected radio instead of the one just clicked -- i.e. the
 * enabled Browse button lags one click behind the radio actually selected. This only
 * reproduces with the Dart/Flutter (equo) backend; the SWT native backend behaves
 * correctly, hence {@code Config.useEquo(...)} below.
 *
 * <p>A {@code parentObjectId}/{@code isParentObjectShadowRoot} pair (not
 * {@code Button.getSelection()}) drives which Browse composite is enabled, with a
 * per-radio guard to ignore the Selection event SWT also fires for the radio(s) a
 * click deselects.
 */
public class RadioSelectionEnablesControlSnippet {

    private enum ParentObjectType { NO_PARENT, PARENT_IFRAME, PARENT_SHADOW_ROOT }

    private static String parentObjectId;
    private static boolean isParentObjectShadowRoot;

    private static Button rdoNoParent, rdoUseParentObject, rdoShadowRootParent;
    private static Button btnBrowseParentObj, btnBrowseParentShadowRoot;
    private static Composite compositeParentObject, compositeShadowRootParent;

    private static ParentObjectType getParentObjectType() {
        if (parentObjectId != null && isParentObjectShadowRoot) return ParentObjectType.PARENT_SHADOW_ROOT;
        if (parentObjectId != null) return ParentObjectType.PARENT_IFRAME;
        return ParentObjectType.NO_PARENT;
    }

    private static void refreshParentObjectComposites() {
        ParentObjectType current = getParentObjectType();
        boolean iframeEnabled = current == ParentObjectType.PARENT_IFRAME;
        boolean shadowEnabled = current == ParentObjectType.PARENT_SHADOW_ROOT;
        // Every descendant's own enabled flag is set explicitly, since there is no
        // native OS-level enable cascading for Dart-backed controls.
        compositeParentObject.setEnabled(iframeEnabled);
        btnBrowseParentObj.setEnabled(iframeEnabled);
        compositeShadowRootParent.setEnabled(shadowEnabled);
        btnBrowseParentShadowRoot.setEnabled(shadowEnabled);
        System.out.println("[state] type=" + current + " bb(iframe).enabled=" + btnBrowseParentObj.getEnabled()
                + " bb(shadowRoot).enabled=" + btnBrowseParentShadowRoot.getEnabled());
    }

    /** Switches the active selection to "Use parent object". */
    private static void checkUseParentObject() {
        if (getParentObjectType() == ParentObjectType.PARENT_IFRAME) return; // guard
        if (parentObjectId == null) parentObjectId = "";
        isParentObjectShadowRoot = false;
        refreshParentObjectComposites();
    }

    /** Switches the active selection to "Shadow root parent". */
    private static void checkUseParentShadowRootObject() {
        if (getParentObjectType() == ParentObjectType.PARENT_SHADOW_ROOT) return; // guard
        if (parentObjectId == null) parentObjectId = "";
        isParentObjectShadowRoot = true;
        refreshParentObjectComposites();
    }

    /** Switches the active selection to "No parent". */
    private static void checkNoParentObject() {
        parentObjectId = null;
        isParentObjectShadowRoot = false;
        refreshParentObjectComposites();
    }

    public static void main(String[] args) {
        Config.useEquo(Button.class);
        Config.useEquo(Composite.class);
        Config.useEquo(Group.class);

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("RadioButtonSnippet");
        shell.setLayout(new GridLayout(1, false));

        Group haveParentGroup = new Group(shell, SWT.NONE);
        haveParentGroup.setText("Have parent object?");
        haveParentGroup.setLayout(new GridLayout(2, false));
        haveParentGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        rdoNoParent = new Button(haveParentGroup, SWT.RADIO);
        rdoNoParent.setText("No parent");
        rdoNoParent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        rdoUseParentObject = new Button(haveParentGroup, SWT.RADIO);
        rdoUseParentObject.setText("Use parent object");
        compositeParentObject = new Composite(haveParentGroup, SWT.NONE);
        compositeParentObject.setLayout(new GridLayout(1, false));
        compositeParentObject.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        btnBrowseParentObj = new Button(compositeParentObject, SWT.FLAT);
        btnBrowseParentObj.setText("Browse");

        rdoShadowRootParent = new Button(haveParentGroup, SWT.RADIO);
        rdoShadowRootParent.setText("Shadow root parent");
        compositeShadowRootParent = new Composite(haveParentGroup, SWT.NONE);
        compositeShadowRootParent.setLayout(new GridLayout(1, false));
        compositeShadowRootParent.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        btnBrowseParentShadowRoot = new Button(compositeShadowRootParent, SWT.FLAT);
        btnBrowseParentShadowRoot.setText("Browse");

        rdoUseParentObject.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(SelectionEvent e) { checkUseParentObject(); }
        });
        rdoShadowRootParent.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(SelectionEvent e) { checkUseParentShadowRootObject(); }
        });
        rdoNoParent.addSelectionListener(new SelectionAdapter() {
            @Override public void widgetSelected(SelectionEvent e) { checkNoParentObject(); }
        });

        parentObjectId = null;
        isParentObjectShadowRoot = false;
        refreshParentObjectComposites();

        shell.setSize(420, 220);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}
