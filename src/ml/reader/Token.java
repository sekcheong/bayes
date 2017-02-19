package edu.wisc.ml.reader;

public class Token {
	
	public enum TokenType {
		STRING,
		DECLARATION,
		COMMA,
		OPENBRACE,
		CLOSEBRACE,
		QUESTIONMARK,
		EOF
	}
	
	private TokenType _type;
	private Object _value;
	
	public Token(TokenType type) {
		_type = type;
		_value = null;
	}
	
	public Token(TokenType type, String value) {
		_type = type;
		_value = value;
	}
	
	public char charValue() {
		return (Character) _value;
	}
	
	public String stringValue() {
		return (String) _value;
	}
	
	public double realValue() {
		return Double.parseDouble((String)_value);
	}
	
	public int intValue() {
		return Integer.parseInt((String) _value);
	}
	
	public TokenType type() {
		return _type;
	}
	
}
