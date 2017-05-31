package org.betterti.titanium.debugger.commands;

import org.betterti.titanium.debugger.DebugCommand;
import org.betterti.titanium.debugger.responses.NoResponse;
import org.betterti.titanium.debugger.responses.ResumeResponse;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class ResumeCommand extends DebugCommand<NoResponse>{
    public ResumeCommand(){
    }


    @Override
    public boolean hasResponse(){return false;}


}
