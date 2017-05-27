package org.betterti.titanium.debugger.api;

import org.betterti.titanium.debugger.RequestCallback;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Future;

public interface TitaniumDebugger {
	void onPause(PauseEvent callback);

	void connect() throws IOException;

	void stop(ShutdownCallback shutdownCallback);

	void initialize();


	Future setBreakpoint(Path relative, int lineNo);

	void removeBreakpoint(Path relative, int lineNo);

	Future stepOver();

	Future stepInto();

	Future stepReturn();

	Future resume();

	void fetchFrames(FramesCallback callback);

	void evaluate(String command, RequestCallback callback);
}
