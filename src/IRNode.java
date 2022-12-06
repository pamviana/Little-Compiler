
public class IRNode {
	NodeInstruction instruction;
	String id, right, left;
	ValTypes type;
	
	public IRNode(NodeInstruction instruction, ValTypes type, String id, String right, String left) {
		this.instruction = instruction;
		this.type = type;
		this.id = id;
		this.right = right;
		this.left = left;
	}
	
	public IRNode(String right, String left) {
		this.right = right;
		this.left = left;
	}
}
