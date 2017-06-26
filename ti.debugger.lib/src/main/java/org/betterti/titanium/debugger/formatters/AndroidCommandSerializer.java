package org.betterti.titanium.debugger.formatters;

import com.google.gson.Gson;
import org.betterti.titanium.debugger.DebugCommand;
import org.betterti.titanium.debugger.commands.*;
import org.betterti.titanium.debugger.receivers.BreakpointDatabase;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class AndroidCommandSerializer implements CommandSerializer {
    private BreakpointDatabase _breakpointDatabase;

    public AndroidCommandSerializer(BreakpointDatabase breakpointDatabase) {
        _breakpointDatabase = breakpointDatabase;
    }

    @Override
    public String serialize(DebugCommand c) throws Exception {

        Map content = new LinkedHashMap();
        content.put("seq", c.getId());
        content.put("type", "request");

        if(c instanceof VersionInfoCommand){
            content.put("command", "version");
        }
        if(c instanceof BreakpointCreateCommand){
            content.put("command", "setbreakpoint");

            HashMap m = new LinkedHashMap();
            BreakpointCreateCommand bcc = (BreakpointCreateCommand) c;
            m.put("line", bcc.getLineNumber() - 1);
            m.put("column", 0);
            m.put("type", "script");
            m.put("enabled", true);
            m.put("target", bcc.getFilename().startsWith("/") ? bcc.getFilename().substring(1) : bcc.getFilename());
            content.put("arguments", m);
        }
        if(c instanceof BreakpointRemoveCommand){
            content.put("command", "clearbreakpoint");
            HashMap m = new LinkedHashMap();
            BreakpointRemoveCommand bcc = (BreakpointRemoveCommand) c;
            int breakpointId = _breakpointDatabase.fetch(((BreakpointRemoveCommand) c).getFilename(), ((BreakpointRemoveCommand) c).getLineNumber());
            m.put("breakpoint", breakpointId);
            content.put("arguments", m);
        }
        if(c instanceof ResumeCommand){
            content.put("command", "continue");
        }
        if(c instanceof FramesCommand){
            //"command":"backtrace","arguments":{"inlineRefs":true}}
            content.put("command", "backtrace");
            HashMap args = new LinkedHashMap<>();
            args.put("inlineRefs", true);
            content.put("arguments", args);
        }
        if(c instanceof FrameVariablesCommand){
            //{"seq":1496885070798,"type":"request","command":"scope","arguments":{"number":0,"frameNumber":0,"inlineRefs":true}}
            content.put("command", "scope");

            HashMap args = new LinkedHashMap();
            args.put("number", 0);
            args.put("frameNumber", 0);
            args.put("inlineRefs", true);

            content.put("arguments", args);
        }
        if(c instanceof StepOverCommand){
            //"seq":15,"type":"request","command":"continue","arguments":{"stepaction":"in","stepcount":1}
            content.put("command", "continue");

            HashMap args = new LinkedHashMap();
            args.put("stepaction", "next");
            args.put("stepcount", 1);

            content.put("arguments", args);
        }
        if(c instanceof StepIntoCommand){
            //"seq":15,"type":"request","command":"continue","arguments":{"stepaction":"in","stepcount":1}
            content.put("command", "continue");

            HashMap args = new LinkedHashMap();
            args.put("stepaction", "in");
            args.put("stepcount", 1);

            content.put("arguments", args);
        }
        if(c instanceof StepReturnCommand){
            //"seq":15,"type":"request","command":"continue","arguments":{"stepaction":"in","stepcount":1}
            content.put("command", "continue");

            HashMap args = new LinkedHashMap();
            args.put("stepaction", "out");
            args.put("stepcount", 1);

            content.put("arguments", args);
        }

        final String s = new Gson().toJson(content);
        return "Content-Length:" + s.length() + "\r\n\r\n" + s;
    }
}
