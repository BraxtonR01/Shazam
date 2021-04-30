//Holds Information For Instructions
public class Instructions {

	public String name;
	public char level;
	public String operand;
	public int location;
	
	//Constructor
	public Instructions(String nameIn, char levelIn, String opIn, int locIn){
		name = nameIn;
		level = levelIn;
		operand = opIn;
		location = locIn;
	}
	
	//Override toString
	public String toString() {
		return " " + name + "      " + level + "            " + operand;
	}
}
