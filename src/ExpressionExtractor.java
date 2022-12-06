import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class ExpressionExtractor extends LittleBaseListener {
	private LinkedHashMap<String, Symbol> symbolTables = new LinkedHashMap<String, Symbol>();
	private ArrayList<IRNode> instructions = new ArrayList<IRNode>();
	private int registerCount = 0;		
		
	@Override
	public void exitProgram(LittleParser.ProgramContext ctx) {
		
		HashMap<Integer, String> registers = new HashMap<Integer,String>();
		
		printVariables();
		
		for(IRNode node : instructions) {
			ValTypes nodeType = symbolTables.get(node.id).type;
			char type = nodeType.getLabel();
			
			if(node.instruction.equals(NodeInstruction.write)) {
				System.out.println("sys " + "write" + type + " " + node.id);
			}
			
			else if(node.instruction.equals(NodeInstruction.read)) {
				System.out.println("sys " + "read" + type + " " + node.id);
			}
			
			else if(node.instruction.equals(NodeInstruction.store)) {
				
				int register = registerCount;				
				Symbol symbol = symbolTables.get(node.id);
				symbol.setRegister(register);
				symbol.setValue(node.left);
				
				registers.put(register, node.left);
											
				System.out.println("move " + node.left + " " + "r" + register);
				System.out.println("move " + "r" + register + " " + node.id);
				
				registerCount++;
			}
			else {
				int idRegister = symbolTables.get(node.id).register;
				int leftRegister = -2;
				int rightRegister = -2;
				
				if(symbolTables.containsKey(node.left)) {
					leftRegister = symbolTables.get(node.left).register;
				}
				
				if(symbolTables.containsKey(node.right)) {
					rightRegister = symbolTables.get(node.right).register;
				}			
				
				if(rightRegister == -2 && registers.containsValue(node.right)) {
					for (Entry<Integer, String> register : registers.entrySet()) {
				        if (Objects.equals(register.getValue(), node.right)) {
				            rightRegister = register.getKey();
				            break;
				        }
				    }
				}
				
				if(leftRegister == -2 && registers.containsValue(node.left)) {
					for (Entry<Integer, String> register : registers.entrySet()) {
				        if (Objects.equals(register.getValue(), node.left)) {
				        	leftRegister = register.getKey();
				            break;
				        }
				    }
				}								
				
				if(leftRegister == -1) {
					leftRegister = registerCount++;
					System.out.println("move " + node.left + " " + "r" + leftRegister);
				}
				
				if(idRegister != leftRegister) {
					symbolTables.get(node.id).setRegister(leftRegister);
					symbolTables.get(node.left).setRegister(-1);
				}				
				
				String leftExp = "r" + leftRegister;
				String rightExp = "r" + rightRegister;
				
				if(rightRegister == -1) {
					rightExp = node.right;
				}		
				
				if(node.instruction.equals(NodeInstruction.add)) {
					System.out.println("add" + type + " " + rightExp + " " + leftExp);
				}
				else if(node.instruction.equals(NodeInstruction.sub)) {
					System.out.println("sub" + type + " " + rightExp + " " + leftExp);
				}
				else if(node.instruction.equals(NodeInstruction.div)) {
					System.out.println("div" + type + " " + rightExp + " " + leftExp);
				}
				else if(node.instruction.equals(NodeInstruction.mul)) {
					System.out.println("mul" + type + " " + rightExp + " " + leftExp);
				}
				
				int index = instructions.indexOf(node);
				boolean showMove = false;
				for(int i = index+1; i < instructions.size() ; i++) {
					if(instructions.get(i).id.equals(node.id) && !instructions.get(i).instruction.equals(NodeInstruction.write)) {
						break;
					}
					
					if(instructions.get(i).id.equals(node.id) && instructions.get(i).instruction.equals(NodeInstruction.write)) {
						showMove = true;
					}
				}
				
				if(showMove) {
					System.out.println("move " + leftExp + " " + node.id);
				}
				
			}
		}		
		
		System.out.println("sys halt");
	}
	
	@Override
	public void enterString_decl(LittleParser.String_declContext ctx) { 	
		String stringValue = ctx.getChild(3).getText();
		String stringName = ctx.getChild(1).getText();
		Symbol stringSymbol = new Symbol(ValTypes.STRING, stringName, stringValue, -1);
		
		symbolTables.put(stringName, stringSymbol);
	}
	
	@Override 
	public void enterVar_decl(LittleParser.Var_declContext ctx) { 
		String initialType = ctx.getChild(0).getText();
		ValTypes type = ValTypes.valueOf(initialType);
		String name = ctx.getChild(1).getText();
		String[] listNames = name.split(",");
		
		for(int i=0; i< listNames.length;i++) {
			Symbol symbol = new Symbol(type, listNames[i], null, -1);		
			symbolTables.put(listNames[i], symbol);
		}
	}
	
	@Override 
	public void enterAssign_expr(LittleParser.Assign_exprContext ctx) { 
		String left, right;
		String id = ctx.id().getText();
		ValTypes type = symbolTables.get(id).type;
		NodeInstruction instruction;
		IRNode node;

		String expression = ctx.expr().getText();
		expression = expression.replaceAll("[()]", ""); //remove parenthesis 
		expression = expression.replaceAll(" ", ""); //remove white spaces
		String[] variables = expression.split("['+'|'-'|'*'|'/']");		
		left = variables[0];
		right =  null;
		
		if(variables.length > 1) {
			right = variables[1];
		}	
		
		if(expression.contains("+")) {			
			instruction = NodeInstruction.add;
		}
		else if (expression.contains("-")) {
			variables = expression.split("-");
			left = variables[0];
			right = variables[1];
			instruction = NodeInstruction.sub;
		}
		else if (expression.contains("/")) {
			instruction = NodeInstruction.div;
		}
		else if (expression.contains("*")) {
			instruction = NodeInstruction.mul;
			
		} else {
			left = expression;
			instruction = NodeInstruction.store;
		}
		
		node = new IRNode(instruction, type, id, right, left);
		instructions.add(node);
	}
		
	@Override 
	public void enterRead_stmt(LittleParser.Read_stmtContext ctx) { 
		String expression = ctx.getChild(2).getText();
		
		String[] ids = expression.split(",");
		
		for(int i=0; i< ids.length;i++) {
			ValTypes type = symbolTables.get(ids[i]).type;
			instructions.add(new IRNode(NodeInstruction.read, type, ids[i], null, null));
		}	
	}
	
	@Override 
	public void enterWrite_stmt(LittleParser.Write_stmtContext ctx) { 
		String expression = ctx.getChild(2).getText();
		
		String[] ids = expression.split(",");
		
		for(int i=0; i< ids.length;i++) {
			ValTypes type = symbolTables.get(ids[i]).type;
			instructions.add(new IRNode(NodeInstruction.write, type, ids[i], null, null));
		}
	}
	
	private void printVariables() {
		for(Map.Entry<String, Symbol> currMap : symbolTables.entrySet()) {					
			if(currMap.getValue().type.equals(ValTypes.INT) || currMap.getValue().type.equals(ValTypes.FLOAT)) {
				System.out.println("var " + currMap.getValue().name);
			}
			
			if(currMap.getValue().type.equals(ValTypes.STRING)) {
				System.out.println("str " + currMap.getValue().name + " " + currMap.getValue().value);
			}				
		}
	}
	
	private void printTinyCode() {
		
	}
}
