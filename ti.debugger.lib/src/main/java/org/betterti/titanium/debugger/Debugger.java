package org.betterti.titanium.debugger;

import org.betterti.titanium.debugger.commands.PendingCommand;
import org.betterti.titanium.debugger.responses.*;

import java.io.IOException;

/**
 * Created by johnsba1 on 5/29/17.
 */
public interface Debugger {


    PendingCommand<SimpleResponse> disconnect();

    PendingCommand<SimpleResponse> stepReturn();

    interface Callback<T extends DebugResponse>{

        void onResponse(T respond);

    }


    abstract void connect() throws IOException;

    PendingCommand<VersionInfoResponse> queryVersion();

    PendingCommand<BreakpointCreatedResponse> createBreakpoint(String filename, int lineNumber);

    PendingCommand<NoResponse> resume();

    PendingCommand<SimpleResponse> stepOver();

    PendingCommand<SimpleResponse> stepInto();

    <T extends DebugResponse> void listen(Class<T> clazz, Callback<T> callback);

}
