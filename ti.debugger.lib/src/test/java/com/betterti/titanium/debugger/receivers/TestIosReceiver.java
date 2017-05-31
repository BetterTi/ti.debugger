package com.betterti.titanium.debugger.receivers;

import org.betterti.titanium.debugger.DebugCommand;
import org.betterti.titanium.debugger.PendingCommandList;
import org.betterti.titanium.debugger.commands.BreakpointCreateCommand;
import org.betterti.titanium.debugger.commands.OptionCommand;
import org.betterti.titanium.debugger.commands.PendingCommand;
import org.betterti.titanium.debugger.commands.VersionInfoCommand;
import org.betterti.titanium.debugger.responses.BreakpointCreatedResponse;
import org.betterti.titanium.debugger.receivers.IosCommandReceiver;
import org.betterti.titanium.debugger.responses.BreakpointReachedResponse;
import org.betterti.titanium.debugger.responses.SimpleResponse;
import org.betterti.titanium.debugger.responses.VersionInfoResponse;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class TestIosReceiver {

    private PendingCommand mockPendingCommand(Class<? extends DebugCommand> c){
        return new PendingCommand(mock(c), null);
    }

    @Test
    public void test_version_response() throws IOException {
        PendingCommandList l = mock(PendingCommandList.class);

        when(l.get(eq(1496073159530l))).thenReturn(mockPendingCommand(VersionInfoCommand.class));

        IosCommandReceiver r = new IosCommandReceiver();
        ByteArrayInputStream bais = new ByteArrayInputStream("21*1496073159530*2*5.5.1".getBytes());
        VersionInfoResponse response = (VersionInfoResponse) r.readNextCommand(bais, l);
        assertEquals(Long.valueOf(1496073159530L), response.getId());
        assertEquals("5.5.1", response.getVersionName());
    }

    @Test
    public void test_option_response() throws IOException {
        PendingCommandList l = mock(PendingCommandList.class);

        when(l.get(eq(1496073159532L))).thenReturn(mockPendingCommand(OptionCommand.class));

        IosCommandReceiver r = new IosCommandReceiver();
        ByteArrayInputStream bais = new ByteArrayInputStream("13*1496073159532".getBytes());
        SimpleResponse response = (SimpleResponse) r.readNextCommand(bais, l);
        assertEquals(Long.valueOf(1496073159532L), response.getId());
    }

    @Test
    public void test_breakpoint_created_response() throws IOException {
        PendingCommandList l = mock(PendingCommandList.class);

        when(l.get(eq(1496073159542L))).thenReturn(mockPendingCommand(BreakpointCreateCommand.class));

        IosCommandReceiver r = new IosCommandReceiver();
        ByteArrayInputStream bais = new ByteArrayInputStream("21*1496073159542*created".getBytes());
        BreakpointCreatedResponse response = (BreakpointCreatedResponse) r.readNextCommand(bais, l);
        assertEquals(Long.valueOf(1496073159542L), response.getId());
    }

    @Test
    public void test_breakpoint_reached() throws IOException {
        PendingCommandList l = mock(PendingCommandList.class);

        IosCommandReceiver r = new IosCommandReceiver();
        ByteArrayInputStream bais = new ByteArrayInputStream("37*suspended*0*breakpoint*app:/app.js*34".getBytes());
        BreakpointReachedResponse response = (BreakpointReachedResponse) r.readNextCommand(bais, l);
        assertNull(response.getId());
        assertEquals("app:/app.js", response.getFilename());
        assertEquals(34, response.getLineNumber());
    }
}
