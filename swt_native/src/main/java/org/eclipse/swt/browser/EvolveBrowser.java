package org.eclipse.swt.browser;

import org.eclipse.swt.browser.WebBrowser;
import org.eclipse.swt.widgets.Composite;

public class EvolveBrowser extends WebBrowser {
    private String url = "";
    private String text = "";

    @Override
    public boolean back() {
        return false;
    }

    @Override
    public void create(Composite parent, int style) {

    }

    @Override
    public boolean execute(String script) {
        return false;
    }

    @Override
    public boolean forward() {
        return false;
    }

    @Override
    public String getBrowserType() {
        return "Evolve";
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public boolean isBackEnabled() {
        return false;
    }

    @Override
    public boolean isForwardEnabled() {
        return false;
    }

    @Override
    public void refresh() {

    }

    @Override
    public boolean setText(String html, boolean trusted) {
        text = html;
        return true;
    }

    @Override
    public boolean setUrl(String url, String postData, String[] headers) {
        this.url = url;
        return true;
    }

    @Override
    public void stop() {

    }
}
