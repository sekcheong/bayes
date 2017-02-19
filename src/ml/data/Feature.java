package ml.data;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import ml.utils.Utils;

public class Feature {
	private String _name;
	private FeatureType _type;
	private String[] _labels;
	private HashMap<String, Integer> _labelToIndex;
	private SimpleDateFormat _dateFormat;
	private int _index;
	
	public enum FeatureType {
		STRING,
		NUMERIC,
		DISCRETE,
		DATE
	}
	
	public Feature(String name, int index, FeatureType type) {
		init(name,index, type);
	}
	
	public Feature(String name,  int index,SimpleDateFormat dateFormat) {
		init(name,index, FeatureType.DATE);
		_dateFormat = dateFormat;
	}
	
	public Feature(String name, int index, List<String> labels) {
		init(name,index, FeatureType.DISCRETE);
		_labels = labels.toArray(new String[labels.size()]);
		_labelToIndex = new HashMap<String, Integer>();
		for (int i=0; i<_labels.length; i++) {
			_labelToIndex.put(_labels[i], i);
		}
	}
	
	private void init(String name, int index, FeatureType type) {
		_name = name;
		_type = type;
		_index = index;
	}
	public String name() {
		return _name;
	}
	
	public FeatureType type() {
		return _type;
	}

	public int index() {
		return _index;
	}
	
	public String[] labels() {
		return _labels;
	}
	
	public String labelAt(int index) {
		return _labels[index];
	}
	
	public int indexOfLabel(String label)  {
		if (!_labelToIndex.containsKey(label)) return -1;
		return _labelToIndex.get(label);
	}
	
	public SimpleDateFormat dateFormat() {
		return _dateFormat;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("@attribute ").append(Utils.formatString(_name)).append(" ");
		switch (_type) {
			case DISCRETE:
				sb.append("{");
				for (String s:_labels) { 
					sb.append( Utils.formatString(s)).append(",");
				}
				if (sb.length()>1) sb.setLength(sb.length()-1);
				sb.append("}");
				break;
				
			case NUMERIC:
				sb.append("numeric");
				break;
				
			case STRING:
				sb.append("string");
				
			case DATE:
				sb.append("date");
				break;
		}
		return sb.toString();
	}
	
}
