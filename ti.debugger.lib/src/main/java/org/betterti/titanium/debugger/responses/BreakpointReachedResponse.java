package org.betterti.titanium.debugger.responses;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class BreakpointReachedResponse extends DebugResponse {
    private String _filename;
    private final int _lineNumber;

    public BreakpointReachedResponse(String filename, int lineNumber) {
        super(null);
        _filename = filename;
        _lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return _lineNumber;
    }

    public String getFilename() {
        return _filename;
    }
}
