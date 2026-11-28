package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import java.util.List;

/**
 * The view rails' pop-up menus, as SWT menus on the main shell.
 *
 * <p>A view's rail button: the workbench's own menu for the view's tab (close, close others,
 * detach), then minimize, maximize or restore, the view menu, and moving the view to another rail
 * end. A rail: show a view at that end, attach a detached view there, switch perspective, and the
 * rails' size.
 *
 * <p>The workbench is driven through its own services and renderer, found by reflection: e4 is not
 * a dependency.
 */
final class ViewRailsMenus {

    static final String[] PLACE_NAMES = {"Left Top", "Left Bottom", "Right Top", "Right Bottom"};
    static final String[] SIZE_NAMES = {"Small", "Medium", "Large"};

    private static final String PART_SERVICE = "org.eclipse.e4.ui.workbench.modeling.EPartService";
    private static final String MODEL_SERVICE = "org.eclipse.e4.ui.workbench.modeling.EModelService";
    private static final String COMMAND_SERVICE = "org.eclipse.e4.core.commands.ECommandService";
    private static final String HANDLER_SERVICE = "org.eclipse.e4.core.commands.EHandlerService";
    private static final String PART_STACK = "org.eclipse.e4.ui.model.application.ui.basic.MPartStack";
    private static final String SHOW_VIEW = "org.eclipse.ui.views.showView";
    private static final String SHOW_PERSPECTIVE = "org.eclipse.ui.perspectives.showPerspective";

    /** EModelService's insertion sides. */
    private static final int BELOW = 1, LEFT_OF = 2, RIGHT_OF = 3;

    /** The share, as a fraction, of the space it splits that a stack the rails create takes. */
    private static final float NEW_STACK_RATIO = 0.3f;

    private final ViewRails rails;

    /**
     * The last pop-up menu this class created, disposed when the next one opens: a Hide arrives
     * before the Selection it closed for, so the menu cannot go on Hide. The workbench's own tab
     * menu is not tracked; the workbench reuses it and clears its items on every call.
     */
    private Menu created;

    ViewRailsMenus(ViewRails rails) {
        this.rails = rails;
    }

    /** The menu of the view at {@code index} of {@code folder}, at {@code (x, y)} on its rail. */
    void viewMenu(DartCTabFolder folder, int index, int x, int y) {
        Shell shell = rails.shell();
        if (shell == null || folder.isDisposed() || index < 0 || index >= folder.getItemCount()) return;
        CTabFolder api = folder.getApi();
        CTabItem item = api.getItem(index);
        Object element = item.getData(ViewRails.MODEL_ELEMENT);
        Object part = partOf(element);
        Control rail = rails.current.railOn(sideOf(rails.current.placeOf(api)));
        if (rail == null || rail.isDisposed() || part == null) return;

        Menu menu = workbenchTabMenu(api, part);
        if (menu == null) menu = create(shell);
        if (menu.getItemCount() > 0) new MenuItem(menu, SWT.SEPARATOR);

        boolean minimized = rails.minimized(folder);
        boolean maximized = api.getMaximized();
        if (!minimized && api.getMinimizeVisible()) {
            push(menu, "Minimize", () -> CTabFolderHelper.handleMinimize(folder, new Event()));
        }
        if (!maximized && api.getMaximizeVisible()) {
            push(menu, "Maximize", () -> {
                showView(folder, index);
                if (!folder.isDisposed()) CTabFolderHelper.handleMaximize(folder, new Event());
            });
        }
        if (minimized || maximized) {
            push(menu, "Restore", () -> CTabFolderHelper.handleRestore(folder, new Event()));
        }
        if (hasViewMenu(part)) push(menu, "View Menu", () -> openViewMenu(folder, index, part));

        new MenuItem(menu, SWT.SEPARATOR);
        Menu moveTo = cascade(menu, "Move To");
        int current = rails.current.placeOf(api);
        for (int place = ViewRails.LEFT_TOP; place <= ViewRails.RIGHT_BOTTOM; place++) {
            int target = place;
            MenuItem move = radio(moveTo, PLACE_NAMES[place], place == current,
                    () -> moveTo(element, api, target));
            move.setEnabled(place != current);
        }
        open(menu, rail, x, y);
    }

    /** The menu of {@code rail}, for its top ({@code end} 0) or bottom end, at {@code (x, y)} on it. */
    void railMenu(Control rail, int end, int x, int y) {
        Shell shell = rails.shell();
        if (shell == null) return;
        int side = rails.current.sideOf(rail);
        if (side == 0) return;
        int place = ViewRails.place(side == SWT.RIGHT, end != 0);

        Menu menu = create(shell);
        push(menu, "Show View...", () -> showViewAt(place));

        Object window = shell.getData(ViewRails.MODEL_ELEMENT);
        List<Object> detached = window == null ? java.util.Collections.emptyList() : detachedViews(window);
        if (!detached.isEmpty()) {
            Menu attach = cascade(menu, "Attach");
            for (Object element : detached) {
                Object part = partOf(element);
                Object label = part == null ? null : rails.model.call(part, "getLocalizedLabel");
                push(attach, String.valueOf(label), () -> moveTo(element, null, place));
            }
        }

        Object perspectives = window == null ? null : rails.model.perspectiveStack(window);
        Menu perspective = cascade(menu, "Perspective");
        if (perspectives != null) {
            Object selected = rails.model.call(perspectives, "getSelectedElement");
            Object children = rails.model.call(perspectives, "getChildren");
            if (children instanceof List) {
                for (Object p : (List<?>) children) {
                    Object label = rails.model.call(p, "getLocalizedLabel");
                    if (label == null) label = rails.model.call(p, "getLabel");
                    radio(perspective, String.valueOf(label), p == selected, () -> switchPerspective(p));
                }
            }
        }
        new MenuItem(perspective, SWT.SEPARATOR);
        push(perspective, "Other...", () -> runCommand(SHOW_PERSPECTIVE));

        new MenuItem(menu, SWT.SEPARATOR);
        Menu size = cascade(menu, "Rail Size");
        for (int s = ViewRails.SMALL; s <= ViewRails.LARGE; s++) {
            int target = s;
            radio(size, SIZE_NAMES[s], s == rails.size(), () -> rails.resize(target));
        }
        open(menu, rail, x, y);
    }

    // --- actions ------------------------------------------------------------------------------

    private void showView(DartCTabFolder folder, int index) {
        rails.show(folder, index);
    }

    /** Shows the view, then opens the menu its header's view-menu button opens. */
    private void openViewMenu(DartCTabFolder folder, int index, Object part) {
        showView(folder, index);
        folder.getApi().getDisplay().asyncExec(() -> {
            if (folder.isDisposed()) return;
            ToolItem viewMenu = viewMenuItem(folder.getApi().getTopRight(), part);
            Object renderer = renderer(folder.getApi());
            if (viewMenu == null || renderer == null) return;
            try {
                rails.model.invoke(renderer, "showMenu", viewMenu);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // The view menu stays reachable from the header.
            }
        });
    }

    /**
     * Moves the view to the stack at {@code place}: the first one listed there, or a stack created
     * beside the editor area (the right rail's bottom end goes under the right top stack).
     */
    private void moveTo(Object element, CTabFolder from, int place) {
        Object window = window();
        Object models = service(window, MODEL_SERVICE);
        if (models == null || element == null) return;
        try {
            CTabFolder existing = rails.current.stackAt(place, from);
            Object target = existing == null ? null : existing.getData(ViewRails.MODEL_ELEMENT);
            if (target == null) target = createStack(models, window, element, place);
            if (target == null) return;
            rails.model.invoke(models, "move", element, target);
            rails.model.invoke(target, "setSelectedElement", element);
            activate(window, partOf(element));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // The view stays where it is.
        }
    }

    /**
     * A view's button dropped on a rail: view {@code index} of {@code from} goes into the listed stack
     * the client knows as {@code targetId}, before its view {@code at} ({@code -1}: last), or, with no
     * such stack, to {@code place} as Move To takes it there.
     */
    void drop(DartCTabFolder from, int index, int place, long targetId, int at) {
        if (from.isDisposed() || index < 0 || index >= from.getItemCount()) return;
        CTabFolder api = from.getApi();
        Object element = api.getItem(index).getData(ViewRails.MODEL_ELEMENT);
        CTabFolder target = rails.current.stackById(targetId);
        if (target == null) {
            if (place >= ViewRails.LEFT_TOP && place <= ViewRails.RIGHT_BOTTOM) moveTo(element, api, place);
            return;
        }
        Object stack = target.getData(ViewRails.MODEL_ELEMENT);
        Object window = window();
        Object models = service(window, MODEL_SERVICE);
        if (models == null || element == null || stack == null) return;
        Object before = at >= 0 && at < target.getItemCount() ? target.getItem(at).getData(ViewRails.MODEL_ELEMENT) : null;
        int position = insertionIndex(rails.model.call(stack, "getChildren"), element, before);
        try {
            rails.model.invoke(models, "move", element, stack, position);
            rails.model.invoke(stack, "setSelectedElement", element);
            activate(window, partOf(element));
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // The view stays where it is.
        }
    }

    /**
     * Where {@code element} goes among {@code children} to land before {@code before} once it has left
     * its own place: one less when it comes from further up the same stack. {@code -1}, last, when
     * {@code before} is not among them.
     */
    static int insertionIndex(Object children, Object element, Object before) {
        if (!(children instanceof List) || before == null) return -1;
        List<?> list = (List<?>) children;
        int at = list.indexOf(before);
        if (at < 0) return -1;
        int current = list.indexOf(element);
        return current >= 0 && current < at ? at - 1 : at;
    }

    private Object createStack(Object models, Object window, Object element, int place) throws ReflectiveOperationException {
        Object area = rails.model.editorArea(window);
        Object relTo = area;
        int where;
        switch (place) {
            case ViewRails.LEFT_TOP: where = LEFT_OF; break;
            case ViewRails.LEFT_BOTTOM: where = BELOW; break;
            case ViewRails.RIGHT_BOTTOM: {
                CTabFolder rightTop = rails.current.stackAt(ViewRails.RIGHT_TOP, null);
                Object stack = rightTop == null ? null : rightTop.getData(ViewRails.MODEL_ELEMENT);
                if (stack != null) {
                    relTo = stack;
                    where = BELOW;
                } else {
                    where = RIGHT_OF;
                }
                break;
            }
            default: where = RIGHT_OF;
        }
        if (relTo == null) return null;
        Class<?> stackType = element.getClass().getClassLoader().loadClass(PART_STACK);
        Object stack = rails.model.invoke(models, "createModelElement", stackType);
        rails.model.invoke(models, "insert", stack, relTo, where, NEW_STACK_RATIO);
        return stack;
    }

    /** Runs Show View, then moves the view it opened to {@code place} when it went elsewhere. */
    private void showViewAt(int place) {
        Object parts = service(window(), PART_SERVICE);
        Object before = parts == null ? null : rails.model.call(parts, "getActivePart");
        runCommand(SHOW_VIEW);
        if (parts == null) return;
        Object shown = rails.model.call(parts, "getActivePart");
        if (shown == null || shown == before) return;
        Object element = rails.model.call(shown, "getCurSharedRef");
        if (element == null) element = shown;
        Object stack = rails.model.call(element, "getParent");
        Object widget = stack == null ? null : rails.model.call(stack, "getWidget");
        if (!(widget instanceof CTabFolder)) return;
        if (rails.current.placeOf((CTabFolder) widget) < 0) return;
        if (rails.current.placeOf((CTabFolder) widget) != place) moveTo(element, (CTabFolder) widget, place);
    }

    private void switchPerspective(Object perspective) {
        Object parts = service(window(), PART_SERVICE);
        if (parts == null) return;
        try {
            rails.model.invoke(parts, "switchPerspective", perspective);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // The perspective stays as it is.
        }
    }

    private void runCommand(String id) {
        Object window = window();
        Object commands = service(window, COMMAND_SERVICE);
        Object handlers = service(window, HANDLER_SERVICE);
        if (commands == null || handlers == null) return;
        try {
            Object command = rails.model.invoke(commands, "createCommand", id, null);
            if (command != null) rails.model.invoke(handlers, "executeHandler", command);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // A command the workbench does not have is not offered by it either.
        }
    }

    private void activate(Object window, Object part) throws ReflectiveOperationException {
        Object parts = service(window, PART_SERVICE);
        if (parts != null && part != null) rails.model.invoke(parts, "activate", part);
    }

    // --- the workbench ------------------------------------------------------------------------

    /**
     * The views in the windows detached from {@code window}, each as its stack holds it: e4 keeps a
     * detached window under the current perspective, or under the window itself.
     */
    private List<Object> detachedViews(Object window) {
        List<Object> views = new java.util.ArrayList<>();
        collectWindows(window, views);
        Object perspectives = rails.model.perspectiveStack(window);
        Object perspective = perspectives == null ? null : rails.model.call(perspectives, "getSelectedElement");
        if (perspective != null) collectWindows(perspective, views);
        return views;
    }

    private void collectWindows(Object owner, List<Object> views) {
        Object windows = rails.model.call(owner, "getWindows");
        if (windows instanceof List) {
            for (Object detached : (List<?>) windows) collectViews(detached, views);
        }
    }

    private void collectViews(Object element, List<Object> views) {
        if (!Boolean.TRUE.equals(rails.model.call(element, "isToBeRendered"))) return;
        Object children = rails.model.call(element, "getChildren");
        if (!(children instanceof List)) return;
        boolean stack = rails.model.is(element, "MPartStack");
        for (Object child : (List<?>) children) {
            if (stack) {
                if (Boolean.TRUE.equals(rails.model.call(child, "isToBeRendered"))) views.add(child);
            } else {
                collectViews(child, views);
            }
        }
    }

    /** The workbench's own pop-up menu for {@code part}'s tab, filled for it; null when unavailable. */
    private Menu workbenchTabMenu(CTabFolder folder, Object part) {
        Object renderer = renderer(folder);
        if (renderer == null) return null;
        try {
            Object menu = rails.model.invoke(renderer, "createTabMenu", folder, part);
            if (!(menu instanceof Menu)) return null;
            ((Menu) menu).setData("stack_selected_part", part);
            return (Menu) menu;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private Object renderer(CTabFolder folder) {
        Object stack = folder.getData(ViewRails.MODEL_ELEMENT);
        return stack == null ? null : rails.model.call(stack, "getRenderer");
    }

    private boolean hasViewMenu(Object part) {
        Object menus = rails.model.call(part, "getMenus");
        if (!(menus instanceof List)) return false;
        for (Object menu : (List<?>) menus) {
            if (rails.model.hasTag(menu, "ViewMenu")) return true;
        }
        return false;
    }

    /** The view-menu button of {@code part} among the header's tool bars. */
    private static ToolItem viewMenuItem(Control topRight, Object part) {
        if (topRight == null || topRight.isDisposed()) return null;
        if (topRight instanceof ToolBar) {
            for (ToolItem item : ((ToolBar) topRight).getItems()) {
                if (item.getData("thePart") == part) return item;
            }
            return null;
        }
        if (topRight instanceof org.eclipse.swt.widgets.Composite) {
            for (Control child : ((org.eclipse.swt.widgets.Composite) topRight).getChildren()) {
                ToolItem found = viewMenuItem(child, part);
                if (found != null) return found;
            }
        }
        return null;
    }

    /** The main window's model element, or null. */
    private Object window() {
        Shell shell = rails.shell();
        return shell == null || shell.isDisposed() ? null : shell.getData(ViewRails.MODEL_ELEMENT);
    }

    private Object service(Object window, String name) {
        Object context = window == null ? null : rails.model.call(window, "getContext");
        if (context == null) return null;
        try {
            return rails.model.invoke(context, "get", name);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private Object partOf(Object element) {
        if (element == null) return null;
        return rails.model.is(element, "MPlaceholder") ? rails.model.call(element, "getRef") : element;
    }

    private static int sideOf(int place) {
        return ViewRails.isRight(place) ? SWT.RIGHT : SWT.LEFT;
    }

    // --- menu building ------------------------------------------------------------------------

    private static MenuItem push(Menu menu, String text, Runnable action) {
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText(text);
        item.addListener(SWT.Selection, e -> later(menu, action));
        return item;
    }

    private static MenuItem radio(Menu menu, String text, boolean selected, Runnable action) {
        MenuItem item = new MenuItem(menu, SWT.RADIO);
        item.setText(text);
        item.setSelection(selected);
        // The item selected to begin with also gets a Selection, as it is deselected: it stands for
        // what is already so.
        if (!selected) item.addListener(SWT.Selection, e -> later(menu, action));
        return item;
    }

    private Menu create(Shell shell) {
        dispose();
        created = new Menu(shell, SWT.POP_UP);
        return created;
    }

    /** Disposes the last menu this class created, when it is still there. */
    void dispose() {
        if (created != null && !created.isDisposed()) created.dispose();
        created = null;
    }

    private static Menu cascade(Menu menu, String text) {
        MenuItem item = new MenuItem(menu, SWT.CASCADE);
        item.setText(text);
        Menu sub = new Menu(menu);
        item.setMenu(sub);
        return sub;
    }

    /** After the menu has closed: an action may open a dialog of its own or rebuild the rails. */
    private static void later(Menu menu, Runnable action) {
        menu.getDisplay().asyncExec(action);
    }

    private static void open(Menu menu, Control rail, int x, int y) {
        Point at = rail.toDisplay(x, y);
        menu.setLocation(at.x, at.y);
        menu.setVisible(true);
    }
}
