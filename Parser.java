import java.io.InputStream;


/**
 * @author Cole Amick 
 * Date: 01.29.2014
 */

public class Parser {
	private ParserRules rules;

	/**
	 * Creates a parser that will use a file as input
	 * @param file - String
	 * @param logBit - boolean
	 */
	public Parser(String file, boolean logBit) {
		rules = new ParserRules(file, logBit);
	}

	/**
	 *  Creates a parser that will use a InputStream object as input
	 * @param inputStream - InputStream
	 * @param logBit - boolean
	 */
	public Parser(InputStream inputStream, boolean logBit) {
		rules = new ParserRules(inputStream, logBit);
	}

	/**
	 * Starts the recursive descent parser that uses the lexical analyzer from project 1
	 */
	public void parse() {
		try {
			rules.startParse();
		} catch (LexemeException e) {
			//Intentionally left blank
		}
	}

}
