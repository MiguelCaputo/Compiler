package plc.compiler;

import java.util.HashMap;
import java.util.Map;

public final class Scope {

    private final Scope parent;
    private final Map<String, Stdlib.Type> variables = new HashMap<>();

    public Scope(Scope parent) {
        this.parent = parent;
    }

    public Scope getParent() {
        return parent;
    }

    public void define(String name, Stdlib.Type type) throws AnalysisException {
        if (variables.containsKey(name)) {
            throw new AnalysisException("The identifier " + name + " is already defined in this scope.");
        } else {
            variables.put(name, type);
        }
    }

    public Stdlib.Type lookup(String name) throws AnalysisException {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else if (parent != null) {
            return parent.lookup(name);
        } else {
            throw new AnalysisException("The identifier " + name + " is not defined.");
        }
    }

}
