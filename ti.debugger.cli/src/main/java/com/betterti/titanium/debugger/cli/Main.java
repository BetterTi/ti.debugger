package com.betterti.titanium.debugger.cli;

import org.betterti.titanium.debugger.BaseDebugger;
import org.betterti.titanium.debugger.Debugger;
import org.betterti.titanium.debugger.EventCallbackTest;
import org.betterti.titanium.debugger.TitaniumAndroidDebugger;
import org.betterti.titanium.debugger.api.FrameResult;
import org.betterti.titanium.debugger.api.FramesCallback;
import org.betterti.titanium.debugger.formatters.AndroidCommandSerializer;
import org.betterti.titanium.debugger.formatters.IosCommandSerializer;
import org.betterti.titanium.debugger.receivers.AndroidCommandReceiver;
import org.betterti.titanium.debugger.receivers.BreakpointDatabase;
import org.betterti.titanium.debugger.receivers.BreakpointDatabaseImpl;
import org.betterti.titanium.debugger.receivers.IosCommandReceiver;
import org.betterti.titanium.debugger.responses.*;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by bourtney on 5/17/2017.
 */
public class Main {

  public static final String BREAKPOINT_CMD = "breakpoint";
  public static final String BREAKPOINT_ADD_CMD = "add";
  private static final String BREAKPOINT_REMOVE_CMD = "remove";

  public static final String RESUME_CMD = "resume";
  private static final String DISCONNECT_CMD = "disconnect";
  private static boolean shutdown;

  public static void main(String[] arg) throws IOException, ExecutionException, InterruptedException {

//    BaseDebugger d = new BaseDebugger(new IosCommandReceiver(), new IosCommandSerializer());
    BreakpointDatabase db = new BreakpointDatabaseImpl();
    BaseDebugger d  = new BaseDebugger(new AndroidCommandReceiver(db), new AndroidCommandSerializer(db));

    d.connect();


    int i = 0;

    d.listen(ResumeResponse.class, response -> {
      System.out.println("resumed");
    });

    d.listen(SuspendedResponse.class, response -> {
      System.out.println("system paused: breakpoint reached: " + response.getFilename() + ":" + response.getLineNumber());
    });

    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    String line;
    while((line = reader.readLine()) != null && !Thread.interrupted() && !shutdown){
      System.out.println(line);

      if(line.startsWith(BREAKPOINT_CMD)){
        line = line.substring(BREAKPOINT_CMD.length()).trim();
        if(line.startsWith(BREAKPOINT_ADD_CMD)){
          line = line.substring(BREAKPOINT_ADD_CMD.length()).trim();
          String[] parts = line.split(" ");
          d.createBreakpoint(parts[0],Integer.parseInt(parts[1])).onDone(new Debugger.Callback<BreakpointCreatedResponse>() {
            @Override
            public void onResponse(BreakpointCreatedResponse respond) {
              System.out.println("breakpoint added " + parts[0] + ":" + parts[1]);
            }
          });
        }
        if(line.startsWith(BREAKPOINT_REMOVE_CMD)){
          line = line.substring(BREAKPOINT_REMOVE_CMD.length()).trim();
          String[] parts = line.split(" ");
          d.removeBreakpoint(parts[0],Integer.parseInt(parts[1])).onDone(new Debugger.Callback<SimpleResponse>() {
            @Override
            public void onResponse(SimpleResponse respond) {
              System.out.println("breakpoint removed");
            }
          });

        }
      }
      if(line.startsWith("version")){
        d.queryVersion().onDone(new Debugger.Callback<VersionInfoResponse>() {
          @Override
          public void onResponse(VersionInfoResponse respond) {
            System.out.println("version: " + respond.getVersionName());
          }
        });
      }
      if(line.startsWith("frames")){
        d.queryFrames().onDone(new Debugger.Callback<FramesResponse>() {
          @Override
          public void onResponse(FramesResponse respond) {
            System.out.println("frames: ");
            for(FramesResponse.Frame f : respond.getFrames()){
              System.out.println("\t" + f.index + ":" + f.functionName + ":" + f.file + ":" + f.lineNumber);
            }
          }
        });
      } else if (line.startsWith("frame")) {
        line = line.substring("frame".length()).trim();
        if(line.startsWith("vars")) {
          line = line.substring("vars".length()).trim();
          d.queryFrameVariables(Integer.parseInt(line)).onDone(new Debugger.Callback<FrameVariablesResponse>() {
            @Override
            public void onResponse(FrameVariablesResponse respond) {
              System.out.println("variables");
              for(FrameVariablesResponse.Variable v : respond.getVariables()){
                System.out.println("\t" + v.type + " " + v.name + " = " + v.value);
              }
            }
          });
        }
      }
      if(line.startsWith("step")){
        line = line.substring("step".length()).trim();
        if(line.trim().equals("over")){
          d.stepOver().onDone(new Debugger.Callback<NoResponse>() {
            @Override
            public void onResponse(NoResponse respond) {
              System.out.println("stepped over");
            }
          });
        }
        if(line.trim().equals("into")){
          d.stepInto().onDone(new Debugger.Callback<NoResponse>() {
            @Override
            public void onResponse(NoResponse respond) {
              System.out.println("stepped into");
            }
          });
        }
        if(line.trim().equals("out")){
          d.stepReturn().onDone(new Debugger.Callback<NoResponse>() {
            @Override
            public void onResponse(NoResponse respond) {
              System.out.println("stepped out");

            }
          });
        }
      }
      if(line.startsWith(RESUME_CMD)){
        d.resume().waitFor();
      }
      if(line.startsWith(DISCONNECT_CMD)){
        d.disconnect().waitFor();
        System.out.println("disconnected");
        shutdown = true;
      }
    }
    if(!shutdown){
      d.disconnect().waitFor();
    }
  }

  private static String readUntil(char token, InputStream is) throws IOException {
    StringBuilder b = new StringBuilder();
    int read;
//    Log.trace("Reading until: " + (token == '\n' ? "\\n" : token));
    while((read = is.read()) != -1 && !Thread.interrupted()) {
      if (read == token) {
        return b.toString();
      } else {
        b.append((char)read);
      }
    }
    return b.toString();
  }

  public static void go() throws IOException, ExecutionException, InterruptedException {

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

    androidDebugger.setEventListener(new EventCallbackTest() {
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
