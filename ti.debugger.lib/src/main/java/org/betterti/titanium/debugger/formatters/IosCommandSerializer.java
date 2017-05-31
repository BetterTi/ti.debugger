package org.betterti.titanium.debugger.formatters;

import org.betterti.titanium.debugger.DebugCommand;
import org.betterti.titanium.debugger.commands.BreakpointCreateCommand;
import org.betterti.titanium.debugger.commands.OptionCommand;
import org.betterti.titanium.debugger.commands.ResumeCommand;
import org.betterti.titanium.debugger.commands.VersionInfoCommand;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class IosCommandSerializer implements CommandSerializer {
    @Override
    public String serialize(DebugCommand c) {

        String content = c.getId() + "";

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
