package com.betterti.titanium.debugger.cli;

import javafx.scene.shape.Path;
import org.betterti.titanium.debugger.TitaniumAndroidDebugger;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

/**
 * Created by bourtney on 5/17/2017.
 */
public class Main {

  public static final String BREAKPOINT_CMD = "breakpoint";
  public static final String BREAKPOINT_ADD_CMD = "add";

  public static final String RESUME_CMD = "resume";

  public static void main(String[] arg) throws IOException {




    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    TitaniumAndroidDebugger androidDebugger = new TitaniumAndroidDebugger();
    System.out.println("connecting...");
    androidDebugger.connect();
    System.out.println("connected");
    String line;
    while((line = reader.readLine()) != null){
      System.out.println(line);

      if(line.startsWith(BREAKPOINT_CMD)){
        line = line.substring(BREAKPOINT_CMD.length()).trim();
        if(line.startsWith(BREAKPOINT_ADD_CMD)){
          line = line.substring(BREAKPOINT_ADD_CMD.length()).trim();
          androidDebugger.addBreakpoint(Paths.get("app.js"),131);
          System.out.println("breakpoint added");
        }
      }
      if(line.startsWith(RESUME_CMD)){
        androidDebugger.resume();
      }
    }

  }
}
