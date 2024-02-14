package main.bytecode;

public class FReturn extends Bytecode {
    @Override
    public String toString() {
        return indent(1) + "freturn";
    }
}
