package main.bytecode;

import main.ast.type.Type;
import main.ast.type.primitiveType.FloatType;

public class Div extends Bytecode {
    Type type;
    public Div(Type type){
        this.type = type;
    }
    @Override
    public String toString() {
        if(type instanceof FloatType){
            return indent(1) + "fdiv";
        }
        else
            return indent(1) + "idiv";
    }
}
