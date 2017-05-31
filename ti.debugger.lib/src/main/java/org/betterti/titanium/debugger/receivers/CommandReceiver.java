package org.betterti.titanium.debugger.receivers;

import org.betterti.titanium.debugger.DebugCommand;
import org.betterti.titanium.debugger.PendingCommandList;
import org.betterti.titanium.debugger.responses.DebugResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

/**
 * Created by johnsba1 on 5/29/17.
 */
public interface CommandReceiver {


    Socket connect() throws IOException;

    DebugResponse readNextCommand(InputStream is, PendingCommandList list) throws IOException;


    List<DebugCommand> getInitialCommands();
}
