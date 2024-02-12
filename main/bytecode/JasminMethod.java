package main.bytecode;

import java.util.List;

import main.ast.type.primitiveType.BoolType;
import main.ast.type.primitiveType.FloatType;
import main.ast.type.primitiveType.IntType;
import main.ast.type.primitiveType.StringType;
import main.visitor.*;
import main.ast.type.*;
import main.visitor.codeGenerator.CodeGenerator;


public class JasminMethod extends Bytecode{
    private final String name;
    private final String returnType;
    private List<String> args;
    private final List<String> body;
    protected boolean isStatic = true;

//    private Integer stackSize = 128;
//    private Integer localSize = 128;
    
    public JasminMethod(String name, Type returnType, List<Type> args, List<String> body) {
        this.name = name;
        this.returnType = makeTypeSignature(returnType);

        for(Type s : args){
            this.args.add(makeTypeSignature(s));
        }
        this.body = body;
    }
    
    private String makeTypeSignature(Type t) {

        if (t instanceof IntType) {
            return "I";
        } else if (t instanceof FloatType) {
            return "F";
        } else if (t instanceof BoolType) {
            return "Z";
        } else if(t instanceof StringType){
            return "[C";
        }
        return "V";
    }

    public String getName() {
        return name;
    }

    public String getSignature() {
        StringBuilder res = new StringBuilder();
        res.append('(');
        for (String arg : args) {
            res.append(arg);
        }
        res.append(')').append(returnType);
        return res.toString();
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append(".method public ");
        if (isStatic) {
            res.append("static ");
        }
        res.append(name).append('(');
        for (String arg : args)
            res.append(arg);
        res.append(')').append(returnType).append('\n');
        Integer stackSize = 128;
        res.append(".limit stack ").append(stackSize).append('\n');
        Integer localSize = 128;
        res.append(".limit locals ").append(localSize).append('\n');
        for (var bytecode : body)
            res.append(bytecode);
        res.append(".end method\n\n");
        return res.toString();
    }
}

