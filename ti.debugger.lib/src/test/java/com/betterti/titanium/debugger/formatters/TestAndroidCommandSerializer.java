package com.betterti.titanium.debugger.formatters;

import org.betterti.titanium.debugger.DebugCommand;
import org.betterti.titanium.debugger.commands.*;
import org.betterti.titanium.debugger.formatters.AndroidCommandSerializer;
import org.betterti.titanium.debugger.formatters.IosCommandSerializer;
import org.betterti.titanium.debugger.receivers.BreakpointDatabase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class TestAndroidCommandSerializer {


    private AndroidCommandSerializer _formatter;

    private BreakpointDatabase _breakpointDatabase;

    @Before
    public void setUp() throws Exception {
        _breakpointDatabase = mock(BreakpointDatabase.class);
        _formatter = new AndroidCommandSerializer(_breakpointDatabase);
    }

    @Test
    public void test_version_serialize() throws Exception {

        DebugCommand version = new VersionInfoCommand();
        String data = _formatter.serialize(version);

        assertEquals("Content-Length:" + "58\r\n\r\n{\"seq\":" + version.getId() + ",\"type\":\"request\",\"command\":\"version\"}",
                data
        );
    }

    @Test
    public void test_breakpoint_formatter() throws Exception {
        DebugCommand c = new BreakpointCreateCommand("/app.js", 58);
        assertEquals("Content-Length:148\r\n\r\n{\"seq\":" + c.getId() + ",\"type\":\"request\",\"command\":\"setbreakpoint\",\"arguments\":{\"line\":57,\"column\":0,\"type\":\"script\",\"enabled\":true,\"target\":\"app.js\"}}",
                _formatter.serialize(c));
    }

    @Test
    public void test_delete_breakpoint_command() throws Exception {
        when(_breakpointDatabase.fetch("/app.js", 58)).thenReturn(5);
        DebugCommand c = new BreakpointRemoveCommand("/app.js", 58);

        String command = "{\"seq\":" + c.getId() + ",\"type\":\"request\",\"command\":\"clearbreakpoint\",\"arguments\":{\"breakpoint\":5}}";

        assertEquals("Content-Length:" + command.length() + "\r\n\r\n" + command,
                _formatter.serialize(c));
    }


    @Test
    public void test_resume_command() throws Exception {
        DebugCommand c = new ResumeCommand();
        assertEquals(   "Content-Length:59\r\n\r\n{\"seq\":" + c.getId() + ",\"type\":\"request\",\"command\":\"continue\"}",
                _formatter.serialize(c));
    }
    @Test
    public void test_frames_command() throws Exception {
        DebugCommand c = new FramesCommand();
        assertEquals("Content-Length:92\r\n\r\n{\"seq\":" + c.getId() + ",\"type\":\"request\",\"command\":\"backtrace\",\"arguments\":{\"inlineRefs\":true}}",
                _formatter.serialize(c));
    }
    @Test
    public void test_frame_variables_command() throws Exception {
        DebugCommand c = new FrameVariablesCommand(5);
        assertEquals("Content-Length:115\r\n\r\n{\"seq\":" + c.getId() + ",\"type\":\"request\",\"command\":\"scope\",\"arguments\":{\"number\":0,\"frameNumber\":0,\"inlineRefs\":true}}" ,
                _formatter.serialize(c));
    }
    @Test
    public void test_stepover() throws Exception {
        DebugCommand c = new StepOverCommand();
        assertEquals("Content-Length:107\r\n\r\n{\"seq\":" + c.getId() + ",\"type\":\"request\",\"command\":\"continue\",\"arguments\":{\"stepaction\":\"next\",\"stepcount\":1}}",
                _formatter.serialize(c));
    }
    @Test
    public void test_stepin() throws Exception {
        DebugCommand c = new StepIntoCommand();
        assertEquals("Content-Length:105\r\n\r\n{\"seq\":" + c.getId() + ",\"type\":\"request\",\"command\":\"continue\",\"arguments\":{\"stepaction\":\"in\",\"stepcount\":1}}",
                _formatter.serialize(c));
    }
    @Test
    public void test_stepout() throws Exception {
        DebugCommand c = new StepReturnCommand();
        assertEquals("Content-Length:106\r\n\r\n{\"seq\":" + c.getId() + ",\"type\":\"request\",\"command\":\"continue\",\"arguments\":{\"stepaction\":\"out\",\"stepcount\":1}}",
                _formatter.serialize(c));
    }
}