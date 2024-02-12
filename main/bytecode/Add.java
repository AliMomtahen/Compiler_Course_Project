package main.bytecode;

import main.ast.type.Type;
import main.ast.type.primitiveType.FloatType;

public class Add extends Bytecode {
    Type type;
    public Add(Type type){
        this.type = type;
    }
    @Override
    public String toString() {
        if(type instanceof FloatType){
            return indent(1) + "fadd";
        }
        else
            return indent(1) + "iadd";
    }
}
