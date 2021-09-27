package plc.compiler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Stdlib {

    private static final Map<String, Type> TYPES = new HashMap<>();
    private static final Map<String, Function> FUNCTIONS = new HashMap<>();

    public static Type getType(String name) throws AnalysisException {
        if (!TYPES.containsKey(name)) {
            throw new AnalysisException("Unknown type " + name + ".");
        }
        return TYPES.get(name);
    }

    public static Function getFunction(String name, int arity) throws AnalysisException {
        String signature = name + "/" + arity;
        if (!FUNCTIONS.containsKey(signature)) {
            throw new AnalysisException("Unknown function " + signature + ".");
        }
        return FUNCTIONS.get(signature);
    }

    public static void registerType(Type type) {
        if (TYPES.containsKey(type.getName())) {
            throw new IllegalArgumentException("Duplicate registration of type " + type.getName() + ".");
        }
        TYPES.put(type.getName(), type);
    }

    public static void registerFunction(Function function) {
        String signature = function.getName() + "/" + function.getParameterTypes().size();
        if (FUNCTIONS.containsKey(signature)) {
            throw new IllegalArgumentException("Duplicate registration of function " + signature + ".");
        }
        FUNCTIONS.put(signature, function);
    }

    public static final class Type {

        public static final Type BOOLEAN = new Type("BOOLEAN", "boolean");
        public static final Type INTEGER = new Type("INTEGER", "int");
        public static final Type DECIMAL = new Type("DECIMAL", "double");
        public static final Type STRING = new Type("STRING", "String");
        public static final Type ANY = new Type("ANY", "Object");
        public static final Type VOID = new Type("VOID", "Void");

        private final String name;
        private final String jvmName;

        public Type(String name, String jvmName) {
            this.name = name;
            this.jvmName = jvmName;
        }

        public String getName() {
            return name;
        }

        public String getJvmName() {
            return jvmName;
        }

        @Override
        public String toString() {
            return "Type{" +
                    "name='" + name + '\'' +
                    ", jvmName='" + jvmName + '\'' +
                    '}';
        }

    }

    public static final class Function {

        public static final Function PRINT = new Function("PRINT", "System.out.println", Arrays.asList(Type.ANY), Type.VOID);

        private final String name;
        private final String jvmName;
        private final List<Type> parameterTypes;
        private final Type returnType;

        public Function(String name, String jvmName, List<Type> parameterTypes, Type returnType) {
            this.name = name;
            this.jvmName = jvmName;
            this.parameterTypes = parameterTypes;
            this.returnType = returnType;
        }

        public String getName() {
            return name;
        }

        public String getJvmName() {
            return jvmName;
        }

        public List<Type> getParameterTypes() {
            return parameterTypes;
        }

        public Type getReturnType() {
            return returnType;
        }

        @Override
        public String toString() {
            return "Function{" +
                    "name='" + name + '\'' +
                    ", jvmName='" + jvmName + '\'' +
                    ", parameterTypes=" + parameterTypes +
                    ", returnType=" + returnType +
                    '}';
        }

    }

    static {
        registerType(Type.BOOLEAN);
        registerType(Type.INTEGER);
        registerType(Type.DECIMAL);
        registerType(Type.STRING);
        registerType(Type.ANY);
        registerFunction(Function.PRINT);
    }

}
