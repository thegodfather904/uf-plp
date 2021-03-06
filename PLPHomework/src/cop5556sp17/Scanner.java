package cop5556sp17;

import java.util.ArrayList;

public class Scanner {
	

	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;
	final ArrayList<Integer> posStarts;
	
	/**
	 * Kind enum
	 */
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
	
	public static enum State{
		START("start"), IN_DIGIT("in_digit"), IN_IDENT("in_ident"), OPERATOR("operator"), SEPARATOR("separator"),
		COMMENT_START("comment_start"), INVALID("INVALID");
		
		State(String stateText){
			this.stateText = stateText;
		}
		
		final String stateText;
		
		public String getStateText(){
			return stateText;
		}
	}
	
	
	/**
	 * Constructor, takes in a String and scans it
	 * 
	 * @param chars
	 */
	public Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
		posStarts = new ArrayList<Integer>();
		posStarts.add(new Integer(0));
	}
	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException { 
		int length = chars.length();
		ScannerSwitchHelper switchHelper = new ScannerSwitchHelper(0, State.START);
		
		while(switchHelper.getCurrentPos() <= length){
			switch(switchHelper.getCurrentState()){
			case START:{
				caseStart(switchHelper, length);
				break;
				}
			case IN_DIGIT:{
				caseInDigit(switchHelper);
				break;
				}
			case IN_IDENT:{
				caseInIdent(switchHelper);
				break;
				}
			case SEPARATOR:{
				caseSeparator(switchHelper);
				break;
				}
			case OPERATOR:{
				caseOperator(switchHelper);
				break;
				}
			case COMMENT_START:{
				caseCommentStart(switchHelper);
				break;
				}
			case INVALID:{
				caseInvalidChar(switchHelper);
				break;
				}
			}
		}	
		return this;  
	}
	
	/**
	 * The starting case for the scanner
	 * 
	 * @param currentPos
	 */
	private void caseStart(ScannerSwitchHelper switchHelper, int length){
		switchHelper.setCurrentPos(skipWhiteSpace(switchHelper.getCurrentPos()));
		int currentChar = getCurrentChar(switchHelper.getCurrentPos(), length);
		
		if(currentChar == -1)//are we at the end of the line
			addNewToken(Kind.EOF, switchHelper.getCurrentPos(), 0, State.START, switchHelper, true);
		else if(isCommentStart(switchHelper))
			switchHelper.setCurrentState(State.COMMENT_START);
		else if(Character.isDigit(currentChar)) //char is a digit
			switchHelper.setCurrentState(State.IN_DIGIT);
		else if(Character.isJavaIdentifierStart(currentChar))
			 switchHelper.setCurrentState(State.IN_IDENT);
		else if(isCharSeparator(currentChar))
			switchHelper.setCurrentState(State.SEPARATOR);
		else if(isCharOperator(currentChar))
			switchHelper.setCurrentState(State.OPERATOR);
		else
			switchHelper.setCurrentState(State.INVALID);
	}
	
	
	/**
	 * Case for a comment start
	 * 
	 * @param switchHelper
	 * @throws IllegalCharException
	 */
	private void caseCommentStart(ScannerSwitchHelper switchHelper) throws IllegalCharException{
		
		//increment past "/*"
		switchHelper.incrememntCurrentPos();
		switchHelper.incrememntCurrentPos();
		
		//make sure next token set isn't "*/"
		if(isCommentEnd(switchHelper))
			throw new IllegalCharException("Comment Failure - comment was empty");
		else{
			while(getCurrentChar(switchHelper.getCurrentPos(), chars.length()) != -1 && !isCommentEnd(switchHelper))
				switchHelper.incrememntCurrentPos();
			
			//if there isnt a closing "*/", throw an error
			if(getCurrentChar(switchHelper.getCurrentPos(), chars.length()) == -1)
				throw new IllegalCharException("Comment Failure - missing closing comment");
			else{
				//increment past the comment end
				switchHelper.incrememntCurrentPos();
				switchHelper.incrememntCurrentPos();	
				switchHelper.setCurrentState(State.START);	
			}
			
			
		}
	}
	
	/** 
	 * Case for when in a digit
	 * 
	 * @param switchHelper
	 */
	private void caseInDigit(ScannerSwitchHelper switchHelper) throws IllegalNumberException{
		
		int currentChar = chars.charAt(switchHelper.getCurrentPos());
		switchHelper.setStartPos(switchHelper.getCurrentPos());
		
		if(currentChar == '0'){ //if 0, create new token and got to Start
			addNewToken(Kind.INT_LIT, switchHelper.getCurrentPos(), 1, State.START, switchHelper, true);
		}else{
			
			while(switchHelper.getCurrentPos() < chars.length() && Character.isDigit(chars.charAt(switchHelper.getCurrentPos()))){
				switchHelper.incrememntCurrentPos();
			}
			
			String digit = chars.substring(switchHelper.getStartPos(), switchHelper.getCurrentPos()); //save in string for error checking
			
			try{
				Integer.parseInt(digit); //parse to verify it is an int
				addNewToken(Kind.INT_LIT, switchHelper.getStartPos(), switchHelper.getCurrentPos() - switchHelper.getStartPos(), 
						State.START, switchHelper, false);
			}catch(NumberFormatException e){
				throw new IllegalNumberException("Digit is to large for int. Digit = " + digit);
			}
			
		}
	}
	
	/**
	 * Case for idents (checks for keywords also
	 * 
	 * @param switchHelper
	 */
	private void caseInIdent(ScannerSwitchHelper switchHelper){
		switchHelper.setStartPos(switchHelper.getCurrentPos());
		
		while(notAtEndOfString(switchHelper.getCurrentPos()) && 
				Character.isJavaIdentifierPart(chars.charAt(switchHelper.getCurrentPos()))){
			switchHelper.incrememntCurrentPos();
		}
		
		String currentToken = buildStringFromToken(switchHelper);
		
		//check for various reserved words
		if(isKeyword(currentToken)){
			Kind keywordType = keywordType(currentToken);
			addNewToken(keywordType, switchHelper.getStartPos(), switchHelper.getCurrentPos() - switchHelper.getStartPos(), 
					State.START, switchHelper, false);
		}else if(isFilterOpKeyword(currentToken)){
			Kind keywordType = filterOpKeywordType(currentToken);
			addNewToken(keywordType, switchHelper.getStartPos(), switchHelper.getCurrentPos() - switchHelper.getStartPos(), 
					State.START, switchHelper, false);
		}else if(isImageOpKeyword(currentToken)){
			Kind keywordType = imageOpKeywordType(currentToken);
			addNewToken(keywordType, switchHelper.getStartPos(), switchHelper.getCurrentPos() - switchHelper.getStartPos(), 
					State.START, switchHelper, false);
		}else if(isFrameOpKeyword(currentToken)){
			Kind keywordType = frameOpKeywordType(currentToken);
			addNewToken(keywordType, switchHelper.getStartPos(), switchHelper.getCurrentPos() - switchHelper.getStartPos(), 
					State.START, switchHelper, false);
		}else if(isBooleanLiteral(currentToken)){
			Kind keywordType = booleanLiteralType(currentToken);
			addNewToken(keywordType, switchHelper.getStartPos(), switchHelper.getCurrentPos() - switchHelper.getStartPos(), 
					State.START, switchHelper, false);
		}else{
			addNewToken(Kind.IDENT, switchHelper.getStartPos(), switchHelper.getCurrentPos() - switchHelper.getStartPos(), 
					State.START, switchHelper, false);
		}
	}
	
	/**
	 * Separator case
	 * 
	 * @param switchHelper
	 */
	private void caseSeparator(ScannerSwitchHelper switchHelper){
		int currentChar = chars.charAt(switchHelper.getCurrentPos());
		Kind separatorType = separatorType(currentChar);
		addNewToken(separatorType, switchHelper.getCurrentPos(), 1,
				State.START, switchHelper, true);
	}
	
	/**
	 * Operator case
	 * 
	 * @param switchHelper
	 */
	private void caseOperator(ScannerSwitchHelper switchHelper) throws IllegalCharException{
		int currentChar = chars.charAt(switchHelper.getCurrentPos());
		switchHelper.setStartPos(switchHelper.getCurrentPos());
		Kind operatorType = operatorType(currentChar, switchHelper);
		addNewToken(operatorType, switchHelper.getStartPos(), switchHelper.getCurrentPos() - switchHelper.getStartPos(),
				State.START, switchHelper, false);
		
	}
	
	/**
	 * Invalid char case (throws exception)
	 * 
	 * @param switchHelper
	 * @throws IllegalCharException
	 */
	private void caseInvalidChar(ScannerSwitchHelper switchHelper) throws IllegalCharException{
		char illegalChar = chars.charAt(switchHelper.getCurrentPos());
		throw new IllegalCharException("Illegal Character found while scanning. Char = " + illegalChar);
	}
	
	/**
	 * Returns true if it is the start of a comment("/*")
	 * 
	 * @param switchHelper
	 * @return
	 */
	private boolean isCommentStart(ScannerSwitchHelper switchHelper){
		boolean commentStart = false;
		int currentChar = chars.charAt(switchHelper.getCurrentPos());
		
		if(currentChar == '/' && getNextChar(switchHelper.getCurrentPos()) == '*')
			commentStart = true;
		
		return commentStart;
	}
	
	/**
	 * Returns true if its the the end of a comment ("*slash")
	 * 
	 * @param switchHelper
	 * @return
	 */
	private boolean isCommentEnd(ScannerSwitchHelper switchHelper){
		boolean commentEnd = false;
		int currentChar = chars.charAt(switchHelper.getCurrentPos());
		
		if(currentChar == '*' && getNextChar(switchHelper.getCurrentPos()) == '/')
			commentEnd = true;
		
		return commentEnd;
	}
	
	/**
	 * Returns true if a separator, false if not
	 * 
	 * @param currentChar
	 * @return
	 */
	private boolean isCharSeparator(int currentChar){
		
		boolean isSeparator;
		
		switch(currentChar){
			case ';': 
				isSeparator = true; 
				break;
			case ',':
				isSeparator = true; 
				break;
			case '(':
				isSeparator = true; 
				break;
			case ')':
				isSeparator = true; 
				break;
			case '{':
				isSeparator = true; 
				break;
			case '}':
				isSeparator = true; 
				break;
			default:
				isSeparator = false;
				break;
		}
		
		return isSeparator;
	}
	
	/**
	 * Returns the kind the separator is
	 * 
	 * @param currentChar
	 * @return
	 */
	private Kind separatorType(int currentChar){
		Kind type = null;
		
		switch(currentChar){
		case ';': 
			type = Kind.SEMI; 
			break;
		case ',':
			type = Kind.COMMA; 
			break;
		case '(':
			type = Kind.LPAREN; 
			break;
		case ')':
			type = Kind.RPAREN; 
			break;
		case '{':
			type = Kind.LBRACE; 
			break;
		case '}':
			type = Kind.RBRACE; 
			break;
		}
		
		return type;
	}
	
	/**
	 * Returns true if the char is an operator
	 * 
	 * @param currentChar
	 * @return
	 */
	private boolean isCharOperator(int currentChar){
		
		boolean isOperator;
		
		switch(currentChar){
			case '|': 
				isOperator = true; 
				break;
			case '&':
				isOperator = true; 
				break;
			case '=':
				isOperator = true; 
				break;
			case '!':
				isOperator = true; 
				break;
			case '<':
				isOperator = true; 
				break;
			case '>':
				isOperator = true; 
				break;
			case '+':
				isOperator = true; 
				break;
			case '-':
				isOperator = true; 
				break;
			case '*':
				isOperator = true; 
				break;
			case '/':
				isOperator = true; 
				break;
			case '%':
				isOperator = true; 
				break;
			default:
				isOperator = false;
				break;
		}
		
		return isOperator;
	}
	
	private Kind operatorType(int currentChar, ScannerSwitchHelper switchHelper) throws IllegalCharException{
		Kind type = null;
		
		int currentPos = switchHelper.getCurrentPos();
		
		switch(currentChar){
		case '|':
			if(getNextChar(currentPos) == '-' && getNextChar(++currentPos) == '>'){
				type = Kind.BARARROW;
				switchHelper.incrememntCurrentPos();
				switchHelper.incrememntCurrentPos();
			}else
				type = Kind.OR;
			break;
		case '&':
			type = Kind.AND;
			break;
		case '=':
			if(getNextChar(currentPos) == '='){
				type = Kind.EQUAL;
				switchHelper.incrememntCurrentPos();
			}else
				throw new IllegalCharException("Operator Failure - EQUAL(=) not followed by cooresponding EQUAL(=)");
			break;
		case '!':
			if(getNextChar(currentPos) == '='){
				type = Kind.NOTEQUAL;
				switchHelper.incrememntCurrentPos();
			}else
				type = Kind.NOT;
			break;
		case '<':
			if(getNextChar(currentPos) == '='){
				type = Kind.LE;
				switchHelper.incrememntCurrentPos();
			}else if (getNextChar(currentPos) == '-'){
				type = Kind.ASSIGN;
				switchHelper.incrememntCurrentPos();
			}else
				type = Kind.LT;
			break;
		case '>':
			if(getNextChar(currentPos) == '='){
				type = Kind.GE;
				switchHelper.incrememntCurrentPos();
			}else
				type = Kind.GT;
			break;
		case '+':
			type = Kind.PLUS;
			break;
		case '-' :
			if(getNextChar(currentPos) == '>'){
				type = Kind.ARROW;
				switchHelper.incrememntCurrentPos();
			}else
				type = Kind.MINUS;
			break;
		case '/' :
			type = Kind.DIV;
			break;
		case '*' :
			type = Kind.TIMES;
			break;
		case '%' :
			type = Kind.MOD;
			break;
		}
		
		switchHelper.incrememntCurrentPos();
		
		return type;
	}	
	
	/**
	 * Returns the char (as an int) at the current position, or -1 (for EOF)
	 * 
	 * @param currentPos
	 * @param length
	 * @return
	 */
	private int getCurrentChar(int currentPos, int length){
		return currentPos < length ?chars.charAt(currentPos) : -1;
	}
	
	/**
	 * Returns the next char in the string (or -1 for EOF)
	 * 
	 * @param currentPos
	 * @param length
	 * @return
	 */
	private int getNextChar(int currentPos){
		currentPos++;
		return currentPos < chars.length() ?chars.charAt(currentPos) : -1;
	}
	
	/** skips white space at the start of a token
	 * @param currentPos
	 * @return
	 */
	private int skipWhiteSpace(int currentPos){
		while(currentPos < chars.length() && Character.isWhitespace(chars.charAt(currentPos))){
			if(isNewLine(currentPos)){
				posStarts.add(new Integer(currentPos + 1));
			}	
			currentPos++;
		}
		return currentPos;
	}
	
	private boolean isNewLine(int currentPos){
		boolean newLine = false;
		char currentChar = chars.charAt(currentPos);
		
		if(currentChar == '\n')
			newLine = true;
		
		return newLine;
	}
	
	/**
	 * Adds a token to the token list, incremements the current position in the string, and goes to the specified state
	 * 
	 * @param kind
	 * @param start
	 * @param length
	 * @param nextState
	 * @param switchHelper
	 */
	private void addNewToken(Kind kind, int start, int length, State nextState, ScannerSwitchHelper switchHelper, 
			boolean increment){
		tokens.add(new Token(kind, start, length));
		if(increment)
			switchHelper.incrememntCurrentPos();
		switchHelper.setCurrentState(nextState);
	}
	
	/**
	 * Returns true if not at end of chars, false if at end
	 * 
	 * @param currentPos
	 * @return
	 */
	private boolean notAtEndOfString(int currentPos){
		return currentPos < chars.length();
	}
	
	/**
	 * Returns true if is a keyword, false if not
	 * 
	 * @param startPos
	 * @param length
	 * @return
	 */
	private boolean isKeyword(String currentToken){
		boolean keyword = false;
		
		if(currentToken.equals(Kind.KW_INTEGER.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.KW_BOOLEAN.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.KW_IMAGE.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.KW_URL.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.KW_FILE.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.KW_FRAME.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.KW_WHILE.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.KW_IF.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.OP_SLEEP.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.KW_SCREENHEIGHT.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.KW_SCREENWIDTH.getText()))
			keyword = true;
		
		return keyword;
	}
	
	/**
	 * Returns the type of keyword the token is
	 * 
	 * @param currentToken
	 * @return
	 */
	private Kind keywordType(String currentToken){
		Kind keyword = null;
		
		if(currentToken.equals(Kind.KW_INTEGER.getText()))
			keyword = Kind.KW_INTEGER;
		else if(currentToken.equals(Kind.KW_BOOLEAN.getText()))
			keyword = Kind.KW_BOOLEAN;
		else if(currentToken.equals(Kind.KW_IMAGE.getText()))
			keyword = Kind.KW_IMAGE;
		else if(currentToken.equals(Kind.KW_URL.getText()))
			keyword = Kind.KW_URL;
		else if(currentToken.equals(Kind.KW_FILE.getText()))
			keyword = Kind.KW_FILE;
		else if(currentToken.equals(Kind.KW_FRAME.getText()))
			keyword = Kind.KW_FRAME;
		else if(currentToken.equals(Kind.KW_WHILE.getText()))
			keyword = Kind.KW_WHILE;
		else if(currentToken.equals(Kind.KW_IF.getText()))
			keyword = Kind.KW_IF;
		else if(currentToken.equals(Kind.OP_SLEEP.getText()))
			keyword = Kind.OP_SLEEP;
		else if(currentToken.equals(Kind.KW_SCREENHEIGHT.getText()))
			keyword = Kind.KW_SCREENHEIGHT;
		else if(currentToken.equals(Kind.KW_SCREENWIDTH.getText()))
			keyword = Kind.KW_SCREENWIDTH;
		
		return keyword;
	}
	
	/**
	 * Returns true if is a filter op keyword, false if not
	 * 
	 * @param currentToken
	 * @return
	 */
	private boolean isFilterOpKeyword(String currentToken){
		boolean keyword = false;
		
		if(currentToken.equals(Kind.OP_GRAY.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.OP_CONVOLVE.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.OP_BLUR.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.KW_SCALE.getText()))
			keyword = true;
		
		return keyword;
	}
	
	/**
	 * Returns the type of filter op keyword the token is
	 * 
	 * @param currentToken
	 * @return
	 */
	private Kind filterOpKeywordType(String currentToken){
		Kind keyword = null;
		
		if(currentToken.equals(Kind.OP_GRAY.getText()))
			keyword = Kind.OP_GRAY;
		else if(currentToken.equals(Kind.OP_CONVOLVE.getText()))
			keyword = Kind.OP_CONVOLVE;
		else if(currentToken.equals(Kind.OP_BLUR.getText()))
			keyword = Kind.OP_BLUR;
		else if(currentToken.equals(Kind.KW_SCALE.getText()))
			keyword = Kind.KW_SCALE;
		
		return keyword;
	}
	
	/**
	 * Returns true if is a image op keyword, false if not
	 * 
	 * @param currentToken
	 * @return
	 */
	private boolean isImageOpKeyword(String currentToken){
		boolean keyword = false;
		
		if(currentToken.equals(Kind.OP_WIDTH.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.OP_HEIGHT.getText()))
			keyword = true;
		
		return keyword;
	}
	
	/**
	 * Returns the type of image op keyword the token is
	 * 
	 * @param currentToken
	 * @return
	 */
	private Kind imageOpKeywordType(String currentToken){
		Kind keyword = null;
		
		if(currentToken.equals(Kind.OP_WIDTH.getText()))
			keyword = Kind.OP_WIDTH;
		else if(currentToken.equals(Kind.OP_HEIGHT.getText()))
			keyword = Kind.OP_HEIGHT;
		
		return keyword;
	}
	
	/**
	 * Returns true if is a frame op keyword, false if not
	 * 
	 * @param currentToken
	 * @return
	 */
	private boolean isFrameOpKeyword(String currentToken){
		boolean keyword = false;
		
		if(currentToken.equals(Kind.KW_XLOC.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.KW_YLOC.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.KW_HIDE.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.KW_SHOW.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.KW_MOVE.getText()))
			keyword = true;
		
		return keyword;
	}
	
	/**
	 * Returns the type of frame op keyword the token is
	 * 
	 * @param currentToken
	 * @return
	 */
	private Kind frameOpKeywordType(String currentToken){
		Kind keyword = null;
		
		if(currentToken.equals(Kind.KW_XLOC.getText()))
			keyword = Kind.KW_XLOC;
		else if(currentToken.equals(Kind.KW_YLOC.getText()))
			keyword = Kind.KW_YLOC;
		else if(currentToken.equals(Kind.KW_HIDE.getText()))
			keyword = Kind.KW_HIDE;
		else if(currentToken.equals(Kind.KW_SHOW.getText()))
			keyword = Kind.KW_SHOW;
		else if(currentToken.equals(Kind.KW_MOVE.getText()))
			keyword = Kind.KW_MOVE;
		
		return keyword;
	}
	
	private boolean isBooleanLiteral(String currentToken){
		boolean keyword = false;
		
		if(currentToken.equals(Kind.KW_TRUE.getText()))
			keyword = true;
		else if(currentToken.equals(Kind.KW_FALSE.getText()))
			keyword = true;
		
		return keyword;
	}
	
	private Kind booleanLiteralType(String currentToken){
		Kind keyword = null;
		
		if(currentToken.equals(Kind.KW_TRUE.getText()))
			keyword = Kind.KW_TRUE;
		else if(currentToken.equals(Kind.KW_FALSE.getText()))
			keyword = Kind.KW_FALSE;
		
		return keyword;
	}
	
	
	
	/**
	 * Builds a String from a given token (used for keyword match, etc)
	 * 
	 * @param switchHelper
	 * @return
	 */
	private String buildStringFromToken(ScannerSwitchHelper switchHelper){
		int start = switchHelper.getStartPos();
		int length = switchHelper.getCurrentPos() - switchHelper.getStartPos();
		return chars.substring(start, start + length);
	}
	
	
	/**
	 * Thrown by Scanner when an illegal character is encountered
	 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	
	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
		
	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;  

		//returns the text of this Token
		public String getText() {
			String text;
			
			try{
				if(length > 0)
					text = chars.substring(pos, pos + length);
				else
					throw new Exception();
			}catch(Exception e){
				text = kind.getText();
			}
			return text;
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){	
			int line = largestIndexFromPos(pos);
			int column = pos - posStarts.get(line);
			return new LinePos(line, column);
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException{
			return Integer.parseInt(getText());
		}
		
		public String toString(){
			return "kind: " + kind.getText() + " pos: " + pos + " length:" + length;
		}
		
		public boolean isKind(Kind someKind){
			if(this.kind.getText() != null && this.kind.getText().equals(someKind.getText()))
				return true;
			else
				return false;
		}
		
		@Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		  return true;
		  }

		 

		  private Scanner getOuterType() {
			  return Scanner.this;
		  }
		
	}
	
	/**
	 * Custom search for finding line number (couldn't use built in binary search)
	 * 
	 * @param pos
	 * @return
	 */
	private int largestIndexFromPos(int pos) {
        if (posStarts.size() < 1) {
            return -1;
        }
        return largestIndexFromPos(pos, 0, posStarts.size() - 1);
    }

    private int largestIndexFromPos(int pos, int lb, int ub) {
        final int mid = (lb + ub) / 2;

        if (mid == lb && posStarts.get(mid) > pos) {
            return -1;
        }
        
        if (posStarts.get(mid) <= pos && (mid == ub || posStarts.get(mid + 1) > pos)) {
            return mid;
        }

        if (posStarts.get(mid) <= pos)
            return largestIndexFromPos(pos, mid + 1, ub);
        else
            return largestIndexFromPos(pos, lb, mid);
    }
	
	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	 /*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */public Token peek() {
	    if (tokenNum >= tokens.size())
	        return null;
	    return tokens.get(tokenNum);
	}

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		return t.getLinePos();
	}
	
	/**
	 * Created this class to help modularize scan() better (needed to return 2 values)
	 * 
	 * @author tony
	 *
	 */
	public class ScannerSwitchHelper{
		
		private int currentPos;
		private State currentState;
		private int startPos;
		
		public ScannerSwitchHelper(){
			
		}
		
		public void incrememntCurrentPos(){
			currentPos++;
		}
		
		public ScannerSwitchHelper(int currentPos, State currentState){
			this.currentPos = currentPos;
			this.currentState = currentState;
		}
		
		public int getCurrentPos() {
			return currentPos;
		}

		public void setCurrentPos(int currentPos) {
			this.currentPos = currentPos;
		}

		public State getCurrentState() {
			return currentState;
		}

		public void setCurrentState(State currentState) {
			this.currentState = currentState;
		}

		public int getStartPos() {
			return startPos;
		}

		public void setStartPos(int startPos) {
			this.startPos = startPos;
		}
	}

}
