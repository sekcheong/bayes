package edu.wisc.ml.reader;

import java.io.BufferedReader;
import java.io.IOException;

import edu.wisc.ml.reader.Token.TokenType;

public class Tokenizer {
	private BufferedReader _br;
	private boolean _putbackChar = false;
	private int _currChar = 0;
	private boolean _putbackToken = false;
	private Token _currToken = null;
	private int _line = 0;
	private int _column = 0;

	public Tokenizer(BufferedReader br) {
		_br = br;
	}

	public Token nextToken() throws Exception {
		if (!_putbackToken) {
			_currToken = this.getNextToken();
		}
		else {
			_putbackToken = false;
		}
		return _currToken;
	}

	public void putbackToken() {
		_putbackToken = true;
	}

	private Token getNextToken() throws Exception {
		int n;
		char ch;
		
		while (true) {
			skipWhiteSpaces();
			n = getNextChar();
			if (n==-1) return new Token(Token.TokenType.EOF);
			ch = (char) n;
			switch (ch) {
				case '\'':
				case '"':
					return matchQuotedString(ch);
				case '@':
					return matchDeclaration();
				case '{':
					return new Token(TokenType.OPENBRACE);
				case '}':
					return new Token(TokenType.CLOSEBRACE);
				case ',':
					return new Token(TokenType.COMMA);
				case '?':
					return new Token(TokenType.QUESTIONMARK);
				case '%':
					skipComments();
					break;
				default:
					putbackChar();
					String str = matchNakedString();
					if (str != null) return new Token(TokenType.STRING, str);
					break;
			}
		}
	}
	
	private void skipComments() throws IOException {
		int n;
		while ((n = getNextChar()) != -1) {
			if (n=='\n' || n=='\r') {
				putbackChar();
				return;
			}
		}
	}

	private void skipWhiteSpaces() throws IOException {
		int n;
		while ((n = getNextChar()) != -1) {
			if (n=='\n') {
				_line ++;
				_column = 0;
			}
			if (!Character.isWhitespace(n)) {
				putbackChar();
				break;
			}
		}
	}

	private Token matchDeclaration() throws Exception {
		int n;
		char ch;
		StringBuffer sb = new StringBuffer();
		while (((n = getNextChar()) != -1)) {		
			ch = (char) n;
			if (Character.isLetter(ch)) sb.append(ch);
			else {
				putbackChar();
				break;
			}
		}
		if (sb.length()>0) return new Token(Token.TokenType.DECLARATION, sb.toString());
		throw new Exception("The declaration name must be a string.");
	}

	private Token matchQuotedString(char quote) throws Exception {
		int n;
		char ch;
		boolean inEscape = false;
		StringBuffer sb = new StringBuffer();
		
		while (((n = getNextChar()) != -1)) {		
			ch = (char) n;
			
			//line breaks are not allowed in quoted strings
			if (ch=='\n' || ch == '\r') break;
			
			if (ch == '\\') {
				inEscape = true;
				continue;
			}

			if (inEscape) {
				ch = xlateEscapeChar(ch);
				inEscape=false;
			}
			else {
				if (ch == quote) return new Token(TokenType.STRING, (sb.length() > 0) ? sb.toString() : "");
			}
			sb.append(ch);
		}
		
		throw new Exception("Missing end quote for string value.");
	}
	
	private char xlateEscapeChar(char ch) {
		switch (ch) {
			case 'n':
				return '\n';
			case 'r':
				return '\r';
			case 't':
				return '\t';
			case 'f':
				return '\f';
			case '\\':
				return '\\';
			case '\'':
				return '\'';
			case '"':
				return '"';
		}
		return ch;
	}

	private String matchNakedString() throws Exception {
		int n;
		char ch;
		StringBuffer sb = new StringBuffer();
		while (((n = getNextChar()) != -1)) {		
			ch = (char) n;
			if (isSymbol(ch) || Character.isWhitespace(ch)) {
				putbackChar();
				break;
			}
			sb.append(ch);
		}
		if (sb.length()>0) return sb.toString();
		return null;
	}

	private boolean isSymbol(char ch) {
		switch (ch) {
			case '{':
			case '}':
			case ',':
			case '@':
			case '?':
			case '%':
				return true;
		}
		return false;
	}


	private int getNextChar() throws IOException {
		if (_putbackChar) {
			_putbackChar = false;
		}
		else {
			_currChar = _br.read();
			_column++;
		}
		return _currChar;
	}

	private void putbackChar() {
		_putbackChar = true;
	}
	
	public int currentLine() {
		return _line+1;
	}
	
	public int currentColumn() {
		return _column+1;
	}
	
}
