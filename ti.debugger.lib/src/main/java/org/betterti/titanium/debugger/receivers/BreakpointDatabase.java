package org.betterti.titanium.debugger.receivers;

/**
 * Created by johnsba1 on 6/12/17.
 */
public interface BreakpointDatabase {
    void register(String file, int lineNumber, int breakpointId);

    Integer fetch(String file, int lineNumber);

    void delete(String file, int lineNumber);

}
