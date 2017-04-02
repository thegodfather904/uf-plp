package cop5556sp17;



import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.Dec;


public class SymbolTable {
	
	private HashMap<String, SymbolTableObject> lcTable = new HashMap<String, SymbolTableObject>();
	
	private Stack<ScopeStack> scopeStackStack = new Stack<ScopeStack>();
	private HashSet<Integer> usedScopeSet = new HashSet<Integer>();
	
	public SymbolTable(){
		scopeStackStack.push(new ScopeStack(0));
		usedScopeSet.add(0);
	}
	
	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		
		//get new scope number
		int nextScope = findNextScopeNumber();
		usedScopeSet.add(nextScope);
		
		//push new stack and scope number onto stack
		scopeStackStack.push(new ScopeStack(nextScope));
		
		
	}
	
	/**
	 * Finds the next available scope number to be used 
	 */
	private int findNextScopeNumber(){
		int currentScope = scopeStackStack.peek().getCurrentScope();
		currentScope++;
		while(isScopeNumberUsed(currentScope))
			currentScope++;
		
		return currentScope;
		
	}
	
	/**
	 * Returns true if the current scope is used
	 * 
	 * @param scopeNumber
	 * @return
	 */
	private boolean isScopeNumberUsed(int scopeNumber){
		return usedScopeSet.contains(scopeNumber);
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//remove the current scopeStack from the scopeStackStack
		scopeStackStack.pop();
	}
	
	/**
	 * Returns the current scope number being used
	 * 
	 * @return
	 */
	private int currentScopeNumber(){
		return scopeStackStack.peek().getCurrentScope();
	}
	
	
	/*True if insert was successfull, false if duplicate record*/
	public boolean insert(String ident, Dec dec){
		
		//check if already in the symbol table
		if(lcTable.containsKey(ident))
			return false;
		else{
			insertIntoLcTable(ident, dec);
			pushOntoScopeStackStack(dec);
		}
			
		return true;
	}
	
	/**
	 * Inserts a new identifier into the symbol table
	 * 
	 * @param ident
	 * @param dec
	 */
	private void insertIntoLcTable(String ident, Dec dec){
		lcTable.put(ident, new SymbolTableObject(0, dec));
	}
	
	/**
	 * Pushes the current dec onto the scope stack
	 * 
	 * @param dec
	 */
	private void pushOntoScopeStackStack(Dec dec){
		scopeStackStack.peek().getDecStack().push(dec);
	}
	
	
	/**
	 * Looks up the value in the lcTable (returns null if not in table aka not declared)
	 * 
	 * @param ident
	 * @return
	 */
	public Dec lookup(String ident){
		SymbolTableObject sto = lcTable.get(ident);
		if(sto != null)
			return sto.getDec();
		else
			return null;
	}

	/**
	 * Returns true if the ident is in the current scope or any previous nested scope
	 * 
	 * @param ident
	 * @return
	 */
	public boolean isIdentVisible(String ident){
		boolean isInScope = false;
		
		Stack<ScopeStack> scopeStackHolder = new Stack<ScopeStack>();
		
		Iterator<ScopeStack> iterator = scopeStackStack.iterator();
		
		while(!isInScope && !scopeStackStack.isEmpty()){
			//check if ident is in current stack
			isInScope = isIdentInScopeStack(ident, scopeStackStack.peek().getDecStack());
			
			//pop and move to stackholder
			scopeStackHolder.push(scopeStackStack.pop());
		}
		
		//move all items in stackholder back onto stack
		while(!scopeStackHolder.isEmpty())
			scopeStackStack.push(scopeStackHolder.pop());
		
		return isInScope;
	}
	
	/**
	 * Returns true if the ident is in the stack, false if not
	 * 
	 * @param ident
	 * @param decStack
	 * @return
	 */
	private boolean isIdentInScopeStack(String ident, Stack<Dec> decStack){
		Iterator<Dec> iterator = decStack.iterator();
		
		while(iterator.hasNext())
			if(ident.equals(iterator.next().getIdent().getText()))
				return true;
		
		return false;
	}
	
	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return "";
	}


	public HashMap<String, SymbolTableObject> getLcTable() {
		return lcTable;
	}


	public void setLcTable(HashMap<String, SymbolTableObject> lcTable) {
		this.lcTable = lcTable;
	}

	public Stack<ScopeStack> getScopeStackStack() {
		return scopeStackStack;
	}

	public void setScopeStackStack(Stack<ScopeStack> scopeStackStack) {
		this.scopeStackStack = scopeStackStack;
	}
}
