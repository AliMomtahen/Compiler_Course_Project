package main.bytecode;

public class Invoke extends Bytecode {
    protected String invokeType = "static";
    private final String name;
    private final String signature;

    public Invoke(String name, String signature) {
        this.name = name;
        this.signature = signature;
    }

    public Invoke(String name, String signature , String invoke_type) {
        this.name = name;
        this.signature = signature;
        this.invokeType = invoke_type;
    }

    @Override
    public String toString() {
        return indent(1) + "invoke" + invokeType + " " + name + signature;
    }
}