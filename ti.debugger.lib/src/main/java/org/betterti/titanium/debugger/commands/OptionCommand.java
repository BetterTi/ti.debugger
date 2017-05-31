package org.betterti.titanium.debugger.commands;

import org.betterti.titanium.debugger.DebugCommand;

import java.util.Map;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class OptionCommand extends DebugCommand{
    private final String _optionName;
    private final String _optionValue;

    public OptionCommand(String optionName, String optionValue) {
        super(System.currentTimeMillis());
        _optionName = optionName;
        _optionValue = optionValue;
    }

    public String getOptionName() {
        return _optionName;
    }

    public String getOptionValue() {
        return _optionValue;
    }
}
