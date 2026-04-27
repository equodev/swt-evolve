package dev.equo.swt;

import org.eclipse.swt.widgets.*;

import java.util.regex.Pattern;

import static dev.equo.swt.Config.*;

public class ConfigDyn {

    static boolean dynEnabled = Boolean.getBoolean("dev.equo.swt.dyn.Composite");

    static final Pattern IDREGEX = Pattern.compile("\\([^)]*\\)");

    public static IWidget getCompositeImpl(Composite parent, int style, Composite composite) {
        if (dynEnabled && ("dyn".equals(parent != null ? parent.getData(Config.getKey(Composite.class)) : null) || mustBeDynComposite(composite)))
            return new DynComposite(parent, style, composite);
        if (mainToolbarImpl == Impl.equo && isMainToolbarComposite(Composite.class, parent))
            return new DartMainToolbar(parent, style, composite);
        if (sideBarImpl == Impl.equo && isSideToolbarComposite(Composite.class, parent))
            return new DartSideBar(parent, style, composite);
        if (statusBarImpl == Impl.equo && isStatusToolbarComposite(Composite.class, parent))
            return new DartStatusBar(parent, style, composite);
        if (mainCompositeImpl == Impl.equo && isMainComposite(Composite.class, parent))
            return new DartMainComposite(parent, style, composite);
        if (defaultImpl == Impl.eclipse || forceEclipse)
            return new SwtComposite(parent, style, composite);
        if (Config.isEquo(composite.getClass(), parent))
            return new DartComposite(parent, style, composite);
        else
            return new SwtComposite(parent, style, composite);
    }

    private static boolean mustBeDynComposite(Composite composite) {
        if (composite.getClass().getName().equals("org.eclipse.e4.ui.workbench.renderers.swt.ContributedPartRenderer$1"))
            return true;
//        return isInStackTrace("org.eclipse.e4.ui.workbench.renderers.swt.ElementReferenceRenderer", "createWidget");
        return false;
    }

    public static IComposite getDynImpl(DynComposite dynComposite) {
        Composite parent = dynComposite.getParent();
        int style = dynComposite.getStyle();
        Composite composite = dynComposite.getApi();
        Object modelElementId = dynComposite.getData("modelElement");

        if (modelElementId != null) {
            String modelStr = modelElementId.toString();
            String forcedImpl;

            int contribIdx = modelStr.indexOf("contributionURI:");
            if (contribIdx >= 0) {
                String afterColon = modelStr.substring(contribIdx + "contributionURI:".length()).trim();
                int end = afterColon.length();
                for (int i = 0; i < afterColon.length(); i++) {
                    char c = afterColon.charAt(i);
                    if (c == ' ' || c == ',' || c == '\n' || c == ')') { end = i; break; }
                }
                String uri = afterColon.substring(0, end);
                int lastSlash = uri.lastIndexOf('/');
                if (lastSlash >= 0) {
                    String className = uri.substring(lastSlash + 1);
                    System.out.print("-- View: " +className);
                    forcedImpl = System.getProperty(PROPERTY_PREFIX + className);
                    if (forcedImpl != null) {
                        return Impl.equo.name().equals(forcedImpl) ? new DartComposite(parent, style, composite) : new SwtComposite(parent, style, composite);
                    }
                }
            }

            String[] parts = modelStr.split("=");
            if (parts.length > 0) {
                String id = IDREGEX.matcher(parts[0]).replaceFirst("");
                System.out.println(" -- id: " +id);
                forcedImpl = System.getProperty(PROPERTY_PREFIX+id);
                if (forcedImpl != null) {
                    return Impl.equo.name().equals(forcedImpl) ? new DartComposite(parent, style, composite) : new SwtComposite(parent, style, composite);
                }
            }
        }

        if (Config.isEquo(composite.getClass(), parent))
            return new DartComposite(parent, style, composite);
        return new SwtComposite(parent, style, composite);
    }

}
