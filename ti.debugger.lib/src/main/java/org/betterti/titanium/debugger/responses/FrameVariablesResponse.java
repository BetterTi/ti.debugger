package org.betterti.titanium.debugger.responses;

import java.util.List;

/**
 * Created by johnsba1 on 5/29/17.
 */
public class FrameVariablesResponse extends DebugResponse {
    private final List<Variable> _variables;

    public static class Variable{
        public final String name;
        public final String type;
        public final String scope; //? I think? Values are nol/wl
        public final String value;

        public Variable(String name, String type, String scope, String value) {
            this.name = name;
            this.type = type;
            this.scope = scope;
            this.value = value;
        }
    }
    //label1|object|nol|[object TiUILabel]

    public FrameVariablesResponse(Long id, List<Variable> variables) {
        super(id);
        _variables = variables;
    }

    public List<Variable> getVariables() {
        return _variables;
    }
}
