package cop5556sp17;

import cop5556sp17.AST.ASTNode;

public class SymbolTableObject {
	
	private int scopeNumber;
	private ASTNode node;
	
	public SymbolTableObject(){
		
	}
	
	public SymbolTableObject(int scopeNumber, ASTNode node){
		this.scopeNumber = scopeNumber;
		this.node = node;
	}
	
	public int getScopeNumber() {
		return scopeNumber;
	}
	public void setScopeNumber(int scopeNumber) {
		this.scopeNumber = scopeNumber;
	}
	public ASTNode getNode() {
		return node;
	}
	public void setNode(ASTNode node) {
		this.node = node;
	}
	
}
