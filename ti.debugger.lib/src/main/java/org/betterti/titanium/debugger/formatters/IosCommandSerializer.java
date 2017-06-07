package org.betterti.titanium.debugger.formatters;

import org.betterti.titanium.debugger.DebugCommand;
import org.betterti.titanium.debugger.commands.*;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class IosCommandSerializer implements CommandSerializer {
    @Override
    public String serialize(DebugCommand c) throws Exception {

        String content = "";



        if(c instanceof VersionInfoCommand){
            content += "*version";
        }
        if(c instanceof OptionCommand){
            OptionCommand oc = (OptionCommand) c;
            content += "*option*" + ((OptionCommand) c).getOptionName() + "*" + oc.getOptionValue();
        }
        if(c instanceof BreakpointCreateCommand){
            BreakpointCreateCommand oc = (BreakpointCreateCommand) c;
            content += "*breakpoint*create*app:" + oc.getFilename() + "*" + oc.getLineNumber() + "*1*0**1";
        }
        if(c instanceof ResumeCommand){
            content += "*resume*0";
        }
        if(c instanceof FramesCommand){
            content += "*frames*0";
        }
        if(c instanceof FrameVariablesCommand){
            content += "*variables*0*frame["  + ((FrameVariablesCommand) c).getFrameNumber() + "]";
        }

        if(c instanceof StepOverCommand){
            content += "*stepOver";
        }

        if(c instanceof StepIntoCommand){
            content += "*stepInto";
        }

        if(c instanceof StepReturnCommand){
            content += "*stepReturn";
        }

        if(content.length() == 0){
            throw new Exception("No implementation yet for command of type: " + c.getClass());
        }

        content = c.getId() + content;
//
//
//        + "*" + c.getCommand() +
//                (
//                        c.getSubCommand() != null ? "*" + c.getSubCommand() : ""
//                ) +
//                (
//                        c.getValue() != null ? "*" + c.getValue() : ""
//                );
//
//        if("breakpoint".equals(c.getCommand())){
//            content += "*" + c.getCustomData().get("action").toString();
//            content += "*" + c.getCustomData().get("filename").toString();
//            content += "*" + c.getCustomData().get("lineNumber").toString();
//            content += "*1*0**1";
//
//        }
//        if(DebugCommand instanceof OptionCommand){
//            OptionCommand oc = (OptionCommand) c;
//            content = c.getId() + "*option*" + oc.getOptionName() + "*" + oc.getOptionValue();
//        }
//
//        content = content.trim();

        return content.length() + "*" + content;
    }
}
