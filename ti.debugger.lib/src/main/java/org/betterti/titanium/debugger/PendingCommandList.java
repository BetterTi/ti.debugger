package org.betterti.titanium.debugger;

import org.betterti.titanium.debugger.commands.PendingCommand;

/**
 * Created by johnsba1 on 5/29/17.
 */
public interface PendingCommandList {

    void put(PendingCommand c);


    PendingCommand get(long commandId);

    void remove(long commandId);


}
