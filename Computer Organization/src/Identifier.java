//Hold Information For Identifiers
public class Identifier {
	
	public String name;
	public int level;
	public int location;
	
	//Constructor
	public Identifier(String nameIn, int levelIn, int locIn) {
		name = nameIn;
		level = levelIn;
		location = locIn;
	}
	
	//Override toString
	public String toString() {
		String hexLoc;
		hexLoc = Integer.toHexString(location);
		while(hexLoc.length()<3) {
			hexLoc = "0" + hexLoc;
		}
		return(" " + name + "     " + hexLoc.toUpperCase() + "          000" + level);
	}
}
