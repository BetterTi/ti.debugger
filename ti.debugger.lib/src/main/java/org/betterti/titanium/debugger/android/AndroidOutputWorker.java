package org.betterti.titanium.debugger.android;

import org.betterti.titanium.debugger.AndroidSubmittedRequest;
import org.betterti.titanium.debugger.SubmittedRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by bourtney on 5/18/2017.
 */
public class AndroidOutputWorker extends Thread {
  private static final Logger Log = LoggerFactory.getLogger(AndroidOutputWorker.class);
  private BlockingQueue<AndroidDebugCommand> _commandQueue;
  private SubmittedRequestList _submittedRequestList;
  private OutputStream _outputStream;

  public AndroidOutputWorker(OutputStream outputStream,
                             BlockingQueue<AndroidDebugCommand> commandQueue,
                             SubmittedRequestList submittedRequestList) {
    _outputStream = outputStream;
    _commandQueue = commandQueue;
    _submittedRequestList = submittedRequestList;
  }

  @Override
  public void run() {
    Log.info("Starting writing commands to output socket");
    try {
      OutputStream writer = _outputStream;
      while (!Thread.currentThread().isInterrupted()) {
        AndroidDebugCommand pending = _commandQueue.take();
        SubmittedRequest currentRequest = new SubmittedRequest(pending);

        _submittedRequestList.addPending(currentRequest);
        Log.info("[To Debugger]: " + currentRequest.getSerializedCommand().trim().replace("\r\n", "\\r\\n").replace("\n", "\\n"));
        writer.write(currentRequest.getSerializedCommand().getBytes());
        writer.flush();
      }
    } catch (Exception e) {
      Log.error("Interrupted while writing to socket", e);
    }
  }
}
