package cop5556sp17;

import cop5556sp17.AST.Dec;

public class SymbolTableObject {
	
	private int scopeNumber;
	private Dec dec;
	
	public SymbolTableObject(){
		
	}
	
	public SymbolTableObject(int scopeNumber, Dec dec){
		this.scopeNumber = scopeNumber;
		this.dec = dec;
	}
	
	public int getScopeNumber() {
		return scopeNumber;
	}
	public void setScopeNumber(int scopeNumber) {
		this.scopeNumber = scopeNumber;
	}

	public Dec getDec() {
		return dec;
	}

	public void setDec(Dec dec) {
		this.dec = dec;
	}
	
}
