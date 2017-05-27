package org.betterti.titanium.debugger.android;

import com.google.gson.Gson;
import org.betterti.titanium.debugger.SubmittedRequest;
import org.betterti.titanium.debugger.TitaniumAndroidDebugger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Created by bourtney on 5/18/2017.
 */
public class AndroidInputWorker extends Thread {
  private static final Logger Log = LoggerFactory.getLogger(AndroidInputWorker.class);
  private InputStream _inputStream;
  private final Executor _callbackExecutor;


  private SubmittedRequestList _submittedRequestList;
  private ConcurrentHashMap<String, List<AndroidDataQueue.EventCallback>> _callbackMap;

  public AndroidInputWorker(InputStream inputStream,
                            Executor callbackExecutor,
                            SubmittedRequestList submittedRequestList,
                            ConcurrentHashMap<String, List<AndroidDataQueue.EventCallback>> callbackMap) {

    _inputStream = inputStream;
    _callbackExecutor = callbackExecutor;
    _submittedRequestList = submittedRequestList;
    _callbackMap = callbackMap;
  }

  @Override
  public void run() {
    try {
			InputStream is = _inputStream;
//
			while(!Thread.interrupted()){
				String contentLength = readUntil('\n',is);
				if(!Thread.interrupted() && contentLength != null && contentLength.contains("Content-Length")){
					Log.trace("Received contentlength: " + contentLength);

					String length = contentLength.replaceAll("Content-Length:", "").trim();

					Log.trace("Looks like we're going to get length: " + length);

					int responseLength = Integer.parseInt(length);

					if(responseLength > 0 ) {
						Log.trace("Reading until newline");
						readUntil('\n', is);
						Log.trace("Waiting for response string");
						String responseStr = readFor(responseLength);
						final Map map = new Gson().fromJson(responseStr, Map.class);
						Log.info("[From Debugger]: " + responseStr);

						try {
							if("response".equals(map.get("type"))) {
								long requestId = ((Number) (map.get("request_seq"))).longValue();


                SubmittedRequest currentRequest = _submittedRequestList.find(requestId);

                if (currentRequest != null && currentRequest.requestId == requestId) {
									Log.info("Request with id: " + requestId + " is completed");
									if (currentRequest.callback != null) {
										_callbackExecutor.execute(new Runnable() {
											@Override
											public void run() {
                        currentRequest.callback.event(map);
											}
										});
									}
                  currentRequest.getCommand().finish();
								}
							}
							else if("event".equals(map.get("type"))){
								final List<AndroidDataQueue.EventCallback> callbacks =
												_callbackMap.getOrDefault(map.get("event").toString(), new ArrayList<AndroidDataQueue.EventCallback>());
								for(AndroidDataQueue.EventCallback cb : callbacks){
									cb.event((Map)map.get("body"));
								}
							}
						} catch (NumberFormatException e) {
							Log.info("Ignoring the NFE");
						}
					}



				}
			}
    }
    catch (SocketException e){
      Log.error("Socket exception reading from socket", e);
    }
    catch (IOException e) {
      Log.error("Serious error while trying to write/read from debug socket", e);
    }
  }


  private String readUntil(char token, InputStream is) throws IOException {
    StringBuilder b = new StringBuilder();
    int read;
    Log.trace("Reading until: " + (token == '\n' ? "\\n" : token));
    while((read = is.read()) != -1 && !Thread.interrupted()) {
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

      int readThisTime = _inputStream.read(buffer, 0, desiredRead);

      final String content = new String(buffer, 0, readThisTime);
      Log.trace("READ: " + readThisTime + ":" + content);
      totalRead += readThisTime;
      b.append(content);
    }

    Log.info("Done reading");

    return b.toString();
  }
}
