package org.sikuli.syntaxhighlight;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import org.sikuli.syntaxhighlight.format.Formatter;
import org.sikuli.syntaxhighlight.grammar.Lexer;

public class Run {

	public static void main(String[] args) throws IOException, ResolutionException {
		String file = System.getProperty("user.dir") + "/org/sikuli/syntaxhighlight/Run.java";
		Lexer lexer = Lexer.getByName("java");
		Formatter formatter = Formatter.getByName("html");
		String code = Util.streamToString(new FileInputStream(file));
		formatter.format(lexer.getTokens(code), new PrintWriter(System.out));
	}
}
