package org.betterti.titanium.debugger.android;

import org.betterti.titanium.debugger.api.FrameResult;
import org.betterti.titanium.debugger.api.FramesCallback;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class AndroidDebugCommand implements Future {
	public final Callback callback;
	public final String command;
	public final boolean fireAndForget;

	private CountDownLatch _latch = new CountDownLatch(1);

	public Map<String, Object> args;

	public AndroidDebugCommand(Callback callback, String command, boolean fireAndForget) {
		this.callback = callback;
		this.command = command;
		this.fireAndForget = fireAndForget;
	}

	public static AndroidDebugCommand fetchFrames(final FramesCallback callback) {
		return new AndroidDebugCommand(new Callback() {
			@Override
			public void event(Map response) {
				Map body = getObj(response, "body");
				List frames = (List) body.get("frames");
				List refs = (List) response.get("refs");

				List<FrameResult> results = new ArrayList<FrameResult>();
				for(Object f : frames){
					Map frame = (Map) f;

					Map scriptRef = refLookup(refs, getNum(getObj(frame, "script"), "ref").intValue());
					Map funcRef   = refLookup(refs, getNum(getObj(frame, "func"),   "ref").intValue());

					if(scriptRef != null) {
						FrameResult r = new FrameResult();
						r.line = getNum(frame, "line").intValue();
						r.funcName = getStr(funcRef, "name");
						r.fileName = getStr(scriptRef, "name");

						results.add(r);
					}
					else{
						System.err.println("NO SCRIPT REF FOUND!");
					}
				}
				callback.event(results);
			}

			private Map getObj(Map frame, String script) {
				return (Map) frame.get(script);
			}

			private Map refLookup(List refs, int refHandle){
				for(Object o : refs){
					Map ref = (Map)o;
					if((getNum(ref, "handle")).intValue() == refHandle){
						return ref;
					}
				}
				return null;
			}
		}, "backtrace", false);
	}

	public static AndroidDebugCommand disconnect(Callback cb) {
		return new AndroidDebugCommand(cb, "disconnect", false);
	}

	public static AndroidDebugCommand halt(Callback callback) {
		String command = "evaluate";
		final AndroidDebugCommand pending = new AndroidDebugCommand(callback, command, true);
		final HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("disable_break", true);
		map.put("expression", "Ti.API.terminate()");
		map.put("inlineRefs", true);
		map.put("global", true);
		pending.setArgs(map);
		return pending;
	}

	public static AndroidDebugCommand stepOver() {
		/*
		{ "seq"       : <number>,
  "type"      : "request",
  "command"   : "continue",
  "arguments" : { "stepaction" : <"in", "next" or "out">,
                  "stepcount"  : <number of steps (default 1)>
                }
}
		 */
		AndroidDebugCommand pending = new AndroidDebugCommand(null, "continue", true);
		final HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("stepaction", "next");
		map.put("stepcount", "1");
		pending.setArgs(map);
		return pending;
	}

	public static AndroidDebugCommand stepInto() {
		AndroidDebugCommand pending = new AndroidDebugCommand(null, "continue", true);
		final HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("stepaction", "in");
		map.put("stepcount", "1");
		pending.setArgs(map);
		return pending;
	}

	public static AndroidDebugCommand stepReturn() {
		AndroidDebugCommand pending = new AndroidDebugCommand(null, "continue", true);
		final HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("stepaction", "out");
		map.put("stepcount", "1");
		pending.setArgs(map);
		return pending;
	}


	public interface Callback{
		void event(Map response);
	}

	public void setArgs(Map<String, Object> args) {
		this.args = args;
	}

	public static AndroidDebugCommand version(Callback callback) {
		return new AndroidDebugCommand(callback, "version", false);
	}

	public static AndroidDebugCommand createBreakpoint(final Path relative, final int lineNo, Callback callback) {

		String command = "setbreakpoint";
		final AndroidDebugCommand pending = new AndroidDebugCommand(callback, command, false);
		final HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("type", "script");
		map.put("target", relative.toString());
		map.put("line", lineNo);
		map.put("column", 0);
		map.put("enabled", true);
		pending.setArgs(map);
		return pending;
	}

	public static AndroidDebugCommand deleteBreakpoint(long breakpointId) {
		final AndroidDebugCommand p = new AndroidDebugCommand(null, "clearbreakpoint", false);
		Map args = new HashMap();
		args.put("breakpoint", breakpointId);
		p.setArgs(args);
		return p;
	}

	public static AndroidDebugCommand resume(Callback callback) {
		return new AndroidDebugCommand(callback, "continue", false);
	}

	public Map<String, Object> getArgs() {
		return args;
	}


	private static String getStr(Map frame, String text) {
		return (String) frame.get(text);
	}

	private static Number getNum(Map frame, String line) {
		return (Number) frame.get(line);
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return _latch.getCount() == 0;
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		_latch.await(); return null;
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return _latch.await(timeout, unit);
	}

	public void finish(){
		_latch.countDown();
	}
}
