package com.equo.chromium.swt;

import org.eclipse.swt.widgets.Composite;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Loads a class from a named OSGi bundle at runtime (bypassing import-package
 * wiring via Equinox internals) and wraps instances of it in a
 * {@link Proxy} for a given interface, without any compile-time dependency on
 * OSGi APIs.
 *
 * <p>Create one instance per (interface, bundle, class) triple. The instance
 * caches the reflectively-loaded class so OSGi navigation only happens once.
 */
class ProxyCreator {

    private final Class<?> proxyInterface;
    private final String bundleName;
    private final String targetClassName;

    private volatile Class<?> cachedClass;

    ProxyCreator(Class<?> proxyInterface, String bundleName, String targetClassName) {
        this.proxyInterface = proxyInterface;
        this.bundleName = bundleName;
        this.targetClassName = targetClassName;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Creates a proxy implementing {@code proxyInterface} that delegates every
     * method call to a new delegate instance constructed with
     * {@code (Composite, int)}.
     */
    @SuppressWarnings("unchecked")
    <T> T createProxy(Composite parent, int style) {
        try {
            Object delegate = getTargetClass()
                    .getConstructor(Composite.class, int.class)
                    .newInstance(parent, style);
            return (T) Proxy.newProxyInstance(
                proxyInterface.getClassLoader(),
                new Class<?>[]{ proxyInterface },
                (proxy, method, args) -> {
                    try {
                        Method m = delegate.getClass().getMethod(
                                method.getName(), method.getParameterTypes());
                        return m.invoke(delegate, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                }
            );
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create proxy for " + targetClassName + " from bundle " + bundleName, e);
        }
    }

    /** Reflectively invokes a static method on the target class. */
    Object invokeStatic(String methodName, Class<?>[] paramTypes, Object... args) {
        try {
            return getTargetClass().getMethod(methodName, paramTypes).invoke(null, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            if (cause instanceof Error) throw (Error) cause;
            throw new RuntimeException(cause);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    // -------------------------------------------------------------------------
    // Class loading (cached)
    // -------------------------------------------------------------------------

    private Class<?> getTargetClass() {
        if (cachedClass == null) {
            synchronized (this) {
                if (cachedClass == null) {
                    cachedClass = loadClassFromBundle(bundleName, targetClassName);
                }
            }
        }
        return cachedClass;
    }

    /**
     * Loads {@code className} from the bundle with the given symbolic name using
     * Equinox-internal APIs, bypassing OSGi import-package wiring so that the
     * class returned is always the one local to that bundle's own JAR.
     */
    private static Class<?> loadClassFromBundle(String bundleName, String className) {
        try {
            // Our classloader implements BundleReference; getBundle() gives the EquinoxBundle.
            // Navigate Equinox internals without needing org.osgi.framework on the classpath
            // and without a BundleContext (which is null on fragment/library bundles).
            // Chain: EquinoxBundle -> EquinoxContainer -> Storage -> ModuleContainer -> modules
            ClassLoader cl = ProxyCreator.class.getClassLoader();
            Object ourBundle  = reflectInvoke(cl,           "getBundle");
            Object container  = reflectInvoke(ourBundle,   "getEquinoxContainer");
            Object storage    = reflectInvoke(container,   "getStorage");
            Object moduleCont = reflectInvoke(storage,     "getModuleContainer");
            Iterable<?> modules = (Iterable<?>) reflectInvoke(moduleCont, "getModules");
            for (Object module : modules) {
                Object bundle = reflectInvoke(module, "getBundle");
                if (bundle == null) continue;
                String sym = (String) bundle.getClass().getMethod("getSymbolicName").invoke(bundle);
                if (bundleName.equals(sym)) {
                    // bundle.loadClass() follows OSGi import-package wiring which may route the
                    // class back to a different bundle. Use findLocalClass() instead: it searches
                    // only the bundle's own JAR entries, bypassing import wiring.
                    Object revision  = reflectInvoke(module,   "getCurrentRevision");
                    Object wiring    = reflectInvoke(revision, "getWiring");
                    ClassLoader bundleCl = (ClassLoader) reflectInvoke(wiring, "getClassLoader");
                    Method findLocal = findMethodInHierarchy(bundleCl.getClass(),
                            "findLocalClass", String.class);
                    return (Class<?>) findLocal.invoke(bundleCl, className);
                }
            }
            throw new RuntimeException("Bundle '" + bundleName + "' not found in OSGi runtime");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load class " + className + " from bundle " + bundleName, e);
        }
    }

    /** Walks the class hierarchy to find a declared method with the given parameter types. */
    private static Method findMethodInHierarchy(Class<?> cls, String name, Class<?>... params)
            throws NoSuchMethodException {
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            try {
                Method m = c.getDeclaredMethod(name, params);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException ignored) {}
        }
        throw new NoSuchMethodException("Method '" + name + "' not found in hierarchy of " + cls.getName());
    }

    /**
     * Invokes a zero-arg method by name. Always calls setAccessible(true) because Equinox
     * returns objects whose runtime type is a non-public nested class, making even public
     * methods inaccessible without it.
     */
    private static Object reflectInvoke(Object target, String methodName) throws Exception {
        Method m = null;
        try {
            m = target.getClass().getMethod(methodName);
        } catch (NoSuchMethodException ignored) {}
        if (m == null) {
            for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
                try {
                    m = c.getDeclaredMethod(methodName);
                    break;
                } catch (NoSuchMethodException ignored) {}
            }
        }
        if (m == null) {
            throw new NoSuchMethodException(
                    "Method '" + methodName + "' not found on " + target.getClass().getName());
        }
        m.setAccessible(true);
        return m.invoke(target);
    }
}