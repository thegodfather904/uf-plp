
package cop5556sp17;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.Program;

public class CodeGenVisitorTest {

	static final boolean doPrint = true;
	static void show(Object s) {
		if (doPrint) {
			System.out.println(s);
		}
	}

	boolean devel = false;
	boolean grade = true;
	
	
	@Test
	public void myTests() throws Exception {
		String filterOpChain = "tp url u, file fn{frame fr image i integer x u -> i -> blur -> fr -> show;}";
		
//		String integerChain = "tp url u, file fn{integer x integer y x <- 7;  x -> y; integer test test <- y;}";
//		String frameOpChain = "tp url u, file fn{frame fr image i integer x u -> i -> fr -> show -> move(1,2) -> yloc -> x;  integer y y <- x;}";
//		String paramDecTest = "emptyProg boolean x, integer z{boolean bool bool <- false; x <- bool;}";
//		String scoping = "emptyProg integer x{integer x x <- 10; integer y y <- 11; integer z z <- 12; if(x == 10){ integer x x <- 999; if(y==11){z <-x;} } y <- x;}";
//		String input = "emptyProg integer x{integer y integer count count <- 0; while(count < 10){y <- count + 1; count <- count + 1;} y <- 2;}";	
//		String simpleTest = "emptyProg integer x{integer y y <- 12;}";	
		Scanner scanner = new Scanner(filterOpChain);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);
		
		//generate code
		CodeGenVisitor cv = new CodeGenVisitor(devel,grade,null);
		byte[] bytecode = (byte[]) program.visit(cv, null);
		
		//output the generated bytecode
		CodeGenUtils.dumpBytecode(bytecode);
		
		//write byte code to file 
		String name = ((Program) program).getName();
		String classFileName = "bin/" + name + ".class";
		OutputStream output = new FileOutputStream(classFileName);
		output.write(bytecode);
		output.close();
		System.out.println("wrote classfile to " + classFileName);
		
		// directly execute bytecode
		String[] args = new String[] {"http://prod.static.jaguars.clubs.nfl.com/nfl-assets/img/gbl-ico-team/JAX/logos/home/large.png", 
				"/home/tony/Desktop_Folder/UF/PLP/uf-plp/large.png"} ;
		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
		instance.run();
		System.out.println("END!");
	}

	@Test
	public void emptyProg() throws Exception {
		//scan, parse, and type check the program
//		String progname = "emptyProg";
//		String input = progname + "  {}";		
//		Scanner scanner = new Scanner(input);
//		scanner.scan();
//		Parser parser = new Parser(scanner);
//		ASTNode program = parser.parse();
//		TypeCheckVisitor v = new TypeCheckVisitor();
//		program.visit(v, null);
//		show(program);
//		
//		//generate code
//		CodeGenVisitor cv = new CodeGenVisitor(devel,grade,null);
//		byte[] bytecode = (byte[]) program.visit(cv, null);
//		
//		//output the generated bytecode
//		CodeGenUtils.dumpBytecode(bytecode);
		
		//write byte code to file 
//		String name = ((Program) program).getName();
//		String classFileName = "bin/" + name + ".class";
//		OutputStream output = new FileOutputStream(classFileName);
//		output.write(bytecode);
//		output.close();
//		System.out.println("wrote classfile to " + classFileName);
		
		// directly execute bytecode
//		String[] args = new String[0]; //create command line argument array to initialize params, none in this case
//		Runnable instance = CodeGenUtils.getInstance(name, bytecode, args);
//		instance.run();
	}


	@Before
	public void initLog(){
		if (devel || grade) PLPRuntimeLog.initLog();
	}
	
	@After
	public void printLog(){
		System.out.println(PLPRuntimeLog.getString());
	}
	
}
