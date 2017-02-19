package edu.wisc.ml.data;

import java.util.Date;

import edu.wisc.ml.util.Util;


public class Value implements Comparable<Value> {
	private Feature _feature;
	boolean _isMissing = false;
	private double _realValue;
	private int _intValue;
	private Object _objectValue;
	
	public Value(Feature feature) {
		_feature = feature;
		_isMissing = true;
	}
	
	public Value(Feature feature, double value) throws Exception {
		if (feature.type()!=Feature.FeatureType.NUMERIC) throw new Exception("Incompatible data type numeric and " + feature.type().toString() + ".");
		_feature = feature;
		_realValue = value;
	}
	
	public Value(Feature feature, int value) throws Exception {
		if (feature.type()!=Feature.FeatureType.DISCRETE) throw new Exception("Incompatible data type discrete and " + feature.type().toString() + ".");
		_feature = feature;
		_intValue = value;
	}
	
	public Value(Feature feature, String value) throws Exception {
		if (feature.type()!=Feature.FeatureType.STRING) throw new Exception("Incompatible data type string and " + feature.type().toString() + ".");
		_feature = feature;
		_objectValue = value;
	}
	
	public Value(Feature feature, Date value) throws Exception {
		if (feature.type()!=Feature.FeatureType.DATE) throw new Exception("Incompatible data type date and " + feature.type().toString() + ".");
		_feature = feature;
		_objectValue = value;
	}
	
	
	public Feature feature() {
		return _feature;
	}
	
	public Feature.FeatureType type() {
		return _feature.type();
	}
	
	public double realValue() {
		return _realValue;
	}
	
	public int intValue() {
		return _intValue;
	}
	
	public String stringValue() {
		return (String) _objectValue;
	}
	
	public Date dateValue() {
		return (Date) _objectValue;
	}
	
	public boolean isMissing() {
		return _isMissing;
	}

	@Override
	public int compareTo(Value other) {
		if (other==null) return 1;
		switch (_feature.type()) {
			case NUMERIC:
				return Double.compare(_realValue, other.realValue());
			case DISCRETE:
				if (_intValue==other.intValue()) return 0;
				if (_intValue<other.intValue()) return -1;
				return 1;
			case STRING:
				return ((String) _objectValue).compareTo(other.stringValue());
			case DATE:
				return ((Date) _objectValue).compareTo(other.dateValue());
		}
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		return this.compareTo((Value) obj)==0;
	}

	@Override
	public String toString() {
		if (_isMissing) return "?";
		switch (_feature.type()) {
			case NUMERIC:
				String v = Double.toString(_realValue);
				if (v.endsWith(".0")) v = v.substring(0,v.length()-2);
				return v;
			case DISCRETE:
				return Util.formatString(_feature.labelAt(_intValue));
			case STRING:
				return Util.formatString((String)_objectValue);
			case DATE:
				return "\"" + _feature.dateFormat().format((Date)_objectValue) + "\"";
		}
		return null;
	}

}