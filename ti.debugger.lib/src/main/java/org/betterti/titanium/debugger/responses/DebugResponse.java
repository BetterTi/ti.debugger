package org.betterti.titanium.debugger.responses;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by johnsba1 on 5/29/17.
 */
public abstract class DebugResponse {

    private Long _id;

    public DebugResponse(Long id) {
        _id = id;
    }

    public Long getId() {
        return _id;
    }
}
