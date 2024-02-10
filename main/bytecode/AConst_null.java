package main.bytecode;

public class AConst_null extends Bytecode {
    @Override
    public String toString() {
        return indent(1) + "aconst_null";
    }
}
