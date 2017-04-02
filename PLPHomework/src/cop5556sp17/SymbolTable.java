package cop5556sp17;



import java.util.HashMap;

import cop5556sp17.AST.Dec;


public class SymbolTable {
	
	
	//TODO  add fields
	private HashMap<String, SymbolTableObject> lcTable = new HashMap<String, SymbolTableObject>();
	private int currentScope = 0;
	
	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		//TODO:  IMPLEMENT THIS
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		//TODO:  IMPLEMENT THIS
	}
	
	
	/*True if insert was successfull, false if duplicate record*/
	public boolean insert(String ident, Dec dec){
		//TODO:  IMPLEMENT THIS
		return true;
	}
	
	public Dec lookup(String ident){
		//TODO:  IMPLEMENT THIS
		return null;
	}
		
	public SymbolTable() {
		//TODO:  IMPLEMENT THIS
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


	public int getCurrentScope() {
		return currentScope;
	}


	public void setCurrentScope(int currentScope) {
		this.currentScope = currentScope;
	}
}
