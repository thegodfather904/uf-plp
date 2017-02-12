package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.Token;

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
		program();
		matchEOF();
		return;
	}

	void program() throws SyntaxException {
		
		while(scanner.peek() != null){
			if(predictFilterOp())
				filterOp();
			else if(predictFrameOp())
				frameOp();
			else if(predictImageOp())
				imageOp();
			else if(predictArrowOp())
				arrowOp();
			else if(predictRelOp())
				relOp();
			else if(predictWeakOp())
				weakOp();
			else if(predictStrongOp())
				strongOp();
			else if(predictParamDec())
				paramDec();
			else if(predictDec())
				dec();
		}
		
//		throw new UnimplementedFeatureException();
	}
	
	
	void expression() throws SyntaxException {
		//TODO
		throw new UnimplementedFeatureException();
	}

	void term() throws SyntaxException {
		//TODO
		throw new UnimplementedFeatureException();
	}

	void elem() throws SyntaxException {
		//TODO
		throw new UnimplementedFeatureException();
	}

	void factor() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
		case IDENT: {
			consume();
		}
			break;
		case INT_LIT: {
			consume();
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			consume();
		}
			break;
		case LPAREN: {
			consume();
			expression();
			match(RPAREN);
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor");
		}
	}

	void block() throws SyntaxException {
		//TODO
		throw new UnimplementedFeatureException();
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
		//TODO
		throw new UnimplementedFeatureException();
	}

	void chain() throws SyntaxException {
		//TODO
		throw new UnimplementedFeatureException();
	}

	void chainElem() throws SyntaxException {
		//TODO
		throw new UnimplementedFeatureException();
	}

	void arg() throws SyntaxException {
		//TODO
		throw new UnimplementedFeatureException();
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
	
	private boolean predictImageOp() throws SyntaxException{
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
