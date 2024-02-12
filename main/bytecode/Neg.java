package main.bytecode;

import main.ast.type.Type;
import main.ast.type.primitiveType.FloatType;

public class Neg extends Bytecode {
    Type type;
    public Neg(Type type){
        this.type = type;
    }
    @Override
    public String toString() {
        if(type instanceof FloatType){
            return indent(1) + "fneg";
        }
        else
            return indent(1) + "ineg";
    }
}
