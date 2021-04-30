import java.util.LinkedList; //Import LinkedLists

//Holds All Assembler Information
public class Procedure {

	public String name;
	public LinkedList<Identifier> ID;
	public LinkedList<String> Var;
	public LinkedList<Instructions> Inst;
	private boolean finished;
	
	//Constructor
	public Procedure(String nameIn) {
		name = nameIn;
		ID = new LinkedList<Identifier>();
		Var = new LinkedList<String>();
		Inst = new LinkedList<Instructions>();
		finished = false;
	}
	
	//Ends the procedure so it wont be executed again
	public void End() {
		finished = true;
	}
	
	//Checks if procedure is finished
	public boolean isFinished() {
		return finished;
	}

}
