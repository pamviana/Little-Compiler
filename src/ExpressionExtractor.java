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
		
		HashMap<String, RegisterInfo> registers = new HashMap<String,RegisterInfo>();
		
		printVariables();
		
		for(IRNode node : instructions) {
			ValTypes nodeType = symbolTables.get(node.id).type;
			char type = nodeType.getLabel();
			
			if(node.instruction.equals(NodeInstruction.write)) {
				System.out.print("sys " + "write" + type);
				if(!symbolTables.get(node.id).type.equals(ValTypes.STRING)) {
					System.out.println(" r" + symbolTables.get(node.id).register);
				} else {
					System.out.println(" " + node.id);
				}				
			}
			
			else if(node.instruction.equals(NodeInstruction.read)) {
				int newReg = registerCount++;
				RegisterInfo newId = new RegisterInfo(newReg, null);
				registers.put(node.id, newId);
				symbolTables.get(node.id).setRegister(newReg);
				System.out.println("sys " + "read" + type + " " + "r"+ newReg);
			}
			
			else if(node.instruction.equals(NodeInstruction.store)) {
				
				int register = registerCount++;				
				RegisterInfo newId = new RegisterInfo(register, node.left);
				Symbol symbol = symbolTables.get(node.id);
				symbol.setRegister(register);
				symbol.setValue(node.left);
				
				registers.put(node.id, newId);
											
				System.out.println("move " + node.left + " " + "r" + register);
			}
			else {
				if(!registers.containsKey(node.id)) {
					int register = registerCount++;				
					RegisterInfo newId = new RegisterInfo(register, null);
					registers.put(node.id, newId);
				}
				if(!registers.containsKey(node.left)) {
					int register = registerCount++;				
					RegisterInfo newId = new RegisterInfo(register, null);
					registers.put(node.left, newId);
				}
				if(!registers.containsKey(node.right)) {
					int register = registerCount++;				
					RegisterInfo newId = new RegisterInfo(register, null);
					registers.put(node.left, newId);
				}
								
				int idRegister = registers.get(node.id).register;
				int leftRegister = registers.get(node.left).register;
				int rightRegister = registers.get(node.right).register;
				
				if(symbolTables.containsKey(node.id)) {
					symbolTables.get(node.id).setRegister(idRegister);
				}
												
				boolean isLeftUsedFurther = false;				
								
				if(!node.id.equals(node.left)) {
					int index = instructions.indexOf(node);
					for(int i = index+1; i < instructions.size() ; i++) {
						if(instructions.get(i).left != null && instructions.get(i).right != null) {
							if(instructions.get(i).left.equals(node.left) || instructions.get(i).right.equals(node.left)) {
								System.out.println("move " + "r" + leftRegister + " r" + idRegister);
								isLeftUsedFurther = true;
								break;
							}
						}
						
						if(instructions.get(i).id.equals(node.left) && instructions.get(i).instruction.equals(NodeInstruction.write)) {
							System.out.println("move " + "r" + leftRegister + " r" + idRegister);
							isLeftUsedFurther = true;
							break;
						}
					}
				}	
				
				
				if(node.id.equals(node.right) || !isLeftUsedFurther) {
					idRegister = leftRegister;
					symbolTables.get(node.id).setRegister(idRegister);
					registers.get(node.id).setRegister(idRegister);
				}
				
				String leftExp = "r" + leftRegister;
				String rightExp = "r" + rightRegister;
				String idExp = "r" + idRegister;
				
				/*if(rightRegister == -1) {
					rightExp = node.right;
				}		*/
				
				if(node.instruction.equals(NodeInstruction.add)) {
					System.out.println("add" + type + " " + rightExp + " " + idExp);
				}
				else if(node.instruction.equals(NodeInstruction.sub)) {
					System.out.println("sub" + type + " " + rightExp + " " + idExp);
				}
				else if(node.instruction.equals(NodeInstruction.div)) {
					System.out.println("div" + type + " " + rightExp + " " + idExp);
				}
				else if(node.instruction.equals(NodeInstruction.mul)) {
					System.out.println("mul" + type + " " + rightExp + " " + idExp);
				}
				
				
				/*registers.get(node.left).setRegister(-1);
				registers.get(node.id).setRegister(leftRegister);*/
				
				
				
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
			/*if(currMap.getValue().type.equals(ValTypes.INT) || currMap.getValue().type.equals(ValTypes.FLOAT)) {
				System.out.println("var " + currMap.getValue().name);
			}*/
			
			if(currMap.getValue().type.equals(ValTypes.STRING)) {
				System.out.println("str " + currMap.getValue().name + " " + currMap.getValue().value);
			}				
		}
	}
	
	private void printTinyCode() {
		
	}
}
