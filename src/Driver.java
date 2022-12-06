import java.io.IOException;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Driver {
	public static void main(String[] args) throws IOException {
		CharStream inputStream = CharStreams.fromFileName("test2.micro");
		
		LittleLexer lexer = new LittleLexer(inputStream);	    
		CommonTokenStream tokens = new CommonTokenStream(lexer);
	    LittleParser parser = new LittleParser(tokens);    
	    
	    ParseTree tree = parser.program();
	    
	    ParseTreeWalker walker = new ParseTreeWalker();
	    walker.walk(new ExpressionExtractor(), tree);
	    
	  }
}
