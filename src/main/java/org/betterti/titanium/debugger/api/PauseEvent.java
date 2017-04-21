package org.betterti.titanium.debugger.api;

public interface PauseEvent {
	void paused(String file, int line);
}
