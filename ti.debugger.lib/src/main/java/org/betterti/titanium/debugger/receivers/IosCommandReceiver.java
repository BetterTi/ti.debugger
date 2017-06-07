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
import java.util.ArrayList;
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
    public DebugResponse readNextCommand(InputStream is, PendingCommandList list) throws Exception {

            String lengthStr = readUntil('*', is);

            int length = Integer.parseInt(lengthStr);

            byte[] data = new byte[length];

            int amountRead = 0;
            while (amountRead < length) {
                amountRead += is.read(data, amountRead, length - amountRead);
            }
            String dataStr = new String(data);

        try {
            String[] parts = dataStr.split("\\*");

            String idStr = parts[0];

            DebugCommand c = null;
            Long id = null;
            try {
                id = Long.parseLong(idStr);

                PendingCommand pending = list.get(id);
                c = pending.getCommand();
            }
            catch(NumberFormatException e) {
                Log.trace("ID could not be parsed for this request. It may not have a corresponding command");
            }
            if(c != null){
                if (c instanceof VersionInfoCommand) {
                    return new VersionInfoResponse(
                            id,
                            parts[2]
                    );
                }
                if (c instanceof BreakpointCreateCommand) {
                    return new BreakpointCreatedResponse(
                            id
                    );
                }
                if (c instanceof OptionCommand) {
                    return new SimpleResponse(
                            id
                    );
                }
                if (c instanceof FramesCommand){
                    List<FramesResponse.Frame> frames = new ArrayList<>();
                    for(int i = 1; i < parts.length; i++) {
                        String[] subparts = parts[i].split("\\|");
                        frames.add(new FramesResponse.Frame(
                            Integer.parseInt(subparts[0]),
                            subparts[1],
                            subparts[3].replace("app:", ""),
                            Integer.parseInt(subparts[4])
                        ));
                    }

                    return new FramesResponse(
                            id,
                            frames
                    );
                }
                if (c instanceof FrameVariablesCommand){
                    List<FrameVariablesResponse.Variable> variables = new ArrayList<>();
                    for(int i = 1; i < parts.length; i++) {
                        //label1|object|nol|[object TiUILabel]
                        String[] subparts = parts[i].split("\\|");
                        variables.add(new FrameVariablesResponse.Variable(
                                subparts[0],
                                subparts[1],
                                subparts[2],
                                subparts[3])
                        );
                    }

                    return new FrameVariablesResponse(
                            id,
                            variables
                    );
                }
            }
            else{
                if ("suspended".equals(parts[0])) {
                    return new SuspendedResponse(
                            parts[2],
                            parts[3],
                            Integer.parseInt(parts[4])
                    );
                }
                if ("log".equals(parts[0])) {
                    return new LogResponse(
                            parts[1],
                            parts[2]
                    );
                }
                if ("resumed".equals(parts[0])) {
                    return new ResumeResponse(
                            Integer.parseInt(parts[1]),
                            parts[2]
                    );
                }

            }
        }
        catch (Exception e){
            throw new Exception("Encountered an error parsing the incoming response: " + lengthStr + "*" + dataStr, e);

        }
        throw new Exception("No response could be created for: " + lengthStr + "*" + dataStr);



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
