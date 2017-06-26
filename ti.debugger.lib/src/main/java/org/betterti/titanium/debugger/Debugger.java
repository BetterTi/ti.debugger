package org.betterti.titanium.debugger;

import org.betterti.titanium.debugger.commands.PendingCommand;
import org.betterti.titanium.debugger.responses.*;

import java.io.IOException;

/**
 * Created by johnsba1 on 5/29/17.
 */
public interface Debugger {


    PendingCommand<SimpleResponse> disconnect();

    PendingCommand<NoResponse> stepReturn();

    PendingCommand<SimpleResponse> removeBreakpoint(String filename, int linenumber);

    interface Callback<T extends DebugResponse>{

        void onResponse(T respond);

    }


    abstract void connect() throws IOException;

    PendingCommand<VersionInfoResponse> queryVersion();

    PendingCommand<FramesResponse> queryFrames();

    PendingCommand<FrameVariablesResponse> queryFrameVariables(int frameNumber);

    PendingCommand<BreakpointCreatedResponse> createBreakpoint(String filename, int lineNumber);

    PendingCommand<NoResponse> resume();

    PendingCommand<NoResponse> stepOver();

    PendingCommand<NoResponse> stepInto();

    <T extends DebugResponse> void listen(Class<T> clazz, Callback<T> callback);

}
