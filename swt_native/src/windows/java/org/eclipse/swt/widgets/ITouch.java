package org.eclipse.swt.widgets;

public interface ITouch {

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the event
     */
    String toString();

    Touch getApi();

    void setApi(Touch api);
}
