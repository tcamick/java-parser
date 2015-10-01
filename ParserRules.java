import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author Cole Amick 
 * Date: 01.29.2014
 */

public class ParserRules {
	private LexicalAnalyser lexAnalyser;
	private ParserLog log;

	private Token peekToken;
	private Deque<String> functionStack; // used primarily for sanity checking
											// and debugging

	public ParserRules(String file, boolean logBit) {
		lexAnalyser = new LexicalAnalyser(file);
		try {
			log = new ParserLog(logBit);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		functionStack = new ArrayDeque<String>();
		peekToken = lexAnalyser.peekToken();
	}

	/**
	 * Creates new parser with inputStream and sets log bit to 1 creates new log
	 * file
	 * 
	 * @param inputStream
	 * @return
	 */
	public ParserRules(InputStream inputStream, boolean logBit) {

		lexAnalyser = new LexicalAnalyser(inputStream);
		try {
			log = new ParserLog(logBit);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		peekToken = lexAnalyser.peekToken();
		functionStack = new ArrayDeque<String>();
	}

	// #######################################################################################################################################
	/**
	 * Checks for an ID token if the ID token is found than the ID token is
	 * consumed if the ID token is not found than an error is thrown
	 * 
	 * @throws LexemeException
	 */
	private boolean ifPeekThenConsume(String token) throws LexemeException {
		if (ifPeek(token)) {
			consumeToken();
			return true;
		} else {
			logErrorMessage(peekToken.getType(), token);
			return false;
		}
	}

	/**
	 * Consumes the next Token into the log if the logBit is equal to one; THIS
	 * EXICUTES lexicalAnalyser.nestToken!!!
	 */
	private void consumeToken() {
		Token token = lexAnalyser.nextToken();
		logMessage(functionStack.peekFirst() + " consumed " + token.getType()
				+ ": <lexeme>" + token.getLexeme() + "</lexeme>");
		peekToken = lexAnalyser.peekToken();
	}

	/**
	 * 
	 */
	private boolean ifPeek(String string) {
		if (peekToken.getType() == string) {
			return true;
		} else
			return false;
	}

	/**
	 * Returns true if the peek token a valid < nameType >
	 */
	private boolean ifPeekIsType() {
		if (peekToken.getType() == "INT_") {
			return true;
		}
		if (peekToken.getType() == "VOID_") {
			return true;
		}
		if (peekToken.getType() == "BOOLEAN_") {
			return true;
		}
		if (peekToken.getType() == "ID_") {
			return true;
		}
		if (peekToken.getType() == "TYPE_") {
			return true;
		}
		return false;
	}

	/**
	 * Creates a log entry based on the msg String parameter does not create log
	 * entry if log bit is equal to 0
	 */
	private void logMessage(String msg) {

		log.logMsg("(L" + lexAnalyser.getLineCount() + ")" + msg + "\n");

		// System.out.println("(L" + lexAnalyser.getLineCount() + ")" + msg);

	}

	/**
	 * Creates a log ERROR entry based on the "found" and "Expected" String
	 * parameter does not create log entry if log bit is equal to 0
	 * 
	 * @throws LexemeException
	 */
	private void logErrorMessage(String found, String expected)throws LexemeException {
		System.out.println("Error: (L" + lexAnalyser.getLineCount()
				+ ") Error on token \"" + found + "\" Expected " + expected);

		logMessage("Error on token \"" + found + "\" Expected " + expected);
		log.closeLog();
		throw new LexemeException("Error on token \"" + found + "\" Expected "
				+ expected);

	}

	private void logVerboseMessage(String callerName) {
		logMessage(functionStack.peekFirst() + " --> returned to --> "
				+ callerName);
	}

	// ############################################################################################################################
	/**
	 * This method initiates the recursive decent parser
	 */
	public ParserLog startParse() throws LexemeException {
		// check token
		if (lexAnalyser.peekToken().getType() == "PUBLIC_") {
			consumeToken();
			doProgram();
		}
		// else exit program
		else {
			logErrorMessage(peekToken.getType(), "PUBLIC_");
		}
		return log;
	}

	/**
	 * program calls the class method in order to initiate the recursive decent
	 * 
	 * @throws LexemeException
	 */
	public void doProgram() throws LexemeException {
		logMessage("<program>-->{<class>}");
		functionStack.push("<program>");
		// while(lexAnalyser.peekToken().getType() != "EOSTREAM_")
		while (ifPeek("CLASS_")) {
			doClass("<program>");
		}
		// else exit program
		while (!ifPeek("EOSTREAM_")) {
			logErrorMessage(peekToken.getType(), "EOSTREAM_");
			peekToken.setType("EOSTREAM_");
		}
		System.out.println("Parser Finished with no errors found");
		log.logMsg("Parser Finished with no errors found");
		log.closeLog();
		functionStack.pop();
	}

	/*
	 * classNT calls memberNT
	 */
	public void doClass(String callerName) throws LexemeException {
		logMessage("<class>--> class ID [extends ID]{{<member>}}");
		functionStack.push("<class>");
		consumeToken(); // consume class token
		// check ID token
		if (ifPeekThenConsume("ID_")) {
			// check for optional EXTENDS_ token
			if (ifPeek("EXTENDS_")) {
				consumeToken();
				ifPeekThenConsume("ID_");
			}
			// check for left curly
			if (ifPeekThenConsume("L_CURLY_")) {

				while (ifPeek("PUBLIC_") || ifPeek("STATIC_") || ifPeekIsType()) {
					// check for optional PUBLIC_ & STATIC_ tokens
					if (ifPeek("PUBLIC_") || ifPeek("STATIC_")) {
						doMember("<class>");
					} else if (ifPeekIsType()) {
						doMember("<class>");
					}
				}
				// TODO check for arraytype!!
				// CHECK ClOSING CURLY

				// TODO uncomment line
				ifPeekThenConsume("R_CURLY_");
			}

		}
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * Consumes a nametype token
	 * 
	 * @throws LexemeException
	 */
	public void doMember(String callerName) throws LexemeException {
		logMessage("<member>-->[pubic][static]<type> ID (<field> | <method>)");
		functionStack.push("<member>");
		if (ifPeek("PUBLIC_") || ifPeek("STATIC_")) {
			consumeToken();
		}
		if (ifPeek("STATIC_")) {
			consumeToken();
		}
		// <type>
		if (ifPeekIsType()) {
			doType("<member>");
			if (ifPeek("SEMI_COL_")) {
				doField("<member>");
				logMessage("(return)-->" + callerName);
				logVerboseMessage(callerName);
				functionStack.pop();
				return;
			}
		}
		// ID
		ifPeekThenConsume("ID_");
		if (ifPeek("COMMA_") || ifPeek("SEMI_COL_")) {
			doField("<member>");
			logVerboseMessage(callerName);
			functionStack.pop();
			return;
		} else {
			if (ifPeek("L_PAREN_")) {
				doMethod("<member>");
				logVerboseMessage(callerName);
				functionStack.pop();
				return;
			} else {
				logErrorMessage(peekToken.getType(), "COMMA_ | L_PAREN_");
			}
		}
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * Consumes a nametype token
	 * 
	 * @throws LexemeException
	 */
	public void doType(String callerName) throws LexemeException {
		logMessage("<type>--> void | Boolean| INT| String| ID");
		functionStack.push("<type>");
		consumeToken();
		if (ifPeek("L_BRAC_")) {
			consumeToken();
			ifPeekThenConsume("R_BRAC_");
		}
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * @param callerName
	 * @throws LexemeException
	 */
	public void doNameType(String callerName) throws LexemeException {
		logMessage("<nameType>-- boolean|int |ID");
		functionStack.push("<nameType>");
		consumeToken();
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * Method for field rule
	 * 
	 * @throws LexemeException
	 */
	public void doField(String callerName) throws LexemeException {
		logMessage("<field>-->{,ID}");
		functionStack.push("<field>");
		while (ifPeek("COMMA_")) {
			if (ifPeek("COMMA_")) {
				consumeToken();
				ifPeekThenConsume("ID_");
			}
		}
		if (ifPeek("SEMI_COL_")) {
			consumeToken();
			logVerboseMessage(callerName);
			functionStack.pop();
			return;
		}
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * Method for method rule in bnf
	 * 
	 * @throws LexemeException
	 */
	public void doMethod(String callerName) throws LexemeException {
		logMessage("<method>-->([<formals>]) [throws ID] <block>");
		functionStack.push("<method>");
		consumeToken();
		if (ifPeekIsType()) {
			doFormals("<method>");
		}
		ifPeekThenConsume("R_PAREN_");
		if (ifPeek("THROWS_")) {
			consumeToken();
			ifPeekThenConsume("ID_");
		}
		if (ifPeek("L_CURLY_")) {
			doBlock("<method>");
		}
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * Method for formals rule
	 * 
	 * @throws LexemeException
	 */
	public void doFormals(String callerName) throws LexemeException {
		logMessage("<formals>--> <type> ID {, <type> ID}");
		functionStack.push("<formals>");
		if (ifPeekIsType()) {
			doType("<formals>");
		}
		ifPeekThenConsume("ID_");
		if (ifPeek("COMMA_")) {
			consumeToken();
			if (ifPeekIsType()) {
				doType("<formals>");
				ifPeekThenConsume("ID_");
			}
		}
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * <block> rule
	 * 
	 * @throws LexemeException
	 ******************************************************************************/
	// TODO finish function
	public void doBlock(String callerName) throws LexemeException {
		logMessage("<block> --> {{<blockStmt>}}");
		functionStack.push("<block>");
		// consume curly
		consumeToken();

		while (ifPeekIsBlockStmt()) {
			doBlockStmt("<block>");
		}
		ifPeekThenConsume("R_CURLY_");
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * 
	 * @return
	 * @throws LexemeException
	 */
	public void doLocals(String callerName) throws LexemeException {
		logMessage("<locals> --> <type> ID [ = <exp> ] {, ID [ <exp ] }");
		functionStack.push("<locals>");
		if (ifPeekIsType()) {
			doType("<locals>");
		}
		ifPeekThenConsume("ID_");
		if (ifPeek("ASSIGN_")) {
			consumeToken();
			doExp("<locals>");
		}
		while (ifPeek("COMMA_")) {
			consumeToken();
			if (ifPeekIsType()) {
				doType("<locals>");
			}
			ifPeekThenConsume("ID_");
		}
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/*
	 * Checks if the next peek token can possibly lead into the <stmt> rule
	 */
	public boolean ifPeekIsStmt() {
		if (ifPeek("IF_")) {
			return true;
		}
		if (ifPeek("WHILE_")) {
			return true;
		}
		if (ifPeek("FOR_")) {
			return true;
		}
		// **************
		// TODO
		if (ifPeek("ID_")) {
			return true;
		}
		// ***************
		if (ifPeek("RETURN_")) {
			return true;
		}
		if (ifPeek("BREAK_")) {
			return true;
		}
		if (ifPeek("CONT_")) {
			return true;
		}
		return false;
	}

	/*
	 * Checks if the next peek token can possibly lead into the <stmt> rule
	 */
	public boolean ifPeekIsBlockStmt() {
		if (ifPeek("IF_")) {
			return true;
		}
		if (ifPeek("WHILE_")) {
			return true;
		}
		if (ifPeek("FOR_")) {
			return true;
		}
		// **************
		// TODO
		if (ifPeekIsType()) {
			return true;
		}
		// ***************
		if (ifPeek("RETURN_")) {
			return true;
		}
		if (ifPeek("BREAK_")) {
			return true;
		}
		if (ifPeek("CONT_")) {
			return true;
		}
		return false;
	}

	/**
	 * @throws LexemeException
	 * 
	 */
	public void doStmt(String callerName) throws LexemeException {
		logMessage("<stmt> --> <if>" + "\n" + "		| <while> " + "\n"
				+ "		| <for>" + "\n" + "		| [<exp>];" + "\n" + "		| <block>"
				+ "\n" + "		| <varExp> = <exp>;" + "\n" + "		| <block>" + "\n"
				+ "		| return  [ <exp> ] ;" + "\n" + "		| break ;" + "\n"
				+ "		| continue ; ");
		functionStack.push("<stmt>");
		// |<if>
		if (ifPeek("IF_")) {
			doIf("<stmt>");
		} else if (ifPeek("WHILE_")) {
			doWhile("<stmt>");
		} else if (ifPeek("FOR_")) {
			doFor("<stmt>");
		}
		// TODO this will probably have to be changed
		else if (ifPeek("ID_")) {
			doBlockExp("<blockStmt>");
			ifPeekThenConsume("SEMI_COL_");
		} else if (ifPeek("L_CURLY_")) {
			doBlock("<blockStmt>");
		} else if (ifPeek("RETURN_")) {
			consumeToken();
			if (!ifPeek("SEMI_COL_")) {
				doExp("<stmt>");
			}
			ifPeekThenConsume("SEMI_COL_");
			// TODO need to add option <exp>
		}
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	// *******************************************************************************
	/**
	 * @throws LexemeException
	 * 
	 */
	public void doBlockStmt(String callerName) throws LexemeException {
		logMessage("<blockStmt> -->" + "	<if>" + "\n" + "			| <while> " + "\n"
				+ "			| <for>" + "\n" + "			| [<exp>];" + "\n"
				+ "			| <blockExp>" + "\n" + "			| <block>" + "\n"
				+ "			| return  [ <exp> ] ;" + "\n" + "			| break ;" + "\n"
				+ "			| continue ; ");
		functionStack.push("<blockStmt>");
		// <type> ID [= <exp> ] {,ID[=<exp>]}
		if (ifPeek("ID_")) {
			doBlockExp("<blockStmt>");
			ifPeekThenConsume("SEMI_COL_");
		}
		// |<if>
		if (ifPeek("IF_")) {
			doIf("<blockStmt>");
		} else if (ifPeek("WHILE_")) {
			doWhile("<blockStmt>");
		} else if (ifPeek("FOR_")) {
			doFor("<blockStmt>");
		}
		// TODO this will probably have to be changed
		else if (ifPeek("ID_")) {
			doBlockExp("<blockStmt>");
			ifPeekThenConsume("SEMI_COL_");
		} else if (ifPeek("L_CURLY_")) {
			doBlock("<blockStmt>");
		} else if (ifPeek("RETURN_")) {
			consumeToken();
			if (!ifPeek("SEMI_COL_")) {
				doExp("<blockStmt>");
			}
			ifPeekThenConsume("SEMI_COL_");
			// TODO need to add option <exp>
		} else if (ifPeekIsType()) {
			doBlockExp("<blockStmt>");
			ifPeekThenConsume("SEMI_COL_");
		}
		switch (peekToken.getType()) {
		case "BREAK_":
			consumeToken();
			ifPeekThenConsume("SEMI_COL_");
			break;
		case "CONT_":
			consumeToken();
			ifPeekThenConsume("SEMI_COL_");
		default:
			break;
		}

		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * @throws LexemeException
	 * 
	 */
	public void doActuals(String callerName) throws LexemeException {
		logMessage("<actuals> --> <exp> {,<exp>} ");
		functionStack.push("<actuals>");
		doExp("<actuals>");
		while (ifPeek("COMMA_")) {
			consumeToken();
			doExp("<actuals>");
		}
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * @throws LexemeException
	 * 
	 */
	public void doIf(String callerName) throws LexemeException {
		logMessage("<if> --> if ( <exp> ) <stmt> [ else <stmt> ]");
		functionStack.push("<if>");
		consumeToken();
		ifPeekThenConsume("L_PAREN_");
		doExp("<if>");
		ifPeekThenConsume("R_PAREN_");
		doStmt("<if>");
		if (ifPeek("ELSE_")) {
			consumeToken();
			doStmt("if");
		}
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * @throws LexemeException
	 * 
	 */
	public void doWhile(String callerName) throws LexemeException {
		logMessage("<while> --> while ( <exp> ) <stmt>");
		functionStack.push("<while>");
		consumeToken();
		ifPeekThenConsume("L_PAREN_");
		doExp("<while>");
		ifPeekThenConsume("R_PAREN_");
		doStmt("<while>");
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * @throws LexemeException
	 * 
	 */
	public void doFor(String callerName) throws LexemeException {
		logMessage("<for> --> for ( [ <locals>  ] ; [ <exp> ] ; [ <exp> { , <exp> } ] ) <stmt>");
		functionStack.push("<for>");
		consumeToken();
		ifPeekThenConsume("L_PAREN_");
		if (ifPeekIsType()) {
			doLocals("<for>");
		}
		ifPeekThenConsume("SEMI_COL_");
		// TODO make optional
		doExp("<for>");
		ifPeekThenConsume("SEMI_COL_");
		// TODO make optional
		doExp("<for>");
		ifPeekThenConsume("R_PAREN_");
		doStmt("<for>");
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * 
	 */
	public void doInfixOperator(String callerName) {
		logMessage("<infixOperator> --> &&  | || | == | != | > | < | <=  | >= | + | - | * | /");
		functionStack.push("<infixOperator>");
		consumeToken();
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * 
	 */
	public void doUnaryOperator(String callerName) {
		logMessage("<infixOperator> --> ! | - ");
		functionStack.push("<unaryOperator>");
		consumeToken();
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * 
	 */
	public void doLiteralConstant(String callerName) {
		logMessage("<literalConstant> --> INT | CHAR | STRING| null | true | false");
		functionStack.push("<literalconstant>");
		if (ifPeek("INT_LIT")) {
			consumeToken();
		} else if (ifPeek("CHAR_LIT")) {
			consumeToken();
		} else if (ifPeek("STRING_LIT")) {
			consumeToken();
		} else if (ifPeek("NULL_")) {
			consumeToken();
		} else if (ifPeek("TRUE_")) {
			consumeToken();
		} else if (ifPeek("FALSE_")) {
			consumeToken();
		}
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * helper function for liternal constant
	 */
	public boolean ifPeekIsLitConstant() {
		if (ifPeek("INT_LIT")) {
			return true;
		} else if (ifPeek("CHAR_LIT")) {
			return true;
		} else if (ifPeek("STRING_LIT")) {
			return true;
		} else if (ifPeek("NULL_")) {
			return true;
		} else if (ifPeek("TRUE_")) {
			return true;
		} else if (ifPeek("FALSE_")) {
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param string
	 * @throws LexemeException
	 */
	public void doExp(String callerName) throws LexemeException {
		logMessage("<exp> --> ID <exp'> " + "\n" + "		| ID = <exp> <exp'> "
				+ "\n" + "		| <literalConstant> <exp'>" + "\n"
				+ "		|( this	| super ) <exp'>" + "\n" + "		| new ID ( ) <exp'>"
				+ "\n" + "		|( this | super ) <exp'>" + "\n"
				+ "		| new <nameType> [ <exp> ] { [ ] } <exp'>" + "\n"
				+ "		|new <nameType>	[ <exp> ] { [ ] } <exp'>;" + "\n"
				+ "		|<unaryOperator> <exp> <exp'>" + "\n"
				+ "		| ( <exp> ) <exp>");
		functionStack.push("<exp>");
		// --> ID <exp’>
		if (ifPeek("ID_")) {
			consumeToken();
			doExpPrime("<exp>");
		}
		// | = <exp> <exp>
		else if (ifPeek("ASSIGN_")) {
			consumeToken();
			doExp("<exp'>");
			doExpPrime("<exp'>");
		} else if (ifPeekIsLitConstant()) {
			doLiteralConstant("<exp>");
			doExpPrime("<exp>");
		}
		// |(this | super) <exp'>
		else if (ifPeek("THIS_") || ifPeek("SUPER_")) {
			consumeToken();
			doExpPrime("<exp>");
		}
		// new ID () <exp'>
		else if (ifPeek("NEW_")) {
			if (ifPeek("ID_")) {
				ifPeekThenConsume("L_PAREN_");
				ifPeekThenConsume("R_PAREN_");
				doExpPrime("<exp>");
			} else {
				if (ifPeekIsType()) {
					doNameType("<exp>");
					ifPeekThenConsume("L_BRAC_");
					doExp("<exp>");
					ifPeekThenConsume("R_BRAC_");
					while (ifPeek("L_BRAC_")) {
						consumeToken();
						ifPeekThenConsume("R_BRAC_");
					}
					doExpPrime("<exp>");
				}
			}
		}
		// <unaryOperator> <exp><exp'>
		else if (ifPeek("UNARY_") || ifPeek("MINUS_")) {
			doUnaryOperator("<exp>");
			doExp("<exp>");
			doExpPrime("<exp>");
		}
		// ( <exp> ) <exp'>
		else if (ifPeek("L_PAREN_")) {
			consumeToken();
			doExp("<exp>");
			ifPeekThenConsume("R_PAREN_");
			doExpPrime("<exp>");
		} else {
			ifPeek("PERIOD_");
			doExpPrime("<exp>");
		}
		// flipping done
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * @throws LexemeException
	 * 
	 */
	public void doExpPrime(String callerName) throws LexemeException {
		logMessage("<exp'> --> <infixOperator> <exp> <exp'> " + "\n"
				+ "		| . ID <exp'> " + "\n"
				+ "		| . ID ( [ <actuals> ] ) <exp'>" + "\n"
				+ "		| . ID = <exp> <exp'>" + "\n"
				+ "		|[ <exp> ] = <exp> <exp'>" + "\n" + "		|[ <exp> ] <exp'>"
				+ "\n" + "		|E");

		functionStack.push("<exp'>");
		// <infix operator> <exp> <exp'>
		if (ifPeek("INFIX_") || ifPeek("MINUS_")) {
			doInfixOperator("<exp'>");
			doExp("<exp'>");
			doExpPrime("<exp'>");
		} else if (ifPeek("ASSIGN_")) {
			consumeToken();
			doExp("<exp'>");
		}
		// <period>
		else if (ifPeek("PERIOD_")) {
			consumeToken();
			ifPeekThenConsume("ID_");
			if (ifPeek("L_PAREN_")) {
				consumeToken();
				doActuals("<exp'>");
				ifPeekThenConsume("R_PAREN_");
				doExpPrime("<exp'>");
			} else if (ifPeek("ASSIGN_")) {
				consumeToken();
				doExp("<exp'>");
				doExpPrime("<exp'>");
			} else {
				doExpPrime("<exp'>");
			}
		}
		// </period>
		// <L_BRAC_>
		else if (ifPeek("L_BRAC_")) {
			consumeToken();
			doExp("<exp'>");
			ifPeekThenConsume("R_BRAC_");
			if (ifPeek("ASSIGN_")) {
				consumeToken();
				doExp("<exp'>");
				doExpPrime("<exp'>");
			} else {
				doExpPrime("<exp'>");
			}
		}
		// </L_BRAC_>
		logVerboseMessage(callerName);
		functionStack.pop();
	}

	/**
	 * @throws LexemeException
	 * 
	 */
	public void doBlockExp(String callerName) throws LexemeException {
		logMessage("<blockExp> --> (void | boolean | int) [[]] ID [= <exp>] {,ID [ = <exp>]} "
				+ "\n"
				+ "		| ID [[]] [= <exp>] {,ID [= <exp>]} "
				+ "\n"
				+ "		| ID <exp'>"
				+ "\n"
				+ "		| ID = <exp> <exp'>"
				+ "\n"
				+ "		| <literalConstant> <exp'>"
				+ "\n"
				+ "		| ( this | super ) <exp'>"
				+ "\n"
				+ "		| new ID ( ) <exp'>"
				+ "\n"
				+ "		| new <nameType> [ <exp> ] { [ ] } <exp'>;"
				+ "\n"
				+ "		| <unaryOperator> <exp> <exp'>"
				+ "\n"
				+ "		| ( <exp> ) <exp'> ");
		functionStack.push("<blockExp>");
		// ID [ [ ] ] [ = <exp> ] { , ID [ = <exp> ] }
		if (ifPeek("ID_")) {
			consumeToken();
			if (ifPeek("ASSIGN_")) {
				consumeToken();
				doExp("<blockExp>");
				doExpPrime("<blockExp>");
			} else if (ifPeek("L_BRAC_")) {
				consumeToken();
				ifPeekThenConsume("R_BRAC_");
				ifPeekThenConsume("ID_");
				if (ifPeek("ASSIGN_")) {
					consumeToken();
					doExp("<blockExp>");
				}
				while (ifPeek("COMMA_")) {
					consumeToken();
					ifPeekThenConsume("ID_");
					if (ifPeek("ASSIGN_")) {
						consumeToken();
						doExp("<blockExp>");
					}
				}
			}

			else {
				if (ifPeek("ID_")) {
					consumeToken();
					while (ifPeek("COMMA_")) {
						consumeToken();
						ifPeekThenConsume("ID_");
						if (ifPeek("ASSIGN_")) {
							consumeToken();
							doExp("<blockExp>");
						}
					}
				}
				doExp("<blockExp>");
			}
		}
		// ( void | boolean | int ) [ [ ] ] ) ID [ = <exp> ] {, ID [ = <exp> ] }
		else if (ifPeekIsType()) {
			if (ifPeekIsType() && !ifPeek("ID")) {
				doType("<blockExp>");
			}
			ifPeekThenConsume("ID_");
			if (ifPeek("ASSIGN_")) {
				consumeToken();
				doExp("<blockExp>");
			}
			while (ifPeek("COMMA_")) {
				consumeToken();
				ifPeekThenConsume("ID_");
				if (ifPeek("ASSIGN_")) {
					consumeToken();
					doExp("<blockExp>");
				}
			}
		}
		logVerboseMessage(callerName);
		functionStack.pop();
	}

}
