package org.betterti.titanium.debugger;

import com.google.gson.Gson;
import org.betterti.titanium.debugger.android.AndroidDebugCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class AndroidSubmittedRequest {
	public static long lastRequestId = 1l;
	public final String command;
	private final AndroidDebugCommand _pending;
	public CountDownLatch waiting = new CountDownLatch(1);
	public long requestId = lastRequestId++;

	public final AndroidDebugCommand.Callback callback;

	public AndroidSubmittedRequest(AndroidDebugCommand pending) {
		_pending = pending;
		this.command = pending.command;
		this.callback = pending.callback;
	}

	public String getSerializedCommand(){
		Map content = new HashMap();
		content.put("type", "request");
		content.put("command", command);
		if(_pending.getArgs() != null){
			content.put("arguments", _pending.getArgs());
		}
		final String s = new Gson().toJson(content);
		return "Content-Length:" + s.length() + "\r\n\r\n" + s;
	}
}
