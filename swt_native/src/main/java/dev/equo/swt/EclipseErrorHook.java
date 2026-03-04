package dev.equo.swt;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Hooks into Eclipse's error handling via reflection to capture errors
 * caught by SafeRunner and other Eclipse framework components.
 * All Eclipse dependencies are accessed via reflection so this class
 * works when Eclipse is not present (standalone SWT apps).
 */
class EclipseErrorHook {

    private static final int ISTATUS_ERROR = 4;

    static void tryInstall() {
        try {
            Class<?> platformClass = Class.forName("org.eclipse.core.runtime.Platform");
            Class<?> logListenerClass = Class.forName("org.eclipse.core.runtime.ILogListener");
            Class<?> statusClass = Class.forName("org.eclipse.core.runtime.IStatus");

            Method getSeverity = statusClass.getMethod("getSeverity");
            Method getException = statusClass.getMethod("getException");

            Object listener = Proxy.newProxyInstance(
                logListenerClass.getClassLoader(),
                new Class<?>[] { logListenerClass },
                (proxy, method, args) -> {
                    if ("logging".equals(method.getName()) && args != null && args.length >= 1) {
                        try {
                            Object status = args[0];
                            int severity = (int) getSeverity.invoke(status);
                            Throwable exception = (Throwable) getException.invoke(status);
                            if (severity >= ISTATUS_ERROR && exception != null) {
                                CrashReporter.handleError(exception);
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                    return null;
                }
            );

            Method addLogListener = platformClass.getMethod("addLogListener", logListenerClass);
            addLogListener.invoke(null, listener);
        } catch (Throwable e) {
            // Eclipse runtime not available, silently skip
        }
    }
}