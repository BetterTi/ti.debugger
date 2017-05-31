package org.betterti.titanium.debugger.commands;

import org.betterti.titanium.debugger.DebugCommand;
import org.betterti.titanium.debugger.Debugger;
import org.betterti.titanium.debugger.responses.DebugResponse;
import org.betterti.titanium.debugger.responses.NoResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class PendingCommand<TR extends DebugResponse>{

    private final DebugCommand<TR> _command;
    private Debugger.Callback<? extends DebugResponse> _callback;
    private TR _result;
    private CountDownLatch _countDownLatch = new CountDownLatch(1);

    private List<Debugger.Callback<TR>> _callbackList;

    public PendingCommand(DebugCommand<TR> c) {
        _command = c;
    }

    public DebugCommand getCommand() {
        return _command;
    }


    public synchronized void finish(TR result){
         _result = result;
         if(_callbackList != null){
             for(Debugger.Callback<TR> c : _callbackList){
                c.onResponse(result);
             }
             _callbackList = null;
         }
        _countDownLatch.countDown();
    }

    public synchronized void onDone(Debugger.Callback<TR> callback){
        if(_result != null){
            callback.onResponse(_result);
        }
        else{
            if(_callbackList == null){
                _callbackList = new ArrayList<>();
            }
            _callbackList.add(callback);
        }
    }

    public TR waitFor() throws InterruptedException {
        if(!_command.hasResponse()){
            return (TR)new NoResponse();
        }
        _countDownLatch.await();
        return _result;
    }



}
