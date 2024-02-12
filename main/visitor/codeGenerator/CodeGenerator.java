package main.visitor.codeGenerator;

import main.ast.node.Program;
import java.util.HashMap;
import main.ast.node.declaration.*;
import main.ast.node.expression.*;
import main.ast.node.expression.operators.BinaryOperator;
import main.ast.node.expression.values.*;
import main.ast.node.statement.*;
import main.ast.type.Type;
import main.ast.type.primitiveType.*;
import main.visitor.Visitor;
import main.visitor.typeAnalyzer.TypeChecker;
import main.bytecode.*;
import java.util.ArrayList;


import java.io.*;


public class CodeGenerator extends Visitor<String> {
    //    You may use following items or add your own for handling typechecker
    TypeChecker expressionTypeChecker;
    //    Graph<String> classHierarchy;
    private HashMap<String,Integer> slots;
    private String outputPath;
    private FileWriter currentFile;
    private FunctionDeclaration currentMethod;

    private HashMap<String , JasminMethod> env;

    public CodeGenerator() {
//        this.classHierarchy = classHierarchy;

//        Uncomment below line to initialize your typechecker
        this.expressionTypeChecker = new TypeChecker(new ArrayList());


        this.slots = new HashMap<>();
        putInHash("UTL");
        this.env = new HashMap<>();
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
        if(command == null){
            return;
        }
        try {
            command = String.join("\n\t\t", command.split("\n"));
            if(command.startsWith("Label_"))
                this.currentFile.write("\t" + command + "\n");
            else if(command.startsWith(".") || command.startsWith("\t"))
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

    private void initial(){
        addCommand(".method public <init>()V");
        addCommand("aload_0");
        addCommand("invokespecial java/lang/Object/<init>()V");
        addCommand(".end method");
    }

    @Override
    public String visit(Program program) {
        var t = program.accept(this.expressionTypeChecker);
        createFile("out.txt");
        addCommand(".class public UTL\n");
        addCommand(".super java/lang/Object\n\n");
        for (var dec : program.getVars()){
            String type = "";

            if(dec.getType() instanceof IntType)
                type = "Integer";
            else if(dec.getType() instanceof FloatType)
                type = "Float";
            else if(dec.getType() instanceof StringType)
                type = "String";
            else
                type = dec.getType().toString();
            addCommand(".field public " + dec.getIdentifier().getName() + " Ljava/lang/" + type);
            String index = this.putInHash(dec.getIdentifier().getName()).toString();
            var exp = dec.getRValue();
            if(exp != null){
                addCommand(exp.accept(this));
                addCommand("putfield  UTL/" + dec.getIdentifier().getName());
            }
            //addCommand(dec.accept(this));
        }
        initial();
        for (var dec : program.getFunctions()){
            addCommand(dec.accept(this));
        }
        for (var dec : program.getStarts()){
            addCommand(dec.accept(this));
        }
        addCommand(program.getMain().accept(this));
        return null;
    }

    @Override
    public String visit(MainDeclaration mainDeclaration) {
        addCommand("\n");
        addCommand(".method public Main(LTrade;)V\n"
                + ".limit stack 128\n"
                + ".limit locals 128\n\n");

        for(Statement stmt : mainDeclaration.getBody()){
            if(stmt.accept(this) == null)
                continue;
            addCommand(stmt.accept(this));
            addCommand("\n");
        }
        addCommand("return\n");
        return ".end method\n";
    }

//    @Override
//    public String visit(OnInitDeclaration onInitDeclaration){
//        addCommand("\n");
//        addCommand(".method public OnInit(LTrade;)V\n"
//                + ".limit stack 128\n"
//                + ".limit locals 128\n\n");
//
//        for(Statement stmt : onInitDeclaration.getBody()){
//            if(stmt.accept(this) == null)
//                continue;
//            addCommand(stmt.accept(this));
//            addCommand("\n");
//        }
//        addCommand("return\n");
//        return ".end method\n";
//    }

    @Override
    public String visit(OnStartDeclaration onStartDeclaration) {
        addCommand("\n");
        addCommand(".method public OnStart(LTrade;)V\n"
         + ".limit stack 128\n"
        + ".limit locals 128\n\n");

        for(Statement stmt : onStartDeclaration.getBody()){
            if(stmt.accept(this) == null)
                continue;
            addCommand(stmt.accept(this));
            addCommand("\n");
        }
        addCommand("return\n");
        return ".end method\n";
    }

    @Override
    public String visit(FunctionDeclaration functionDeclaration) {
        var res = new JasminMethod(functionDeclaration.getName().getName(), functionDeclaration.getReturnType(),
                functionDeclaration.getArgs().stream().map(VarDeclaration::getType).toList(),
                functionDeclaration.getBody().stream().map(s->s.accept(this)).toList());
        env.put(functionDeclaration.getName().getName(), res);
        return res.toString();
    }

    @Override
    public String visit(VarDeclaration varDeclaration) {
        var res = new StringBuilder();
        String var_name = varDeclaration.getIdentifier().getName();
        Type vartype = varDeclaration.getType();
        Expression assignVal = varDeclaration.getRValue();
        Integer slot_ind = this.putInHash(var_name);
        if(assignVal != null){
            res.append(assignVal.accept(this));// must first load val then
            //addCommand();// store it to slot_ind
            if(vartype instanceof BoolType || vartype instanceof IntType || vartype instanceof StringType){
                res.append("istore      " + slot_ind + "\n");
            }
            else if(vartype instanceof FloatType){
                res.append("fstore      " + slot_ind + "\n");
            }
            else{
                res.append("astore      " + slot_ind + "\n");
            }
        }

        return res.toString();
    }


    @Override
    public String visit(VarAccess varAccess){
        var res = new StringBuilder();
        var c = varAccess.getInstance();
        var v = varAccess.getVariable();

        var nam =  c.getName();
        var field = v.getName();
        var ind = putInHash(nam);
        res.append("aload   " + ind.toString() + "\n");
        res.append("getfield    " + field + "\n");

        return res.toString();
    }

    @Override
    public String visit(AssignStmt assignmentStmt) {
        StringBuilder res = new StringBuilder();
        String val = assignmentStmt.getLValue().getName();
        Expression iden = assignmentStmt.getLValue();
        Expression rval = assignmentStmt.getRValue();
        if(rval != null) {
            res.append(rval.accept(this));
        }

        String index = this.putInHash(val).toString();
        Type idt = iden.getType();
        if(idt instanceof BoolType || idt instanceof IntType || idt instanceof StringType){
            res.append("istore      " + index + "\n");
        }
        else if(idt instanceof FloatType){
            res.append("fstore      " + index + "\n");
        }
        else{
            res.append("astore      " + index + "\n");
        }
        return res.toString();
    }

    @Override
    public String visit(ExpressionStmt expressionStmt){
        Expression exp = expressionStmt.getExpression();
        if(     exp instanceof FunctionCall ||
                exp instanceof UnaryExpression )
            return expressionStmt.getExpression().accept(this);
        return null;
    }

    @Override
    public String visit(FunctionCall functionCall) {

        Identifier functionName = functionCall.getFunctionName();
        Type t = functionCall.getType();
        var res = new StringBuilder();
        if(functionName.getName().equals("print") || functionName.getName().equals("Print")){
            String command = "";
            GetStatic staticObj = new GetStatic("java/lang/System", "out", "Ljava/io/PrintStream;");
            
            InvokeVirtual invVirObj = new InvokeVirtual("java/io/PrintStream", "println", makeTypeSignature(t));
            
            command += staticObj.toString();
            command+="\n";
            for(Expression arg : functionCall.getArgs()){
                command += arg.accept(this);
                command += "\n";
            }
            command += invVirObj.toString();
            command+="\n";
            return command;
        }else if(functionName.getName().equals("Order")){

            res.append("\t\tnew Order\ndup\n");
            for (var args : functionCall.getArgs()){
                res.append(args.accept(this));
            }
            res.append(new Invoke("Order/<init>", "(Ljava/lang/String;III)V\n", "special"));
            return res.toString();
        }
        else if(functionName.getName().equals( "GetCandle")) {
            for (var args : functionCall.getArgs()){
                res.append(args.accept(this));
            }
            res.append("\ninvokestatic Prog/getCandle(Ljava/lang/Integer;)LCandle\n");
        }
        else if(functionName.getName().equals( "Observe")) {
            for (var args : functionCall.getArgs()){
                res.append(args.accept(this));
            }
            res.append("\ninvokestatic Prog/Observe(Ljava/lang/Integer;)Ltrade\n");
        }
        else if(functionName.getName().equals( "Terminate")) {
            for (var args : functionCall.getArgs()){
                res.append(args.accept(this));
            }
            res.append("\ninvokestatic Prog/Terminate()V\n");
        }
        else if(functionName.getName().equals( "Connect")){
            for (var args : functionCall.getArgs()){
                res.append(args.accept(this));
            }

            res.append("\ninvokestatic Prog/Connect(Ljava/lang/String;Ljava/lang/String;)V\n");
        }
        else{


            ArrayList<Expression> args = functionCall.getArgs();
            for (Expression arg : args){
                res.append(arg.accept(this));
                res.append("\n");
            }


            var fc = new InvokeStatic( "Main" , functionName.getName()
                    , env.get(functionName.getName()).getSignature());
            res.append(fc.toString());
            
            return res.toString();
        }
        return res.toString();
    }

    

    @Override
    public String visit(ReturnStmt returnStmt) {
        Type type = returnStmt.getReturnedExpr().accept(expressionTypeChecker);

        String command = "";
        if(type instanceof NullType) {
            Return returnObj = new Return();
            command += returnObj.toString();
        }
        else {
            command += returnStmt.getReturnedExpr().accept(this);
            command += "\n";
            IReturn ireturnObj = new IReturn();
            command += ireturnObj.toString();
        }
        return command;
    }

    @Override
    public String visit(WhileStmt whileStmt){
        var res = new StringBuilder();
        Expression condition = whileStmt.getCondition();
        res.append("Label_start : \n");
        res.append(condition.accept(this));

        res.append("Label_if : \n");
        for(Statement stmt : whileStmt.getBody()){
            res.append(stmt.accept(this));
        }
        res.append("goto\t\t" + "Label_start\n");
        res.append("Label_else : \n");
        return res.toString();
    }

    @Override
    public String visit(IfElseStmt ifElseStmt) {
        var res = new StringBuilder();
        Expression condition = ifElseStmt.getCondition();
        res.append(condition.accept(this));

        res.append("Label_if : \n");
        for(Statement stmt : ifElseStmt.getThenBody()){
            res.append(stmt.accept(this));
        }
        res.append("goto\t\t" + "Label_exit\n");
        res.append("Label_else : \n");
        for(Statement stmt : ifElseStmt.getElseBody()){
            res.append(stmt.accept(this));
        }
        res.append("Label_exit : \n");
        return res.toString();
    }

public String visit(BinaryExpression binaryExpression) {
        Expression lOperand = binaryExpression.getLeft();
        Expression rOperand = binaryExpression.getRight();
        String command = "";

        if(binaryExpression.getBinaryOperator() == BinaryOperator.AND){
            command += lOperand.accept(this);
            command += "ifeq        Label_else\n";
            command += rOperand.accept(this);
            command += "ifeq        Label_else\n";
        }
        else if(binaryExpression.getBinaryOperator() == BinaryOperator.OR){
            command += lOperand.accept(this);
            command += "ifge        Label_if\n";
            command += rOperand.accept(this);
            command += "ifeq        Label_else\n";
        }
        else if(binaryExpression.getBinaryOperator() == BinaryOperator.LT){
            command += lOperand.accept(this);
            command += rOperand.accept(this);
            command += "if_icmple        Label_if\n";
            command += "goto        Label_else\n";
        }
        else if(binaryExpression.getBinaryOperator() == BinaryOperator.GT){
            command += lOperand.accept(this);
            command += rOperand.accept(this);
            command += "if_icmpge        Label_if\n";
            command += "goto        Label_else\n";
        }
        else if(binaryExpression.getBinaryOperator() == BinaryOperator.EQ){
            command += lOperand.accept(this);
            command += rOperand.accept(this);
            command += "if_icmpeq        Label_if\n";
            command += "goto        Label_else\n";
        }
        else{
            command += lOperand.accept(this);
            command += rOperand.accept(this);
            switch (binaryExpression.getBinaryOperator()) {
                case PLUS -> {
                    IAdd obj = new IAdd();
                    command += obj.toString();
                }
                case MINUS-> {
                    INeg obj = new INeg();
                    command += obj.toString();
                }
                case MULT -> {
                    IMul obj = new IMul();
                    command += obj.toString();
                }
                case DIV -> {
                    IDiv obj = new IDiv();
                    command += obj.toString();
                }
                case MOD -> {
                    IRem obj = new IRem();
                    command += obj.toString();
                }
                default -> {
                }
            }
        }
        command += "\n";
        return command;
    }

    @Override
    public String visit(UnaryExpression unaryExpression) {
        Expression operand = unaryExpression.getOperand();
        String command = "";
        int index = putInHash(operand.getName());
        switch (unaryExpression.getUnaryOperator()) {
            case INC -> {
                command += operand.accept(this);
                String s = "i" + "inc\t" + index + ", " + "1\n";
                command += s;
            }

            case DEC -> {
                command += operand.accept(this);
                command += "i" + "inc\t" + index + ", " + "-1\n";
            }

            case MINUS -> {
                command += operand.accept(this);
                command += "\nineg";
            }

            case NOT -> {
                command += operand.accept(this);
                IConst iconstObject = new IConst(1);
                command += iconstObject.toString();
                command += "\n";
                IXor xorObject = new IXor();
                command += xorObject.toString();
                command += "\n";
            }

            case BIT_NOT -> {
                command += operand.accept(this);
                IConst iconstObject = new IConst(-1);
                command += iconstObject.toString();
                command += "\n";
                IXor xorObject = new IXor();
                command += xorObject.toString();
                command += "\n";
            }

        }
        return command;
    }

    @Override
    public String visit(IntValue intValue) {
        String commands = "";
        String iv = String.valueOf(intValue.getConstant());
        commands = "ldc      " + iv + "\n";

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
        commands = "ldc     " + bv + "\n";
        return commands;
    }

    @Override
    public String visit(StringValue stringValue) {
        String vall = stringValue.getConstant();

        String commands = "ldc      " +vall + "\n";
        return commands;
    }

    @Override
    public String visit(TradeValue tradeValue) {
        String vall = tradeValue.getConstant();

        String commands = "ldc      " +vall + "\n";
        return commands;
    }

    @Override
    public String visit(NullValue nullValue) {
        AConst_null acnObj = new AConst_null();
        return acnObj.toString();
    }

    @Override
    public String visit(Identifier identifier) {
        Type type = identifier.getType();
        int index = (putInHash(identifier.getName()));
        if(type instanceof FloatType){
            return "\t" + (index < 4 ? "fload_" + index : "fload " + index )+ "\n";
        }
        else{
            ILoad iloadObject = (new ILoad(index));
            return iloadObject.toString();
        }

    }

}