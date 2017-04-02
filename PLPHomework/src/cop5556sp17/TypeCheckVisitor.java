package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
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
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.ArrayList;

import com.sun.xml.internal.ws.policy.sourcemodel.ModelNode.Type;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		
		//enter scope
		symtab.enterScope();
		
		//visit list<dec>
		for(Dec dec : block.getDecs())
			visitDec(dec, null);
		
		//visit list<statement>
		for(Statement statement : block.getStatements())
			statement.visit(this, null);
			
		//leave scope
		symtab.leaveScope();
		
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		booleanLitExpression.setTypeName(TypeName.BOOLEAN);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		intLitExpression.setTypeName(TypeName.INTEGER);
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		if(!symtab.insert(declaration.getIdent().getText(), declaration))
			throw new Exception("Duplicate param dec found in program");
		
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		//visit each ParamDec (throws error if duplicate dec found)
		for(ParamDec pd : program.getParams())
			visitParamDec(pd, null);
		
		//visit the block
		program.getB().visit(this, null);
		
		//leave the scope
		symtab.leaveScope();
		
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		
		//visit identLvalue
		visitIdentLValue(assignStatement.getVar(), null);
		
		//visit expression
		assignStatement.getE().visit(this, null);
		
		TypeName identLType = symtab.lookup(assignStatement.getVar().getText()).getTypeName();
		TypeName expressionType = assignStatement.getE().getTypeName();
		
		if(identLType != expressionType)
			throw new TypeCheckException("Wrong type in assignment: " + identLType + " -> " + expressionType);
		
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		
		//condition: ident has been declared and is visible in current scope
		Dec dec = symtab.lookup(identX.getText());
		if(dec == null)
			throw new Exception("Ident was never declared");
		
		if(!symtab.isIdentVisible(identX.getText()))
			throw new Exception("Ident was declared but is not visisble in the current block");
		
		//set ident dec to dec of ident
		identX.setDec(dec);
		
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		if(!symtab.insert(paramDec.getIdent().getText(), paramDec))
			throw new Exception("Duplicate param dec found in program");
		
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


}