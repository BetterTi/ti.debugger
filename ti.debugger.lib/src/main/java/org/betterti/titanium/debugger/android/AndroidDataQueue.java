package org.betterti.titanium.debugger.android;


import com.google.gson.Gson;

import org.betterti.titanium.debugger.AndroidSubmittedRequest;
import org.betterti.titanium.debugger.TitaniumAndroidDebugger;
import org.betterti.titanium.debugger.api.ShutdownCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class AndroidDataQueue {

	private ConcurrentHashMap<String, List<TitaniumAndroidDebugger.EventCallback>> _callbacks = new ConcurrentHashMap<String, List<TitaniumAndroidDebugger.EventCallback>>();
	private AndroidSubmittedRequest currentRequest = null;
	private Socket _debugSocket;
	private BlockingQueue<AndroidDebugCommands> _commandQueue = new LinkedBlockingQueue<AndroidDebugCommands>();
	private final ExecutorService _debuggerExecutor = Executors.newFixedThreadPool(3);
	private InputStream _debugInput;
	private static final Logger Log = LoggerFactory.getLogger(AndroidDataQueue.class);

	public void start() {
		_debuggerExecutor.execute(new Runnable() {

			@Override
			public void run() {
				ServerSocket socket = null;
				Log.debug("Trying to connect to daemon");
				while(_debugSocket == null){
					try {
						Socket s = new Socket("localhost", 54321);
						_debugSocket = s;
					}
					catch(Exception e){
						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
							Log.info("Interruption exception occurred. Probably a shut down call.");
							return;
						}
					}
				}

				_debuggerExecutor.submit(new Runnable() {
					@Override
					public void run() {
						startReadingResponses();
					}
				});
			}
		});


	}


	private String readUntil(char token) throws IOException {
		StringBuilder b = new StringBuilder();
		int read;
		Log.info("Reading until: " + token);
		while((read = _debugSocket.getInputStream().read()) != -1 && !Thread.interrupted()) {
			Log.debug("READ ONE TOKEN: " + (char)read);
			if (read == token) {
				return b.toString();
			} else {
				b.append((char)read);
			}
		}
		return b.toString();
	}

	private String readFor(int amount) throws IOException {
		final int bufferSize = 10;
		byte[] buffer = new byte[bufferSize];


		Log.info("Starting read");
		StringBuffer b = new StringBuffer();
		int totalRead = 0;
		while(totalRead < amount){
			int desiredRead = Math.min(bufferSize, amount - totalRead);

			int readThisTime = _debugSocket.getInputStream().read(buffer, 0, desiredRead);

			final String content = new String(buffer, 0, readThisTime);
			Log.debug("READ: " + readThisTime + ":" + content);
			totalRead += readThisTime;
			b.append(content);
		}

		Log.info("Done reading");

		return b.toString();
	}


	private void startReadingResponses() {
		//						String line;
		try {
//
			Log.info("1st " + readUntil('\n'));
			Log.info("1st " + readUntil('\n'));
			Log.info("1st " + readUntil('\n'));
			Log.info("1st " + readUntil('\n'));



			startReadingInputQueue();
//
			while(!Thread.interrupted()){
				String contentLength = readUntil('\n');
				if(!Thread.interrupted() && contentLength != null && contentLength.contains("Content-Length")){
					Log.info("Received contentlength: " + contentLength);

//									String blank = r.readLine();
//									if(blank.trim().length() == 0){
//										Log.info("Seems to be blank");
//									}

					String length = contentLength.replaceAll("Content-Length:", "").trim();

					Log.info("Looks like we're going to get length: " + length);

					int responseLength = Integer.parseInt(length);

					if(responseLength > 0 ) {
						Log.info("Reading until newline");
						readUntil('\n');
						Log.info("Waiting for response string");
						String responseStr = readFor(responseLength);
						final Map map = new Gson().fromJson(responseStr, Map.class);
						Log.info("[From Debugger]: " + responseStr);;

						try {
							if("response".equals(map.get("type"))) {
								long requestId = ((Number) (map.get("request_seq"))).longValue();
								if (currentRequest.requestId == requestId) {
									final AndroidSubmittedRequest lastRequest = currentRequest;
									Log.info("Request with id: " + requestId + " is completed");
									lastRequest.waiting.countDown();
									if (lastRequest.callback != null) {
										_debuggerExecutor.execute(new Runnable() {
											@Override
											public void run() {
												lastRequest.callback.event(map);
											}
										});
									}
								}
							}
							else if("event".equals(map.get("type"))){
								final List<TitaniumAndroidDebugger.EventCallback> callbacks =
												_callbacks.getOrDefault(map.get("event").toString(), new ArrayList<TitaniumAndroidDebugger.EventCallback>());
								for(TitaniumAndroidDebugger.EventCallback cb : callbacks){
									cb.event((Map)map.get("body"));
								}
							}
						} catch (NumberFormatException e) {
							Log.info("Ignoring the NFE");
						}
					}



				}
				else if(contentLength == null){
//							Log.info("Whoa whoa, contentlength not found: " + contentLength);

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
				else{
					Log.info("Could not read next. Retrying soon...");
					Thread.sleep(500);

				}
			}
//
//		//					while((line = r.readLine()) != null){
//		//						Log.info(line);
//		//					}
		} catch (IOException e) {
			Log.error("Serious error while trying to write/read from debug socket", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			Log.error("Received interrupt");
		}
	}

	private void startReadingInputQueue() {
		_debuggerExecutor.execute(new Runnable() {
			@Override
			public void run() {

				Log.info("Starting writing commands to output socket");
				try {
					OutputStream writer = _debugSocket.getOutputStream();

//					Thread.sleep(10000);
					while (!Thread.currentThread().isInterrupted()) {
						AndroidDebugCommands pending = _commandQueue.take();
						currentRequest = new AndroidSubmittedRequest(pending);
						Log.info("[To Debugger]: " + currentRequest.getSerializedCommand().replaceAll("\n",
										"\\n"));
						writer.write(currentRequest.getSerializedCommand().getBytes());
						writer.flush();

						try {
							if (pending.fireAndForget) {
								Log.info("NOT Waiting for command to return: " + currentRequest.requestId);
								if(currentRequest.callback != null){
									currentRequest.callback.event(null);
								}
							} else if (!currentRequest.waiting.await(10000, TimeUnit.SECONDS)) {
								Log.info("Failed to get response for command: " + currentRequest.getSerializedCommand());
							} else {
								Log.info("Returned: " + currentRequest.getSerializedCommand());
							}
						} catch (InterruptedException e) {
							Log.error("Failed to await request response: " + pending, e);
						}
					}
				} catch (Exception e) {
					Log.error("Interrupted while writing to socket", e);
				}
			}
		});
	}


	public void on(String event, TitaniumAndroidDebugger.EventCallback callback){
		_callbacks.putIfAbsent(event, new ArrayList<TitaniumAndroidDebugger.EventCallback>());
		_callbacks.get(event).add(callback);
	}

	public void send(AndroidDebugCommands request) {
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


		send(AndroidDebugCommands.halt(new AndroidDebugCommands.Callback(){
			@Override
			public void event(Map response) {
//				send(AndroidDebugCommands.disconnect(new AndroidDebugCommands.Callback() {
//					@Override
//					public void event(Map response) {
						_debuggerExecutor.shutdownNow();
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
