package main.bytecode;

import main.ast.type.Type;
import main.ast.type.primitiveType.FloatType;

public class Mul extends Bytecode {
    Type type;
    public Mul(Type type){
        this.type = type;
    }
    @Override
    public String toString() {
        if(type instanceof FloatType){
            return indent(1) + "fmul";
        }
        else
            return indent(1) + "imul";
    }
}
