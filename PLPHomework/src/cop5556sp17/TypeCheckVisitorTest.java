/**  Important to test the error cases in case the
 * AST is not being completely traversed.
 * 
 * Only need to test syntactically correct programs, or
 * program fragments.
 */

package cop5556sp17;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.Statement;
import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;

public class TypeCheckVisitorTest {
	

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test 
	public void myTest() throws Exception {
		String testArrowArrow2Error = "p integer a, integer b {image img1 image img2 if(img1 != img2) {image a a <- img1; } if(a != b) {boolean a a <- img1 != img2; }}";
//		String simpleChain = "testProgram {image x image x x -> blur;}";
//		String ifWhile = "testProgram {integer x integer y while(x == y){integer x integer testScope x <- 12;} while(x == y){y <- 12;}}";
//		String expression = "testProgram {integer test integer test2 integer test3 test <- test3 == test2;}";
//		String input = "test url test2, url test3 {test <- 10 + 7;}";
//		String input = "test url test2, url test3 {if(a < 12){integer test} if(a < 12){integer test}}";
		Scanner scanner = new Scanner(testArrowArrow2Error);
		scanner.scan();
		Parser parser = new Parser(scanner);
		Program program = parser.parse();
		TypeCheckVisitor v = new TypeCheckVisitor();
		program.visit(v, null);	
	}
	
	
//	@Test
//	public void testAssignmentBoolLit0() throws Exception{
//		String input = "p {\nboolean y \ny <- false;}";
//		Scanner scanner = new Scanner(input);
//		scanner.scan();
//		Parser parser = new Parser(scanner);
//		Program program = parser.parse();
//		TypeCheckVisitor v = new TypeCheckVisitor();
//		program.visit(v, null);		
//	}
//
//	@Test
//	public void testAssignmentBoolLitError0() throws Exception{
//		String input = "p {\nboolean y \ny <- 3;}";
//		Scanner scanner = new Scanner(input);
//		scanner.scan();
//		Parser parser = new Parser(scanner);
//		ASTNode program = parser.parse();
//		TypeCheckVisitor v = new TypeCheckVisitor();
//		thrown.expect(TypeCheckVisitor.TypeCheckException.class);
//		program.visit(v, null);		
//	}		



}