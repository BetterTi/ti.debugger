package org.betterti.titanium.debugger.android;


import org.betterti.titanium.debugger.AndroidSubmittedRequest;
import org.betterti.titanium.debugger.api.ShutdownCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class AndroidDataQueue {

	private ConcurrentHashMap<String, List<EventCallback>> _callbacks = new ConcurrentHashMap<>();
	private Socket _debugSocket;
	private BlockingQueue<AndroidDebugCommand> _commandQueue = new LinkedBlockingQueue<AndroidDebugCommand>();
	private final ExecutorService _connectExecutor = Executors.newFixedThreadPool(3);
	private AndroidOutputWorker _debuggerOutputWorker;
	private AndroidInputWorker _debuggerInputWorker;
	private InputStream _debugInput;
	private static final Logger Log = LoggerFactory.getLogger(AndroidDataQueue.class);
	private SubmittedRequestList _submittedRequestList;

	public interface EventCallback{
		void event(Map eventData);
	}

	public void startSocket() {
		Log.debug("Trying to connect to app...");
		_debugSocket = null;
		while(_debugSocket == null){
			try {
				Socket s = new Socket("localhost", 5432);
				InputStream inputStream = s.getInputStream();
				BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
				if(r.readLine() == null){
					throw new Exception("Bad connection");
				}
				_debugSocket = s;
				Log.debug("Connection looks good.");
				_submittedRequestList = new SubmittedRequestList();
				_debuggerOutputWorker = new AndroidOutputWorker(_debugSocket.getOutputStream(), _commandQueue, _submittedRequestList);
				_debuggerInputWorker  = new AndroidInputWorker(inputStream, _connectExecutor, _submittedRequestList, _callbacks);
				_debuggerInputWorker.start();
				_debuggerOutputWorker.start();
			}
			catch(Exception e){
				try {
					Thread.sleep(300);
				} catch (InterruptedException e1) {
					Log.info("Interruption exception occurred. Probably a shut down call.");
					return;
				}
			}
		}
	}

	public void on(String event, EventCallback callback){
		_callbacks.putIfAbsent(event, new ArrayList<>());
		_callbacks.get(event).add(callback);
	}

	public void send(AndroidDebugCommand request) {
		_commandQueue.add(request);

	}

	public void shutdown(final ShutdownCallback shutdownCallback) {


		try {
			Socket s = new Socket("localhost", 54321 + 2);

			final OutputStream os = s.getOutputStream();
			os.write("10**terminate".getBytes());
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}


		send(AndroidDebugCommand.halt(new AndroidDebugCommand.Callback(){
			@Override
			public void event(Map response) {
//				send(AndroidDebugCommand.disconnect(new AndroidDebugCommand.Callback() {
//					@Override
//					public void event(Map response) {
//						_debuggerExecutor.shutdownNow();
						try {
							_debugSocket.close();
						} catch (IOException e) {
							Log.warn("Could not close debugger socket");
						}
						shutdownCallback.completed();
//					}
//				}));
			}
		}));


	}
}
