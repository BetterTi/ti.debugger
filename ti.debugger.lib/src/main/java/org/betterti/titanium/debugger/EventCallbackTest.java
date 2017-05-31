package org.betterti.titanium.debugger;

/**
 * Created by johnsba1 on 5/27/17.
 */
public interface EventCallbackTest {

    class PauseEvent {
        public String filename;
        public int line;

        public PauseEvent(String filename, int lineCount) {
            this.filename = filename;
            this.line = lineCount;
        }
    }

    void onPause(PauseEvent e);
}
