/**
 * @author Cole Amick Date: 01.29.2014
 */

public class ParserTest {

	public static void main(String[] args) {
		Parser p = null;
		//check command line input
		if (args.length == 2) {
			//determine logging
			if(!args[0].equals("-v") && !args[0].equals("-d")){
				System.out.println("Only -v or -d are valid flags");
			}else{
			boolean logBit = args[0].equals("-v") ? true : false;
			//create parser
			try{
				p = new Parser(args[1], logBit);
			}catch(Exception e){
				//Intentionally left blank
			}
			if( p != null )p.parse();
			}
		}
		else{
			System.out.println("Invalid number of arguments");
			System.out.println("Try: java ParserTester <flags>  <file_name>");
		}
		
	}
}
