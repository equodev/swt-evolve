package org.eclipse.swt.widgets;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

/**
 * What a page shows of the JFace dialog it presents, read from the dialog by reflection (JFace is not
 * a dependency): the title area as a heading the client draws over it, with the message and its kind;
 * a wizard's steps; and the Preferences category column, widened to its labels.
 */
public final class DialogPageParts {

    public static final int MESSAGE_NONE = 0;
    public static final int MESSAGE_INFORMATION = 1;
    public static final int MESSAGE_WARNING = 2;
    public static final int MESSAGE_ERROR = 3;

    /** The widest the Preferences category column is made to fit its labels. */
    public static final int MAX_NAVIGATION_WIDTH = 280;

    private static final String TITLE_AREA_DIALOG = "org.eclipse.jface.dialogs.TitleAreaDialog";
    private static final String WIZARD_DIALOG = "org.eclipse.jface.wizard.WizardDialog";
    private static final String PREFERENCE_DIALOG = "org.eclipse.jface.preference.PreferenceDialog";

    private DialogPageParts() {
    }

    /** Called once a page is placed: keeps its heading, steps and columns current. */
    static void attach(Shell page) {
        Object dialog = page.getData();
        if (is(dialog, TITLE_AREA_DIALOG)) {
            Control message = (Control) field(dialog, TITLE_AREA_DIALOG, "messageLabel");
            if (message != null) message.addListener(SWT.Modify, e -> changed(page));
        }
        if (is(dialog, WIZARD_DIALOG)) {
            try {
                Class<?> listenerType = dialog.getClass().getClassLoader().loadClass("org.eclipse.jface.dialogs.IPageChangedListener");
                Object listener = Proxy.newProxyInstance(listenerType.getClassLoader(), new Class<?>[] { listenerType },
                        (proxy, method, args) -> {
                            if ("pageChanged".equals(method.getName())) changed(page);
                            return method.getName().equals("hashCode") ? System.identityHashCode(proxy)
                                    : method.getName().equals("equals") ? proxy == args[0] : null;
                        });
                dialog.getClass().getMethod("addPageChangedListener", listenerType).invoke(dialog, listener);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // A WizardDialog without page change events keeps the steps it opened with.
            }
        }
        if (is(dialog, PREFERENCE_DIALOG)) widenNavigation(dialog);
        changed(page);
    }

    private static void changed(Shell page) {
        if (page.isDisposed() || !(page.getImpl() instanceof DartShell)) return;
        DialogPages.markChanged(page, VShell.PAGE_HEADING, headingHeight(page));
        DialogPages.markChanged(page, VShell.PAGE_TITLE, title(page));
        DialogPages.markChanged(page, VShell.PAGE_MESSAGE, message(page));
        DialogPages.markChanged(page, VShell.PAGE_MESSAGE_TYPE, messageType(page));
        DialogPages.markChanged(page, VShell.PAGE_STEPS, steps(page));
        DialogPages.markChanged(page, VShell.PAGE_STEP, step(page));
        DialogPages.contentChanged(page);
    }

    /** The height of the title area at the top of a placed page, which the client draws its heading over; 0 for none. */
    public static int headingHeight(Shell page) {
        if (!DialogPages.isHosted(page)) return 0;
        Object dialog = page.getData();
        Control filler = (Control) field(dialog, TITLE_AREA_DIALOG, "bottomFillerLabel");
        if (filler == null || filler.isDisposed()) return 0;
        Point bottom = page.toControl(filler.toDisplay(0, filler.getSize().y));
        return Math.max(0, bottom.y);
    }

    public static String title(Shell page) {
        Control label = headingHeight(page) > 0 ? (Control) field(page.getData(), TITLE_AREA_DIALOG, "titleLabel") : null;
        return label instanceof Label ? ((Label) label).getText() : null;
    }

    public static String message(Shell page) {
        if (headingHeight(page) == 0) return null;
        Object dialog = page.getData();
        Object error = text(dialog, "getErrorMessage", "errorMessage");
        if (error instanceof String) return (String) error;
        Object message = text(dialog, "getMessage", "message");
        return message instanceof String ? (String) message : null;
    }

    public static int messageType(Shell page) {
        if (headingHeight(page) == 0) return MESSAGE_NONE;
        Object dialog = page.getData();
        if (text(dialog, "getErrorMessage", "errorMessage") instanceof String) return MESSAGE_ERROR;
        // A wizard or preference page says its message's kind itself (IMessageProvider, the same numbers).
        Object type = invoke(invoke(dialog, "getCurrentPage"), "getMessageType");
        if (type instanceof Integer && (Integer) type >= MESSAGE_NONE && (Integer) type <= MESSAGE_ERROR) return (Integer) type;
        Object image = field(dialog, TITLE_AREA_DIALOG, "messageImage");
        if (image == null) return MESSAGE_NONE;
        if (image == dialogImage(dialog, "DLG_IMG_MESSAGE_WARNING")) return MESSAGE_WARNING;
        if (image == dialogImage(dialog, "DLG_IMG_MESSAGE_ERROR")) return MESSAGE_ERROR;
        return MESSAGE_INFORMATION;
    }

    /** A wizard's page titles, in order; the current one is added when the wizard did not list it. */
    public static String[] steps(Shell page) {
        if (!DialogPages.isHosted(page) || !is(page.getData(), WIZARD_DIALOG)) return null;
        List<Object> pages = wizardPages(page.getData());
        if (pages.size() < 2) return null;
        List<String> titles = new ArrayList<>();
        for (Object p : pages) titles.add(pageTitle(p));
        return titles.toArray(new String[0]);
    }

    /** The current step's index in {@link #steps}; -1 with no steps. */
    public static int step(Shell page) {
        if (steps(page) == null) return -1;
        Object current = invoke(page.getData(), "getCurrentPage");
        return wizardPages(page.getData()).indexOf(current);
    }

    private static List<Object> wizardPages(Object dialog) {
        List<Object> pages = new ArrayList<>();
        Object current = invoke(dialog, "getCurrentPage");
        Object all = invoke(invoke(current, "getWizard"), "getPages");
        if (all instanceof Object[]) {
            for (Object p : (Object[]) all) pages.add(p);
        }
        if (current != null && !pages.contains(current)) pages.add(current);
        return pages;
    }

    private static String pageTitle(Object wizardPage) {
        Object title = invoke(wizardPage, "getTitle");
        if (title instanceof String && !((String) title).isEmpty()) return (String) title;
        Object name = invoke(wizardPage, "getName");
        return name instanceof String ? (String) name : "";
    }

    /** Widens the Preferences category column to fit its labels, up to {@link #MAX_NAVIGATION_WIDTH}. */
    private static void widenNavigation(Object dialog) {
        Object viewer = invoke(dialog, "getTreeViewer");
        Object control = viewer == null ? null : invoke(viewer, "getControl");
        if (control instanceof Tree) widenColumn((Tree) control);
    }

    /** Widens the column {@code tree} sits in to fit its items, up to {@link #MAX_NAVIGATION_WIDTH}. */
    static void widenColumn(Tree tree) {
        // The items' width, plus what the tree itself takes around them: its border and scroll bar.
        int wanted = Math.min(MAX_NAVIGATION_WIDTH, tree.computeTrim(0, 0, labelsWidth(tree), 0).width);
        // The dialog's sash sizes the column through the layout data of the tree or of the composite
        // holding it: a GridData width hint, or a FormData width.
        for (Control c = tree; c != null && !(c instanceof Shell); c = c.getParent()) {
            Object data = c.getLayoutData();
            if (data instanceof org.eclipse.swt.layout.GridData && ((org.eclipse.swt.layout.GridData) data).widthHint > 0) {
                org.eclipse.swt.layout.GridData grid = (org.eclipse.swt.layout.GridData) data;
                if (wanted > grid.widthHint) {
                    grid.widthHint = wanted;
                    c.getParent().layout(true, true);
                }
                return;
            }
            if (data instanceof org.eclipse.swt.layout.FormData) {
                org.eclipse.swt.layout.FormData form = (org.eclipse.swt.layout.FormData) data;
                if (wanted > form.width) {
                    form.width = wanted;
                    c.getParent().layout(true, true);
                }
                return;
            }
        }
    }

    /** The width the tree's top-level items need, as Evolve measures an item. */
    static int labelsWidth(Tree tree) {
        int widest = 0;
        for (TreeItem item : tree.getItems()) {
            if (item.getImpl() instanceof DartTreeItem) {
                widest = Math.max(widest,
                        dev.equo.swt.size.TreeItemSizes.computeSize((DartTreeItem) item.getImpl(), SWT.DEFAULT, SWT.DEFAULT, false).x);
            }
        }
        return widest;
    }

    /** The image JFace registers under the key its {@code Dialog} constant {@code constant} names. */
    private static Object dialogImage(Object dialog, String constant) {
        try {
            ClassLoader loader = dialog.getClass().getClassLoader();
            Object key = loader.loadClass("org.eclipse.jface.dialogs.Dialog").getField(constant).get(null);
            Class<?> resources = loader.loadClass("org.eclipse.jface.resource.JFaceResources");
            return resources.getMethod("getImage", String.class).invoke(null, key);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static boolean is(Object object, String className) {
        for (Class<?> c = object == null ? null : object.getClass(); c != null; c = c.getSuperclass()) {
            if (c.getName().equals(className)) return true;
        }
        return false;
    }

    /** A TitleAreaDialog's message through its public getter; its field on a JFace that has no such getter. */
    private static Object text(Object dialog, String getter, String field) {
        try {
            return dialog.getClass().getMethod(getter).invoke(dialog);
        } catch (NoSuchMethodException e) {
            return field(dialog, TITLE_AREA_DIALOG, field);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static Object field(Object object, String declaringClass, String name) {
        return dev.equo.swt.DeclaredFields.read(object, declaringClass, name);
    }

    private static Object invoke(Object object, String method) {
        if (object == null) return null;
        try {
            Method m = object.getClass().getMethod(method);
            m.setAccessible(true);
            return m.invoke(object);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }
}
