package org.betterti.titanium.debugger;


import com.google.gson.Gson;
import org.betterti.titanium.debugger.android.AndroidDebugCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class SubmittedRequest {
	public final String command;
	private final CountDownLatch _latch = new CountDownLatch(1);
	public long requestId = System.currentTimeMillis();

	public final AndroidDebugCommand.Callback callback;
	private AndroidDebugCommand _pending;

	public SubmittedRequest(AndroidDebugCommand pending) {
		_pending = pending;
		this.callback = pending.callback;
		Map content = new HashMap();
		content.put("seq", requestId);
		content.put("type", "request");
		content.put("command", pending.command);
		if(pending.getArgs() != null){
			content.put("arguments", pending.getArgs());
		}
		final String s = new Gson().toJson(content);
		this.command = "Content-Length:" + s.length() + "\r\n\r\n" + s;
	}

	public String getSerializedCommand(){
		return this.command;
	}

	public AndroidDebugCommand getCommand(){
		return _pending;
	}


}
