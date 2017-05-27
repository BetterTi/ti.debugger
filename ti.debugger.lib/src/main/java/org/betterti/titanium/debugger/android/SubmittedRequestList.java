package org.betterti.titanium.debugger.android;

import org.betterti.titanium.debugger.AndroidSubmittedRequest;
import org.betterti.titanium.debugger.SubmittedRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bourtney on 5/18/2017.
 */
public class SubmittedRequestList {

  private List<SubmittedRequest> _pendingCommands = new ArrayList<>();

  public void addPending(SubmittedRequest c){
    _pendingCommands.add(c);
  }

  public SubmittedRequest find(final long requestId){
    return _pendingCommands.stream().filter(x-> x.requestId == requestId).findFirst().orElse(null);
  }

}
