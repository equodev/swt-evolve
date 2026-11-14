package dev.equo.swt;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Empties the font-name substitutions for each test, as on the desktop: a web test earlier in the
 * JVM registers them for good ({@code WebFontSubstitutions} is a static initializer).
 */
public final class WithoutFontSubstitutions implements BeforeEachCallback, AfterEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(WithoutFontSubstitutions.class);

    @Override
    public void beforeEach(ExtensionContext context) {
        Map<String, String> saved = new LinkedHashMap<>();
        for (String from : FontMetricsUtil.fontSubstitutionKeys()) {
            saved.put(from, FontMetricsUtil.substituteFontName(from));
        }
        saved.keySet().forEach(FontMetricsUtil::removeFontSubstitution);
        context.getStore(NAMESPACE).put(WithoutFontSubstitutions.class, saved);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        @SuppressWarnings("unchecked")
        Map<String, String> saved = (Map<String, String>) context.getStore(NAMESPACE)
                .remove(WithoutFontSubstitutions.class, Map.class);
        if (saved != null) saved.forEach(FontMetricsUtil::registerFontSubstitution);
    }
}
