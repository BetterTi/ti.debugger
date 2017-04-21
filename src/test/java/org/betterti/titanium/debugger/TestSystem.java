package org.betterti.titanium.debugger;


import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by bourtney on 4/19/2017.
 */
public class TestSystem {

  @Test
  public void test() throws IOException, InterruptedException {

    TitaniumAndroidDebugger debugger = new TitaniumAndroidDebugger();
    ProcessBuilder b = new ProcessBuilder();
    b.command("node", "C:\\Program Files\\nodejs\\node_modules\\titanium\\bin\\titanium", "build", "-p", "android", "-T", "device", "--skip-js-minify");
    b.directory(new File("testprojects/KitchenSink"));

//    Process p = b.connect();





    debugger.connect();

    debugger.resume();
    debugger.addBreakpoint(Paths.get("app.js"), 130);



    while(true){Thread.sleep(100);}


  }
}
