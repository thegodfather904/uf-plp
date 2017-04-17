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
	int currentAvailableSlot = 1;
	Label blockStartLabel;
	Label blockEndLabel;
	
	int argArrayIndex = 0;
	boolean isLeftChain = false;
	boolean isBinaryChainVisit = false;
	
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
		
		//visiting a binary chain, pop end of statement if needed
		isBinaryChainVisit = true;
		
		//visit left chain
		isLeftChain = true;
		binaryChain.getE0().visit(this, null);
		
		//visit right chain
		isLeftChain = false;
		binaryChain.getE1().visit(this, null);

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
		
		//visit decs
		for(Dec dec : block.getDecs())
			dec.visit(this, null);
		
		//visit statements
		for(Statement statement : block.getStatements()){
			statement.visit(this, null);
			
			//if binary chain, pop what may be left on the stack
			if(isBinaryChainVisit){
				mv.visitInsn(POP);
				isBinaryChainVisit = false;
			}
		}
			
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
		//TODO
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
		
		//visit tuple
		filterOpChain.getArg().visit(this, null);
		
		if(filterOpChain.getFirstToken().isKind(OP_BLUR)){
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "blurOp", 
					"(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
		}
		else if(filterOpChain.getFirstToken().isKind(OP_GRAY)){
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "grayOp", 
					"(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
		}
		else if(filterOpChain.getFirstToken().isKind(OP_CONVOLVE)){
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFilterOps", "convolveOp", 
					"(Ljava/awt/image/BufferedImage;Ljava/awt/image/BufferedImage;)Ljava/awt/image/BufferedImage;", false);
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		
		//visit tuple
		frameOpChain.getArg().visit(this, null);
		
		if(frameOpChain.getFirstToken().isKind(KW_SHOW))
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "showImage", "()Lcop5556sp17/PLPRuntimeFrame;", false);
		else if(frameOpChain.getFirstToken().isKind(KW_HIDE))
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "hideImage", "()Lcop5556sp17/PLPRuntimeFrame;", false);
		else if(frameOpChain.getFirstToken().isKind(KW_MOVE))
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "moveFrame", "(II)Lcop5556sp17/PLPRuntimeFrame;", false);
		else if(frameOpChain.getFirstToken().isKind(KW_XLOC))
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "getXVal", "()I", false);
		else if(frameOpChain.getFirstToken().isKind(KW_YLOC))
			mv.visitMethodInsn(INVOKEVIRTUAL, "cop5556sp17/PLPRuntimeFrame", "getYVal", "()I", false);
		
		
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		
		if(isLeftChain){
			if(identChain.getTypeName().isType(TypeName.URL)){
				mv.visitFieldInsn(GETSTATIC, className, identChain.getFirstToken().getText(), "Ljava/net/URL;");
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromURL", 
						"(Ljava/net/URL;)Ljava/awt/image/BufferedImage;", false);
			}else if (identChain.getTypeName().isType(TypeName.FILE)){
				mv.visitFieldInsn(GETSTATIC, className, identChain.getFirstToken().getText(), "Ljava/io/File;");
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromFile", 
						"(Ljava/io/File;)Ljava/awt/image/BufferedImage;", false);
			}else{
				if(identChain.getDec().getType().isKind(KW_INTEGER) || identChain.getDec().getType().isKind(KW_BOOLEAN))
					mv.visitVarInsn(ILOAD, identChain.getDec().getSlotNumber());
				else{
					mv.visitVarInsn(ALOAD, identChain.getDec().getSlotNumber());
					throw new Exception(); //TODO can it be another type?
				}
			}
		}else{
			if(identChain.getDec().getType().isKind(KW_INTEGER)){
				mv.visitInsn(DUP);
				mv.visitVarInsn(ISTORE, identChain.getDec().getSlotNumber());
			}else if(identChain.getDec().getType().isKind(KW_IMAGE)){
				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE, identChain.getDec().getSlotNumber());
			}else if (identChain.getDec().getType().isKind(KW_FILE)){
				//TODO
			}else if (identChain.getDec().getType().isKind(KW_FRAME)){
				//image is already on top of stack, load null as second param
				mv.visitInsn(ACONST_NULL);
				
				//call createOrSetFrame()
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "createOrSetFrame", 
						"(Ljava/awt/image/BufferedImage;Lcop5556sp17/PLPRuntimeFrame;)Lcop5556sp17/PLPRuntimeFrame;", false);
				
				mv.visitInsn(DUP);
				mv.visitVarInsn(ASTORE, identChain.getDec().getSlotNumber());
				
			}else
				throw new Exception(); //TODO can it be another type?
		}
		
		
		
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		//push value of ident onto stack
		if(identExpression.getDec().getSlotNumber() == -1)
			mv.visitFieldInsn(GETSTATIC, className, identExpression.getDec().getIdent().getText(), 
					identExpression.getDec().getTypeName().getJVMTypeDesc());
		else
			mv.visitVarInsn(ILOAD, identExpression.getDec().getSlotNumber());
		
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//store value on top of stack into this variable
		//if sn == -1, its a field of the class, if any other sn its a local var
		if(identX.getDec().getSlotNumber() == -1){
			mv.visitFieldInsn(PUTSTATIC, className, identX.getText(), identX.getDec().getTypeName().getJVMTypeDesc());
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
		//TODO
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
		FieldVisitor fv = cw.visitField(ACC_STATIC, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc(), null, null);
		fv.visitEnd();

		if(paramDec.getTypeName().isType(TypeName.INTEGER))
			intParamDec(paramDec);
		else if (paramDec.getTypeName().isType(TypeName.BOOLEAN))
			boolParamDec(paramDec);
		else if (paramDec.getTypeName().isType(TypeName.FILE))
			fileParamDec(paramDec);
		else if (paramDec.getTypeName().isType(TypeName.URL))
			urlParamDec(paramDec);
		
		mv.visitFieldInsn(PUTSTATIC, className, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc());
		
		return null;

	}
	
	private void intParamDec(ParamDec paramDec){
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(argArrayIndex++);
		mv.visitInsn(AALOAD);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
	}
	
	private void boolParamDec(ParamDec paramDec){
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(argArrayIndex++);
		mv.visitInsn(AALOAD);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);;
	}
	
	private void fileParamDec(ParamDec paramDec){
		mv.visitVarInsn(ALOAD, 0);
		mv.visitTypeInsn(NEW, "java/io/File");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(argArrayIndex++);
		mv.visitInsn(AALOAD);
		mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
	}
	
	private void urlParamDec(ParamDec paramDec){
		//instaniate new PLPRuntimeImageIO object and save in slot 2
		mv.visitTypeInsn(NEW, "cop5556sp17/PLPRuntimeImageIO");
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESPECIAL, "cop5556sp17/PLPRuntimeImageIO", "<init>", "()V", false);
		mv.visitVarInsn(ASTORE, 2);
		
		//load arg array then current index
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(argArrayIndex++);
		
		//run the method to get the url
		mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "getURL", "([Ljava/lang/String;I)Ljava/net/URL;", false);
	}
	
	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		
		//visit expression
		sleepStatement.getE().visit(this, null);
		
		//convert to long
		mv.visitInsn(I2L);
		
		//call thread.sleep
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		
		//visit the expressions
		for(Expression e : tuple.getExprList())
			e.visit(this, null);
		
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		
		Label guard = new Label();
		Label block = new Label();
		
		blockStartLabel = block;
		whileStatement.getB().visit(this, null);
		
		mv.visitLabel(guard);
		whileStatement.getE().visit(this, null);
		
		mv.visitJumpInsn(IFNE, block);
		
		Label endWhile = new Label();
		mv.visitLabel(endWhile);
		
		return null;
	}

}
