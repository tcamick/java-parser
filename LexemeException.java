
/**
 * @author Cole Amick 
 * Data: 01.29.2014
 * Description: A Lexeme exception
 */
public class LexemeException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LexemeException() {
		super();
	}

	public LexemeException(String message) {
		super(message);
	}

	public LexemeException(String message, Throwable cause) {
		super(message, cause);
	}

	public LexemeException(Throwable cause) {
		super(cause);
	}
}
