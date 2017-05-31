package org.betterti.titanium.debugger;

import org.betterti.titanium.debugger.responses.DebugResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class DebugCommand<TResponse extends DebugResponse> {

    private Long _id;


    public DebugCommand(Long id) {
        _id = id;
    }

    public DebugCommand() {
        _id = System.currentTimeMillis();
    }


    public Long getId() {
        return _id;
    }
    public boolean hasResponse(){return true;}

}
