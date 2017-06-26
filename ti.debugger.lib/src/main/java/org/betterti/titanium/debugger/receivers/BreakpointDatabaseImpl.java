package org.betterti.titanium.debugger.receivers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by johnsba1 on 6/20/17.
 */
public class BreakpointDatabaseImpl implements BreakpointDatabase {

    private static final Logger Log  = LoggerFactory.getLogger(BreakpointDatabaseImpl.class);


    public static class Entry{
        public final String file;
        public final int lineNumber;

        public Entry(String file, int lineNumber) {
            this.file = file;
            this.lineNumber = lineNumber;
        }

        @Override
        public int hashCode() {
            return Objects.hash(file, lineNumber);
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Entry) {
                Entry other = (Entry) obj;
                return Objects.equals(this.file, other.file) && Objects.equals(this.lineNumber, other.lineNumber);
            }
            return false;
        }
    }

    private Map<Entry, Integer> _registry = new HashMap<>();



    @Override
    public void register(String file, int lineNumber, int breakpointId) {
        Log.debug("Registering breakpoint {}:{} => {}", file, lineNumber, breakpointId);
        _registry.put(new Entry(file, lineNumber), breakpointId);
    }

    @Override
    public Integer fetch(String file, int lineNumber) {
        return _registry.get(new Entry(file, lineNumber));
    }

    @Override
    public void delete(String file, int lineNumber) {
        _registry.remove(new Entry(file, lineNumber));
        Log.debug("Deleting breakpoint {}:{}, breakpoints left: {}", file, lineNumber, _registry.size());
    }
}
