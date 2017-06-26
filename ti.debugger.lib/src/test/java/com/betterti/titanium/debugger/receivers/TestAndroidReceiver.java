package com.betterti.titanium.debugger.receivers;

import org.betterti.titanium.debugger.DebugCommand;
import org.betterti.titanium.debugger.PendingCommandList;
import org.betterti.titanium.debugger.commands.*;
import org.betterti.titanium.debugger.receivers.AndroidCommandReceiver;
import org.betterti.titanium.debugger.receivers.BreakpointDatabase;
import org.betterti.titanium.debugger.receivers.IosCommandReceiver;
import org.betterti.titanium.debugger.responses.*;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class TestAndroidReceiver {

    private PendingCommandList _l;
    private AndroidCommandReceiver _r;
    private BreakpointDatabase _db;

    private PendingCommand mockPendingCommand(Class<? extends DebugCommand> c){
        return new PendingCommand(mock(c));
    }

    @Before
    public void setUp() throws Exception {

        _l = mock(PendingCommandList.class);
        _db = mock(BreakpointDatabase.class);
        _r = new AndroidCommandReceiver(_db);
    }

    @Test
    public void test_version_response() throws Exception {

        when(_l.get(eq(1496887573317L))).thenReturn(mockPendingCommand(VersionInfoCommand.class));

        ByteArrayInputStream bais = new ByteArrayInputStream("Content-Length:148\r\n\r\n{\"seq\":2,\"request_seq\":1496887573317,\"type\":\"response\",\"command\":\"version\",\"success\":true,\"body\":{\"V8Version\":\"3.9.24.29\"},\"refs\":[],\"running\":true}".getBytes());
        VersionInfoResponse response = (VersionInfoResponse) _r.readNextCommand(bais, _l);
        assertEquals(Long.valueOf(1496887573317L), response.getId());
        assertEquals("3.9.24.29", response.getVersionName());
    }


    @Test
    public void test_breakpoint_created_response() throws Exception {

        when(_l.get(eq(1496889083139L))).thenReturn(mockPendingCommand(BreakpointCreateCommand.class));


        String json = "{\"seq\":0,\"request_seq\":1496889083139,\"type\":\"response\",\"command\":\"setbreakpoint\",\"success\":true,\"body\":{\"type\":\"scriptName\",\"breakpoint\":1,\"script_name\":\"app.js\",\"line\":81,\"column\":0,\"actual_locations\":[]},\"refs\":[],\"running\":false}";
        ByteArrayInputStream bais = new ByteArrayInputStream(("Content-Length:" + json.length() + "\r\n\r\n" + json)
                .getBytes());
        BreakpointCreatedResponse response = (BreakpointCreatedResponse) _r.readNextCommand(bais, _l);
        assertEquals(Long.valueOf(1496889083139L), response.getId());

        verify(_db, times(1)).register(eq("/app.js"),eq(82), eq(1));
    }

    @Test
    public void test_breakpoint_reached() throws Exception {
        String json = "{\"seq\":3,\"type\":\"event\",\"event\":\"break\",\"body\":{\"invocationText\":\"[anonymous]() returning undefined\",\"sourceLine\":81,\"sourceColumn\":0,\"sourceLineText\":\"}\",\"script\":{\"id\":37,\"name\":\"app.js\",\"lineOffset\":0,\"columnOffset\":0,\"lineCount\":82},\"breakpoints\":[1]}}";
        ByteArrayInputStream bais = new ByteArrayInputStream(("Content-Length:" + json.length() + "\r\n\r\n" + json)
            .getBytes());
        SuspendedResponse response = (SuspendedResponse) _r.readNextCommand(bais, _l);
        assertNull(response.getId());
        assertEquals("/app.js", response.getFilename());
        assertEquals(82, response.getLineNumber());
    }

    @Test
    public void test_resume() throws Exception {
        when(_l.get(eq(1496891230578L))).thenReturn(mockPendingCommand(ResumeCommand.class));

        String json = "{\"seq\":4,\"request_seq\":1496891230578,\"type\":\"response\",\"command\":\"continue\",\"success\":true,\"running\":true}";
        ByteArrayInputStream bais = new ByteArrayInputStream(("Content-Length:" + json.length() + "\r\n\r\n" + json)
                .getBytes());
        ResumeResponse response = (ResumeResponse) _r.readNextCommand(bais, _l);
        assertNull(response.getId());
        assertEquals(0, response.getUnknownValue());
        assertEquals("resumed", response.getType());
    }

    @Test
    public void test_frames_response() throws Exception {
        PendingCommandList l = mock(PendingCommandList.class);


        when(l.get(eq(1497035630619L))).thenReturn(mockPendingCommand(FramesCommand.class));

        String json = "{\"seq\":4,\"request_seq\":1497035630619,\"type\":\"response\",\"command\":\"backtrace\",\"success\":true,\"body\":{\"fromFrame\":0,\"toFrame\":3,\"totalFrames\":3,\"frames\":[{\"type\":\"frame\",\"index\":0,\"receiver\":{\"ref\":1,\"type\":\"object\",\"className\":\"global\"},\"func\":{\"ref\":0,\"type\":\"function\",\"name\":\"\",\"inferredName\":\"onceMore\",\"scriptId\":37},\"script\":{\"ref\":7},\"constructCall\":false,\"atReturn\":true,\"returnValue\":{\"ref\":2,\"type\":\"undefined\"},\"debuggerFrame\":false,\"arguments\":[],\"locals\":[],\"position\":1448,\"line\":81,\"column\":0,\"sourceLineText\":\"}\",\"scopes\":[{\"type\":1,\"index\":0},{\"type\":0,\"index\":1}],\"text\":\"#00 [anonymous]() returning undefined app.js line 82 column 1 (position 1449)\"},{\"type\":\"frame\",\"index\":1,\"receiver\":{\"ref\":1,\"type\":\"object\",\"className\":\"global\"},\"func\":{\"ref\":8,\"type\":\"function\",\"name\":\"insideAnother\",\"inferredName\":\"\",\"scriptId\":37},\"script\":{\"ref\":7},\"constructCall\":false,\"atReturn\":false,\"debuggerFrame\":false,\"arguments\":[],\"locals\":[],\"position\":1333,\"line\":74,\"column\":4,\"sourceLineText\":\"    onceMore();\",\"scopes\":[{\"type\":1,\"index\":0},{\"type\":0,\"index\":1}],\"text\":\"#01 insideAnother() app.js line 75 column 5 (position 1334)\"},{\"type\":\"frame\",\"index\":2,\"receiver\":{\"ref\":9,\"type\":\"object\",\"className\":\"Titanium\"},\"func\":{\"ref\":10,\"type\":\"function\",\"name\":\"\",\"inferredName\":\"onceMore\",\"scriptId\":37},\"script\":{\"ref\":7},\"constructCall\":false,\"atReturn\":false,\"debuggerFrame\":false,\"arguments\":[],\"locals\":[],\"position\":1241,\"line\":69,\"column\":4,\"sourceLineText\":\"    insideAnother()\",\"scopes\":[{\"type\":1,\"index\":0},{\"type\":0,\"index\":1}],\"text\":\"#02 #<Titanium>.[anonymous]() app.js line 70 column 5 (position 1242)\"}]},\"refs\":[{\"handle\":7,\"type\":\"script\",\"name\":\"app.js\",\"id\":37,\"lineOffset\":0,\"columnOffset\":0,\"lineCount\":82,\"sourceStart\":\"// this sets the background color of the master UIView (when there are no window\",\"sourceLength\":1449,\"scriptType\":2,\"compilationType\":0,\"context\":{\"ref\":6},\"text\":\"app.js (lines: 82)\"}],\"running\":false}";
        ByteArrayInputStream bais = new ByteArrayInputStream(("Content-Length:" + json.length() + "\r\n\r\n" + json).getBytes());

        AndroidCommandReceiver r = new AndroidCommandReceiver(mock(BreakpointDatabase.class));
        FramesResponse response = (FramesResponse) r.readNextCommand(bais, l);
        assertEquals(Long.valueOf(1497035630619L), response.getId());
        assertEquals(3, response.getFrames().size());
        assertEquals(0, response.getFrames().get(0).index);
        assertEquals("[anonymous]()", response.getFrames().get(0).functionName);
        assertEquals("/app.js", response.getFrames().get(0).file);
        assertEquals(81, response.getFrames().get(0).lineNumber);
        assertEquals("#<Titanium>.[anonymous]()" , response.getFrames().get(2).functionName);
    }
}
