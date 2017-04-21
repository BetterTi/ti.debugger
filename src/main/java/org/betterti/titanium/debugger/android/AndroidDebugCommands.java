package org.betterti.titanium.debugger.android;

import org.betterti.titanium.debugger.api.FrameResult;
import org.betterti.titanium.debugger.api.FramesCallback;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AndroidDebugCommands {
	public final Callback callback;
	public final String command;
	public final boolean fireAndForget;

	public Map<String, Object> args;

	public AndroidDebugCommands(Callback callback, String command, boolean fireAndForget) {
		this.callback = callback;
		this.command = command;
		this.fireAndForget = fireAndForget;
	}

	public static AndroidDebugCommands fetchFrames(final FramesCallback callback) {
		return new AndroidDebugCommands(new Callback() {
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

	public static AndroidDebugCommands disconnect(Callback cb) {
		return new AndroidDebugCommands(cb, "disconnect", false);
	}

	public static AndroidDebugCommands halt(Callback callback) {
		String command = "evaluate";
		final AndroidDebugCommands pending = new AndroidDebugCommands(callback, command, true);
		final HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("disable_break", true);
		map.put("expression", "Ti.API.terminate()");
		map.put("inlineRefs", true);
		map.put("global", true);
		pending.setArgs(map);
		return pending;
	}

	public static AndroidDebugCommands stepOver() {
		/*
		{ "seq"       : <number>,
  "type"      : "request",
  "command"   : "continue",
  "arguments" : { "stepaction" : <"in", "next" or "out">,
                  "stepcount"  : <number of steps (default 1)>
                }
}
		 */
		AndroidDebugCommands pending = new AndroidDebugCommands(null, "continue", true);
		final HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("stepaction", "next");
		pending.setArgs(map);
		return pending;
	}

	public static AndroidDebugCommands stepInto() {
		AndroidDebugCommands pending = new AndroidDebugCommands(null, "continue", true);
		final HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("stepaction", "in");
		pending.setArgs(map);
		return pending;
	}

	public static AndroidDebugCommands stepReturn() {
		AndroidDebugCommands pending = new AndroidDebugCommands(null, "continue", true);
		final HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("stepaction", "out");
		pending.setArgs(map);
		return pending;
	}


	public interface Callback{
		void event(Map response);
	}

	public void setArgs(Map<String, Object> args) {
		this.args = args;
	}

	public static AndroidDebugCommands version(Callback callback) {
		return new AndroidDebugCommands(callback, "version", false);
	}

	public static AndroidDebugCommands createBreakpoint(final Path relative, final int lineNo, Callback callback) {

		String command = "setbreakpoint";
		final AndroidDebugCommands pending = new AndroidDebugCommands(callback, command, false);
		final HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("type", "script");
		map.put("target", relative.toString());
		map.put("line", lineNo);
		map.put("column", 0);
		map.put("enabled", true);
		pending.setArgs(map);
		return pending;
	}

	public static AndroidDebugCommands deleteBreakpoint(long breakpointId) {
		final AndroidDebugCommands p = new AndroidDebugCommands(null, "clearbreakpoint", false);
		Map args = new HashMap();
		args.put("breakpoint", breakpointId);
		p.setArgs(args);
		return p;
	}

	public static AndroidDebugCommands resume(Callback callback) {
		return new AndroidDebugCommands(callback, "continue", false);
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
}
