import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Driver {
	public static void main(String[] args) throws IOException {
		CharStream inputStream = CharStreams.fromFileName("test1.micro");
		
		LittleLexer lexer = new LittleLexer(inputStream);	    
		CommonTokenStream tokens = new CommonTokenStream(lexer);
	    LittleParser parser = new LittleParser(tokens);    
	    
	    ParseTree tree = parser.program();
	    
	    ParseTreeWalker walker = new ParseTreeWalker();
	    walker.walk(new ShortToUnicodeString(), tree);
	    
	  }
}

class ShortToUnicodeString extends LittleBaseListener {

	LinkedHashMap<String, Symbol> symbolTables = new LinkedHashMap<String, Symbol>();
	ArrayList<IRNode> instructions = new ArrayList<IRNode>();
	int registerCount = 0;
	
@Override
public void enterProgram(LittleParser.ProgramContext ctx) {	
	
}

@Override
public void exitProgram(LittleParser.ProgramContext ctx) {
	
	for(Map.Entry<String, Symbol> currMap : symbolTables.entrySet()) {					
		if(currMap.getValue().type.equals("INT") || currMap.getValue().type.equals("FLOAT")) {
			System.out.println("var " + currMap.getValue().name);
		}
		
		if(currMap.getValue().type.equals("STRING")) {
			System.out.println("str " + currMap.getValue().name + " value " + currMap.getValue().value);
		}				
	}
	
	for(IRNode node : instructions) {
	}
	/*stack.pop();
	Scope currTbl;
	
	for(int i=0; i<listSymbolTables.size();i++) {
		currTbl = listSymbolTables.get(i);
		
		if(currTbl.getType() == "GLOBAL") {
			System.out.println("Symbol table GLOBAL");
			
			for(Map.Entry<String, Symbol> currMap : currTbl.getSymbolTable().entrySet()) {					
				if(currMap.getValue().type.equals("INT") || currMap.getValue().type.equals("FLOAT")) {
					System.out.println("name " + currMap.getValue().name + " type " + currMap.getValue().type);
				}
				
				if(currMap.getValue().type.equals("STRING")) {
					System.out.println("name " + currMap.getValue().name + " type " + currMap.getValue().type + " value " + currMap.getValue().value);
				}				
			}
			
		}
		if(currTbl.getType() == "LOCAL") {
			System.out.println();
			System.out.println("Symbol table " + currTbl.getName());
			
			for(Map.Entry<String, Symbol> currMap : currTbl.getSymbolTable().entrySet()) {					
				if(currMap.getValue().type.equals("INT") || currMap.getValue().type.equals("FLOAT")) {
					System.out.println("name " + currMap.getValue().name + " type " + currMap.getValue().type);
				}
				
				if(currMap.getValue().type.equals("STRING")) {
					System.out.println("name " + currMap.getValue().name + " type " + currMap.getValue().type + " value " + currMap.getValue().value);
				}
			}				
		}
		if(currTbl.getType().contains("BLOCK ")){
			System.out.println();
			int blockCount = Integer.parseInt(currTbl.getType().replaceAll("[^0-9]", "")) + 1;
			System.out.println("Symbol table BLOCK " + blockCount);			
			
			for(Map.Entry<String, Symbol> currMap : currTbl.getSymbolTable().entrySet()) {
				if(currMap.getValue().type.equals("INT") || currMap.getValue().type.equals("FLOAT")) {
					System.out.println("name " + currMap.getValue().name + " type " + currMap.getValue().type);
				}
				
				if(currMap.getValue().type.equals("STRING")) {
					System.out.println("name " + currMap.getValue().name + " type " + currMap.getValue().type + " value " + currMap.getValue().value);
				}
			}
			
		}
	}*/
	
}

@Override
public void enterString_decl(LittleParser.String_declContext ctx) { 	
	String stringValue = ctx.getChild(3).getText();
	String stringName = ctx.getChild(1).getText();
	Symbol stringSymbol = new Symbol("STRING", stringName, stringValue);
	
	symbolTables.put(stringName, stringSymbol);
}

@Override 
public void enterVar_decl(LittleParser.Var_declContext ctx) { 
	String type = ctx.getChild(0).getText();
	String name = ctx.getChild(1).getText();
	String[] listNames = name.split(",");
	
	for(int i=0; i< listNames.length;i++) {
		Symbol symbol = new Symbol(type, listNames[i], null);		
		symbolTables.put(listNames[i], symbol);
	}
}
@Override 
public void enterAssign_expr(LittleParser.Assign_exprContext ctx) { 
	String left, right, value;
	String id = ctx.getChild(0).getText();
	String type = symbolTables.get(id).type;
	Instructions instruction;
	IRNode node;
	
	String expression = ctx.getChild(2).getText();
	
	if(expression.contains("+")) {
		left = ctx.getChild(2).getChild(0).getChild(1).getText();
		right = ctx.getChild(2).getChild(1).getChild(1).getText();
		
		if(type.equals("INT")) {
			instruction = Instructions.ADDI;
		} else {
			instruction = Instructions.ADDF;
		}
		
		node = new IRNode(instruction, type, id, right, left);
	}
	else if (expression.contains("-")) {
		left = ctx.getChild(2).getChild(0).getChild(1).getText();
		right = ctx.getChild(2).getChild(1).getChild(1).getText();
		
		if(type.equals("INT")) {
			instruction = Instructions.SUBI;
		} else {
			instruction = Instructions.SUBF;
		}
		
		node = new IRNode(instruction, type, id, right, left);
	}
	else if (expression.contains("/")) {
		left = ctx.getChild(2).getChild(1).getChild(0).getChild(1).getText();
		right = ctx.getChild(2).getChild(1).getChild(1).getText();
		
		if(type.equals("INT")) {
			instruction = Instructions.DIVI;
		} else {
			instruction = Instructions.DIVF;
		}
		
		node = new IRNode(instruction, type, id, right, left);
	}
	else if (expression.contains("*")) {
		left = ctx.getChild(2).getChild(1).getChild(0).getChild(1).getText();
		right = ctx.getChild(2).getChild(1).getChild(1).getText();
		
		if(type.equals("INT")) {
			instruction = Instructions.MULTI;
		} else {
			instruction = Instructions.MULTF;
		}
		
		node = new IRNode(instruction, type, id, right, left);
		
	} else {
		value = expression;

		if(type.equals("INT")) {
			instruction = Instructions.STOREI;
		} else {
			instruction = Instructions.STOREF;
		}
		
		node = new IRNode(instruction, type, id, value);
	}
	
	instructions.add(node);
}

/*@Override
public void enterParam_decl(LittleParser.Param_declContext ctx) {
	String type = ctx.getChild(0).getText();
	String name = ctx.getChild(1).getText();
	
	Scope currScope = stack.peek();
	Symbol symbol = new Symbol(type, name, null);
	currScope.addToSymbolTable(name, symbol);
	
}*/

class Symbol {
	
	String name, type, value; 
	
	public Symbol(String type, String name, String value) {
		this.name = name;
		this.type = type;
		this.value = value;
	}
}

public enum Instructions{
	ADDI, ADDF, SUBI, SUBF, MULTI, MULTF, DIVI, DIVF, STOREI, 
	STOREF, READI, READF, WRITEI, WRITEF, WRITES;
}

class IRNode  {
	
	Instructions instruction;
	String valResult, type;
	String right, left, value;
	
	public IRNode(Instructions instruction, String type, String valResult, String right, String left) {
		this.instruction = instruction;
		this.type = type;
		this.valResult = valResult;
		this.right = right;
		this.left = left;
	}
	
	public IRNode(Instructions instruction, String type, String valResult, String value) {
		this.instruction = instruction;
		this.type = type;
		this.valResult = valResult;
		this.value = value;
	}
	
}
}
