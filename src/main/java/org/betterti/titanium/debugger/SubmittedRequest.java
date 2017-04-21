package org.betterti.titanium.debugger;


import org.betterti.titanium.debugger.android.AndroidDebugCommands;

import java.util.concurrent.CountDownLatch;

public class SubmittedRequest {
	public final String command;
	public CountDownLatch waiting = new CountDownLatch(1);
	public long requestId = System.currentTimeMillis();

	public final AndroidDebugCommands.Callback callback;

	public SubmittedRequest(AndroidDebugCommands pending) {
		this.command = pending.command;
		this.callback = pending.callback;
	}

	public String getSerializedCommand(){
		final String s = requestId + "*" + command;
		return s.length() + "*" + s;
	}
}
