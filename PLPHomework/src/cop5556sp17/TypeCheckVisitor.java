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
		
		//visit chain
		binaryChain.getE0().visit(this, null);
		
		//visit chain elem
		binaryChain.getE1().visit(this, null);
		
		//typecheck
		TypeName e0 = binaryChain.getE0().getTypeName();
		TypeName e1 = binaryChain.getE1().getTypeName();
		
		Token op = binaryChain.getArrow();
		
		if(e0.isType(TypeName.URL) && op.isKind(ARROW) && e1.isType(TypeName.IMAGE))
			binaryChain.setTypeName(TypeName.IMAGE);
		else if (e0.isType(TypeName.FILE) && op.isKind(ARROW) && e1.isType(TypeName.IMAGE))
			binaryChain.setTypeName(TypeName.IMAGE);
		else if (e0.isType(TypeName.FRAME) && op.isKind(ARROW) && (binaryChain.getE1() instanceof FrameOpChain 
				&& (binaryChain.getE1().getFirstToken().isKind(KW_XLOC) || binaryChain.getE1().getFirstToken().isKind(KW_YLOC))))
			binaryChain.setTypeName(TypeName.INTEGER);
		else if (e0.isType(TypeName.FRAME) && op.isKind(ARROW) && (binaryChain.getE1() instanceof FrameOpChain 
				&& (binaryChain.getE1().getFirstToken().isKind(KW_SHOW) || binaryChain.getE1().getFirstToken().isKind(KW_HIDE)
						|| binaryChain.getE1().getFirstToken().isKind(KW_MOVE))))
			binaryChain.setTypeName(TypeName.FRAME);
		else if (e0.isType(TypeName.IMAGE) && op.isKind(ARROW) && (binaryChain.getE1() instanceof ImageOpChain && 
				(binaryChain.getE1().getFirstToken().isKind(OP_WIDTH) || binaryChain.getE1().getFirstToken().isKind(OP_HEIGHT))))
			binaryChain.setTypeName(TypeName.INTEGER);
		else if (e0.isType(TypeName.IMAGE) && op.isKind(ARROW) && e1.isType(TypeName.FRAME))
			binaryChain.setTypeName(TypeName.FRAME);
		else if (e0.isType(TypeName.IMAGE) && op.isKind(ARROW) && e1.isType(TypeName.FILE))
			binaryChain.setTypeName(TypeName.NONE);
		else if (e0.isType(TypeName.IMAGE) && (op.isKind(ARROW) || op.isKind(BARARROW)) 
				&& (binaryChain.getE1() instanceof FilterOpChain && (binaryChain.getE1().getFirstToken().isKind(OP_GRAY)
						|| binaryChain.getE1().getFirstToken().isKind(OP_BLUR) || binaryChain.getE1().getFirstToken().isKind(OP_CONVOLVE))))
			binaryChain.setTypeName(TypeName.IMAGE);
		else if (e0.isType(TypeName.IMAGE) && op.isKind(ARROW) && (binaryChain.getE1() instanceof ImageOpChain && 
				(binaryChain.getE1().getFirstToken().isKind(KW_SCALE))))
			binaryChain.setTypeName(TypeName.IMAGE);
		else if (e0.isType(TypeName.IMAGE) && op.isKind(ARROW) && binaryChain.getE1() instanceof IdentChain)
			binaryChain.setTypeName(TypeName.IMAGE);
		
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		
		//visit both the binary expressions
		binaryExpression.getE0().visit(this, null);
		binaryExpression.getE1().visit(this, null);
		
		//get the types for the expressions
		TypeName e0Type = binaryExpression.getE0().getTypeName();
		TypeName e1Type = binaryExpression.getE1().getTypeName();
		
		Token op = binaryExpression.getOp();
		
		
		if(op.isKind(EQUAL) || op.isKind(NOTEQUAL))
			if(e0Type.equals(e1Type))
				binaryExpression.setTypeName(TypeName.BOOLEAN);
			else
				throw new TypeCheckException("Types are not equal in binary expression");
		else{
			if(e0Type.isType(TypeName.INTEGER) && e1Type.isType(TypeName.INTEGER)){
				switch(op.kind){
					case PLUS:
					case MINUS:
					case TIMES:
					case DIV:
						binaryExpression.setTypeName(TypeName.INTEGER);
						break;
					case LT:
					case GT:
					case LE:
					case GE:
						binaryExpression.setTypeName(TypeName.BOOLEAN);
						break;
					
				}
			}else if (e0Type.isType(TypeName.IMAGE) && e1Type.isType(TypeName.IMAGE)){
				switch(op.kind){
				case PLUS:
				case MINUS:
					binaryExpression.setTypeName(TypeName.IMAGE);
					break;
				}
			}else if ((e0Type.isType(TypeName.IMAGE) && e1Type.isType(TypeName.INTEGER)) ||
					(e0Type.isType(TypeName.INTEGER) && e1Type.isType(TypeName.IMAGE))){
				switch(op.kind){
				case TIMES:
					binaryExpression.setTypeName(TypeName.IMAGE);
					break;
				}
			}else if (e0Type.isType(TypeName.BOOLEAN) && e1Type.isType(TypeName.BOOLEAN)){
				switch(op.kind){
					case LT:
					case GT:
					case LE:
					case GE:
					binaryExpression.setTypeName(TypeName.BOOLEAN);
					break;
				}
			}
		}
			
			
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
		
		//visit the tuple
		filterOpChain.getArg().visit(this, null);
		
		if(filterOpChain.getArg().getExprList().size() == 0)
			filterOpChain.setTypeName(TypeName.INTEGER);
		
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		
		//visit the tuples
		frameOpChain.getArg().visit(this, null);
		
		Token frameOpToken = frameOpChain.getFirstToken();
		
		//type checking
		if((frameOpToken.isKind(KW_SHOW) || frameOpToken.isKind(KW_HIDE)) 
				&& frameOpChain.getArg().getExprList().size() == 0)
			frameOpChain.setTypeName(TypeName.NONE);
		else if ((frameOpToken.isKind(KW_XLOC) || frameOpToken.isKind(KW_YLOC)) 
				&& frameOpChain.getArg().getExprList().size() == 0)
			frameOpChain.setTypeName(TypeName.INTEGER);
		else if ((frameOpToken.isKind(KW_MOVE)) 
				&& frameOpChain.getArg().getExprList().size() == 2)
			frameOpChain.setTypeName(TypeName.NONE);
			
		
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		
		String ident = identChain.getFirstToken().getText();
		
		Dec dec = symtab.lookup(ident);
		if(dec == null)
			throw new TypeCheckException("Ident was never declared");
		
		if(!symtab.isIdentVisible(ident))
			throw new TypeCheckException("Ident was declared but is not visisble in the current block");
		
		//set ident chain type to ident type
		identChain.setTypeName(dec.getTypeName());
		
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		
		//ident declared and in current scope
		String ident = identExpression.getFirstToken().getText();
		
		Dec dec = symtab.lookup(ident);
		if(dec == null)
			throw new TypeCheckException("Ident was never declared");
		
		if(!symtab.isIdentVisible(ident))
			throw new TypeCheckException("Ident was declared but is not visisble in the current block");
		
		//Set the ident expression to the type of the ident
		identExpression.setTypeName(dec.getTypeName());
		
		//Set the ident expression dec to the dec of the ident
		identExpression.setDec(dec);
		
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		
		//visit expression and verify type is boolean
		ifStatement.getE().visit(this, null);
		
		if(!ifStatement.getE().getTypeName().isType(TypeName.BOOLEAN))
			throw new TypeCheckException("If statement: expression type is : " + ifStatement.getE().getTypeName());
		
		//visit block
		ifStatement.getB().visit(this, null);
		
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		intLitExpression.setTypeName(TypeName.INTEGER);
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		
		//visit the expression
		sleepStatement.getE().visit(this, null);

		//if expression type not integer, throw exception
		if(!sleepStatement.getE().getTypeName().isType(TypeName.INTEGER))
			throw new TypeCheckException("Sleep statement: expression type is : " + sleepStatement.getE().getTypeName());
		
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		
		//visit expression and verify type is boolean
		whileStatement.getE().visit(this, null);
		
		if(!whileStatement.getE().getTypeName().isType(TypeName.BOOLEAN))
			throw new TypeCheckException("If statement: expression type is : " + whileStatement.getE().getTypeName());
		
		//visit block
		whileStatement.getB().visit(this, null);
		
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		if(!symtab.insert(declaration.getIdent().getText(), declaration))
			throw new TypeCheckException("Duplicate param dec found in program at same scope");
		
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
			throw new TypeCheckException("Ident was never declared");
		
		if(!symtab.isIdentVisible(identX.getText()))
			throw new TypeCheckException("Ident was declared but is not visisble in the current block");
		
		//set ident dec to dec of ident
		identX.setDec(dec);
		
		return null;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		if(!symtab.insert(paramDec.getIdent().getText(), paramDec))
			throw new TypeCheckException("Duplicate param dec found in program");
		
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		constantExpression.setTypeName(TypeName.INTEGER);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		
		//visit the tuple
		imageOpChain.getArg().visit(this, null);
		
		if(imageOpChain.getArg().getExprList().size() == 0 && (imageOpChain.getFirstToken().isKind(OP_WIDTH) 
				|| imageOpChain.getFirstToken().isKind(OP_WIDTH)))
			imageOpChain.setTypeName(TypeName.INTEGER);
		else if(imageOpChain.getArg().getExprList().size() == 1 && imageOpChain.getFirstToken().isKind(KW_SCALE))
			imageOpChain.setTypeName(TypeName.IMAGE);
		
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		
		for(Expression e : tuple.getExprList()){
			e.visit(this, null);
			if(!e.getTypeName().isType(TypeName.INTEGER))
				throw new TypeCheckException("Tuple: expression type is : " + e.getTypeName());
		}
		
		return null;
	}


}