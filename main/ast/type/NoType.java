package main.ast.type;

public class NoType extends Type {
    @Override
    public String toString() {
        return "noType";
    }
    public String getStrType(){
        return "V";
    }

}
