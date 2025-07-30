package org.eclipse.swt.widgets;

import com.equo.comm.api.ICommService;

public interface Delegate {
    ICommService createComm();
    void createClient() throws Exception;
    boolean isAlive();
    void dispose();
}
