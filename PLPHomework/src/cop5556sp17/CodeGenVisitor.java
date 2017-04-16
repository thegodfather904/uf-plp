package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	
	//ADDED BY ME
	int currentAvailableSlot = 0;
	Label blockStartLabel;
	Label blockEndLabel;
	
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params)
			dec.visit(this, mv);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
//TODO  visit the local variables
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method
		
		
		cw.visitEnd();//end of class
		
		//generate classfile and return it
		return cw.toByteArray();
	}
	
	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		
		//visit expression, prob need to evaluate and then leave on top of stack
		assignStatement.getE().visit(this, arg);
		
		//print whats on top of stack for grading
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeName());
		
		//visit var, prob need to consume whats on the top of the stack
		assignStatement.getVar().visit(this, arg);
		
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
      
		//visit e0;
		binaryExpression.getE0().visit(this, null);
		
		//visit e1;
		binaryExpression.getE1().visit(this, null);
		
		//do operation and leave on stack
		if(binaryExpression.getOp().isKind(PLUS))
			mv.visitInsn(IADD);
		else if(binaryExpression.getOp().isKind(MINUS))
			mv.visitInsn(ISUB);
		else if(binaryExpression.getOp().isKind(OR))
			mv.visitInsn(IOR);
		else if (binaryExpression.getOp().isKind(TIMES))
			mv.visitInsn(IMUL);
		else if (binaryExpression.getOp().isKind(DIV))
			mv.visitInsn(IDIV);
		else if(binaryExpression.getOp().isKind(AND))
			mv.visitInsn(IAND);
		else if(binaryExpression.getOp().isKind(MOD))
			mv.visitInsn(IREM);
		else{
			
			Label trueLabel = new Label();
			Label falseLabel = new Label();
			Label doneLabel = new Label();
			
			if(binaryExpression.getOp().isKind(LT))
				mv.visitJumpInsn(IF_ICMPGE, falseLabel);
			else if(binaryExpression.getOp().isKind(LE))
				mv.visitJumpInsn(IF_ICMPGT, falseLabel);
			else if (binaryExpression.getOp().isKind(GT))
				mv.visitJumpInsn(IF_ICMPLE, falseLabel);
			else if (binaryExpression.getOp().isKind(GE))
				mv.visitJumpInsn(IF_ICMPLT, falseLabel);
			else if(binaryExpression.getOp().isKind(EQUAL))
				mv.visitJumpInsn(IF_ICMPNE, falseLabel);
			else if(binaryExpression.getOp().isKind(NOTEQUAL))
				mv.visitJumpInsn(IF_ICMPEQ, falseLabel);
			
			//if true
			mv.visitLabel(trueLabel);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, doneLabel);
			
			//if false
			mv.visitLabel(falseLabel);
			mv.visitInsn(ICONST_0);
			
			//done label
			mv.visitLabel(doneLabel);
			
		}
		
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		
		if(blockStartLabel == null)
			blockStartLabel = new Label();
		
		//visit start label
		mv.visitLabel(blockStartLabel);
		
		currentAvailableSlot = 1;	//set to 1 to account for 'this'
		
		//visit decs
		for(Dec dec : block.getDecs())
			dec.visit(this, null);
		
		//visit statements
		for(Statement statement : block.getStatements())
			statement.visit(this, null);
		
		//visit end label
		blockEndLabel = new Label();
		mv.visitLabel(blockEndLabel);
		
		//set blockStart and End labels to null
		blockStartLabel = null;
		blockEndLabel = null;
		
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		if(booleanLitExpression.getValue())
			mv.visitInsn(ICONST_1);
		else
			mv.visitInsn(ICONST_0);
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//Assign a slot in the local variable array to this variable and save it in the new slot attribute in the  Dec class
		declaration.setSlotNumber(currentAvailableSlot++);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		
		//push value of ident onto stack
		if(identExpression.getDec().getSlotNumber() == -1){
			//FIX THIS STUPID ASS SHIT
			mv.visitFieldInsn(GETFIELD, className, identExpression.getDec().getIdent().getText(), "I"); //TODO check for bool or int
		}
		else
			mv.visitVarInsn(ILOAD, identExpression.getDec().getSlotNumber());
		
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//store value on top of stack into this variable
		//if sn == -1, its a field of the class, if any other sn its a local var
		if(identX.getDec().getSlotNumber() == -1){
			//TODO FIX THIS FUCKING STUPID ASS SHIT
		}else
			mv.visitVarInsn(ISTORE, identX.getDec().getSlotNumber());
		
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		
		
		Label skipBlock = new Label();
		Label doBlock = new Label();
		
		//visit expression
		ifStatement.getE().visit(this, null);
		
		//do the if statement
		mv.visitJumpInsn(IFEQ, skipBlock);
		
		//visit the block
		//set blockStart to doBlock
		blockStartLabel = doBlock;
		ifStatement.getB().visit(this, null);
		
		//visit the skipBlock label
		mv.visitLabel(skipBlock);
		
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//load constant onto stack
		mv.visitLdcInsn(intLitExpression.value);
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		if(paramDec.getTypeName().isType(TypeName.INTEGER)){
			
			//TODO FIX THIS FUCKING SHIT
			
			FieldVisitor fv = cw.visitField(0, paramDec.getIdent().getText(), "I", null, null);
			fv.visitEnd();
			
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitInsn(ICONST_0);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			
//			CodeGenUtils.genPrintTOS(GRADE, mv, paramDec.getTypeName());
			
//			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
			
		}else if (paramDec.getTypeName().isType(TypeName.BOOLEAN)){
			//TODO Implement this
		}

		//For assignment 5, only needs to handle integers and booleans
		return null;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//TODO Implement this
		return null;
	}

}
