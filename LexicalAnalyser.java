
import java.io.InputStream;

/**
 * @author Cole Amick 
 * Date: 01.29.2014
 */


public class LexicalAnalyser {
	private LexicalStateMachine lexicalStateMachine;
	
	/**
	 * @param inputStream -class InputStream
	 */
	public LexicalAnalyser(InputStream inputStream) {
		super();
		lexicalStateMachine = new LexicalStateMachine(inputStream);
	}
	
	/**
	 * 
	 * @param fileInputStream - String
	 */
	public LexicalAnalyser(String file) {
		super();
		lexicalStateMachine = new LexicalStateMachine(file);
	}
	
	/**
	 * @return class Token
	 */
	public Token nextToken(){
		Token t = lexicalStateMachine.getToken();
		return t;
	}

	/**
	 * @return class Token
	 */
	public Token peekToken(){
		Token t = lexicalStateMachine.getPeekToken();
		return t;
	}
	/**
	 * This is for debugging
	 * It returns the current line of the file that the lexical analyzer is analyzing...
	 * @return
	 */
	public int getLineCount(){
		return lexicalStateMachine.getLineCount();
	}

}
