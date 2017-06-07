package org.betterti.titanium.debugger.responses;

import java.util.List;

/**
 * Created by johnsba1 on 5/30/17.
 */
public class FramesResponse extends DebugResponse {

    public static class Frame{
        public final int index;
        public final String functionName;
        public final String file;
        public final int lineNumber;

        public Frame(int index, String functionName, String file, int lineNumber) {
            this.index = index;
            this.functionName = functionName;
            this.file = file;
            this.lineNumber = lineNumber;
        }
    }


    private List<Frame> _frames;

    public FramesResponse(long id, List<Frame> frames) {
        super(id);
        _frames = frames;
    }

    public List<Frame> getFrames() {
        return _frames;
    }
}
