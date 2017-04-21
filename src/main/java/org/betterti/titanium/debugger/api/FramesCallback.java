package org.betterti.titanium.debugger.api;

import java.util.List;

public interface FramesCallback {
	void event(List<FrameResult> results);
}
