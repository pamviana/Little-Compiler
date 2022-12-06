
public class Symbol {
	String name, value; 
	ValTypes type;
	int register;
	
	public Symbol(ValTypes type, String name, String value, int register) {
		this.name = name;
		this.type = type;
		this.value = value;
		this.register = register;
	}
	
	public void setRegister(int register) {
		this.register = register;
	}
	
	public void setValue(String value) {
		this.value = value;
	}	
	
}
