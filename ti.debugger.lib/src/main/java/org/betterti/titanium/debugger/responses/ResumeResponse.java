package org.betterti.titanium.debugger.responses;

/**
 * Created by johnsba1 on 5/30/17.
 */
public class ResumeResponse extends DebugResponse {
    private final int _unknownValue;
    private final String _type;

    public ResumeResponse(int unknownValue, String type) {
        super(null);
        _unknownValue = unknownValue;
        _type = type;
    }

    public int getUnknownValue() {
        return _unknownValue;
    }

    public String getType() {
        return _type;
    }
}
