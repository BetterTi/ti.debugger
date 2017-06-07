package org.betterti.titanium.debugger.responses;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class LogResponse extends DebugResponse {
    private String _out;
    private String _message;

    public LogResponse(String out, String message) {
        super(null);
        _out = out;
        _message = message;
    }

    public String getOut() {
        return _out;
    }

    public String getMessage() {
        return _message;
    }
}