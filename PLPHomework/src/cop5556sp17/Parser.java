package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Token;
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
import cop5556sp17.AST.WhileStatement;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}

	Program program() throws SyntaxException {
		
		Token firstToken = t;
		ArrayList<ParamDec> paramDecList = new ArrayList<ParamDec>();
		Block b = null;
		
		match(IDENT);
		if(predictBlock())
			b = block();
		else if (predictParamDec()){
			paramDecList.add(paramDec());
			while(t.isKind(COMMA)){
				match(COMMA);
				if(predictParamDec())
					paramDecList.add(paramDec());
				else
					throw new SyntaxException("Expected ParamDec but recieved token: " + t.toString());
			}
			if(predictBlock())
				b = block();
			else
				throw new SyntaxException("Expected Block but recieved token: " + t.toString());
		}else
			throw new SyntaxException("Expected Block or ParamDec but recieved token: " + t.toString());
		
		return new Program(firstToken, paramDecList, b);
	}
	
	Expression expression() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = term();
		Expression e1 = null;
		while(predictRelOp()){
			Token op = t;
			relOp();
			e1 = term();
			e0 = new BinaryExpression(firstToken, e0, op, e1);
		}
		
		return e0;
	}

	Expression term() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = elem();
		Expression e1 = null;
		while(predictWeakOp()){
			Token op = t;
			weakOp();
			e1 = elem();
			e0 = new BinaryExpression(firstToken, e0, op, e1);
		}
		
		return e0;
	}

	Expression elem() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = factor();
		Expression e1 = null;
		while(predictStrongOp()){
			Token op = t;
			strongOp();
			e1 = factor();
			e0 = new BinaryExpression(firstToken, e0, op, e1);
		}
		
		return e0;
	}

	Expression factor() throws SyntaxException {
		Expression e = null;
		Kind kind = t.kind;
		switch (kind) {
		case IDENT: {
			e = new IdentExpression(t);
			consume();
		}
			break;
		case INT_LIT: {
			e = new IntLitExpression(t);
			consume();
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			e = new BooleanLitExpression(t);
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			e = new ConstantExpression(t);
			consume();
		}
			break;
		case LPAREN: {
			consume();
			e = expression();
			match(RPAREN);
		}
			break;
		default:
			throw new SyntaxException("Expected factor but recieved token: " + t.toString());
		}
		
		return e;
	}

	Block block() throws SyntaxException {
		Token firstToken = t;
		ArrayList<Dec> decList = new ArrayList<Dec>();
		ArrayList<Statement> statementList = new ArrayList<Statement>();
		
		match(LBRACE);
		while(predictDec() || predictStatement())
			if(predictDec())
				decList.add(dec());
			else
				statementList.add(statement());
		match(RBRACE);
		
		return new Block(firstToken, decList, statementList);
	}

	ParamDec paramDec() throws SyntaxException {
		if(predictType()){
			Token firstToken = t;
			consume();
			return new ParamDec(firstToken, match(IDENT));
		}else{
			throw new SyntaxException("Expected Dec but recieved token: " + t.toString());
		}
	}

	Dec dec() throws SyntaxException {
		if(predictType()){
			Token firstToken = t;
			consume();
			return new Dec(firstToken, match(IDENT));
		}else{
			throw new SyntaxException("Expected Dec but recieved token: " + t.toString());
		}
	}

	Statement statement() throws SyntaxException {
		Statement s = null;
		Token firstToken = t;
		if(t.isKind(OP_SLEEP)){
			match(OP_SLEEP);
			Expression e;
			if(predictExpression())
				e = expression();
			else
				throw new SyntaxException("Expected Expression but recieved token: " + t.toString());
			s = new SleepStatement(firstToken, e);
			match(SEMI);
		}else if(predictWhileStatement())
			s = whileStatement();
		else if(predictIfStatement())
			s = ifStatement();
		else if(predictAssign() && scanner.peek() != null && scanner.peek().isKind(ASSIGN)){
			s = assign();
			match(SEMI);
		}
		else if(predictChain()){
			s = chain();
			match(SEMI);
		}else
			throw new SyntaxException("In statement but token doesn't match any predict; token: " + t.toString());
		
		return s;
	}

	Chain chain() throws SyntaxException {
		Token firstToken = t;
		
		BinaryChain bc = null;
		ChainElem ce1 = chainElem();
		Token arrowOp = null;;
		if(predictArrowOp()){
			arrowOp = t;
			consume();
		}else
			throw new SyntaxException("In Chain but token doesn't match arrowOp; token: " + t.toString());
		
		ChainElem ce2 = chainElem();
		bc = new BinaryChain(firstToken, ce1, arrowOp, ce2);
		
		while(predictArrowOp()){
			arrowOp = t;
			consume();
			ce2 = chainElem();
			bc = new BinaryChain(firstToken, bc, arrowOp, ce2);
		}
		
		return bc;
		
	}

	ChainElem chainElem() throws SyntaxException {
		ChainElem ce = null;
		if(t.kind == IDENT){
			ce = new IdentChain(t);
			consume();
		}
		else{
			Token firstToken = t;
			Tuple tuple;
			if(predictFilterOp()){
				consume();
				tuple = arg();
				ce = new FilterOpChain(firstToken, tuple);
			}else if(predictFrameOp()){
				consume();
				tuple = arg();
				ce = new FrameOpChain(firstToken, tuple);
			}else if(predictImageOp()){
				consume();
				tuple = arg();
				ce = new ImageOpChain(firstToken, tuple);
			}
		}
		
		return ce;
	}

	Tuple arg() throws SyntaxException {
		Tuple tuple = null;
		if(t.isKind(LPAREN)){
			Token firstToken = t;
			List<Expression> expList = new ArrayList<Expression>();
			consume();
			expList.add(expression());
			while(t.isKind(COMMA)){
				consume();
				expList.add(expression());
			}
			match(RPAREN);
			tuple = new Tuple(firstToken, expList);
		}else{
			tuple = new Tuple(t, new ArrayList<Expression>());
		}
		return tuple;
	}

	AssignmentStatement assign() throws SyntaxException{
		Token firstToken = t;
		IdentLValue identLValue= new IdentLValue(match(IDENT));
		match(ASSIGN);
		if(predictExpression()){
			Expression e = expression();
			return new AssignmentStatement(firstToken, identLValue, e);
		}
		else
			throw new SyntaxException("Expected Expression but recieved token: " + t.toString());
	}
	
	WhileStatement whileStatement() throws SyntaxException{
		Token firstToken = t;
		match(KW_WHILE);
		match(LPAREN);
		
		Expression e;
		if(predictExpression())
			e = expression();
		else
			throw new SyntaxException("Expected Expression but recieved token: " + t.toString());
		
		match(RPAREN);
		
		Block b;
		if(predictBlock())
			b = block();
		else
			throw new SyntaxException("Expected Block but recieved token: " + t.toString());
		
		return new WhileStatement(firstToken, e, b);
	}
	
	IfStatement ifStatement() throws SyntaxException{
		Token firstToken = t;
		match(KW_IF);
		match(LPAREN);
		
		Expression e;
		if(predictExpression())
			e = expression();
		else
			throw new SyntaxException("Expected Expression but recieved token: " + t.toString());
		
		match(RPAREN);
		
		Block b;
		if(predictBlock())
			b = block();
		else
			throw new SyntaxException("Expected Block but recieved token: " + t.toString());
		
		return new IfStatement(firstToken, e, b);
		
	}
	
	
	
	void filterOp() throws SyntaxException{
		Kind kind = t.kind;
		switch (kind) {
		case OP_BLUR:
			consume();
			break;
		case OP_GRAY:
			consume();
			break;
		case OP_CONVOLVE:
			consume();
			break;
		default:
			throw new SyntaxException("Expected filterOp but recieved token: " + t.toString());
		}
	}
	
	private boolean predictFilterOp(){
		boolean isFilterOp;
		Kind kind = t.kind;
		switch (kind) {
		case OP_BLUR:
			isFilterOp = true;
			break;
		case OP_GRAY:
			isFilterOp = true;
			break;
		case OP_CONVOLVE:
			isFilterOp = true;
			break;
		default:
			isFilterOp = false;
		}
		return isFilterOp;
	}
	
	void frameOp() throws SyntaxException{
		Kind kind = t.kind;
		switch (kind) {
		case KW_SHOW:
			consume();
			break;
		case KW_HIDE:
			consume();
			break;
		case KW_MOVE:
			consume();
			break;
		case KW_XLOC:
			consume();
			break;
		case KW_YLOC:
			consume();
			break;
		default:
			throw new SyntaxException("Expected frameOp but recieved token: " + t.toString());
		}
	}
	
	private boolean predictFrameOp(){
		boolean isFrameOp;
		Kind kind = t.kind;
		switch (kind) {
		case KW_SHOW:
			isFrameOp = true;
			break;
		case KW_HIDE:
			isFrameOp = true;
			break;
		case KW_MOVE:
			isFrameOp = true;
			break;
		case KW_XLOC:
			isFrameOp = true;
			break;
		case KW_YLOC:
			isFrameOp = true;
			break;
		default:
			isFrameOp = false;
		}
		return isFrameOp;
	}
	
	void imageOp() throws SyntaxException{
		Kind kind = t.kind;
		switch (kind) {
		case OP_WIDTH:
			consume();
			break;
		case OP_HEIGHT:
			consume();
			break;
		case KW_SCALE:
			consume();
			break;
		default:
			throw new SyntaxException("Expected imageOp but recieved token: " + t.toString());
		}
	}
	
	private boolean predictImageOp(){
		boolean isImageOp;
		Kind kind = t.kind;
		switch (kind) {
		case OP_WIDTH:
			isImageOp = true;
			break;
		case OP_HEIGHT:
			isImageOp = true;
			break;
		case KW_SCALE:
			isImageOp = true;
			break;
		default:
			isImageOp = false;
		}
		return isImageOp;
	}
	
	void arrowOp() throws SyntaxException{
		Kind kind = t.kind;
		switch (kind) {
		case ARROW:
			consume();
			break;
		case BARARROW:
			consume();
			break;
		default:
			throw new SyntaxException("Expected arrowOp but recieved token: " + t.toString());
		}
	}
	
	private boolean predictArrowOp() throws SyntaxException{
		boolean isArrow;
		Kind kind = t.kind;
		switch (kind) {
		case ARROW:
			isArrow = true;
			break;
		case BARARROW:
			isArrow = true;
			break;
		default:
			isArrow = false;
		}
		return isArrow;
	}
	
	void relOp() throws SyntaxException{
		Kind kind = t.kind;
		switch (kind) {
		case LT:
			consume();
			break;
		case LE:
			consume();
			break;
		case GT:
			consume();
			break;
		case GE:
			consume();
			break;
		case EQUAL:
			consume();
			break;
		case NOTEQUAL:
			consume();
			break;
		default:
			throw new SyntaxException("Expected relOp but recieved token: " + t.toString());
		}
	}
	
	private boolean predictRelOp() throws SyntaxException{
		boolean isRelOp;
		Kind kind = t.kind;
		switch (kind) {
		case LT:
			isRelOp = true;
			break;
		case LE:
			isRelOp = true;
			break;
		case GT:
			isRelOp = true;
			break;
		case GE:
			isRelOp = true;
			break;
		case EQUAL:
			isRelOp = true;
			break;
		case NOTEQUAL:
			isRelOp = true;
			break;
		default:
			isRelOp = false;
		}
		return isRelOp;
	}
	
	void weakOp() throws SyntaxException{
		Kind kind = t.kind;
		switch (kind) {
		case PLUS:
			consume();
			break;
		case MINUS:
			consume();
			break;
		case OR:
			consume();
			break;
		default:
			throw new SyntaxException("Expected weakOp but recieved token: " + t.toString());
		}
	}
	
	private boolean predictWeakOp() throws SyntaxException{
		boolean isWeakOp;
		Kind kind = t.kind;
		switch (kind) {
		case PLUS:
			isWeakOp = true;
			break;
		case MINUS:
			isWeakOp = true;
			break;
		case OR:
			isWeakOp = true;
			break;
		default:
			isWeakOp = false;
		}
		return isWeakOp;
	}
	
	void strongOp() throws SyntaxException{
		Kind kind = t.kind;
		switch (kind) {
		case TIMES:
			consume();
			break;
		case DIV:
			consume();
			break;
		case AND:
			consume();
			break;
		case MOD:
			consume();
			break;
		default:
			throw new SyntaxException("Expected strongOp but recieved token: " + t.toString());
		}
	}
	
	private boolean predictStrongOp() throws SyntaxException{
		boolean isStrongOp;
		Kind kind = t.kind;
		switch (kind) {
		case TIMES:
			isStrongOp = true;
			break;
		case DIV:
			isStrongOp = true;
			break;
		case AND:
			isStrongOp = true;
			break;
		case MOD:
			isStrongOp = true;
			break;
		default:
			isStrongOp = false;
		}
		return isStrongOp;
	}
	
	private boolean predictParamDec(){
		boolean isparamDec;
		Kind kind = t.kind;
		switch (kind) {
		case KW_URL:
			isparamDec = true;
			break;
		case KW_FILE:
			isparamDec = true;
			break;
		case KW_INTEGER:
			isparamDec = true;
			break;
		case KW_BOOLEAN:
			isparamDec = true;
			break;
		default:
			isparamDec = false;
		}
		return isparamDec;
	}
	
	private boolean predictDec(){
		boolean isDec;
		Kind kind = t.kind;
		switch (kind) {
		case KW_INTEGER:
			isDec = true;
			break;
		case KW_BOOLEAN:
			isDec = true;
			break;
		case KW_IMAGE:
			isDec = true;
			break;
		case KW_FRAME:
			isDec = true;
			break;
		default:
			isDec = false;
		}
		return isDec;
	}
	
	private boolean predictFactor(){
		boolean isFactor;
		Kind kind = t.kind;
		switch (kind) {
		case IDENT:
			isFactor = true;
			break;
		case INT_LIT:
			isFactor = true;
			break;
		case KW_TRUE:
			isFactor = true;
			break;
		case KW_FALSE:
			isFactor = true;
			break;
		case KW_SCREENWIDTH:
			isFactor = true;
			break;
		case KW_SCREENHEIGHT:
			isFactor = true;
			break;
		case LPAREN:
			isFactor = true;
			break;
		default:
			isFactor = false;
		}
		return isFactor;
	}
	
	private boolean predictElem(){
		return predictFactor();
	}
	
	private boolean predictTerm(){
		return predictElem();
	}
	
	private boolean predictExpression(){
		return predictTerm();
	}
	
	private boolean predictArg(){
		boolean isArg;
		Kind kind = t.kind;
		switch (kind) {
		case LPAREN:
			isArg = true;
			break;
		default:
			isArg = false;
		}
		return isArg;
	}
	
	private boolean predictChainElem(){
		boolean isChainElem;
		if(t.kind == IDENT)
			isChainElem = true;
		else if(predictFilterOp())
			isChainElem = true;
		else if(predictFrameOp())
			isChainElem = true;
		else if(predictImageOp())
			isChainElem = true;
		else
			isChainElem = false;
		return isChainElem;
	}
	
	private boolean predictChain(){
		return predictChainElem();
	}
	
	private boolean predictAssign(){
		boolean isAssign = false;
		if(t.kind == IDENT)
			isAssign = true;
		return isAssign;
	}
	
	private boolean predictWhileStatement(){
		boolean isWhile = false;
		if(t.isKind(KW_WHILE))
			isWhile = true;
		return isWhile;
	}
	
	private boolean predictBlock(){
		boolean isBlock = false;
		if(t.isKind(LBRACE))
			isBlock = true;
		return isBlock;
	}
	
	private boolean predictIfStatement(){
		boolean isIf = false;
		if(t.isKind(KW_IF))
			isIf = true;
		return isIf;
	}
	
	private boolean predictStatement(){
		boolean isStatement = false;
		
		if(t.isKind(OP_SLEEP))
			isStatement = true;
		else if(predictWhileStatement())
			isStatement = true;
		else if(predictIfStatement())
			isStatement = true;
		else if(predictChain())
			isStatement = true;
		else if(predictAssign())
			isStatement = true;
		
		return isStatement;
	}
	
	private boolean predictType(){
		boolean isType;
		Kind kind = t.kind;
		switch (kind) {
		case KW_INTEGER:
			isType = true;
			break;
		case KW_IMAGE:
			isType = true;
			break;
		case KW_FRAME:
			isType = true;
			break;
		case KW_FILE:
			isType = true;
			break;
		case KW_BOOLEAN:
			isType = true;
			break;
		case KW_URL:
			isType = true;
			break;
		default:
			isType = false;
		}
		return isType;
	}
	
	
	
	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
