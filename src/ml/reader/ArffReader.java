package ml.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import ml.data.DataSet;
import ml.data.Feature;
import ml.data.Instance;
import ml.data.Value;
import ml.reader.Token.TokenType;

public class ArffReader {
	private static final String DECL_RELATION = "relation";
	private static final String DECL_ATTRIBUTE = "attribute";
	private static final String DECL_DATA = "data";
	private static final String TYPE_STRING = "string";
	private static final String TYPE_NUMERIC = "numeric";
	private static final String TYPE_REAL = "real";
	private static final String TYPE_INTEGER = "integer";
	private static final String TYPE_DATE = "date";

	private String _fileName;

	public ArffReader(String fileName) {
		_fileName = fileName;
	}

	public DataSet readDataSet() throws Exception {
		FileReader fr = null;
		BufferedReader br = null;
		DataSet ds = null;

		try {
			fr = new FileReader(_fileName);
			br = new BufferedReader(fr);
			Tokenizer tokenizer = new Tokenizer(br);
			try {
				ds = parseDataSet(tokenizer);
			}
			catch (Exception ex) {
				throw new Exception("Error (" + tokenizer.currentLine() + "," + tokenizer.currentColumn() + "):" + ex.getMessage(), ex);
			}
		}
		finally {
			if (br != null) br.close();
			if (fr != null) fr.close();
		}

		return ds;
	}

	private DataSet parseDataSet(Tokenizer tokens) throws Exception {
		DataSet ds = parseHeader(tokens);
		parseData(tokens, ds);
		return ds;
	}
	
	private DataSet parseHeader(Tokenizer tokens) throws Exception {
		String relation = parseRelationName(tokens);
		List<Feature> features = parseAttributes(tokens);
		return new DataSet(relation, features);
	}
	
	private String parseRelationName(Tokenizer tokens) throws Exception {
		Token t = tokens.nextToken();
		if (t.type() != Token.TokenType.DECLARATION) {
			throw new Exception("No declarations found in the file header.");
		}
		if (t.stringValue().toLowerCase().equals(DECL_RELATION)) {
			t = tokens.nextToken();
			if (t.type()!=Token.TokenType.STRING) throw new Exception( "The relation name must be a string.");
			return t.stringValue();
		}
		else {
			tokens.putbackToken();
		}
		return null;
	}

	private List<Feature> parseAttributes(Tokenizer tokens) throws Exception {
		List<Feature> features = new ArrayList<Feature>();
		int count = 0;
		while (true) {
			Token t = tokens.nextToken();
			if (t.type() != Token.TokenType.DECLARATION) {
				throw new Exception("No attribute found in the file header.");
			}

			if (!t.stringValue().toLowerCase().equals(DECL_ATTRIBUTE)) {
				tokens.putbackToken();
				break;
			}
			
			t = tokens.nextToken();
			if (t.type() != Token.TokenType.STRING) {
				throw new Exception("The attribute name must be a string.");
			}
			
			String name = t.stringValue();
			if (name.isEmpty()) {
				throw new Exception("The attribute name canot be empty.");
			}
			
			if (!Character.isLetter(name.charAt(0))) {
				throw new Exception("The attribute name must start with a letter.");
			}
			
			Feature feature = null;
			t = tokens.nextToken();
			if (t.type()==Token.TokenType.OPENBRACE) {
					//nominal attribute
					List<String> labels = parseDiscreteValues(tokens);
					feature = new Feature(name, count, labels);
			}
			else {
				if (t.type()!=Token.TokenType.STRING) {
					throw new Exception("Attribute data type must be a string");
				}
				String dataType = t.stringValue().toLowerCase();
				if (dataType.equals(TYPE_NUMERIC) || dataType.equals(TYPE_REAL) || dataType.equals(TYPE_INTEGER)) {
					feature = new Feature(name, count, Feature.FeatureType.NUMERIC);
				}
				else if (dataType.equals(TYPE_STRING)) {
					feature = new Feature(name, count, Feature.FeatureType.STRING);
				}
				else if (dataType.equals(TYPE_DATE)) {
					t = tokens.nextToken();
					if (t.type()==TokenType.STRING) {
						feature = new Feature(name, count, new SimpleDateFormat(t.stringValue()));
					}
					else {
						feature = new Feature(name, count, new SimpleDateFormat());
						tokens.putbackToken();
					}
				}
				else {
					throw new Exception("Unsupported attribute data type.");
				}
			}
			features.add(feature);
			count ++ ;
		}
		return features;
	}

	private List<String> parseDiscreteValues(Tokenizer tokens) throws Exception {
		ArrayList<String> labels = new ArrayList<String>();
		boolean lastString = false;
		while (true) {
			Token t = tokens.nextToken();
			if (t.type()==Token.TokenType.STRING) {
				if (lastString) throw new Exception("The nominal value must be a string.");
				String val = t.stringValue();
				if (val.isEmpty()) throw new Exception("The nominal value cannot be empty.");
				labels.add(val);
				lastString = true;
			}
			else if (t.type()==TokenType.COMMA) {
				if (!lastString) throw new Exception("The nominal value must be a string.");
				lastString = false;
			}
			else if (t.type()==TokenType.CLOSEBRACE) {
				if (!lastString) throw new Exception("The nominal value must be a string.");
				if (labels.size()<1) throw new Exception("The nominal values cannot be empty.");
				return labels;
			}
			else if (t.type()==TokenType.EOF) {
				throw new Exception("Missing the end brace for the nominal attribute.");
			}
		}
	}

	private void parseData(Tokenizer tokens, DataSet ds) throws Exception {
		Token t = tokens.nextToken();
		if (t.type()!=TokenType.DECLARATION || !t.stringValue().equals((DECL_DATA))) throw new Exception("Expecting @data declaration.");
		while (true) {
			Instance c = parseDataRow(tokens, ds);
			if (c==null) break;
			ds.instances().add(c);
		}
	}

	private Instance parseDataRow(Tokenizer tokens, DataSet ds) throws Exception {
		Instance c = new Instance(ds.features().length);
		int i=0;
		while (true) {
			Token t = tokens.nextToken();
			if (t.type()==TokenType.EOF) {
				if (i==0) {
					return null;
				}
				else throw new Exception("Missing value at feature position " + i + ".");
			}
			
			if (t.type()==TokenType.COMMA) {
				if (i==0) throw new Exception("The attribute value is missing."); 
				continue;
			}

			Value v = parseValue(ds.features()[i], t);
			c.setValue(v);
			
			i++;
			if (i==ds.features().length) {
				return c;
			}
		}
	}

	private Value parseValue(Feature feature, Token t) throws Exception {
		if (t.type()==Token.TokenType.QUESTIONMARK) {
			return new Value(feature);
		}
		else {
			if (t.type()!=Token.TokenType.STRING) throw new Exception("The attribute value is missing.");
		}
		switch (feature.type()) {
			case NUMERIC:
				return new Value(feature, Double.parseDouble(t.stringValue()));
			case DISCRETE:
				int labelIdx = feature.indexOfLabel(t.stringValue());
				if (labelIdx<0) {
					throw new Exception("Invalid discrete value: " + t.stringValue());
				}
				return new Value(feature, labelIdx);
			case STRING:
				return new Value(feature, t.stringValue());
			case DATE:
				return new Value(feature, feature.dateFormat().parse(t.stringValue()));
		}
		return null;
	}

}