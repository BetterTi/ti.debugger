package org.betterti.titanium.debugger;

import org.betterti.titanium.debugger.android.AndroidDataQueue;
import org.betterti.titanium.debugger.android.AndroidDebugCommands;
import org.betterti.titanium.debugger.api.FramesCallback;
import org.betterti.titanium.debugger.api.PauseEvent;
import org.betterti.titanium.debugger.api.ShutdownCallback;
import org.betterti.titanium.debugger.api.TitaniumDebugger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TitaniumAndroidDebugger implements TitaniumDebugger {

	private static final Logger Log = LoggerFactory.getLogger(TitaniumAndroidDebugger.class);


	private static class BreakpointRegistry{
		public long id;
		public int line;
		public String file;
	}

	private List<BreakpointRegistry> _breakpointRegistries = new ArrayList<BreakpointRegistry>();


	private AndroidDataQueue _queue = new AndroidDataQueue();

	public TitaniumAndroidDebugger(){

	}



	public interface EventCallback{
		void event(Map eventData);
	}

	public interface BreakpointCallback{
		void event(Map breakpointData);
	}


	@Override
	public void onPause(final PauseEvent callback){
		_queue.on("break", new EventCallback() {
			@Override
			public void event(Map eventData) {
				Log.warn("PAUSE RECEIVED");
				callback.paused(((Map)eventData.get("script")).get("name").toString(), ((Number)eventData.get("sourceLine")).intValue());
			}
		});
	}



	private void send(AndroidDebugCommands request){
		_queue.send(request);
	}


	@Override
	public void connect(){
		_queue.start();
    initialize();
	}


	@Override
	public void stop(ShutdownCallback shutdownCallback) {
		_queue.shutdown(shutdownCallback);
	}

	@Override
	public void initialize() {
		send(AndroidDebugCommands.version(new AndroidDebugCommands.Callback() {
			@Override
			public void event(Map response) {
				Log.warn("Versions response: " + response);
			}
		}));
	}

	@Override
	public void addBreakpoint(Path relative, int lineNo) {
		send(AndroidDebugCommands.createBreakpoint(relative, lineNo, new AndroidDebugCommands.Callback(){
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
				send(AndroidDebugCommands.deleteBreakpoint(reg.id));
				i.remove();
				found = true;
			}
		}
		if(!found) {
			Log.error("Could not remove breakpoint. The");
		}

	}

	@Override
	public void stepOver() {
		send(AndroidDebugCommands.stepOver());
	}

	@Override
	public void stepInto() {
		send(AndroidDebugCommands.stepInto());
	}

	@Override
	public void stepReturn() {
		send(AndroidDebugCommands.stepReturn());
	}

	@Override
	public void resume() {
		send(AndroidDebugCommands.resume(null));

	}

	@Override
	public void fetchFrames(FramesCallback callback) {
		send(AndroidDebugCommands.fetchFrames(callback));
	}

	@Override
	public void evaluate(String command, RequestCallback callback) {
//		sendCommand("eval*0*frame[0]*" + command, callback);

	}
}
