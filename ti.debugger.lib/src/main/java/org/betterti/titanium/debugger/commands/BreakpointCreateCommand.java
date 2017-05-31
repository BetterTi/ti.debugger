package org.betterti.titanium.debugger.commands;

import org.betterti.titanium.debugger.DebugCommand;
import org.betterti.titanium.debugger.responses.BreakpointCreatedResponse;

import java.util.Map;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class BreakpointCreateCommand extends DebugCommand<BreakpointCreatedResponse> {
    private final String _filename;
    private final int _lineNumber;

    public BreakpointCreateCommand(String filename, int lineNumber){
        _filename = filename;
        _lineNumber = lineNumber;
    }

    public String getFilename() {
        return _filename;
    }

    public int getLineNumber() {
        return _lineNumber;
    }
}
