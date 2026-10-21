package dev.equo.swt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines a Browser's registered {@code BrowserFunction}s in a served document before the page's
 * own scripts run, as the native backends do. The embedding page can only script the iframe once
 * its document has loaded, which is too late for a page that calls a function from a top-level
 * script or from {@code onload}.
 *
 * <p>The script is the first child of {@code <head>}. It reads the Browser's id from the iframe's
 * {@code name} ({@code equo-browser-<id>}), asks {@code /equo-browser-function} for the names
 * registered on it with a synchronous GET, and defines the same synchronous-XHR shims as
 * {@code _injectBrowserFunction} in {@code browser_evolve.dart}, which re-defines them after load
 * for functions registered later. The endpoint is resolved against the embedding page's base URI,
 * because the served document's own {@code <base href>} may point at the proxied site.
 */
final class BrowserFunctionBootstrap {

    private BrowserFunctionBootstrap() {
    }

    static final String SCRIPT = "<script>(function(){try{"
            + "var f=window.frameElement,m=f&&/^equo-browser-(-?\\d+)$/.exec(f.name);if(!m)return;"
            + "var id=+m[1],ep=new URL('equo-browser-function',f.ownerDocument.baseURI).href;"
            + "var x=new XMLHttpRequest();x.open('GET',ep+'?browserId='+id,false);x.send();"
            + "if(x.status!==200)return;"
            + "JSON.parse(x.responseText).names.forEach(function(n){window[n]=function(){"
            + "var r=new XMLHttpRequest();r.open('POST',ep,false);"
            + "r.setRequestHeader('Content-Type','application/json');"
            + "r.send(JSON.stringify({browserId:id,name:n,args:Array.prototype.slice.call(arguments)}));"
            + "if(r.status!==200)throw new Error('browser function failed: '+r.status);"
            + "var v=JSON.parse(r.responseText);if(v&&v.error!=null)throw new Error(v.error);"
            + "return v?v.value:undefined;};});"
            + "}catch(e){}})();</script>";

    private static final Pattern HEAD_TAG = Pattern.compile("<head(\\s[^>]*)?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern DOCTYPE = Pattern.compile("<!doctype[^>]*>", Pattern.CASE_INSENSITIVE);

    /** {@code html} with {@link #SCRIPT} ahead of every script of its own. */
    static String inject(String html) {
        Matcher head = HEAD_TAG.matcher(html);
        if (head.find()) return html.substring(0, head.end()) + SCRIPT + html.substring(head.end());
        // Ahead of the doctype the document would render in quirks mode.
        Matcher doctype = DOCTYPE.matcher(html);
        if (doctype.lookingAt()) return html.substring(0, doctype.end()) + SCRIPT + html.substring(doctype.end());
        return SCRIPT + html;
    }
}
