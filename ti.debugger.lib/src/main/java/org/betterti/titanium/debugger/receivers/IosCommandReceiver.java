package org.betterti.titanium.debugger.receivers;

import org.betterti.titanium.debugger.DebugCommand;
import org.betterti.titanium.debugger.PendingCommandList;
import org.betterti.titanium.debugger.commands.*;
import org.betterti.titanium.debugger.responses.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class IosCommandReceiver implements CommandReceiver {

    private static final Logger Log = LoggerFactory.getLogger(IosCommandReceiver.class);

    @Override
    public Socket connect() throws IOException {
        ServerSocket c = new ServerSocket(6543);
        Log.debug("Now listening for debug connections");
        Socket s = c.accept();
        Log.debug("Connection received");
        return s;
    }

    @Override
    public DebugResponse readNextCommand(InputStream is, PendingCommandList list) throws IOException {

        String lengthStr = readUntil('*', is);

        int length = Integer.parseInt(lengthStr);

        byte[] data = new byte[length];

        int amountRead = 0;
        while(amountRead < length) {
            amountRead += is.read(data, amountRead, length - amountRead);
        }
        String dataStr = new String(data);

        String[] parts = dataStr.split("\\*");

        String idStr = parts[0];

        try {
            long id = Long.parseLong(idStr);

            PendingCommand pending = list.get(id);
            DebugCommand c = pending.getCommand();
            DebugResponse r = null;
            if (c != null) {
                if (c instanceof VersionInfoCommand) {
                    r = new VersionInfoResponse(
                            id,
                            parts[2]
                    );
                }
                if (c instanceof BreakpointCreateCommand) {
                    r = new BreakpointCreatedResponse(
                            id
                    );
                }
                if (c instanceof OptionCommand) {
                    r = new SimpleResponse(
                            id
                    );
                }
                if (c instanceof ResumeCommand) {
                    r = new ResumeResponse(
                            id
                    );
                }
                return r;
            }
        }
        catch(NumberFormatException e){
            if("suspended".equals(parts[0]) && parts.length > 3 && "breakpoint".equals(parts[2])){
                return new BreakpointReachedResponse(
                        parts[3],
                        Integer.parseInt(parts[4])
                );
            }
        }




//        Map<String, String> customData = new HashMap<>();
//        if(parts.length > 1){
//            if("created".equals(parts[1])){
//                customData.put("type", "breakpoint");
//                customData.put("result", "created");
//            }
//            if("suspended".equals(parts[0]) && parts.length == 5 && "breakpoint".equals(parts[2])){
//                customData.put("type", "suspended");
//                customData.put("filename", parts[3]);
//                customData.put("lineNumber", parts[4]);
//            }
//            else{
//                id = parts[0];
//            }
//
//        }

//        DebugResponse r = new DebugResponse(id, customData);





        return null;
    }

    @Override
    public List<DebugCommand> getInitialCommands() {
        return Arrays.asList(
                new OptionCommand("stepFiltersEnabled", "false"),
                new OptionCommand("monitorXHR", "true"),
                new OptionCommand("suspendOnFirstLine", "false"),
                new OptionCommand("suspendOnExceptions", "false"),
                new OptionCommand("suspendOnErrors", "true"),
                new OptionCommand("suspendOnKeywords", "true"),
                new OptionCommand("bypassConstructors", "false"),
                new OptionCommand("stepFiltersEnabled", "false")
        );
    }


    private static String readUntil(char token, InputStream is) throws IOException {
        StringBuilder b = new StringBuilder();
        int read;
//    Log.trace("Reading until: " + (token == '\n' ? "\\n" : token));
        while((read = is.read()) != -1 && !Thread.interrupted()) {
            if (read == token) {
                return b.toString();
            } else {
                b.append((char)read);
            }
        }
        return b.toString();
    }
}
