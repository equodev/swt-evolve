package org.eclipse.swt.widgets;

import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import java.util.EventListener;
import java.util.stream.Stream;

public class FlutterTabItem implements ITabItem {
    @Override
    public Rectangle getBounds() {
        return null;
    }

    @Override
    public IControl getControl() {
        return null;
    }

    @Override
    public ITabFolder getParent() {
        return null;
    }

    @Override
    public String getToolTipText() {
        return "";
    }

    @Override
    public void setControl(IControl control) {

    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public String getText() {
        return "";
    }

    @Override
    public void setImage(Image image) {

    }

    @Override
    public void setText(String string) {

    }

    @Override
    public void setToolTipText(String string) {

    }

    @Override
    public void addListener(int eventType, Listener listener) {

    }

    @Override
    public void addDisposeListener(DisposeListener listener) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public Object getData() {
        return null;
    }

    @Override
    public Object getData(String key) {
        return null;
    }

    @Override
    public IDisplay getDisplay() {
        return null;
    }

    @Override
    public Listener[] getListeners(int eventType) {
        return new Listener[0];
    }

    @Override
    public <L extends EventListener> Stream<L> getTypedListeners(int eventType, Class<L> listenerType) {
        return Stream.empty();
    }

    @Override
    public int getStyle() {
        return 0;
    }

    @Override
    public boolean isAutoDirection() {
        return false;
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    @Override
    public boolean isListening(int eventType) {
        return false;
    }

    @Override
    public void notifyListeners(int eventType, Event event) {

    }

    @Override
    public void removeListener(int eventType, Listener listener) {

    }

    @Override
    public void reskin(int flags) {

    }

    @Override
    public void removeDisposeListener(DisposeListener listener) {

    }

    @Override
    public void setData(Object data) {

    }

    @Override
    public void setData(String key, Object value) {

    }

    @Override
    public long getHandle() {
        return 0;
    }
}
