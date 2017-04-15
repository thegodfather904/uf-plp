package cop5556sp17;



import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Type;


public class SymbolTable {
	
	private HashMap<String, Stack<SymbolTableObject>> lcTable = new HashMap<String, Stack<SymbolTableObject>>();

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
		int currentScopeNumber = scopeStackStack.peek().getCurrentScope();
		
		//remove the current scopeStack from the scopeStackStack
		scopeStackStack.pop();
		
		//pop all elements from the lcTable that are from the old scope
		for(Stack<SymbolTableObject> stack : lcTable.values())
			if(!stack.isEmpty() && stack.peek().getScopeNumber() == currentScopeNumber)
				stack.pop();
	}
	
	/*True if insert was successfull, false if duplicate record*/
	public boolean insert(String ident, Dec dec){
		
		boolean success;
		
		//check if already in the symbol table at same scope level
		if(lcTable.containsKey(ident) && !lcTable.get(ident).isEmpty() 
				&& lcTable.get(ident).peek().getScopeNumber() == scopeStackStack.peek().getCurrentScope())
			success = false;
		else{
			try{
				addTypeName(dec);
			}catch(Exception e){
				success = false;
			}
			insertIntoLcTable(ident, dec);
			pushOntoScopeStackStack(dec);
			
			success = true;
		}
			
		return success;
	}
	
	private void addTypeName(Dec dec)throws Exception{
		try{
			dec.setTypeName(Type.getTypeName(dec.getFirstToken()));
		}catch(Exception e){
			throw new Exception("Illegal type for dec");
		}
		
	}
	
	/**
	 * Inserts a new identifier into the symbol table
	 * 
	 * @param ident
	 * @param dec
	 */
	private void insertIntoLcTable(String ident, Dec dec){
		
		Stack<SymbolTableObject> lsTableStack = lcTable.get(ident);
		
		if(lsTableStack == null)
			lsTableStack = new Stack<SymbolTableObject>();
		
		lsTableStack.push(new SymbolTableObject(scopeStackStack.peek().getCurrentScope(), dec));
		
		lcTable.put(ident, lsTableStack);
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
		Stack<SymbolTableObject> lcTableIdentStack = lcTable.get(ident);
		if(lcTableIdentStack != null)
			return lcTableIdentStack.peek().getDec();
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

	public Stack<ScopeStack> getScopeStackStack() {
		return scopeStackStack;
	}

	public void setScopeStackStack(Stack<ScopeStack> scopeStackStack) {
		this.scopeStackStack = scopeStackStack;
	}
	
	public HashMap<String, Stack<SymbolTableObject>> getLcTable() {
		return lcTable;
	}

	public void setLcTable(HashMap<String, Stack<SymbolTableObject>> lcTable) {
		this.lcTable = lcTable;
	}

	public HashSet<Integer> getUsedScopeSet() {
		return usedScopeSet;
	}

	public void setUsedScopeSet(HashSet<Integer> usedScopeSet) {
		this.usedScopeSet = usedScopeSet;
	}
}
