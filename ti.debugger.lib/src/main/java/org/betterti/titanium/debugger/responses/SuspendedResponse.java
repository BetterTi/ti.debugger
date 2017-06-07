package org.betterti.titanium.debugger.responses;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class SuspendedResponse extends DebugResponse {
    private String _type;
    private String _filename;
    private final int _lineNumber;

    public SuspendedResponse(String type, String filename, int lineNumber) {
        super(null);
        _type = type;
        _filename = filename;
        _lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return _lineNumber;
    }

    public String getFilename() {
        return _filename;
    }

    public String getType() {
        return _type;
    }
}
