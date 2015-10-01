JCC = javac

objects = LexicalAnalyser.java LexicalStateMachine.java Token.java Parser.java ParserRules.java ParserLog.java LexemeException.java ParserTest.java

Parse.class:
	$(JCC) $(objects)

.PHONY: clean

clean:
	rm *.class *~ ParserLog.txt
