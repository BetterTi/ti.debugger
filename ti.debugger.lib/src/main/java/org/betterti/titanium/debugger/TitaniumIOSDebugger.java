package org.betterti.titanium.debugger;

import org.betterti.titanium.debugger.android.AndroidDebugCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TitaniumIOSDebugger {

	private final ExecutorService _debuggerExecutor = Executors.newFixedThreadPool(3);
	private Socket _debugSocket;
	private BlockingQueue<AndroidDebugCommand> _commandQueue = new LinkedBlockingQueue<AndroidDebugCommand>();
	private static final Logger Log = LoggerFactory.getLogger(TitaniumIOSDebugger.class);
	private SubmittedRequest currentRequest = null;

	private ConcurrentHashMap<String, List<RequestCallback>> _callbacks = new ConcurrentHashMap<String, List<RequestCallback>>();

	private InputStream _debugInput;

	public TitaniumIOSDebugger(){

	}

	public void on(String event, RequestCallback callback){
		_callbacks.putIfAbsent(event, new ArrayList<RequestCallback>());
		_callbacks.get(event).add(callback);
	}


	//


	private void sendCommand(String command){

		Log.debug("Queuing: " + command);
		_commandQueue.add(new AndroidDebugCommand(null, command, false));
	}

	private void sendCommand(String command, boolean fireAndForget){

		Log.debug("Queuing: " + command);
		_commandQueue.add(new AndroidDebugCommand(null, command, fireAndForget));
	}

	private void sendCommand(String command, RequestCallback callback){
//		_commandQueue.add(new AndroidPendingRequest(callback, command, false));
	}


	public void start(){
		_debuggerExecutor.execute(new Runnable() {

			@Override
			public void run() {
				ServerSocket socket = null;
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Log.debug("Trying to connect to daemon");

				while(_debugInput == null){
					try {
						Thread.sleep(2000);
						Socket s = new Socket("localhost", 54321);
						_debugInput = s.getInputStream();
						_debugSocket = s;
					}
					catch(Exception e){
						Log.warn("Failed to open connection to debug port: " + e.getMessage());

					}
				}
				_debuggerExecutor.submit(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						PrintWriter p = null;
						try {
							p = new PrintWriter(new OutputStreamWriter(_debugSocket.getOutputStream()));
							p.print("Content-Length:46\r\n\r\n{\"seq\":1,\"type\":\"request\",\"command\":\"version\"}");
							p.flush();
							p.print("Content-Length:46\r\n\r\n{\"seq\":1,\"type\":\"request\",\"command\":\"version\"}");
							p.flush();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});

				String line;
				try {

					BufferedReader r = new BufferedReader(new InputStreamReader(_debugSocket.getInputStream()));

					Log.info("1st " + r.readLine());
					Log.info("2nd " + r.readLine());
					Log.info("3rd " + r.readLine());
					Log.info("4th " + r.readLine());

					while(!Thread.interrupted()){
						String contentLength = r.readLine();
						if(contentLength.contains("Content-Length")){
							Log.info("Received contentlength");

							String blank = r.readLine();
							if(blank.trim().length() == 0){
								Log.info("Seems to be blank");
							}

							String length = contentLength.replaceAll("Content-Length:", "").trim();

							Log.info("Looks like we're going to get length: " + length);

							char[] data = new char[Integer.parseInt(length)];

							r.read(data);

							Log.info("Received response: " + new String(data));


						}
						else{
							Log.info("Whoa whoa, contentlength not found: " + contentLength);
						}
					}

					while((line = r.readLine()) != null){
						Log.info(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

////					socket = new ServerSocket(54321);
//
////						_debugSocket = socket.accept();
//					Log.debug("Received debug connection");
////					_debugInput = ((_debugSocket.getInputStream()));
//					_debuggerExecutor.execute(new Runnable() {
//						@Override
//						public void run() {
//							Log.info("Starting read of debug input socket");
//							try {
//								byte[] data = new byte[1];
//								while (_debugInput.read(data) > 0) {
//									Log.debug("Reading response");
//									String responseLengthStr = "";
//									while ((char) data[0] != '*') {
//										Log.debug("Read: " + (char) data[0]);
//										responseLengthStr += (char) data[0];
//										_debugInput.read(data);
//									}
//
//									int responseLength = Integer.parseInt(responseLengthStr);
//
//									byte[] response = new byte[responseLength];
//									_debugInput.read(response);
//
//									final String responseStr = new String(response);
//									Log.info("CLIENT: " + responseStr);
//									final String[] parts = responseStr.split("\\*");
//
//									try {
//										long requestId = Long.parseLong(parts[0]);
//										if(currentRequest.requestId == requestId){
//											final SubmittedRequest lastRequest = currentRequest;
//											Log.info("Request with id: " + requestId + " is completed");
//											lastRequest.waiting.countDown();
//											if(lastRequest.callback != null){
//												_debuggerExecutor.execute(new Runnable() {
//													@Override
//													public void run() {
//														lastRequest.callback.completed(parts);
//													}
//												});
//											}
//										}
//									}
//									catch(NumberFormatException e){
//										Log.info("Ignoring the NFE");
//									}
//									String event = parts[0];
//									for(RequestCallback callback : _callbacks.getOrDefault(event, new ArrayList<RequestCallback>())){
//										callback.completed(parts);
//									}
//								}
//							} catch (Exception e) {
//								Log.error("Failed reading from debug stream", e);
//							}
//
//						}
//					});
//					_debuggerExecutor.execute(new Runnable() {
//						@Override
//						public void run() {
//
//
//							Log.info("Starting writing commands to output socket");
//							try{
//								PrintWriter p = new PrintWriter(new OutputStreamWriter(_debugSocket.getOutputStream()));
//								p.print("Content-Length:46\r\n\r\n{\"seq\":1,\"type\":\"request\",\"command\":\"version\"}");
//								p.flush();
////								while (!Thread.currentThread().isInterrupted()) {
////											try {
////												PendingRequest pending = _commandQueue.take();
////												currentRequest = new SubmittedRequest(pending);
////												PrintWriter writer = new PrintWriter(new OutputStreamWriter(_debugSocket.getOutputStream()));
////												Log.info("SERVER: " + currentRequest.getSerializedCommand());
////												writer.print(currentRequest.getSerializedCommand());
////												writer.flush();
////
////												try {
////													if(pending.fireAndForget){
////														Log.info("NOT Waiting for command to return: " + currentRequest.requestId);
////													}
////													else if(!currentRequest.waiting.await(10, TimeUnit.SECONDS)) {
////														Log.info("Failed to get response for command: " + currentRequest.getSerializedCommand());
////													}
////													else{
////														Log.info("Returned: " + currentRequest.getSerializedCommand());
////													}
////												} catch (InterruptedException e) {
////													Log.error("Failed to await request response: " + pending, e);
////												}
////											}
////											catch(IOException e){
////												Log.error("Failed writing to debug socket", e);
////											}
////										}
////									} catch (InterruptedException e) {
////										Log.error("Interrupted while writing to socket", e);
//									} catch (IOException e) {
//								e.printStackTrace();
//							}
//						}
//					});
//					sendCommand("enable");
//
//				}
//				catch(Exception e){
//					Log.error("Failed to connect debugger for system", e);
//				}
			}
		});
	}

	public void stop() {
		_debuggerExecutor.shutdown();
	}

	public void initialize() {
		sendCommand("version");
		sendCommand("option*monitorXHR*true");
		sendCommand("option*stepFiltersEnabled*true");
		sendCommand("option*suspendOnFirstLine*false");
		sendCommand("option*suspendOnExceptions*false");
		sendCommand("option*suspendOnError*true");
		sendCommand("option*bypassConstructors*false");
		sendCommand("option*stepFiltersEnabled*false");
		sendCommand("option*detailFormatters");

	}

	public void addBreakpoint(Path relative, int lineNo) {
		String command = "breakpoint*create*" +
				"app:/" + relative.toString() +
				"*" + lineNo +
				"*1*0**1";
		sendCommand(command);
	}

	public void removeBreakpoint(Path relative, int lineNo) {
		String command = "breakpoint*remove*" +
				"app:/" + relative.toString() +
				"*" + lineNo;

	}

	public void stepOver() {
		sendCommand("stepOver", true);

	}

	public void stepInto() {
		sendCommand("stepInto", true);

	}

	public void stepReturn() {
		sendCommand("stepReturn", true);

	}

	public void resume() {
		sendCommand("resume", true);

	}

	public void fetchFrames(RequestCallback callback) {
		sendCommand("frames", callback);

	}

	public void evaluate(String command, RequestCallback callback) {
		sendCommand("eval*0*frame[0]*" + command, callback);


	}
}
