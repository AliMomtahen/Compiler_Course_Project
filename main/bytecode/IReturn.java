package main.bytecode;

import main.bytecode.Bytecode;

public class IReturn extends Bytecode {
    @Override
    public String toString() {
        return indent(1) + "ireturn";
    }
}
