package org.betterti.titanium.debugger;

import org.betterti.titanium.debugger.commands.*;
import org.betterti.titanium.debugger.formatters.CommandSerializer;
import org.betterti.titanium.debugger.receivers.CommandReceiver;
import org.betterti.titanium.debugger.responses.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class BaseDebugger implements Debugger {

    private static final Logger Log = LoggerFactory.getLogger(BaseDebugger.class);


    private ExecutorService _outputWorker = Executors.newSingleThreadExecutor();
    private ExecutorService _inputWorker = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("debugger-input-reader");
            return t;
        }
    });
    private PendingCommandList _list = new DefaultPendingCommandList();
    private static Map<Class<? extends DebugResponse>, List<Callback<? extends DebugResponse>>> _responseCallbacks = new HashMap<>();

    private Socket _socket;
    private InputStream _inputStream;
    private OutputStream _outputStream;

    private CommandSerializer _commandSerializer;
    private CommandReceiver _commandReceiver;

    public BaseDebugger(CommandReceiver receiver, CommandSerializer serializer){
        _commandReceiver = receiver;
        _commandSerializer = serializer;
    }

    @Override
    public PendingCommand<SimpleResponse> disconnect() {
        return send(new DisconnectCommand());
    }

    @Override
    public PendingCommand<NoResponse> stepReturn() {
        return send(new StepReturnCommand());
    }

    @Override
    public PendingCommand<SimpleResponse> removeBreakpoint(String filename, int linenumber) {
        return send(new BreakpointRemoveCommand(filename, linenumber));
    }

    @Override
    public void connect() throws IOException {
        _socket = _commandReceiver.connect();
        _inputStream = _socket.getInputStream();
        _outputStream = _socket.getOutputStream();
        _inputWorker.submit(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()){
                    try {
                        DebugResponse r = _commandReceiver.readNextCommand(_inputStream, _list);
                        if(r.getId() != null){
                            PendingCommand c = _list.get(r.getId());
                            if(c != null){
                                c.finish(r);
                            }
                            _list.remove(r.getId());
                        }
                        else{
                            for(Callback c :_responseCallbacks.getOrDefault(r.getClass(), new ArrayList<>())){
                                c.onResponse(r);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        _commandReceiver.getInitialCommands();
    }

    @Override
    public PendingCommand<VersionInfoResponse> queryVersion(){
        Log.debug("Issuing version query command");
        return send(new VersionInfoCommand());
    }

    @Override
    public PendingCommand<FramesResponse> queryFrames(){
        Log.debug("Issuing frames query command");
        return send(new FramesCommand());
    }


    @Override
    public PendingCommand<FrameVariablesResponse> queryFrameVariables(int frameNumber){
        Log.debug("Issuing frames query command");
        return send(new FrameVariablesCommand(frameNumber));
    }

    @Override
    public PendingCommand<BreakpointCreatedResponse> createBreakpoint(String filename, int lineNumber){
        Log.debug("Issuing create breakpoint command: {}:{}", filename, lineNumber);
        return send(new BreakpointCreateCommand(filename, lineNumber));
    }

    @Override
    public PendingCommand<NoResponse> resume(){
        Log.debug("Issuing resume command");
        return send(new ResumeCommand());
    }

    @Override
    public PendingCommand<NoResponse> stepOver(){
        Log.debug("Issuing step over command");
        return send(new StepOverCommand());
    }

    @Override
    public PendingCommand<NoResponse> stepInto(){
        Log.debug("Issuing step into command");
        return send(new StepIntoCommand());
    }

    private <T extends DebugCommand<TR>, TR extends DebugResponse> PendingCommand<TR> send(T command) {
        PendingCommand c = new PendingCommand<>(command);
        _list.put(c);
        _outputWorker.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    String serialized = _commandSerializer.serialize(c.getCommand());
                    Log.trace("Command of type {} serialized to {}", command.getClass(), serialized);
                    _outputStream.write(serialized.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return c;
    }

    @Override
    public <T extends DebugResponse> void listen(Class<T> clazz, Callback<T> callback) {
        List<Callback<?>> callbacks = _responseCallbacks.getOrDefault(clazz, new ArrayList<>());
        callbacks.add(callback);
        _responseCallbacks.put(clazz, callbacks);
    }
}
