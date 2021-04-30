//Created By Braxton Rolle For CSCI 270 2021
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//Simulates basic CPU functions
//Run clear to start program
//Then load a file
//Run interpreter or assembler depending on type of file
//Machine code files need the assembler, raw bit files need the interpreter
//These produce their own output files, but dump can be called to see the full 'memory' and registers
//All input and output files should be in the same directory as the .java file
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

import javax.swing.JFileChooser; //Import FIle Chooser
import java.io.File; //Import the File class
import java.io.FileNotFoundException; //Input exceptions for file handling
import java.io.IOException; //Import the IOException class to handle errors
import java.io.FileWriter;  //Import the FileWriter class
import java.io.BufferedWriter; //Import the BufferedWriter class
import java.util.*;  //Extra various imports
import javax.swing.*; //Extra various imports

//Main class (only extends JPanel for the JFileChooser)
public class SHAZAM extends JPanel{

	//Initialization of global Variables
	//Registers
	private int basePointer;
	private int programCounter;
	private int topOfStack;
	private int ir;
	private int r1;
	private int r2;

	//Arrays simulating memory
	private int[] instructions = new int[1024];
	private int[] data = new int[2048];
	
	//Output for QuietTrace
	private String quietOutput = "";

	//Linked Lists to hold dynamic amounts of data
	private LinkedList<String> AssemblerInst;
	private LinkedList<Procedure> Assembly;
	private LinkedList<Integer> Input;
	
	//Main Method
	public static void main(String[] args){

		//Initialization of menu variables and user input
		SHAZAM shazam = new SHAZAM();
		boolean isRunning = true;
		int userInput = 0;
		Scanner scnr = new Scanner(System.in);
		
		//Running menu loop
		while(isRunning == true) {
			
			//Menu Selection
			System.out.println("Menu");
			System.out.println("~~~~~");
			System.out.println("1. Clear");
			System.out.println("2. Dump");
			System.out.println("3. Load");
			System.out.println("4. Interpreter");
			System.out.println("5. Assembler");
			System.out.println("6. Exit");
			
			//Switch statement for menu options
			userInput = scnr.nextInt();
			switch(userInput) {
			case 1:
				shazam.Clear();
				break;
				
			case 2:
				shazam.Dump();
				break;
				
			case 3:
				File file = LoadFile();
				
				//Verifying that File is not null
				if(file != null) {
					shazam.Load(file);
				}
				else {
					System.out.println("Error Loading File");
				}
				break;
			case 4:
				shazam.Interpreter();
				break;
			case 5:
				File file2 = LoadFile();
				//Verifying that File is not null

				if(file2 != null) {
					shazam.Assembler(file2);
				}
				else {
					System.out.println("Error Loading File");
				}
				break;
			case 6:
			
				//End
				isRunning = false;
				break;
				
			default:
				System.out.println("Command Not Recognized");
				break;
				
			}

		}
		scnr.close();
	}
	//Shazam Clear
	public void Clear() {
		//Setting registers to 0
		basePointer = 000;
		programCounter = 000;
		topOfStack = 000;
		ir = 0;
		r1 = 0;
		r2 = 0;
		
		//Setting instructions array to 63000 hex, which stops the interpreter when encountered
		for(int x = 0; x<1024; x++) {
			instructions[x] = 405504;
		}
		
		//Setting data array to 0
		for(int x = 0; x<2048; x++) {
			data[x] = 0;
		}
		
		//Defining and Clearing linked lists
		Input = new LinkedList<Integer>();
		AssemblerInst = new LinkedList<String>();
		Assembly = new LinkedList<Procedure>();
		Input.clear();
		AssemblerInst.clear();
		Assembly.clear();
		
		//Clearing output
		quietOutput = "";
		
		System.out.println("Data and Instructions Cleared");
	}
	//Shazam Dump
	public void Dump() {
		
		//Creating a new file if needed
		File file = new File("Dump.txt");
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			
			//Initializing variables for file outputting
			FileWriter fw = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fw);
			
			//Instructions output
			out.write("Memory Dump: Instructions");
			out.newLine();
			
			//Writing the array locations
			for(int x = 0; x<64; x++) {
				if(x<16) {
					out.write("0");
				}
				out.write(Integer.toHexString(x).toUpperCase() + "0 ");
				for(int y = 0; y<16; y++) {
					
					//Converting data to hex for output
					String output = Integer.toHexString(instructions[(x*16) + y]).toUpperCase();
					
					//Formatting output to correct length
					while(output.length()<5) {
						output = "0" + output;
					}
					out.write(output + " ");
				}
				out.newLine();
			}
			out.write("End Memory Dump: Instructions");
			out.newLine();
			out.newLine();
			
			//Register output
			//Converting registers to hex
			String bpOut = Integer.toHexString(basePointer);
			String pcOut = Integer.toHexString(programCounter);
			String topOut = Integer.toHexString(topOfStack);
			
			//Formatting register output
			while(bpOut.length()<3) {
				bpOut = "0" + bpOut; 
			}
			while(pcOut.length()<3) {
				pcOut = "0" + pcOut; 
			}
			while(topOut.length()<3) {
				topOut = "0" + topOut; 
			}
			out.write("CPU Dump: Registers   B = " + bpOut + " T = " + topOut + " P = " + pcOut);
			out.newLine();
			
			//Data output
			out.write("Memory Dump: Data");
			out.newLine();
			
			//Writing the array locations
			for(int x = 0; x<128; x++) {
				if(x<16) {
					out.write("0");
				}
				
				//Converting data to hex for output
				out.write(Integer.toHexString(x).toUpperCase() + "0 ");
				for(int y = 0; y<16; y++) {
					String output = Integer.toHexString(data[(x*16) + y]).toUpperCase();
					
					//Formatting output to correct length
					while(output.length()<4) {
						output = "0" + output;
					}
					out.write(output + " ");
				}
				out.newLine();
			}
			out.write("End Memory Dump: Data");
			out.newLine();
			out.newLine();
			
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
			System.out.println("Dump Finished");
	}
	//Shazam Load
	public void Load(File file) {
		
		//Initializing a Scanner
		Scanner scnr = null;
		
		//Creating a new file if needed
		try {
			scnr = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//Runs while scanner still has values to read
		while(scnr.hasNext()) {
			
		//Initialization of tracking variables
		char type;
		String first;
		String baseMemory;
		int firstLocation;
		int dataLength = 0;
	
		first = scnr.next();
		
		//Check for end-of-file 'user input'
		if(first.length()>1) {
			UserInput(first);
			System.out.println("Load Finished");
			return;
		}
		
		//Determines type of the line just read in
		type = first.charAt(0);
		
		//Sets correct length for the data (4 for memory data, 5 for instructions)
		if(type == 'I') {
			dataLength = 5;
		}else if(type == 'D') {
			dataLength = 4;
		}else {
			return;
		}
		
		//Finds correct starting point for data storage
		baseMemory = scnr.next();
		firstLocation = Integer.parseInt(baseMemory, 16);
		
		//More variables for parsing the string
		String baseValues;
		int numValues;
		String endLine = "";
		endLine = scnr.nextLine();
		
		//Determines correct spacing
		baseValues = endLine.charAt(1) + "";
		if(type == 'D'){
			baseValues += endLine.charAt(2);
		}
		
		//Number of values being read in for current line
		numValues = Integer.parseInt(baseValues, 16);
	
		//Sets the correct register to the correct position based on data type if there are 0 values in the line
		if(numValues == 0) {
			if(type == 'D') {
				topOfStack = Integer.parseInt(baseMemory, 16);
			}else {
				programCounter = Integer.parseInt(baseMemory, 16);
			}
		}
		
		//Runs if there are values to be read in on the current line
		else {
			int counter = 3;
			int x = 0;
			while(x<numValues) {
				String baseData = "";
				
				//Parses up the string depending on which data type it is
				for(int y = 0; y<dataLength; y++) {
					baseData += endLine.charAt(counter);
					counter++;
				}
				
				//Stores in either data memory or instruction memory based on data type
				if(type == 'D') {
					data[firstLocation] = Integer.parseInt(baseData, 16);
				}else if(type == 'I') {
					instructions[firstLocation] = Integer.parseInt(baseData, 16);
				}
				
				firstLocation++;
				x++;
			}
		}
		
	}
		System.out.println("Load Finished");
	}
	//Handle File Loading
	public static File LoadFile() {
		
		//Initialize a file and FileChooser
		File file = null;
		JFileChooser fc = new JFileChooser();
		
		//Allow user to select files and directories
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		//Open dialog box
		int returnVal = fc.showOpenDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			
			//Return selected file
			file = fc.getSelectedFile();
		}
		return file;
	}
	//End Of File Input Handling
	public void UserInput(String input) {
		
		//Initializing variables
		Input = new LinkedList<Integer>();
		String sub;
		
		//Loops depending on how long the final string is
		for(int x = 0; x<(input.length()/4); x++) {
			
			//Parses up final string into individual integers and then stores into a LinkedList for use later
			sub = input.substring((4*x), ((4*x) + 4));
			Input.add(Integer.parseInt(sub, 16));
			
			//Ends if an input is recorded twice
			if(!Input.contains(Integer.parseInt(sub, 16))) {
				return;
			}
		}
	
	}
	//Shazam Interpreter
	public void Interpreter() {
		
		//Declaration of parsing variables
		String sVal;
		int opcode;
		int level;
		int address;
		String addressS;
		char levelS;
		boolean cont = true;
		String printString;
		
		//Creation of Trace File
		File file = new File("Trace.txt");
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			
			//initializing variables for file outputting
			FileWriter fw = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fw);
	
			//Top of file output
			out.write("Interpreter --- Begin at location " + ConvertHex(programCounter, 3) + " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3));
			out.newLine();
			out.write("Full Trace");
			out.newLine();
	
		//Main loop for following the stack
		while(cont == true) {
			
			//Reading and sorting the codes from the instructions
			r1 = instructions[programCounter];
			sVal = Integer.toHexString(r1);
			while(sVal.length() < 5) {
				sVal = "0" + sVal;
			}
			opcode = Character.getNumericValue(sVal.charAt(0));
			level = Character.getNumericValue(sVal.charAt(1));
			levelS = sVal.charAt(1);
			addressS = sVal.substring(2);
			address = Integer.parseInt(sVal.substring(2), 16);
			printString = ConvertHex(programCounter, 3) + ": " + sVal.toUpperCase();
			
			//opcode switch statement
			switch(opcode) {
			case 0:
				//LIT
				//Interpret the address field as an 11-bit 2’s complement number. Push that value onto the stack
				topOfStack++;
				data[topOfStack] = FindTwos(addressS);
				printString += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(data[topOfStack], 4);
				break;
				
			case 1:
				//OPR
				//Interpret the address field as a number indicating the operation to be performed from the OPR Opcode table
				printString = OPROpcode(level, address, printString);
				break;
				
			case 2:
				//LOD
				//Push the value at the effective address, computed by the level and address fields, onto the stack
				topOfStack++;
				r1 = data[FindTwos(addressS) + basePointer];
				data[topOfStack] = r1;
				printString += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(data[basePointer + FindTwos(addressS)],4);
				break;
				
			case 3:
				//STO
				//Pop the top of the stack and store it at the effective address, computed by the level and address fields
				r1 = data[topOfStack];
				topOfStack--;
				data[basePointer + FindTwos(addressS)] = r1;
				printString += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex((FindTwos(addressS) + basePointer), 3) + "] <-- " + ConvertHex(data[basePointer + FindTwos(addressS)],4);
				break;
				
			case 4:
				//CAL
				//Call the function at the given address
				r1 = basePointer; //Old Base
				r2 = programCounter + 1; //Old Counter
				programCounter = FindTwos(addressS) - 1;
				basePointer = topOfStack + 1;
				topOfStack++;
				data[topOfStack] = r1;
				topOfStack++;
				data[topOfStack] = r1;
				topOfStack++;
				data[topOfStack] = r2;
				printString += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(basePointer, 3) + "] <-- " + ConvertHex(data[basePointer], 4) + " DATA[" + ConvertHex((basePointer + 1), 3) + "] <-- " + ConvertHex(data[basePointer], 4)  + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(data[topOfStack], 4);
				break;
				
			case 5:
				//INT
				//Interpret the address field as an 11-bit 2’s complement number. Increment or decrement the value in T by the amount specified in the address field
				r1 = FindTwos(addressS);
				topOfStack += r1;
				printString += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3);
				break;
				
			case 6:
				//JMP
				switch(level) {
				case 0:
					//JPU
					//Unconditionally fetch the next instruction from the provided address field value
					programCounter = Integer.parseInt(addressS, 16) - 1;
					printString += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3);
					break;
					
				case 1:
					//JPC
					//Pop the top of the stack. If that popped value is not 0, fetch the next instruction from the instruction’s address field. If that popped value is 0, fetch the next instruction as normal
					r1=data[topOfStack];
					topOfStack--;
					if(r1 != 0) {
						programCounter = Integer.parseInt(addressS, 16) - 1;
					}
					printString += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3);
					break;
					
				case 2: 
					//JPT
					//Pop the top of the stack and place that value in P
					r1 = data[topOfStack];
					topOfStack--;
					programCounter = r1;
					printString += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3);
					break;
					
				case 3:
					//HLT
					//Load P with the instruction’s address field and halt
					programCounter = Integer.parseInt(addressS);
					cont = false;
					printString += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3);
					break;
					
				default:
					System.out.println("Error reading JMP level (opcode 6)");
						break;
						
				}
				break;
				
			case 7:
				//ADR
				//Treat the combined level and address fields as a four-digit memory address. Push this address onto the stack
				String push = levelS + addressS;
				topOfStack++;
				data[topOfStack] = Integer.parseInt(push, 16);
				printString += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(data[topOfStack], 4);
				break;
				
			default:
				System.out.println("Error Reading at " + programCounter);
				break;
			}
			//incrementing the program counter and outputting to the trace file
			programCounter++;
			out.write(printString);
			out.newLine();
		}
		out.write("Program Completed");
		out.close();
		QuietTrace('e', 0);
		System.out.println("Intepreter Finished");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	//OPR Opcode switch handler
	public String OPROpcode(int level, int address, String printS) throws IOException {
			
		switch(address) {
		case 0:
			//RET
			//Return from a function call
			basePointer++;;
			r2 = data[basePointer];
			basePointer++;
			r1 = data[basePointer];
			programCounter = r1-1;
			basePointer = r2;
			topOfStack=topOfStack-5;
			printS += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3);
			return printS;
			
		case 1:
			//NEG
			//Negate the contents at the top of the stack
			data[topOfStack] = 0;
			topOfStack--;
			printS += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3);
			return printS;
			
		case 2:
			//ADD
			//Add the top two items on the stack
			r1 = data[topOfStack];
			topOfStack--;
			r2 = data[topOfStack];
			topOfStack--;
			r1= r2+r1;
			topOfStack++;
			data[topOfStack] = r1;
			printS += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(data[topOfStack], 4);
			return printS;
			
		case 3:
			//SUB
			//Subtract the top item on the stack from the second from the top item
			r1 = data[topOfStack];
			topOfStack--;
			r2 = data[topOfStack];
			topOfStack--;
			r1= r2-r1;
			topOfStack++;
			data[topOfStack] = r1;
			printS += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(data[topOfStack], 4);
			return printS;
			
		case 4:
			//MUL
			//Multiply the top two items on the stack
			r1 = data[topOfStack];
			topOfStack--;
			r2 = data[topOfStack];
			topOfStack--;
			r1= r2*r1;
			topOfStack++;
			data[topOfStack] = r1;
			printS += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(data[topOfStack], 4);
			return printS;
			
		case 5:
			//DIV
			//Divide the second item from the top of the stack by the item on the top of the stack. Produce only an integer quotient
			r1 = data[topOfStack];
			topOfStack--;
			r2 = data[topOfStack];
			topOfStack--;
			r1= r2/r1;
			topOfStack++;
			data[topOfStack] = r1;
			printS += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(data[topOfStack], 4);
			return printS;
			
		case 6:
			//DUP
			//Push a copy of the item, at the top of the stack, onto the stack
			r1 = data[topOfStack];
			topOfStack++;
			data[topOfStack] = r1;
			printS += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(topOfStack-1, 3) + "] <-- " + ConvertHex(data[topOfStack-1], 4) + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(data[topOfStack], 4);
			return printS;
			
		case 7:
			//EQL
			//Push a 1 onto the stack, if the top two items are equal. Otherwise, push a 0
			r1 = FindDataTwos(ConvertHex(data[topOfStack], 4));
			topOfStack--;
			r2 = FindDataTwos(ConvertHex(data[topOfStack], 4));
			if(r1 == r2) {
				data[topOfStack] = 1;
			}else {
				data[topOfStack] = 0;
			}
			printS +=(" B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(data[topOfStack], 4));
			return printS;
			
		case 8:
			//NEQ
			//Push a 1 onto the stack, if the top two items are not equal. Otherwise, push a 0
			r1 = FindDataTwos(ConvertHex(data[topOfStack], 4));
			topOfStack--;
			r2 = FindDataTwos(ConvertHex(data[topOfStack], 4));
			if(r1 != r2) {
				data[topOfStack] = 1;
			}else {
				data[topOfStack] = 0;
			}
			printS +=(" B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(data[topOfStack], 4));
			return printS;
			
		case 9:
			//LSS
			//Push a 1 onto the stack, if the second item from the top of the stack is less than the item at the top. Otherwise, push a 0
			r1 = FindDataTwos(ConvertHex(data[topOfStack], 4));
			topOfStack--;
			r2 = FindDataTwos(ConvertHex(data[topOfStack], 4));
			if(r1 > r2) {
				data[topOfStack] = 1;
			}else {
				data[topOfStack] = 0;
			}
			printS +=(" B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(data[topOfStack], 4));
			return printS;
			
		case 10:
			//LEQ
			//Push a 1 onto the stack, if the second item from the top of the stack is less than or equal to the item at the top. Otherwise, push a 0
			r1 = FindDataTwos(ConvertHex(data[topOfStack], 4));
			topOfStack--;
			r2 = FindDataTwos(ConvertHex(data[topOfStack], 4));
			if(r1 >= r2) {
				data[topOfStack] = 1;
			}else {
				data[topOfStack] = 0;
			}
			printS +=(" B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(data[topOfStack], 4));
			return printS;
			
		case 11:
			//GEQ
			//Push a 1 onto the stack, if the second item from the top of the stack is greater than or equal to the item at the top. Otherwise, push a 0
			r1 = FindDataTwos(ConvertHex(data[topOfStack], 4));
			topOfStack--;
			r2 = FindDataTwos(ConvertHex(data[topOfStack], 4));
			if(r1 <= r2) {
				data[topOfStack] = 1;
			}else {
				data[topOfStack] = 0;
			}
			printS +=(" B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(data[topOfStack], 4));
			return printS;
			
		case 12:
			//GTR
			//Push a 1 onto the stack, if the second item from the top of the stack is greater than the item at the top. Otherwise, push a 0
			r1 = FindDataTwos(ConvertHex(data[topOfStack], 4));
			topOfStack--;
			r2 = FindDataTwos(ConvertHex(data[topOfStack], 4));
			if(r1 < r2) {
				data[topOfStack] = 1;
			}else {
				data[topOfStack] = 0;
			}
			printS +=(" B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(data[topOfStack], 4));
			return printS;
			
		case 13:
			//GET
			//Retrieve a four-digit value from the end of the Loader input file. Push this value onto the top of the stack
			//Simulates 'user input'
			topOfStack++;
			data[topOfStack] = Input.get(ir);
			QuietTrace('g', Input.get(ir));
			printS += " B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(Input.get(ir), 4) + "\nGet -------> " + ConvertHex(Input.get(ir), 4);
				if(ir<Input.size()-1) {
					ir++;
				}
			return printS;
			
		case 14:
			//PUT
			//Pop the item at the top of the stack and display it as the next line in the trace output
			r1 = data[topOfStack];
			topOfStack--;
			QuietTrace('p', r1);
			printS +=(" B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + "\nPut ---> " + ConvertHex(r1, 4));
			return printS;
			
		case 15:
			//LDA
			//Interpret the item at the top of the stack as an absolute data memory address. Replace the item at the top of the stack with the contents of that absolute address
			r1 = data[topOfStack];
			data[topOfStack] = data[r1];
			printS +=(" B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(data[topOfStack], 4));
			return printS;
			
		case 16:
			//STA
			//Pop the top two items from the stack. Interpret the top item as an absolute data memory address. Store the second item from the top at that address
			r1 = data[topOfStack];
			topOfStack--;
			r2 = data[topOfStack];
			topOfStack--;
			data[r1] = r2;
			printS +=(" B = " + ConvertHex(basePointer, 3) + " T = " + ConvertHex(topOfStack, 3) + " DATA[" + ConvertHex(topOfStack, 3) + "] <-- " + ConvertHex(data[topOfStack], 4));
			return printS;
			
		default:
			System.out.println("Error reading address for OPR Opcode Table");
			return printS;
			
		}
	}
	//Formatting shortcut for strings
	public String ConvertHex(int input, int length) {
		
		//Covers negative values
		if(input<0) {
			input = 2048+input;
		}
		String output = "";
		
		//Converting to hex
		output = Integer.toHexString(input).toUpperCase();
		
		//Handling format
		while(output.length() < length) {
			output = "0" + output;
		}
		return output;
	}
	//Finding Twos Complement for 16-bit numbers
	//Only used for converting data in the Interpreter
	public int FindDataTwos(String num) {
		
		//Parsing the int from the input
		int twos = Integer.parseInt(num, 16);
		
		//Converting the int to binary
		String twosS;
		twosS = Integer.toBinaryString(Integer.parseInt(num, 16));
		
		//Making the binary 16 digits in length
		while(twosS.length() < 16) {
			twosS = "0" + twosS;
		}
		
		//Checking if binary is negative
		if(twosS.charAt(0) == '1') {
			
			//Converting to Two's Complement
			twos = 65535 - Integer.parseInt(num, 16) + 1;
			twos = twos * (-1);
		}
		return twos;
	}
	//Finding Twos Complement for 11-bit numbers
	public int FindTwos(String num) {
	
		//Parsing the int from the input
		int twos = Integer.parseInt(num, 16);
		
		//Converting the int to binary
		String twosS;
		twosS = Integer.toBinaryString(Integer.parseInt(num, 16));
		
		//Making the binary 11 digits in length
		while(twosS.length() < 11) {
			twosS = "0" + twosS;
		}
		
		//Checking if binary is negative
		if(twosS.charAt(0) == '1') {
			
			//Converting to Two's Complement
			twos = 2047 - Integer.parseInt(num, 16) + 1;
			twos = twos * (-1);
		}
		return twos;
	}
	//Quiet Trace Output
	public void QuietTrace(char type, int num){
		
		//Printing Gets
		if(type == 'g') {
			quietOutput += "GET -----> " + ConvertHex(num, 4) + "\n"; 
		}
	
		//Printing Puts
		else if(type == 'p'){
			quietOutput += "PUT ----------> " + ConvertHex(num, 4) + "\n"; 
		}
		
		//Finishing output
		else {
		
			//Creation of Trace File
			File file = new File("QuietTrace.txt");
			try {
				if(!file.exists()) {
					file.createNewFile();
				}
				
				//initializing variables for file outputting
				FileWriter fw = new FileWriter(file);
				BufferedWriter out = new BufferedWriter(fw);

				out.write("Quiet Trace");
				out.newLine();
				out.write(quietOutput);
				out.close();
			}catch(Exception e) {
			}
		}
	}
	//Shazam Assembler
	public void Assembler(File file){
		
		//Defining Linked List, Scanner, and File
		Assembly = new LinkedList<Procedure>();
		Scanner scnr = null;
		try {
			scnr = new Scanner(file);
		} catch (FileNotFoundException e) {
		}
		
		//Counters
		int tracker = -1;
		programCounter = 0;
		
		while(scnr.hasNext()) {
			
			//Variables to split the input lines up
			String inLine = scnr.nextLine();
			String label ="";
			String opcode = "";
			char level = ' ';
			String operand = "";
			
			//Checking for comments
			if(inLine.charAt(0) == '*') {
				continue;
			}
			
			//Splitting up input
			label = inLine.substring(0, 8);
			opcode = inLine.substring(9, 16);
			level = inLine.charAt(18);
			operand = inLine.substring(20, 28);
	
			//Checking for special cases
			//Proc
			if(opcode.contains("PROC")) {
				tracker++;
				Assembly.add(new Procedure(label));
			}
			
			//Variable
			else if(opcode.contains("VAR")) {
				Assembly.get(tracker).Var.add(label);
			}
			
			//End
			else if(opcode.contains("END")) {
				Assembly.get(tracker).End();
				while(Assembly.get(tracker).isFinished() && tracker>0) {
				tracker--;
				}
				continue;
			}
			
			//Other ID
			else if(hasLetters(label)) {
				Assembly.get(tracker).ID.add(new Identifier(label, (tracker+1), programCounter));
				Assembly.get(tracker).Inst.add(new Instructions(opcode, level, operand, programCounter));
				programCounter++;
			}
			
			//Begin
			else if(opcode.contains("BEGIN")) {
				String tempN = Assembly.get(tracker).name;
				Assembly.get(tracker).ID.add(new Identifier(tempN, (tracker + 1), programCounter));				
				Assembly.get(tracker).Inst.add(new Instructions(opcode, level, operand, programCounter));
				programCounter++;
			}
			
			//Catch all instructions
			else {
				Assembly.get(tracker).Inst.add(new Instructions(opcode, level, operand, programCounter));
				programCounter++;
			}
		}
		
		//Call for output
		AssemblerOut();
		System.out.println("Assembler Pass 1 Finished");
		
		//Call for second pass
		AssemblerTwo();
	}
	//Assembler Pass 2
	public void AssemblerTwo() {
		
		//Creating a new output file
				File file = new File("Mapping.txt");
				
				try {
					if(!file.exists()) {
						file.createNewFile();
					}

					//File writing variables
					FileWriter fw = new FileWriter(file);
					BufferedWriter out = new BufferedWriter(fw);
					
					//Top of file output
					out.write("LOC/OFFSET CODE  LABEL     OP/PO    L  OPERAND");
					out.newLine();
					
					out.write("----------------------------------------------");
					out.newLine();
					
					//Tracking Variables
					int tracker;
					int nextLoc = 0;
					int highest = Assembly.getFirst().Inst.getLast().location;
					
					//Printing Var and ID
					for(int x = 0; x<Assembly.size(); x++) {
						
						//ID
						out.write(ConvertHex(Assembly.get(x).ID.get(0).location, 3) + "              "	+ Assembly.get(x).name + "  PROC" );
						out.newLine();
						
						//VAR
						for(int y = 0; y<Assembly.get(x).Var.size(); y++) {
							
						out.write("    " + ConvertHex(y+3, 3) + "          " + Assembly.get(x).Var.get(y) + "  VAR");
						out.newLine();
						
						}
					}
					
					//Finding starting point
					tracker = FindNext(nextLoc);
					
					//Main loop
					while(nextLoc<=highest) {
						
						//Dealing with output and mapping file
						for(int x = 0; x<Assembly.get(tracker).Inst.size(); x++) {
							if(Assembly.get(tracker).Inst.get(x).location == nextLoc) {
								
								//Cover END cases
								if(nextLoc>0 && Assembly.get(tracker).Inst.get(x).name.contains("BEGIN")) {
									out.write("			   END");
									out.newLine();
								}
								boolean found = false;
							
								//Call for 'encoder'
								String encoded = Encode(tracker, x);
								
								//Store 'encoded' data
								AssemblerInst.add(encoded);
								out.write(ConvertHex(nextLoc, 3) +"        " + encoded + " ");
								
								for(int y = 0; y<Assembly.size(); y++) {
									for(int z = 0; z<Assembly.get(y).ID.size(); z++) {
									
										//Cover identifier cases
										if(Assembly.get(y).ID.get(z).location == nextLoc) {
											out.write(Assembly.get(y).ID.get(z).name + "  ");
											found = true;
										}
									}
								}
								if(found == false) {
									out.write("          ");
								}
								
								//Final output handling
								out.write(Assembly.get(tracker).Inst.get(x).name+ "  " + Assembly.get(tracker).Inst.get(x).level + "  " + Assembly.get(tracker).Inst.get(x).operand);
								out.newLine();
								nextLoc++;
							}else {
								
								//Covers for switching of procedures
								tracker = FindNext(nextLoc);
								break;
							}

						}

					}
					
					//Calls for preparation of new load file
					PrepLoad();
					
					System.out.println("Assembler Pass 2 Finished");
					out.close();
				}
				catch(IOException e) {
					e.printStackTrace();
				}
	}
	//Assembler Output
	public void AssemblerOut() {
		
		//Creating a new output file
		File file = new File("SymbolTables.txt");
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			
			//File writing variables
			FileWriter fw = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fw);
			
			out.write("Assembler Dump Symbol Table");
			
			//Looping through procedures
			for(int x = 0; x<Assembly.size(); x++) {
				out.newLine();
				out.newLine();
				out.write("PROCEDURE... " + Assembly.get(x).name);
				out.newLine();
				out.newLine();
				
				//ID
				out.write("IDENTIFIER   LOCATION     LEVEL");
				out.newLine();
				for(int y = 0; y<Assembly.get(x).ID.size(); y++) {
					out.write(Assembly.get(x).ID.get(y).toString());
					out.newLine();
				}
				
				//Variables
				out.newLine();
				out.write(Assembly.get(x).name + "... VARIABLES    TOTAL SPACE REQUIRED: " + ConvertHex(Assembly.get(x).Var.size(), 3));
				out.newLine();
				out.newLine();
	
				out.write("IDENTIFIER   OFFSET       NUMBER OF WORDS");
				out.newLine();
				for(int y = 0; y<Assembly.get(x).Var.size(); y++) {
					out.write(" " + Assembly.get(x).Var.get(y) + "     " + ConvertHex((y+3), 3) + "          0001" );
					out.newLine();
				}
				
				//Instructions
				out.newLine();
				out.write(Assembly.get(x).name + "... INSTRUCTIONS TOTAL SPACE REQUIRED: " + ConvertHex(Assembly.get(x).Inst.size(), 3));
				out.newLine();
				out.newLine();
	
				out.write("OP/PSEUDO-OP LEVEL        OPERAND");
				out.newLine();
				for(int y = 0; y<Assembly.get(x).Inst.size(); y++) {
					out.write(Assembly.get(x).Inst.get(y).toString());
					out.newLine();
				}
				
				out.newLine();
				out.write("------------------------------");
	
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//Prepare Input File From Assembler Data
	public void PrepLoad() {
	
		//Creating a new output file
		File file = new File("AssemblerLoad.txt");
		try {
			if(!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fw);
			
		//Determine how many loops to make
		int loops;
		loops = (int)Math.ceil(AssemblerInst.size() / 14.0);
		
		//Tracking Variables
		int size = AssemblerInst.size();
		int counter = 0;
		
		//Main Loop
		for(int x = 0; x<loops; x++) {
			
			//Data Type and location
			out.write("I ");
			out.write(ConvertHex(counter, 3) + " ");
			
			//Checking boundaries
			if(size-counter>=14) {
				out.write("E ");
			}else {
				out.write(ConvertHex(size-counter, 1) + " ");
			}
			if(size-counter>=14) {
				for(int y = 0; y<14; y++) {
					out.write(AssemblerInst.get(counter));
					counter++;
				}
			}
			else {
				int remaining = (size-counter);
				for(int y = 0; y<remaining; y++) {
					out.write(AssemblerInst.get(counter));
					counter++;
				}
			}
			out.newLine();
		}
		
		//Setting starting point
		out.write("I ");
		
		//Finding location of starting point (MAIN)
		int location = 0;;
		for(int x = 0; x<Assembly.size(); x++) {
			for(int y = 0; y<Assembly.get(x).ID.size(); y++) {
				if(Assembly.get(x).ID.get(y).name.contains("MAIN")) {
					location = Assembly.get(x).ID.get(y).location;
				}
			}
		}
		
		out.write(ConvertHex(location, 3) + " 0");
		out.newLine();
		
		//Finishing data output
		out.write("D 002 00");
		out.newLine();
		
		//Writing extra 'user input'
		out.write("00040002FE029932222300060006");
		
		out.close();
		}catch(IOException e){
		}
	}
	//Converting Into Bits For Interpreter
	public String Encode(int tracker, int x) {
		
		//Retrieve name from specified position
		String name = Assembly.get(tracker).Inst.get(x).name;
		String returnS = "";
		
		//BEGIN
		if(name.contains("BEGIN")){
			returnS = "50002";
		}
		
		//LOD
		else if(name.contains("LOD")) {
			returnS = "2" + Assembly.get(tracker).Inst.get(x).level;
			if(hasLetters(Assembly.get(tracker).Inst.get(x).operand)) {
				returnS += ConvertHex(Assembly.get(tracker).Var.indexOf(Assembly.get(tracker).Inst.get(x).operand)+3, 3);
			}else {
				returnS += ConvertHex(Integer.parseInt(Assembly.get(tracker).Inst.get(x).operand.strip()),3);
			}
		}
		
		//DUP
		else if(name.contains("DUP")) {
			returnS = "10006";
		}
		
		//STO
		else if(name.contains("STO")) {
			returnS = "30" + ConvertHex(Assembly.get(tracker).Var.indexOf(Assembly.get(tracker).Inst.get(x).operand)+3, 3);
		}
		
		//LIT
		else if(name.contains("LIT")) {
			returnS = "00" + ConvertHex(Integer.parseInt(Assembly.get(tracker).Inst.get(x).operand.strip()), 3);
		}
		
		//ADD
		else if(name.contains("ADD")) {
			returnS = "10002";
		}
		
		//LDA
		else if(name.contains("LDA")) {
			returnS = "1000F";
		}
		
		//LSS
		else if(name.contains("LSS")) {
			returnS = "10009";
		}
		
		//JPC
		else if(name.contains("JPC")) {
			returnS = "61";
			for(int y = 0; y<Assembly.size(); y++) {
				for(int z = 0; z<Assembly.get(y).ID.size(); z++) {
					if(Assembly.get(y).ID.get(z).name.contains(Assembly.get(tracker).Inst.get(x).operand)) {
						returnS += ConvertHex(Assembly.get(y).ID.get(z).location, 3);
					}
				}
			}
		}
		
		//RET
		else if(name.contains("RET")) {
			returnS = "10000";
		}
		
		//STA
		else if(name.contains("STA")) {
			returnS = "10010";
		}
		
		//GET
		else if(name.contains("GET")) {
			returnS = "1000D";
		}
		
		//NEQ
		else if(name.contains("NEQ")) {
			returnS = "10008";
		}
		
		//INT
		else if(name.contains("INT")) {
			returnS = "50" + ConvertHex(Integer.parseInt(Assembly.get(tracker).Inst.get(x).operand.strip()), 3);
		}
		
		//ADR
		else if(name.contains("ADR")) {
			returnS = "70" + ConvertHex(Assembly.get(tracker).Var.indexOf(Assembly.get(tracker).Inst.get(x).operand)+3, 3);
		}
		
		//CAL
		else if(name.contains("CAL")) {
			returnS = "40";
			for(int y = 0; y<Assembly.size(); y++) {
				for(int z = 0; z<Assembly.get(y).ID.size(); z++) {
					if(Assembly.get(y).ID.get(z).name.contains(Assembly.get(tracker).Inst.get(x).operand)) {
						returnS += ConvertHex(Assembly.get(y).ID.get(z).location, 3);
					}
				}
			}
		}
		
		//SUB
		else if(name.contains("SUB")) {
			returnS = "10003";
		}
		
		//PUT
		else if(name.contains("PUT")) {
			returnS = "1000E";
		}
		
		//HLT
		else if(name.contains("HLT")) {
			returnS = "63000";
		}
		
		returnS = returnS.replaceAll("\\s", "0");
		return returnS;
	}
	//Finding Next Instruction To Print
	public int FindNext(int nextLoc) {
		
		//Returns which part of Assembly to start in (not the specific instruction #)
		for(int x = 0; x <Assembly.size(); x++) {
			for(int y = 0; y<Assembly.get(x).Inst.size(); y++) {
				if(Assembly.get(x).Inst.get(y).location == nextLoc) {
					return x;
				}
			}
		}
		return 0;
	}
	//Check if a String Has Any Letters
	public boolean hasLetters(String s) {
		
		//Loop through string to determine if any character is classified as a 'letter'
		for(int x = 0; x<s.length(); x++) {
			if(Character.isLetter(s.charAt(x)) == true) {
		return true;
			}
		}
		return false;
	}
}
