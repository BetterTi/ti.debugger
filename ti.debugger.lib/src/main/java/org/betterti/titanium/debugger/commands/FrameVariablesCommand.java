package org.betterti.titanium.debugger.commands;

import org.betterti.titanium.debugger.DebugCommand;
import org.betterti.titanium.debugger.responses.FrameVariablesResponse;
import org.betterti.titanium.debugger.responses.VersionInfoResponse;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class FrameVariablesCommand extends DebugCommand<FrameVariablesResponse>{
    private int _frameNumber;

    public FrameVariablesCommand(int frameNumber){
        _frameNumber = frameNumber;
    }

    public int getFrameNumber() {
        return _frameNumber;
    }
}
