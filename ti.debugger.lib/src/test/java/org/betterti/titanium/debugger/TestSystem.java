package org.betterti.titanium.debugger;


import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bourtney on 4/19/2017.
 */
public class TestSystem {

  @Test
  public void test() throws IOException, InterruptedException {

    TitaniumAndroidDebugger debugger = new TitaniumAndroidDebugger();
    ProcessBuilder b = new ProcessBuilder();
    b.command("node", "C:\\Program Files\\nodejs\\node_modules\\titanium\\bin\\titanium",
            "build",
            "-p", "android",
            "--debug-host", "/127.0.0.1:54321",
            "--skip-js-minify");
    b.directory(new File("testprojects/KitchenSink"));
    final Process p = b.start();


    List<Thread> threads = new ArrayList<Thread>();

    threads.add(new Thread(){
      @Override
      public void run() {
        try {
          IOUtils.copy(p.getInputStream(), System.out);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
    threads.add(new Thread(){
      @Override
      public void run() {
        try {
          IOUtils.copy(p.getErrorStream(), System.err);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });






    debugger.connect();

    debugger.resume();
    debugger.setBreakpoint(Paths.get("app.js"), 130);
//
//
//
    while(true){Thread.sleep(100);}


  }
}
