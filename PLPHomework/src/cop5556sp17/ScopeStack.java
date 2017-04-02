package cop5556sp17;

import java.util.Stack;

import cop5556sp17.AST.Dec;

public class ScopeStack {

	private int currentScope;
	private Stack<Dec> decStack;
	
	public ScopeStack(){
		decStack = new Stack<Dec>();
	}
	
	public ScopeStack(int currentScope){
		this.currentScope = currentScope;
		decStack = new Stack<Dec>();
	}
	
	public int getCurrentScope() {
		return currentScope;
	}
	public void setCurrentScope(int currentScope) {
		this.currentScope = currentScope;
	}

	public Stack<Dec> getDecStack() {
		return decStack;
	}

	public void setDecStack(Stack<Dec> decStack) {
		this.decStack = decStack;
	}
	
}
