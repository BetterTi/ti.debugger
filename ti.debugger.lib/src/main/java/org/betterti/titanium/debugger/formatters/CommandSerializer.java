package org.betterti.titanium.debugger.formatters;

import org.betterti.titanium.debugger.DebugCommand;

/**
 * Created by johnsba1 on 5/29/17.
 */
public interface CommandSerializer {
    
    
    String serialize(DebugCommand c);
}
