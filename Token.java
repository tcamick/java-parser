/**
 * @author Cole Amick
 * Data: 01.29.2014
 * Description: A token object
 */
public class Token {
	private String type;
	private String lexeme;
	/**
	 * @return type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type- String
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * @return the lexeme
	 */
	public String getLexeme() {
		return lexeme;
	}
	/**
	 * @param lexeme the lexeme to set
	 */
	public void setLexeme(String lexeme) {
		this.lexeme = lexeme;
	}
	
	public String toString(){
	return "<token>" +type+ "</token>" + "--------------" + "<lexeme>"+lexeme+"</lexeme>";
	}		
		
}
