package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IntLitExpression;

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
	void parse() throws SyntaxException {
		
		Expression e = expression();
		
		System.out.println(e.toString());
		
//		program();
		matchEOF();
		return;
	}

	void program() throws SyntaxException {
		match(IDENT);
		if(predictBlock())
			block();
		else if (predictParamDec()){
			paramDec();
			while(t.isKind(COMMA)){
				match(COMMA);
				if(predictParamDec())
					paramDec();
				else
					throw new SyntaxException("Expected ParamDec but recieved token: " + t.toString());
			}
			if(predictBlock())
				block();
			else
				throw new SyntaxException("Expected Block but recieved token: " + t.toString());
		}else
			throw new SyntaxException("Expected Block or ParamDec but recieved token: " + t.toString());
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

	void block() throws SyntaxException {
		match(LBRACE);
		while(predictDec() || predictStatement())
			if(predictDec())
				dec();
			else
				statement();
		match(RBRACE);
		
	}

	void paramDec() throws SyntaxException {
		consume();
		match(IDENT);
	}

	void dec() throws SyntaxException {
		consume();
		match(IDENT);
	}

	void statement() throws SyntaxException {
		if(t.isKind(OP_SLEEP)){
			match(OP_SLEEP);
			if(predictExpression())
				expression();
			else
				throw new SyntaxException("Expected Expression but recieved token: " + t.toString());
			match(SEMI);
		}else if(predictWhileStatement())
			whileStatement();
		else if(predictIfStatement())
			ifStatement();
		else if(predictAssign() && scanner.peek() != null && scanner.peek().isKind(ASSIGN)){
			assign();
			match(SEMI);
		}
		else if(predictChain()){
			chain();
			match(SEMI);
		}else
			throw new SyntaxException("In statement but token doesn't match any predict; token: " + t.toString());
	}

	void chain() throws SyntaxException {
		chainElem();
		arrowOp();
		chainElem();
		while(predictArrowOp()){
			arrowOp();
			chainElem();
		}
	}

	void chainElem() throws SyntaxException {
		if(t.kind == IDENT)
			consume();
		else{
			consume();
			arg();
		}
	}

	void arg() throws SyntaxException {
		if(t.isKind(LPAREN)){
			consume();
			expression();
			while(t.isKind(COMMA)){
				consume();
				expression();
			}
			match(RPAREN);
		}
	}

	void assign() throws SyntaxException{
		match(IDENT);
		match(ASSIGN);
		
		if(predictExpression())
			expression();
		else
			throw new SyntaxException("Expected Expression but recieved token: " + t.toString());
	}
	
	void whileStatement() throws SyntaxException{
		match(KW_WHILE);
		match(LPAREN);
		
		if(predictExpression())
			expression();
		else
			throw new SyntaxException("Expected Expression but recieved token: " + t.toString());
		
		match(RPAREN);
		
		if(predictBlock())
			block();
		else
			throw new SyntaxException("Expected Block but recieved token: " + t.toString());
	}
	
	void ifStatement() throws SyntaxException{
		match(KW_IF);
		match(LPAREN);
		
		if(predictExpression())
			expression();
		else
			throw new SyntaxException("Expected Expression but recieved token: " + t.toString());
		
		match(RPAREN);
		
		if(predictBlock())
			block();
		else
			throw new SyntaxException("Expected Block but recieved token: " + t.toString());
		
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
