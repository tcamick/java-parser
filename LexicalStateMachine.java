/**
 * @author Cole Amick 
 * Data: 01.29.2014
 * Description: A finite state machine for a lexical analyser
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class LexicalStateMachine {

	private static final int START_ = 0;
	private static final int ID_ = 1;
	private static final int SYM_ = 2;
	private static final int INT_ = 3;
	private static final int INT2_ = 4;
	private static final int CHAR_ = 5;
	private static final int CHAR2_ = 6;
	private static final int STRING_ = 7;
	private static final int READ_AHEAD_LIMIT = 50;
	private String[] stateType = { "start_s", "id_s", "sym_s", "int_s",
			"int_s2", "char_s", "char_s2", "string_s" };
	private int currentState; //
	private String currentLine; // Current line the state machine is parsing
	private int lineCount;
	private String lexemBuffer;
	private int currentLineIndex;

	private Token bufferToken;
	private Token token; // used when lexical analyser calls next token;
	private BufferedReader br;
	private Map<String, String> lexem_tokens;
	
	/**
	 * 
	 * @param inputStream
	 */
	public LexicalStateMachine(InputStream inputStream) {
		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
		} catch (Exception ex) {
			System.out.println("Error: Input stream not found 								:(");
			System.exit(1);
			return;
		}
		currentState = 0;
		getNextLine();
		currentLineIndex = 0;
		initHashMaps();
		lexemBuffer = "";
		
		bufferToken = null;
		token = null;
		lineCount = 1;
	}

	/**
	 * 
	 * @param file
	 */
	public LexicalStateMachine(String file) {
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: File not found							:(");
			return;
		}
		getNextLine();
		currentLineIndex = 0;
		initHashMaps();
		currentState = 0;
		lexemBuffer = "";
		bufferToken = null;
		token = null;
		lineCount = 1;
	}

	/**
	 * Initalizes a hashmap with key value pairs the keys are valid java lexemes
	 * (based on the context-free grammer) and the values are there respective
	 * tokens
	 */
	private void initHashMaps() {
		lexem_tokens = new HashMap<String, String>();
		lexem_tokens.put("||", "INFIX_");
		lexem_tokens.put("&&", "INFIX_");
		lexem_tokens.put("==", "INFIX_");
		lexem_tokens.put("<=", "INFIX_");
		lexem_tokens.put(">=", "INFIX_");
		lexem_tokens.put("!=", "INFIX_");
		lexem_tokens.put("=", "ASSIGN_");
		lexem_tokens.put("<", "INFIX_");
		lexem_tokens.put(">", "INFIX_");
		lexem_tokens.put("+", "INFIX_");
		lexem_tokens.put("-", "MINUS_");
		lexem_tokens.put("*", "INFIX_");
		lexem_tokens.put("/", "INFIX_");
		lexem_tokens.put("!", "UNARY_");
		lexem_tokens.put("(", "L_PAREN_");
		lexem_tokens.put(")", "R_PAREN_");
		lexem_tokens.put("{", "L_CURLY_");
		lexem_tokens.put("}", "R_CURLY_");
		lexem_tokens.put("[", "L_BRAC_");
		lexem_tokens.put("]", "R_BRAC_");
		lexem_tokens.put(";", "SEMI_COL_");
		lexem_tokens.put(",", "COMMA_");
		lexem_tokens.put(".", "PERIOD_");
		lexem_tokens.put("null", "NULL_");
		lexem_tokens.put("true", "TRUE_");
		lexem_tokens.put("false", "FALSE_");
		lexem_tokens.put("this", "THIS_");
		lexem_tokens.put("class", "CLASS_");
		lexem_tokens.put("if", "IF_");
		lexem_tokens.put("else", "ELSE_");
		lexem_tokens.put("super", "SUPER_");
		lexem_tokens.put("while", "WHILE_");
		lexem_tokens.put("for", "FOR_");
		lexem_tokens.put("void", "VOID_");
		lexem_tokens.put("return", "RETURN_");
		lexem_tokens.put("break", "BREAK_");
		lexem_tokens.put("continue", "CONT_");
		lexem_tokens.put("extends", "EXTENDS_");
		lexem_tokens.put("static", "STATIC_");
		lexem_tokens.put("throws", "THROWS_");
		lexem_tokens.put("public", "PUBLIC_");
		lexem_tokens.put("new", "NEW_");
		lexem_tokens.put("try", "TRY_");
		lexem_tokens.put("catch", "CATCH_");
		lexem_tokens.put("public", "PUBLIC_");
		lexem_tokens.put("private", "PRIVATE_");
		lexem_tokens.put("extends", "EXTENDS_");
		lexem_tokens.put("int", "INT_");
		lexem_tokens.put("boolean", "BOOLEAN_");
		lexem_tokens.put("char", "TYPE_");
		lexem_tokens.put("String", "TYPE_");
	
	}

	/**
	 * Reads in the next character from the current line does not change state
	 * or interpret character at all increments the currentLineIndex counter
	 * 
	 * @return
	 */
	private CharacterType readNextChar() {
		CharacterType nextCharT = null;
		if (currentLineIndex == currentLine.length()) {
			getNextLine();
		}

		if (currentLine != null && currentLine.length() != 0)
			nextCharT = new CharacterType(currentLine.charAt(currentLineIndex));
		currentLineIndex += 1;
		return nextCharT;
	}

	/**
	 * 
	 * @param c
	 */
	private void startState(CharacterType c) {
		if (c.getCharacterType() == "LETTER") {
			currentState = ID_;
			lexemBuffer += c.getCharacter();
		} else if (c.getCharacterType() == "SYMBOL"
				&& (c.getCharacter() == '$' || c.getCharacter() == '_')) {
			currentState = ID_;
		} else if (c.getCharacterType() == "SYMBOL"
				&& (c.getCharacter() == '"')) {
			lexemBuffer += c.getCharacter();
			currentState = STRING_;
		} else if (c.getCharacterType() == "SYMBOL"
				&& (c.getCharacter() == '\'')) {
			lexemBuffer += c.getCharacter();
			currentState = CHAR_;
		} else if (c.getCharacterType() == "SYMBOL") {
			if (c.getCharacter() == '&' || c.getCharacter() == '='
					|| c.getCharacter() == '<') {
				lexemBuffer += c.getCharacter();
				currentState = SYM_;
			} else if (c.getCharacter() == '>' || c.getCharacter() == '!') {
				lexemBuffer += c.getCharacter();
				currentState = SYM_;
			} else {
				token = new Token();
				token.setLexeme("" + c.getCharacter());
				String s = lexem_tokens.get("" + c.getCharacter());
				token.setType(s);
				currentState = START_;
			}

		} else if (c.getCharacterType() == "SYMBOL"
				&& (c.getCharacter() == '\'')) {
			currentState = CHAR_;
			lexemBuffer += c.getCharacter();
		} else if (c.getCharacterType() == "NUM") {
			currentState = INT_;
			lexemBuffer += c.getCharacter();
		}
	}

	/**
	 * 
	 * @param c
	 */
	private void idState(CharacterType c) {
		if (c.getCharacterType() == "W_SPACE") {
			token = new Token();
			currentState = START_;
			token.setLexeme(lexemBuffer);
			String t = lexem_tokens.get(lexemBuffer);
			if (t != null)
				token.setType(t);
			else {
				token.setType("ID_");
			}
			lexemBuffer = "";
		} else if (c.getCharacterType() == "NUM") {
			lexemBuffer += c.getCharacter();
		} else if (c.getCharacterType() == "LETTER") {
			lexemBuffer += c.getCharacter();
		} else if (c.getCharacterType() == "SYMBOL") {
			currentState = START_; // change state
			token = new Token(); // print previous ID_ token
			token.setLexeme(lexemBuffer);
			String t = lexem_tokens.get(lexemBuffer);
			if (t != null)
				token.setType(t);
			else {
				token.setType("ID_");
			}
			lexemBuffer = "";
			setBufferToken(c); // set a buffer token
		}
	}

	/**
	 * 
	 */
	private void setBufferToken(CharacterType c) {
		bufferToken = new Token();
		bufferToken.setLexeme("" + c.getCharacter());
		bufferToken.setType(lexem_tokens.get("" + c.getCharacter()));
	}

	/**
	 * 
	 * @param c
	 */
	private void symState(CharacterType c) {
		if (c.getCharacterType() == "SYMBOL" && (c.getCharacter() == '"')) {
			token = new Token();
			token.setLexeme(lexemBuffer);
			token.setType(lexem_tokens.get("" + lexemBuffer));
			lexemBuffer = "" + c.getCharacter();
			currentState = STRING_;
		} else if (c.getCharacterType() == "SYMBOL"
				&& (c.getCharacter() == '\'')) {
			token = new Token();
			token.setLexeme(lexemBuffer);
			token.setType(lexem_tokens.get("" + lexemBuffer));
			//
			lexemBuffer += c.getCharacter();
			currentState = CHAR_;

		} else if (c.getCharacterType() == "SYMBOL"
				&& lexem_tokens.get(lexemBuffer + c.getCharacter()) != null) {
			currentState = START_;
			lexemBuffer += c.getCharacter();
			token = new Token();
			token.setLexeme(lexemBuffer);
			token.setType(lexem_tokens.get("" + lexemBuffer));
			lexemBuffer = "";
		} else if (c.getCharacterType() == "SYMBOL") {
			currentState = START_; // change state
			token = new Token(); // print previous ID_ token
			token.setLexeme(lexemBuffer);
			String t = lexem_tokens.get(lexemBuffer);
			if (t != null)
				token.setType(t);
			else {
				token.setType("UNKNOWN_");
			}
			lexemBuffer = "";
			setBufferToken(c); // set a buffer token
		}

		else if (c.getCharacterType() == "W_SPACE") {
			currentState = START_;
			token = new Token();
			token.setLexeme(lexemBuffer);
			token.setType(lexem_tokens.get(lexemBuffer));
			lexemBuffer = "";
		} else if (c.getCharacterType() == "LETTER") {
			currentState = ID_;
			if (lexemBuffer.length() != 0) {
				token = new Token();
				token.setLexeme(lexemBuffer);
				token.setType(lexem_tokens.get(lexemBuffer));
			}
			lexemBuffer = "" + c.getCharacter();
		} else if (c.getCharacterType() == "NUM") {
			currentState = INT_;
			setToken(c);
		}
	}

	/**
	 * 
	 */
	private void setToken(CharacterType c) {
		token = new Token();
		if (lexemBuffer.length() != 0) {
			token.setLexeme(lexemBuffer);
			token.setType(lexem_tokens.get(lexemBuffer));
			lexemBuffer = "";
		} else {
			token.setLexeme("" + c.getCharacter());
			token.setType(lexem_tokens.get(c.getCharacter()));
		}
	}

	/**
	 * 
	 * @param c
	 */
	private void intState(CharacterType c) {
		if (c.getCharacterType() == "W_SPACE") {
			token = new Token();
			currentState = START_;
			token.setLexeme("" + c.getCharacter());
			lexemBuffer = "";
			token.setType("INT_LIT");
		} else if (c.getCharacterType() == "NUM") {
			currentState = INT2_;
			lexemBuffer += c.getCharacter();
		} else if (c.getCharacterType() == "LETTER" && c.getCharacter() == 'x'
				&& lexemBuffer.charAt(0) == '0') {
			currentState = INT2_;
			lexemBuffer += c.getCharacter();
		} else if (c.getCharacterType() == "LETTER") {
			setToken(new CharacterType('!'));
			token.setType("ERROR_");
			token.setLexeme("");
			System.out.println("ERROR: invalid lexeme");
			System.exit(1);
		} else if (c.getCharacterType() == "SYMBOL") {
			currentState = START_;
			setToken(c);
			token.setType("INT_LIT");
			setBufferToken(c);
		}
	}

	/**
	 * 
	 * @param c
	 */
	private void intState2(CharacterType c) {
		if (c.getCharacterType() == "W_SPACE") {
			currentState = START_;
			token = new Token();
			token.setLexeme(lexemBuffer + c.getCharacter());
			lexemBuffer = "";
			token.setType("INT_LIT");
		} else if (c.getCharacterType() == "SYMBOL") {
			currentState = START_; // if setBufferToken is going to be set,
									// currentState must be put to start!
			setToken(c);
			setBufferToken(c);
			token.setType("INT_LIT");
		} else if (c.getCharacterType() == "NUM") {
			currentState = INT2_;
			lexemBuffer += c.getCharacter();
		}
	}

	/**
	 * 
	 * @param c
	 */
	private void charState(CharacterType c) {
		if (c.getCharacterType() == "LETTER"
				|| c.getCharacterType() == "SYMBOL") {
			lexemBuffer += c.getCharacter();
			currentState = CHAR2_;
		} else if (c.getCharacterType() == "SYMBOL"
				&& (c.getCharacter() == '\'')) {
			setToken(new CharacterType('!'));
			token.setLexeme("");
			token.setType("ERROR_");
			currentState = START_;
			System.out.println("ERROR: invalid lexeme");
			System.exit(1);
		}
	}

	/**
	 * 
	 * @param c
	 */
	private void charState2(CharacterType c) {
		if (c.getCharacter() == '\'') {
			currentState = START_;
			lexemBuffer += c.getCharacter();
			setToken(c);
			token.setType("CHAR_LIT");
		} else {
			currentState = START_;
			System.out.println("ERROR: invalid lexeme");
			System.exit(1);
		}
	}

	/**
	 * 
	 * @param c
	 */
	private void stringState(CharacterType c) {
		lexemBuffer += c.getCharacter();
		if (c.getCharacterType() == "SYMBOL" && c.getCharacter() == '"') {
			currentState = START_;
			token = new Token();
			token.setLexeme(lexemBuffer);
			lexemBuffer = "";
			token.setType("STRING_LIT");
		}
	}

	/*
	 * 
	 */
	private void changeState(CharacterType c) {
		// System.out.println(c.getCharacter());
		// System.out.println(c.getCharacterType());
		switch (stateType[currentState]) {
		case "start_s":
			startState(c);
			break;
		case "id_s":
			idState(c);
			break;
		case "sym_s":
			symState(c);
			break;
		case "int_s":
			intState(c);
			break;
		case "int_s2":
			intState2(c);
			break;
		case "char_s":
			charState(c);
			break;
		case "char_s2":
			charState2(c);
			break;
		case "string_s":
			stringState(c);
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * @return
	 */
	public Token getToken() {
		token = null;
		while (token == null) {
			if (bufferToken != null) {
				token = bufferToken;
				bufferToken = null;
			} else {
				CharacterType c = readNextChar();
				if (currentLine != null && c != null)
					changeState(c);
				else {
					token = new Token();
					token.setLexeme("");
					token.setType("EOSTREAM_");
					try {
						br.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.err.println("Error closing file");
					}
				}
			}
		}
		return token;
	}

	/**
	 * Returns the token that comes next in the input stream does not change
	 * state or increment any counter
	 * 
	 * @return
	 */
	public Token getPeekToken() {
		Token t;
		if (bufferToken != null) {
			return bufferToken;
		} else {
			int tempState = currentState;
			int tempIndex = currentLineIndex;
			String tempLine = currentLine;
			int tempLineCount = lineCount;
			try {
				br.mark(READ_AHEAD_LIMIT); // mark bufferd reader
			} catch (Exception ex) {
			}
			t = getToken(); //
			try {
				br.reset(); // reset bufferd reader
			} catch (Exception ex) {
			}
			lineCount = tempLineCount;
			currentState = tempState;
			currentLineIndex = tempIndex;
			currentLine = tempLine;
			bufferToken = null;
		}
		return t;
	}

	/**
	 * returns the current line that state machine is parsing
	 * 
	 * @return the currentLine
	 */
	public String getCurrentLine() {
		return currentLine;
	}

	/**
	 * Call this method if every the entire current line has been parsed sets
	 * the currentLineIndex to zero
	 * 
	 * @return
	 */
	private String getNextLine() {
		if (currentState == STRING_) {
			System.out.println("ERROR: invalid lexeme");
			System.exit(1);
		}

		try {
			currentLine = br.readLine();
			lineCount += 1;
			if (currentLine == null) {
				token = new Token();
				token.setLexeme("");
				token.setType("EOSTREAM_");
				br.close();
			} else {
				if (currentLine.length() == 0) {
					currentLine = br.readLine();
					lineCount += 1;
				}
			}
		} catch (IOException e) {
			// System.err.println("Caught IOException: " + e.getMessage());
		}
		currentLineIndex = 0;
		return currentLine;
	}

	/**
	 * @return the stateType
	 */
	public String getStateType() {
		return stateType[currentState];
	}

	/**
	 * @return the currentState
	 */
	public int getCurrentState() {
		return currentState;
	}

	/**
	 * @param currentState
	 *            the currentState to set
	 */
	public void setCurrentState(int currentState) {
		this.currentState = currentState;
	}
	public int getLineCount(){
		return lineCount;
	}

	
	
	/**
	 * @author cole
	 * 
	 */
	class CharacterType {
		private char c;
		private String type;

		CharacterType(char c) {
			this.c = c;
			initCharacterType();
		}

		private void initCharacterType() {
			int i = (int) c;
			if (i == 32 || i == 9) {
				type = "W_SPACE";
				return;
			}
			if (i == 10) {
				type = "N_LINE";
				return;
			} else if (i >= 33 && i <= 47) {
				type = "SYMBOL";
				return;
			} else if (i >= 58 && i <= 64) {
				type = "SYMBOL";
				return;
			} else if (i >= 91 && i <= 96) {
				type = "SYMBOL";
				return;
			} else if (i >= 123 && i <= 126) {
				type = "SYMBOL";
				return;
			} else if (i >= 48 && i <= 57) {
				type = "NUM";
				return;
			} else if (i >= 65 && i <= 90) {
				type = "LETTER";
				return;
			} else if (i >= 97 && i <= 122) {
				type = "LETTER";
				return;
			}
		}

		public char getCharacter() {
			return c;
		}

		public String getCharacterType() {
			return type;
		}
	}

}
