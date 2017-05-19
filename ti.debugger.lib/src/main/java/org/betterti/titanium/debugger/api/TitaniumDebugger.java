package org.betterti.titanium.debugger.api;

import org.betterti.titanium.debugger.RequestCallback;

import java.nio.file.Path;

public interface TitaniumDebugger {
	void onPause(PauseEvent callback);

	void connect();

	void stop(ShutdownCallback shutdownCallback);

	void initialize();


	void addBreakpoint(Path relative, int lineNo);

	void removeBreakpoint(Path relative, int lineNo);

	void stepOver();

	void stepInto();

	void stepReturn();

	void resume();

	void fetchFrames(FramesCallback callback);

	void evaluate(String command, RequestCallback callback);
}
