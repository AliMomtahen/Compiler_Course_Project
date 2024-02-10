package main.visitor.codeGenerator;

import main.ast.node.Program;
import java.util.HashMap;
import main.ast.node.declaration.*;
import main.ast.node.expression.Expression;
import main.ast.node.expression.Identifier;
import main.ast.node.statement.*;
import main.ast.type.Type;
import main.ast.type.primitiveType.*;
import main.visitor.Visitor;
import main.ast.node.expression.FunctionCall;
import main.ast.node.expression.values.BoolValue;
import main.ast.node.expression.values.IntValue;
import main.ast.node.declaration.FunctionDeclaration;
import main.ast.node.expression.values.StringValue;
import main.visitor.typeAnalyzer.TypeChecker;
import java.lang.String.*


import java.util.ArrayList;


import java.io.*;
import java.util.List;

public class CodeGenerator extends Visitor<String> {
    //    You may use following items or add your own for handling typechecker
    TypeChecker expressionTypeChecker;
    //    Graph<String> classHierarchy;
    private HashMap<String,Integer> slots;
    private String outputPath;
    private FileWriter currentFile;
    private FunctionDeclaration currentMethod;

    public CodeGenerator() {
//        this.classHierarchy = classHierarchy;

//        Uncomment below line to initialize your typechecker
        this.expressionTypeChecker = new TypeChecker(new ArrayList());

//        Call your type checker here!
//        ----------------------------
        
        this.prepareOutputFolder();
        this.createFile("out");

    }
    private Integer putInHash(String var){
        if (slots.containsKey(var)){
            return slots.get(var);
        }
        else{
            slots.put(var,slots.size());
            return slots.size();
        }
    }
    private void prepareOutputFolder() {
        this.outputPath = "output/";
        String jasminPath = "utilities/jarFiles/jasmin.jar";
        String listClassPath = "utilities/codeGenerationUtilityClasses/List.j";
        String fptrClassPath = "utilities/codeGenerationUtilityClasses/Fptr.j";
        try{
            File directory = new File(this.outputPath);
            File[] files = directory.listFiles();
            if(files != null)
                for (File file : files)
                    file.delete();
            directory.mkdir();
        }
        catch(SecurityException e) { }
        copyFile(jasminPath, this.outputPath + "jasmin.jar");
        copyFile(listClassPath, this.outputPath + "List.j");
        copyFile(fptrClassPath, this.outputPath + "Fptr.j");
    }

    private void copyFile(String toBeCopied, String toBePasted) {
        try {
            File readingFile = new File(toBeCopied);
            File writingFile = new File(toBePasted);
            InputStream readingFileStream = new FileInputStream(readingFile);
            OutputStream writingFileStream = new FileOutputStream(writingFile);
            byte[] buffer = new byte[1024];
            int readLength;
            while ((readLength = readingFileStream.read(buffer)) > 0)
                writingFileStream.write(buffer, 0, readLength);
            readingFileStream.close();
            writingFileStream.close();
        } catch (IOException e) { }
    }

    private void createFile(String name) {
        try {
            String path = this.outputPath + name + ".j";
            File file = new File(path);
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(path);
            this.currentFile = fileWriter;
        } catch (IOException e) {}
    }

    private void addCommand(String command) {
        try {
            command = String.join("\n\t\t", command.split("\n"));
            if(command.startsWith("Label_"))
                this.currentFile.write("\t" + command + "\n");
            else if(command.startsWith("."))
                this.currentFile.write(command + "\n");
            else
                this.currentFile.write("\t\t" + command + "\n");
            this.currentFile.flush();
        } catch (IOException e) {}
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

    @Override
    public String visit(Program program) {
        createFile("out.txt");
        for (var dec : program.getVars()){
            addCommand(dec.accept(this));
        }
        for (var dec : program.getFunctions()){
            addCommand(dec.accept(this));
        }
        return null;
    }

    @Override
    public String visit(FunctionDeclaration functionDeclaration) {
        var res = new JasminMethod(functionDeclaration.getName().getName(), functionDeclaration.getReturnType(),
                functionDeclaration.getArgs().stream().map(VarDeclaration::getType).toList(),
                functionDeclaration.getBody().stream().map(s->s.accept(this)).toList());
        Signatures.put(functionDeclaration.getName().getName(), res);
        return res.toString();
    }

    @Override
    public String visit(VarDeclaration varDeclaration) {
        String varname = varDeclaration.getIdentifier().getName();
        Type vartype = varDeclaration.getIdentifier().getType();
        Expression assignval = varDeclaration.getRValue();
        Integer slot_ind = this.putInHash(varname);
        if(assignval != null){
            addCommand(assignval.accept(this));// must first load val then
            //addCommand();// store it to slot_ind
        }

        return null;
    }

    @Override
    public String visit(main.ast.node.statement.AssignStmt assignmentStmt) {
        String val = assignmentStmt.getLValue().getName();
        Expression iden = assignmentStmt.getLValue();
        Expression rval = assignmentStmt.getRValue();
        if(rval != null) {
            addCommand(rval.accept(this));
        }

        String index = this.putInHash(val).toString();
        Type idt = iden.getType();
        if(idt instanceof BoolType || idt instanceof IntType || idt instanceof StringType){
            addCommand("istore      " + index);
        }
        else{
            addCommand("astore      " + index);
        }
        return null;
    }

//    @Override
//    public String visit(BlockStmt blockStmt) {
//        //todo
//        return null;
//    }

//    @Override
//    public String visit(ConditionalStmt conditionalStmt) {
//        //todo
//        return null;
//    }

    @Override
    public String visit(FunctionCall functionCall) {
        
        ArrayList<Expression> args = functionCall.getArgs();
        for (Expression arg : args){
            addCommand(arg.accept(this));
        }
        
        addCommand("invokestatic/ ... ");
        return null;
    }

//    @Override
//    public String visit(PrintStmt print) {
//        //todo
//        return null;
//    }

    @Override
    public String visit(ReturnStmt returnStmt) {
        Type type = returnStmt.getReturnedExpr().accept(expressionTypeChecker);
        if(type instanceof NullType) {
            addCommand("return");
        }
        else {
            //todo add commands to return
        }
        return null;
    }

    @Override
    public String visit(main.ast.node.expression.values.NullValue nullValue) {
        String commands = "";
        //todo
        return commands;
    }

    @Override
    public String visit(IntValue intValue) {
        String commands = "";
        String iv = String.valueOf(intValue.getConstant());
        commands = "ldc      " + iv;

        return commands;
    }

    @Override
    public String visit(BoolValue boolValue) {
        String commands = "";
        String bv =  String.valueOf(boolValue.getConstant());
        if(bv.equals("False") || bv.equals("false")){
            bv = "0";
        }else{
            bv = "1";
        }
        commands = "ldc     " + bv;
        return commands;
    }

    @Override
    public String visit(StringValue stringValue) {
        String vall = stringValue.getConstant();

        String commands = "ldc      \"" +vall + "\"";
        return commands;
    }

}