package com.betterti.titanium.debugger.formatters;

import org.betterti.titanium.debugger.DebugCommand;
import org.betterti.titanium.debugger.commands.BreakpointCreateCommand;
import org.betterti.titanium.debugger.commands.OptionCommand;
import org.betterti.titanium.debugger.commands.ResumeCommand;
import org.betterti.titanium.debugger.commands.VersionInfoCommand;
import org.betterti.titanium.debugger.formatters.IosCommandSerializer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class TestIosCommandFormatter {


    @Test
    public void test_version_serialize(){
        IosCommandSerializer formatter = new IosCommandSerializer();

        DebugCommand version = new VersionInfoCommand();
        String data = formatter.serialize(version);

        assertEquals(
            "21*" + version.getId() + "*version",
            data
        );
    }
    @Test
    public void test_ios_command_step_filters_formatter(){
        IosCommandSerializer formatter = new IosCommandSerializer();


        DebugCommand c = new OptionCommand("monitorXHR", "true");
        assertEquals("36*" + c.getId() + "*option*monitorXHR*true",
                formatter.serialize(c));

        c = new OptionCommand("suspendOnFirstLine", "false");
        assertEquals("45*" + c.getId() + "*option*suspendOnFirstLine*false",
                formatter.serialize(c));

        c = new OptionCommand("suspendOnExceptions", "false");
        assertEquals("46*" + c.getId() + "*option*suspendOnExceptions*false",
                formatter.serialize(c));

        c = new OptionCommand("suspendOnErrors", "true");
        assertEquals("41*" + c.getId() + "*option*suspendOnErrors*true",
                formatter.serialize(c));

        c = new OptionCommand("suspendOnKeywords", "true");
        assertEquals("43*" + c.getId() + "*option*suspendOnKeywords*true",
                formatter.serialize(c));

        c = new OptionCommand("bypassConstructors", "false");
        assertEquals("45*" + c.getId() + "*option*bypassConstructors*false",
                formatter.serialize(c));

        c = new OptionCommand("stepFiltersEnabled", "false");
        assertEquals("45*" + c.getId() + "*option*stepFiltersEnabled*false",
                formatter.serialize(c));
    }

    @Test
    public void test_breakpoint_formatter(){
        IosCommandSerializer formatter = new IosCommandSerializer();

        DebugCommand c = new BreakpointCreateCommand("/app.js", 44);
        assertEquals("53*" + c.getId() + "*breakpoint*create*app:/app.js*44*1*0**1",
                formatter.serialize(c));
    }

    @Test
    public void test_resume_command(){
        IosCommandSerializer formatter = new IosCommandSerializer();

        DebugCommand c = new ResumeCommand();
        assertEquals("22*" + c.getId() + "*resume*0",
                formatter.serialize(c));
    }
}
