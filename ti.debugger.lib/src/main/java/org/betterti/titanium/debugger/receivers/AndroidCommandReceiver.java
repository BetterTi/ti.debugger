package org.betterti.titanium.debugger.receivers;

import com.google.gson.Gson;
import org.betterti.titanium.debugger.DebugCommand;
import org.betterti.titanium.debugger.PendingCommandList;
import org.betterti.titanium.debugger.commands.*;
import org.betterti.titanium.debugger.responses.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class AndroidCommandReceiver implements CommandReceiver {

    private static final Logger Log = LoggerFactory.getLogger(AndroidCommandReceiver.class);
    private BreakpointDatabase _database;

    public AndroidCommandReceiver(BreakpointDatabase database) {
        _database = database;
    }

    @Override
    public Socket connect() throws IOException {
        Socket s = new Socket("localhost", 6543);
        InputStream inputStream = s.getInputStream();
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        if(r.readLine() == null){
            throw new IOException("Bad connection");
        }
        Log.debug("Connection looks good.");
        String input = r.readLine();
        Log.debug("Received: " + input);
        input = r.readLine();
        Log.debug("Received: " + input);
        input = r.readLine();
        Log.debug("Received: " + input);
        return s;
    }

    @Override
    public DebugResponse readNextCommand(InputStream is, PendingCommandList list) throws Exception {

        String contentLength = readUntil('\n', is);

        Log.trace("Received contentlength: " + contentLength);

        String length = contentLength.replaceAll("Content-Length:", "").trim();

        Log.trace("Looks like we're going to get length: " + length);

        int responseLength = Integer.parseInt(length);

        if (responseLength > 0) {
            Log.trace("Reading until newline");
            readUntil('\n', is);
            Log.trace("Waiting for response string");
            String responseStr = readFor(responseLength, is);
            final Map map = new Gson().fromJson(responseStr, Map.class);
            Log.info("[From Debugger]: " + responseStr);


            Number requestId = (Number) map.get("request_seq");

            if("continue".equals(map.get("command"))){
                return new ResumeResponse(0,"resumed");
            }
            else if(requestId != null){
                Long id = requestId.longValue();

                PendingCommand pending = list.get(id);
                DebugCommand command = pending.getCommand();
                if(command instanceof VersionInfoCommand){
                    return new VersionInfoResponse(
                            id,
                            ((Map)map.get("body")).get("V8Version").toString()
                    );
                }
                if(command instanceof BreakpointCreateCommand){
                    _database.register(
                            "/" + getStr(map, "body", "script_name"),
                            getInt(map, "body", "line") + 1,
                            getInt(map, "body", "breakpoint"));
                    return new BreakpointCreatedResponse(
                            id
                    );
                }

                if(command instanceof BreakpointRemoveCommand){
                    _database.delete(((BreakpointRemoveCommand) command).getFilename(), ((BreakpointRemoveCommand) command).getLineNumber());
                    return new SimpleResponse(
                            id
                    );
                }

                if(command instanceof FramesCommand){
                    List scriptRefs = (List) map.get("refs");

                    List<FramesResponse.Frame> frames = new ArrayList<>();
                    int i = 0;
                    for(Object incomingFrame : getList(map, "body", "frames")){
                        Map scriptRef = null;
                        for(Object s : scriptRefs){
                            if(getStr(s,"handle").equals(getStr(incomingFrame,"script","ref"))){
                                scriptRef = (Map) s;
                            }
                        }
                        String text = getStr(incomingFrame, "text");
                        String func = text.split("\\s")[1];


                        String functionFilePath = getStr(scriptRef, "name");
                        URI functionUri;
                        if(functionFilePath.startsWith("ti:")){
                            functionUri = new URI( functionFilePath);
                        }
                        else{
                            functionUri =  new URI("app:/" + functionFilePath);
                        }
                        frames.add(new FramesResponse.Frame(
                                i,
                                func,
                                functionUri,
                                getInt(incomingFrame,"line")));
                        i++;
                    }
                    return new FramesResponse(
                            getLong(map, "request_seq"),
                            frames);
                }
            }
            else{
                if("event".equals(map.get("type")) && "break".equals(map.get("event"))){
                    Map body = (Map) map.get("body");
                    Map script = (Map) body.get("script");
                    return new SuspendedResponse(
                            "suspended",
                            "/" + script.get("name").toString(),
                            ((Number) body.get("sourceLine")).intValue() + 1
                    );
                }
            }



        }


        return null;
    }

    private static String readFor(int amount, InputStream inputStream) throws IOException {
        final int bufferSize = 10;
        byte[] buffer = new byte[bufferSize];


        Log.info("Starting read");
        StringBuffer b = new StringBuffer();
        int totalRead = 0;
        while(totalRead < amount){
            int desiredRead = Math.min(bufferSize, amount - totalRead);

            int readThisTime = inputStream.read(buffer, 0, desiredRead);

            final String content = new String(buffer, 0, readThisTime);
            Log.trace("READ: " + readThisTime + ":" + content);
            totalRead += readThisTime;
            b.append(content);
        }

        Log.info("Done reading");

        return b.toString();
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

    private static List getList(Object map, String ... props){
        return (List)getObj(map, props);
    }

    private static int getInt(Object map, String ... props){
        return ((Number)getObj(map, props)).intValue();
    }
    private static long getLong(Object map, String ... props){
        return ((Number)getObj(map, props)).longValue();
    }
    private static String getStr(Object map, String ... props){
        return getObj(map, props).toString();
    }

    private static Object getObj(Object map, String ... prop){
        Object latest = map;
        for(int i = 0; i < prop.length; i++){
            latest = ((Map)latest).get(prop[i]);
        }
        return latest;
    }
}
