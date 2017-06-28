package com.betterti.titanium.debugger.receivers;

import org.betterti.titanium.debugger.DebugCommand;
import org.betterti.titanium.debugger.PendingCommandList;
import org.betterti.titanium.debugger.commands.*;
import org.betterti.titanium.debugger.responses.*;
import org.betterti.titanium.debugger.receivers.IosCommandReceiver;
import org.junit.Test;

import java.io.ByteArrayInputStream;

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
        return new PendingCommand(mock(c));
    }

    @Test
    public void test_version_response() throws Exception {
        PendingCommandList l = mock(PendingCommandList.class);

        when(l.get(eq(1496073159530l))).thenReturn(mockPendingCommand(VersionInfoCommand.class));

        IosCommandReceiver r = new IosCommandReceiver();
        ByteArrayInputStream bais = new ByteArrayInputStream("21*1496073159530*2*5.5.1".getBytes());
        VersionInfoResponse response = (VersionInfoResponse) r.readNextCommand(bais, l);
        assertEquals(Long.valueOf(1496073159530L), response.getId());
        assertEquals("5.5.1", response.getVersionName());
    }

    @Test
    public void test_option_response() throws Exception {
        PendingCommandList l = mock(PendingCommandList.class);

        when(l.get(eq(1496073159532L))).thenReturn(mockPendingCommand(OptionCommand.class));

        IosCommandReceiver r = new IosCommandReceiver();
        ByteArrayInputStream bais = new ByteArrayInputStream("13*1496073159532".getBytes());
        SimpleResponse response = (SimpleResponse) r.readNextCommand(bais, l);
        assertEquals(Long.valueOf(1496073159532L), response.getId());
    }

    @Test
    public void test_breakpoint_created_response() throws Exception {
        PendingCommandList l = mock(PendingCommandList.class);

        when(l.get(eq(1496073159542L))).thenReturn(mockPendingCommand(BreakpointCreateCommand.class));

        IosCommandReceiver r = new IosCommandReceiver();
        ByteArrayInputStream bais = new ByteArrayInputStream("21*1496073159542*created".getBytes());
        BreakpointCreatedResponse response = (BreakpointCreatedResponse) r.readNextCommand(bais, l);
        assertEquals(Long.valueOf(1496073159542L), response.getId());
    }

    @Test
    public void test_breakpoint_reached() throws Exception {
        PendingCommandList l = mock(PendingCommandList.class);

        IosCommandReceiver r = new IosCommandReceiver();
        ByteArrayInputStream bais = new ByteArrayInputStream("37*suspended*0*breakpoint*app:/app.js*34".getBytes());
        SuspendedResponse response = (SuspendedResponse) r.readNextCommand(bais, l);
        assertNull(response.getId());
        assertEquals("app:/app.js", response.getFilename());
        assertEquals(34, response.getLineNumber());
    }

    @Test
    public void test_resume() throws Exception {
        PendingCommandList l = mock(PendingCommandList.class);

        IosCommandReceiver r = new IosCommandReceiver();
        ByteArrayInputStream bais = new ByteArrayInputStream("16*resumed*0*resume".getBytes());
        ResumeResponse response = (ResumeResponse) r.readNextCommand(bais, l);
        assertNull(response.getId());
        assertEquals(0, response.getUnknownValue());
        assertEquals("resume", response.getType());
    }

    @Test
    public void test_frames_response() throws Exception {
        PendingCommandList l = mock(PendingCommandList.class);


        when(l.get(eq(1496777153885L))).thenReturn(mockPendingCommand(FramesCommand.class));

        String data = "121*1496777153885*0|onceMore||app:/app.js|81|false|0|0*1|insideAnother||app:/app.js|75|false|0|0*2|||app:/app.js|70|false|0|0";

        IosCommandReceiver r = new IosCommandReceiver();
        ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes());
        FramesResponse response = (FramesResponse) r.readNextCommand(bais, l);
        assertEquals(3, response.getFrames().size());
        assertEquals(0, response.getFrames().get(0).index);
        assertEquals("onceMore", response.getFrames().get(0).functionName);
        assertEquals("app:/app.js", response.getFrames().get(0).file.toString());
        assertEquals(81, response.getFrames().get(0).lineNumber);
        assertEquals("" , response.getFrames().get(2).functionName);
    }

    @Test
    public void test_frames_variables() throws Exception {
        PendingCommandList l = mock(PendingCommandList.class);


        when(l.get(eq(1496073159545L))).thenReturn(mockPendingCommand(FrameVariablesCommand.class));

        String data = "247*1496073159545*label1|object|nol|[object TiUILabel]*label2|undefined|wl|undefined*tab1|object|nol|[object TiUITab]*tab2|undefined|wl|undefined*tabGroup|object|nol|[object TiUITabGroup]*win1|object|nol|[object TiUIWindow]*win2|undefined|wl|undefined";

        IosCommandReceiver r = new IosCommandReceiver();
        ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes());
        FrameVariablesResponse response = (FrameVariablesResponse) r.readNextCommand(bais, l);
        assertEquals(7, response.getVariables().size());
//        assertEquals(0, response.getVariables().get(0).index);
    }
}
