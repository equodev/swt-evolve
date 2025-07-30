package org.eclipse.swt.opengl;

public interface IGLData {

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the data
     */
    String toString();

    GLData getApi();

    void setApi(GLData api);
}
