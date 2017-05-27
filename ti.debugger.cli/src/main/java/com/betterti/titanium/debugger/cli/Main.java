package com.betterti.titanium.debugger.cli;

import org.betterti.titanium.debugger.TitaniumAndroidDebugger;
import org.betterti.titanium.debugger.api.FrameResult;
import org.betterti.titanium.debugger.api.FramesCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by bourtney on 5/17/2017.
 */
public class Main {

  public static final String BREAKPOINT_CMD = "breakpoint";
  public static final String BREAKPOINT_ADD_CMD = "add";

  public static final String RESUME_CMD = "resume";
  private static final String DISCONNECT_CMD = "disconnect";
  private static boolean shutdown;

  public static void main(String[] arg) throws IOException, ExecutionException, InterruptedException {

    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    TitaniumAndroidDebugger androidDebugger = new TitaniumAndroidDebugger();
    System.out.println("connecting...");
    androidDebugger.connect();
    System.out.println("connected");
    String line;
    shutdown = false;

    Runtime.getRuntime().addShutdownHook(new Thread(){
      @Override
      public void run() {
        System.out.println("here?");
        if(!shutdown){
          try {
            androidDebugger.disconnect().get();
          } catch (InterruptedException e) {
            e.printStackTrace();
          } catch (ExecutionException e) {
            e.printStackTrace();
          }
        }
      }
    });

    androidDebugger.setEventListener(new TitaniumAndroidDebugger.EventCallbacks() {
      @Override
      public void onPause(PauseEvent e) {
        System.out.println("pause");
        System.out.println("\t" + e.filename + ":" + e.line);
      }
    });

    while((line = reader.readLine()) != null && !Thread.interrupted() && !shutdown){
      System.out.println(line);

      if(line.startsWith(BREAKPOINT_CMD)){
        line = line.substring(BREAKPOINT_CMD.length()).trim();
        if(line.startsWith(BREAKPOINT_ADD_CMD)){
          line = line.substring(BREAKPOINT_ADD_CMD.length()).trim();
          androidDebugger.setBreakpoint(Paths.get("app.js"),131).get();
          System.out.println("breakpoint added");
        }
      }
      if(line.startsWith("frames")){
        androidDebugger.fetchFrames(new FramesCallback() {
          @Override
          public void event(List<FrameResult> results) {
            System.out.println("Frames fetched");
            for(FrameResult r : results){
              System.out.println("\t" + r.fileName + ":" + r.funcName + ":" + r.line);
            }
          }
        });
      }
      if(line.startsWith("step")){
        line = line.substring("step".length()).trim();
        if(line.trim().equals("over")){
          androidDebugger.stepOver().get();
          System.out.println("stepped over");
        }
        if(line.trim().equals("into")){
          androidDebugger.stepInto().get();
          System.out.println("stepped into");
        }
        if(line.trim().equals("out")){
          androidDebugger.stepReturn().get();
          System.out.println("stepped out");
        }
      }
      if(line.startsWith(RESUME_CMD)){
        androidDebugger.resume().get();
        System.out.println("resumed");
      }
      if(line.startsWith(DISCONNECT_CMD)){
        androidDebugger.disconnect().get();
        System.out.println("disconnected");
        shutdown = true;
      }
    }
    if(!shutdown){
      androidDebugger.disconnect().get();
    }
  }
}
