package org.betterti.titanium.debugger;

import org.betterti.titanium.debugger.android.AndroidDataQueue;
import org.betterti.titanium.debugger.android.AndroidDebugCommand;
import org.betterti.titanium.debugger.api.FramesCallback;
import org.betterti.titanium.debugger.api.PauseEvent;
import org.betterti.titanium.debugger.api.ShutdownCallback;
import org.betterti.titanium.debugger.api.TitaniumDebugger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class TitaniumAndroidDebugger implements TitaniumDebugger {

	private static final Logger Log = LoggerFactory.getLogger(TitaniumAndroidDebugger.class);


	private static class BreakpointRegistry{
		public long id;
		public int line;
		public String file;
	}

	private List<BreakpointRegistry> _breakpointRegistries = new ArrayList<BreakpointRegistry>();


	private AndroidDataQueue _queue = new AndroidDataQueue();

	public TitaniumAndroidDebugger(){}

	public interface EventCallbacks{

		class PauseEvent{
			public String filename;
			public int line;
			public PauseEvent(String filename, int lineCount) {
				this.filename = filename;
				this.line = lineCount;
			}
		}

		void onPause(PauseEvent e);
	}

	@Override
	public void onPause(final PauseEvent callback){
	}



	private Future send(AndroidDebugCommand request){
		_queue.send(request);
		return request;
	}


	@Override
	public void connect() throws IOException {
		_queue.startSocket();
    initialize();
	}


	public void setEventListener(EventCallbacks callbacks){
		_queue.on("break", new AndroidDataQueue.EventCallback() {
			@Override
			public void event(Map body) {
				Map s = (Map) body.get("script");
				EventCallbacks.PauseEvent e = new EventCallbacks.PauseEvent((String)s.get("name"), ((Number) body.get("sourceLine")).intValue());
				callbacks.onPause(e);
			}
		});
	}

	@Override
	public void stop(ShutdownCallback shutdownCallback) {
		_queue.shutdown(shutdownCallback);
	}

	@Override
	public void initialize() {
		send(AndroidDebugCommand.version(new AndroidDebugCommand.Callback() {
			@Override
			public void event(Map response) {
				Log.warn("Versions response: " + response);
			}
		}));
	}

	@Override
	public Future setBreakpoint(Path relative, int lineNo) {
		return send(AndroidDebugCommand.createBreakpoint(relative, lineNo, new AndroidDebugCommand.Callback(){
			public void event(Map map){
				Map body = (Map) map.get("body");
				long breakpointId = ((Number)body.get("breakpoint")).longValue();

				BreakpointRegistry r = new BreakpointRegistry();
				r.file = body.get("script_name").toString();
				r.line = ((Number)body.get("line")).intValue();
				r.id = ((Number) body.get("breakpoint")).longValue();

				_breakpointRegistries.add(r);
			}
		}));
	}

	@Override
	public void removeBreakpoint(Path relative, int lineNo) {
		Iterator<BreakpointRegistry> i = _breakpointRegistries.iterator();
		boolean found = false;
		while(i.hasNext()){
			BreakpointRegistry reg = i.next();
			if(reg.line == lineNo && reg.file.equals(relative.toString())){
				send(AndroidDebugCommand.deleteBreakpoint(reg.id));
				i.remove();
				found = true;
			}
		}
		if(!found) {
			Log.error("Could not remove breakpoint. The");
		}

	}

	public Future disconnect() {
		return send(AndroidDebugCommand.disconnect(null));
	}



	@Override
	public Future stepOver() {
		return send(AndroidDebugCommand.stepOver());
	}

	@Override
	public Future stepInto() {
		return send(AndroidDebugCommand.stepInto());
	}

	@Override
	public Future stepReturn() {
		return send(AndroidDebugCommand.stepReturn());
	}

	@Override
	public Future resume() {
		return send(AndroidDebugCommand.resume(null));

	}

	@Override
	public void fetchFrames(FramesCallback callback) {
		send(AndroidDebugCommand.fetchFrames(callback));
	}

	@Override
	public void evaluate(String command, RequestCallback callback) {
//		sendCommand("eval*0*frame[0]*" + command, callback);

	}
}
