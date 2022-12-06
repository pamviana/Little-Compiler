
public enum ValTypes {
	STRING('s'), INT('i'), FLOAT('r');
	
	private char label;
	
	ValTypes(char label){
		this.label = label;
	}
	
	public char getLabel() {
		return label;
	}
}
