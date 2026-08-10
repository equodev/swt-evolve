package dev.equo.swt;

import dev.equo.swt.comm.CommService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Debug-gated bridge action that runs a reflective action on the app's SWT UI (Display) thread.
 * Registered from {@link FlutterBridge#newComm()} only when {@link Config#isDebug()} is on.
 *
 * <p>It reaches app surfaces the Flutter action layer cannot drive — chiefly a dialog opened from a
 * native OS menu (a Preferences dialog behind macOS Cmd+, / Window &rarr; Preferences), which no
 * synthetic Flutter gesture can trigger. Once such a dialog is open its content is Flutter-rendered
 * again, so the existing action layer takes over.
 *
 * <p><b>Application-agnostic by construction.</b> The concrete opener/registry class names, the
 * builder style and the page id all arrive <i>in the payload</i>, supplied by the caller (the private
 * tooling side). This class hard-codes only generic Eclipse/JFace shapes — a preference-dialog
 * <i>builder</i> and Eclipse's {@code PreferencesUtil} — selected by the payload's {@code style};
 * it names no application and carries no product-specific literal.
 *
 * <p>Wire — Flutter &rarr; Java on {@link #CHANNEL} ({@code swt.evolve.test.runUi}), a JSON object:
 * <pre>
 *   { "action":"openPreferences", "opener":"&lt;fqcn&gt;", "style":"builder"|"util", "id":"&lt;pageId?&gt;" }
 *   { "action":"openPreferences", "style":"registry", "registry":"&lt;fqcn&gt;", "xpField":"&lt;staticField&gt;", "id":"&lt;pageId?&gt;" }
 *   { "action":"listPrefPages",  "registry":"&lt;fqcn&gt;", "xpField":"&lt;staticField?&gt;" }
 *   { "action":"openProject",    "project":"&lt;path&gt;", "eventClass":"&lt;fqcn&gt;", "topicField":"&lt;staticField&gt;" }
 *   { "action":"openFile",       "path":"&lt;absolute file path&gt;" }
 * </pre>
 * A {@code listPrefPages} result is returned on {@link #RESPONSE_CHANNEL}
 * ({@code swt.evolve.test.runUiResponse}) and also logged as a {@code chk:} line for the run log.
 */
final class TestUiRunner {

    static final String CHANNEL = "swt.evolve.test.runUi";
    static final String RESPONSE_CHANNEL = "swt.evolve.test.runUiResponse";

    private TestUiRunner() {
    }

    /** Registered from {@link FlutterBridge#newComm()} when debugging is on. */
    static void handle(CommService comm, Object payload) {
        if (!(payload instanceof Map)) {
            DebugLog.checkpoint(CHANNEL, "ignored: non-map payload", payload);
            return;
        }
        Map<?, ?> m = (Map<?, ?>) payload;
        String action = str(m.get("action"));
        if (action == null) {
            DebugLog.checkpoint(CHANNEL, "ignored: no action");
            return;
        }
        Display d = Display.getDefault();
        if (d == null || d.isDisposed()) {
            DebugLog.checkpoint(CHANNEL, action, "no display");
            return;
        }
        // asyncExec, NOT syncExec: a dialog's open() runs its own modal event loop, so a syncExec
        // would block the comm (network) thread until the dialog closes; async returns immediately
        // and the modal loop keeps pumping so the action layer can still drive the dialog's controls.
        d.asyncExec(() -> {
            try {
                switch (action) {
                    case "openPreferences" -> openPreferences(m);
                    case "listPrefPages" -> listPrefPages(comm, m);
                    case "openProject" -> openProject(m);
                    case "openFile" -> openFile(m);
                    default -> DebugLog.checkpoint(CHANNEL, "unknown action:", action);
                }
            } catch (Throwable t) {
                DebugLog.checkpoint(CHANNEL, action + " failed:", t);
            }
        });
    }

    // ---- actions ------------------------------------------------------------

    private static void openPreferences(Map<?, ?> m) throws Exception {
        String style = str(m.get("style"));
        String id = str(m.get("id"));
        Shell shell = activeShell();
        // "registry" style: build a plain JFace PreferenceDialog on a preference manager from an
        // app-supplied registry + extension-point constant (see buildPreferenceManager). This reaches
        // preference registries outside Eclipse's PreferencesUtil set and the project-settings builder
        // — e.g. an app's general (non-project) settings pages. Only generic JFace types are named here;
        // the concrete registry/xpField arrive in the payload.
        if ("registry".equalsIgnoreCase(style)) {
            Object pm = buildPreferenceManager(m);
            if (pm == null) {
                DebugLog.checkpoint(CHANNEL, "openPreferences[registry]: null preference manager");
                return;
            }
            Class<?> dlgCls = loadAppClass("org.eclipse.jface.preference.PreferenceDialog");
            Class<?> pmCls = loadAppClass("org.eclipse.jface.preference.PreferenceManager");
            Object dialog = dlgCls.getConstructor(Shell.class, pmCls).newInstance(shell, pm);
            if (id != null && !id.isEmpty()) {
                // setSelectedNode only stores the target id; the tree selection is applied by open()'s
                // createContents, so it must be set BEFORE open(), not after.
                dlgCls.getMethod("setSelectedNode", String.class).invoke(dialog, id);
            }
            DebugLog.checkpoint(CHANNEL, "openPreferences[registry]",
                    id == null || id.isEmpty() ? "(default page)" : id);
            dlgCls.getMethod("open").invoke(dialog);
            return;
        }
        String opener = str(m.get("opener"));
        if (opener == null) {
            DebugLog.checkpoint(CHANNEL, "openPreferences: no opener class in payload");
            return;
        }
        Class<?> oc = loadAppClass(opener);
        if (oc == null) {
            DebugLog.checkpoint(CHANNEL, "openPreferences: cannot load opener", opener);
            return;
        }
        Object dialog;
        if ("util".equalsIgnoreCase(style)) {
            // Eclipse: PreferencesUtil.createPreferenceDialogOn(shell, pageId, displayedIds, data)
            Method createOn = oc.getMethod("createPreferenceDialogOn",
                    Shell.class, String.class, String[].class, Object.class);
            dialog = createOn.invoke(null, shell, emptyToNull(id), null, null);
        } else {
            // JFace builder: create().addShell(shell)[.addSelectedNode(id)].build()
            Object b = oc.getMethod("create").invoke(null);
            b = oc.getMethod("addShell", Shell.class).invoke(b, shell);
            if (id != null && !id.isEmpty()) {
                b = oc.getMethod("addSelectedNode", String.class).invoke(b, id);
            }
            dialog = oc.getMethod("build").invoke(b);
        }
        if (dialog == null) {
            DebugLog.checkpoint(CHANNEL, "openPreferences: opener returned null dialog");
            return;
        }
        DebugLog.checkpoint(CHANNEL, "openPreferences",
                id == null || id.isEmpty() ? "(default page)" : id,
                "via", style == null ? "builder" : style);
        // Window#open() — starts the (modal) dialog; keeps pumping events so `act` can drive it.
        dialog.getClass().getMethod("open").invoke(dialog);
    }

    /**
     * Opens an application project so project-scoped surfaces (e.g. some preference pages) can build.
     * Mirrors the app's own recent-project flow — post a workbench event carrying the project path —
     * rather than reaching into a controller. The event topic is read at runtime from an app-supplied
     * constant ({@code eventClass}#{@code topicField}), and delivery is through the standard E4
     * {@code IEventBroker}, so nothing here names an application.
     */
    private static void openProject(Map<?, ?> m) throws Exception {
        String project = str(m.get("project"));
        String eventClass = str(m.get("eventClass"));
        String topicField = str(m.get("topicField"));
        if (project == null || eventClass == null || topicField == null) {
            DebugLog.checkpoint(CHANNEL, "openProject: need project + eventClass + topicField");
            return;
        }
        Class<?> platformUi = loadAppClass("org.eclipse.ui.PlatformUI");
        Object workbench = platformUi.getMethod("getWorkbench").invoke(null);
        Class<?> iWorkbench = loadAppClass("org.eclipse.ui.IWorkbench");
        Object window = iWorkbench.getMethod("getActiveWorkbenchWindow").invoke(workbench);
        Class<?> eclipseCtx = loadAppClass("org.eclipse.e4.core.contexts.IEclipseContext");
        Class<?> iWorkbenchWindow = loadAppClass("org.eclipse.ui.IWorkbenchWindow");
        Object ctx = iWorkbenchWindow.getMethod("getService", Class.class).invoke(window, eclipseCtx);
        Class<?> brokerCls = loadAppClass("org.eclipse.e4.core.services.events.IEventBroker");
        Object broker = eclipseCtx.getMethod("get", Class.class).invoke(ctx, brokerCls);
        if (broker == null) {
            DebugLog.checkpoint(CHANNEL, "openProject: no IEventBroker in workbench context");
            return;
        }
        Object topic = loadAppClass(eventClass).getField(topicField).get(null);
        DebugLog.checkpoint(CHANNEL, "openProject sending", topic, "->", project);
        Object sent = brokerCls.getMethod("send", String.class, Object.class).invoke(broker, topic, project);
        DebugLog.checkpoint(CHANNEL, "openProject send returned", sent);
    }

    /** Opens a file in its default editor via {@code IDE.openEditorOnFileStore}. */
    private static void openFile(Map<?, ?> m) throws Exception {
        String path = str(m.get("path"));
        if (path == null) {
            DebugLog.checkpoint(CHANNEL, "openFile: no path in payload");
            return;
        }
        Class<?> platformUi = loadAppClass("org.eclipse.ui.PlatformUI");
        Object workbench = platformUi.getMethod("getWorkbench").invoke(null);
        Class<?> iWorkbench = loadAppClass("org.eclipse.ui.IWorkbench");
        Object window = iWorkbench.getMethod("getActiveWorkbenchWindow").invoke(workbench);
        Class<?> iWorkbenchWindow = loadAppClass("org.eclipse.ui.IWorkbenchWindow");
        Class<?> iWorkbenchPage = loadAppClass("org.eclipse.ui.IWorkbenchPage");
        Object page = iWorkbenchWindow.getMethod("getActivePage").invoke(window);
        Class<?> efs = loadAppClass("org.eclipse.core.filesystem.EFS");
        Class<?> iFileStore = loadAppClass("org.eclipse.core.filesystem.IFileStore");
        Object fileStore = efs.getMethod("getStore", java.net.URI.class).invoke(null, new java.io.File(path).toURI());
        Class<?> ideCls = loadAppClass("org.eclipse.ui.ide.IDE");
        Method openEditor = ideCls.getMethod("openEditorOnFileStore", iWorkbenchPage, iFileStore);
        DebugLog.checkpoint(CHANNEL, "openFile", path);
        Object editorPart = openEditor.invoke(null, page, fileStore);
        DebugLog.checkpoint(CHANNEL, "openFile opened", editorPart);
    }

    /**
     * Builds a JFace {@code PreferenceManager} from an app-supplied preference {@code registry} class
     * and an optional {@code xpField} extension-point constant (both in the payload), or null when no
     * registry is given. The registry is a DI-constructed object bound to the workbench's
     * {@code IEclipseContext}, so it is built the same way the app's own code does — all through
     * standard Eclipse/JFace types, each resolved through the app's bundles (see {@link #loadAppClass}).
     * Shared by {@link #listPrefPages} (enumerate the pages) and the {@code registry}-style
     * {@link #openPreferences} (open a dialog on them).
     */
    private static Object buildPreferenceManager(Map<?, ?> m) throws Exception {
        String registryClass = str(m.get("registry"));
        String xpField = str(m.get("xpField"));
        if (registryClass == null) {
            DebugLog.checkpoint(CHANNEL, "buildPreferenceManager: no registry class in payload");
            return null;
        }
        Class<?> platformUi = loadAppClass("org.eclipse.ui.PlatformUI");
        Object workbench = platformUi.getMethod("getWorkbench").invoke(null);
        Class<?> iWorkbench = loadAppClass("org.eclipse.ui.IWorkbench");
        Object window = iWorkbench.getMethod("getActiveWorkbenchWindow").invoke(workbench);
        Class<?> eclipseCtx = loadAppClass("org.eclipse.e4.core.contexts.IEclipseContext");
        Class<?> iWorkbenchWindow = loadAppClass("org.eclipse.ui.IWorkbenchWindow");
        Object ctx = iWorkbenchWindow.getMethod("getService", Class.class).invoke(window, eclipseCtx);
        Class<?> cif = loadAppClass("org.eclipse.e4.core.contexts.ContextInjectionFactory");
        Class<?> registry = loadAppClass(registryClass);
        Object reg = cif.getMethod("make", Class.class, eclipseCtx).invoke(null, registry, ctx);
        Object xp = xpField != null ? registry.getField(xpField).get(null) : null;
        return registry.getMethod("getPreferenceManager", String.class).invoke(reg, xp);
    }

    private static void listPrefPages(CommService comm, Map<?, ?> m) throws Exception {
        Object pm = buildPreferenceManager(m);
        if (pm == null) {
            DebugLog.checkpoint(CHANNEL, "listPrefPages: null preference manager");
            return;
        }
        Class<?> pmCls = loadAppClass("org.eclipse.jface.preference.PreferenceManager");
        int preOrder = pmCls.getField("PRE_ORDER").getInt(null);
        List<?> nodes = (List<?>) pmCls.getMethod("getElements", int.class).invoke(pm, preOrder);
        Class<?> nodeItf = loadAppClass("org.eclipse.jface.preference.IPreferenceNode");
        Method getId = nodeItf.getMethod("getId");
        Method getLabel = nodeItf.getMethod("getLabelText");

        StringBuilder json = new StringBuilder("{\"action\":\"listPrefPages\",\"pages\":[");
        boolean first = true;
        for (Object node : nodes) {
            if (node == null) continue;
            if (!first) json.append(',');
            first = false;
            json.append("{\"id\":").append(quote(str(getId.invoke(node))))
                    .append(",\"label\":").append(quote(str(getLabel.invoke(node)))).append('}');
        }
        json.append("]}");
        byte[] bytes = json.toString().getBytes(StandardCharsets.UTF_8);
        DebugLog.checkpoint(CHANNEL, "listPrefPages", nodes.size(), "pages:", json);
        comm.send(RESPONSE_CHANNEL, bytes);
    }

    // ---- helpers ------------------------------------------------------------

    private static Shell activeShell() {
        Display d = Display.getDefault();
        Shell s = d.getActiveShell();
        if (s == null) {
            Shell[] shells = d.getShells();
            if (shells != null && shells.length > 0) s = shells[0];
        }
        return s;
    }

    /**
     * Loads an application class the bridge's own bundle can't see (OSGi isolation), or null.
     *
     * <p>Two naive routes fail on a real Equinox app, learned the hard way: the UI thread's context
     * loader can't see the app's UI bundles, and our own {@code -hyb} bundle is a resolved-but-never-
     * <i>started</i> library, so its {@code getBundleContext()} is null — there's no context to
     * enumerate bundles from. So we go through the Equinox module container instead: our class loader's
     * {@code getBundle()} yields our {@code EquinoxBundle}; {@code getModule().getContainer()} reaches
     * the shared {@code ModuleContainer}; each module's wiring exposes a {@code BundleWiring}'s class
     * loader. We ask each such loader to load the class until one resolves it. Everything is by method
     * name on the concrete returned objects, so there's no compile or load dependency on OSGi, and it
     * no-ops cleanly in a non-OSGi build.
     */
    private static Class<?> loadAppClass(String className) {
        // Cheap first try: the context loader (works in some hosts, and in non-OSGi test runs).
        ClassLoader ctx = Thread.currentThread().getContextClassLoader();
        if (ctx != null) {
            try {
                return ctx.loadClass(className);
            } catch (Throwable ignore) {
                // fall through to the module-wiring walk
            }
        }
        for (ClassLoader cl : appClassLoaders()) {
            try {
                return cl.loadClass(className);
            } catch (Throwable ignore) {
                // not this bundle; keep looking
            }
        }
        // Last resort: our own loader (only resolves classes visible to this bundle).
        try {
            return Class.forName(className, true, TestUiRunner.class.getClassLoader());
        } catch (Throwable t) {
            return null;
        }
    }

    /** Cached per-bundle class loaders from the Equinox module container (see {@link #loadAppClass}). */
    private static volatile List<ClassLoader> appClassLoaders;

    private static List<ClassLoader> appClassLoaders() {
        List<ClassLoader> cached = appClassLoaders;
        if (cached != null) return cached;
        List<ClassLoader> loaders = new ArrayList<>();
        try {
            Object bundle = invoke0(TestUiRunner.class.getClassLoader(), "getBundle"); // EquinoxBundle
            Object module = bundle == null ? null : invoke0(bundle, "getModule");       // Module
            Object container = module == null ? null : invoke0(module, "getContainer"); // ModuleContainer
            Object modules = container == null ? null : invoke0(container, "getModules"); // List<Module>
            if (modules instanceof List) {
                for (Object mod : (List<?>) modules) {
                    ClassLoader cl = classLoaderOfModule(mod);
                    if (cl != null) loaders.add(cl);
                }
            }
        } catch (Throwable t) {
            DebugLog.checkpoint(CHANNEL, "appClassLoaders lookup failed:", t);
        }
        DebugLog.checkpoint(CHANNEL, "appClassLoaders resolved", loaders.size(), "bundle loaders");
        appClassLoaders = loaders;
        return loaders;
    }

    /** A module's {@code BundleWiring} class loader, or null if unresolved / unavailable. */
    private static ClassLoader classLoaderOfModule(Object module) {
        try {
            Object revision = invoke0(module, "getCurrentRevision");                    // ModuleRevision
            Object wiring = revision == null ? null : invoke0(revision, "getWiring");   // ModuleWiring
            Object cl = wiring == null ? null : invoke0(wiring, "getClassLoader");      // BundleWiring
            return cl instanceof ClassLoader ? (ClassLoader) cl : null;
        } catch (Throwable t) {
            return null;
        }
    }

    /** Invoke a public no-arg method by name on {@code target} (setAccessible for internal impls). */
    private static Object invoke0(Object target, String method) throws Exception {
        Method mth = target.getClass().getMethod(method);
        mth.setAccessible(true);
        return mth.invoke(target);
    }

    private static String str(Object o) {
        return o == null ? null : o.toString();
    }

    private static String emptyToNull(String s) {
        return s == null || s.isEmpty() ? null : s;
    }

    /** Minimal JSON string quoting for the ids/labels in a listPrefPages response. */
    private static String quote(String s) {
        if (s == null) return "null";
        StringBuilder b = new StringBuilder(s.length() + 2).append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> {
                    if (c < 0x20) b.append(String.format("\\u%04x", (int) c));
                    else b.append(c);
                }
            }
        }
        return b.append('"').toString();
    }
}
