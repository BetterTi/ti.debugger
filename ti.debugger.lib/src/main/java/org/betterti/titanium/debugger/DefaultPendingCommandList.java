package org.betterti.titanium.debugger;

import org.betterti.titanium.debugger.commands.PendingCommand;

import java.util.HashMap;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class DefaultPendingCommandList implements PendingCommandList {

    private HashMap<Long, PendingCommand> _commandMap = new HashMap<>();

    @Override
    public void put(PendingCommand c) {
        _commandMap.put(c.getCommand().getId(), c);
    }

    @Override
    public PendingCommand get(long commandId) {
        return null;
    }

    @Override
    public void remove(long commandId) {

    }
}
