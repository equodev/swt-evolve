package dev.equo.swt;

/**
 * Adjusts the flags {@link Config} read from the system properties, before anything else reads them,
 * found through {@link java.util.ServiceLoader}. A build without one keeps the flags as read.
 */
public interface ConfigFlagsDefaults {

    /** Sets what this build defaults or derives on {@code flags}, which hold what was configured. */
    void apply(ConfigFlags flags);
}
