package dev.equo.swt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApiObjectsTest {

    @Test
    void describe_uses_the_impl_when_set() {
        Object impl = new Object() {
            public String toString() {
                return "Button {OK}";
            }
        };
        assertThat(ApiObjects.describe(new Object(), impl)).isEqualTo("Button {OK}");
    }

    @Test
    void describe_without_impl_names_the_class_and_identity() {
        Object api = new ApiObjectsTest();
        assertThat(ApiObjects.describe(api, null)).isEqualTo("ApiObjectsTest {" + System.identityHashCode(api) + "}");
    }

    @Test
    void describe_without_impl_names_an_anonymous_class() {
        Object api = new Object() {};
        assertThat(ApiObjects.describe(api, null)).startsWith("ApiObjectsTest$");
    }
}
