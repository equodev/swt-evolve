package dev.equo.swt;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A page served by the web backend's {@code /proxy} comes from the app's own origin, so the
 * proxied site's cookies are kept server-side: these are the browser rules that jar has to follow.
 */
@Tag("native-unit")
public class ProxyCookieJarNativeTest {

    private final ProxyCookieJar jar = new ProxyCookieJar(url -> url.contains("example.test"));

    private static URI uri(String s) {
        return URI.create(s);
    }

    @Test
    public void a_cookie_set_on_one_hop_is_sent_on_the_next() {
        jar.store(uri("https://app.example.test/oauth2/start"),
                Collections.singletonList("SESSION=abc; Path=/; Secure; HttpOnly"));

        assertThat(jar.header(uri("https://app.example.test/idp/realms/r/auth?state=s"))).isEqualTo("SESSION=abc");
    }

    @Test
    public void the_longest_path_goes_first_and_a_narrower_path_stays_put() {
        jar.store(uri("https://app.example.test/"), Collections.singletonList("SESSION=abc; Path=/"));
        jar.store(uri("https://app.example.test/idp/realms/r/auth"), Arrays.asList(
                "AUTH=x;Version=1;Path=/idp/realms/r/;Secure;HttpOnly",
                "HASH=\"h\";Version=1;Path=/idp/realms/r/;Max-Age=60;Secure"));

        assertThat(jar.header(uri("https://app.example.test/idp/realms/r/login-actions/authenticate")))
                .isEqualTo("AUTH=x; HASH=\"h\"; SESSION=abc");
        assertThat(jar.header(uri("https://app.example.test/login/code"))).isEqualTo("SESSION=abc");
    }

    @Test
    public void a_new_value_replaces_the_old_one_and_max_age_zero_deletes() {
        URI page = uri("https://app.example.test/");
        jar.store(page, Collections.singletonList("SESSION=old; Path=/"));
        jar.store(page, Collections.singletonList("SESSION=new; Path=/"));
        assertThat(jar.header(page)).isEqualTo("SESSION=new");

        jar.store(page, Collections.singletonList("SESSION=; Max-Age=0; Path=/"));
        assertThat(jar.header(page)).isNull();
    }

    @Test
    public void expires_in_the_past_is_not_kept_and_a_dashed_date_is_understood() {
        URI page = uri("https://app.example.test/");
        jar.store(page, Arrays.asList(
                "OLD=1; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT",
                "NEW=1; Path=/; Expires=Wed, 21-Oct-2099 07:28:00 GMT"));

        assertThat(jar.header(page)).isEqualTo("NEW=1");
    }

    @Test
    public void secure_cookies_stay_off_plain_http() {
        jar.store(uri("https://app.example.test/"), Collections.singletonList("S=1; Path=/; Secure"));

        assertThat(jar.header(uri("http://app.example.test/"))).isNull();
    }

    @Test
    public void host_only_and_domain_cookies_reach_the_hosts_they_should() {
        jar.store(uri("https://app.example.test/"), Arrays.asList("HOST=1; Path=/", "WIDE=1; Domain=.example.test; Path=/"));

        assertThat(jar.header(uri("https://app.example.test/"))).contains("HOST=1").contains("WIDE=1");
        assertThat(jar.header(uri("https://www.example.test/"))).isEqualTo("WIDE=1");
    }

    @Test
    public void a_foreign_or_single_label_domain_is_refused() {
        jar.store(uri("https://app.example.test/"), Arrays.asList("EVIL=1; Domain=other.test; Path=/", "TLD=1; Domain=test; Path=/"));

        assertThat(jar.header(uri("https://other.test/"))).isNull();
        assertThat(jar.header(uri("https://www.example.test/"))).isNull();
    }

    @Test
    public void the_default_path_is_the_request_directory() {
        jar.store(uri("https://app.example.test/a/b/page"), Collections.singletonList("DEF=1"));

        assertThat(jar.get("DEF", "https://app.example.test/a/b/other")).isEqualTo("1");
        assertThat(jar.get("DEF", "https://app.example.test/a/")).isNull();
        assertThat(jar.get("DEF", "https://app.example.test/a/bc")).isNull();
    }

    @Test
    public void get_and_set_cookie_only_answer_for_hosts_the_proxy_serves() {
        assertThat(jar.set("SET=1; Path=/", "https://app.example.test/")).isTrue();
        assertThat(jar.get("SET", "https://app.example.test/x")).isEqualTo("1");

        assertThat(jar.set("SET=1", "https://not-proxied.test/")).isFalse();
        assertThat(jar.get("SET", "https://not-proxied.test/")).isNull();
    }

    @Test
    public void the_oldest_cookie_goes_past_the_cap() {
        URI page = uri("https://app.example.test/");
        for (int i = 0; i <= 300; i++) {
            jar.store(page, Collections.singletonList("C" + i + "=v; Path=/"));
        }

        assertThat(jar.get("C0", "https://app.example.test/")).isNull();
        assertThat(jar.get("C300", "https://app.example.test/")).isEqualTo("v");
    }
}
