package ml.data;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ml.utils.Utils;


public class DataSet {
	private String _relation = null;
	private Feature[] _features;
	private List<Instance> _instances = null;
	private int _target  = -1;
	private static final String CLASS_LABEL = "class";
	
	public DataSet(DataSet ref, List<Instance> instances) {
		_relation = ref.relation();
		_target = ref.target();
		_features = ref.features();
		_instances = instances;
	}
	
	public DataSet(String relation, List<Feature> features, List<Instance> instances) {
		init(relation, features, instances);
	}

	public DataSet(String relation, List<Feature> features) {
		init(relation, features, null);
	}
	

	private void init(String relation, List<Feature> features, List<Instance> instances) {
		_relation = relation;
		_features = new Feature[features.size()];
		_features = features.toArray(_features);
	
		if (instances != null) {
			_instances = instances;
		}
		else {
			_instances = new ArrayList<Instance>();
		}
		
		//starting from the last feature because usually the class label is the last one 
		for (int i=_features.length-1; i>=0; i--) {
			if (_features[i].name().toLowerCase().compareTo(CLASS_LABEL)==0) {
				_target = i;
				break;
			}
		}
	}
	
	public List<Instance> instances() {
		return _instances;
	}

	public Feature[] features() {
		return _features;
	}
	
	public void setTarget(int target) throws Exception {
		_target = target;
	}
	
	public int target() {
		return _target;
	}
	
	public String relation() {
		return _relation;
	}
	
	public DataSet[] split(double ratio) throws Exception {
		int n = (int) (_instances.size()*ratio);
		if (n==0 || n==_instances.size()) throw new Exception("Unable to split the instances into two sets.");
		
		List<Instance> s1 = new ArrayList<Instance>(_instances);
		List<Instance> s2 = new ArrayList<Instance>();
		Collections.shuffle(s1);
		for (int i=0; i<n; i++) {
			Instance d = s1.remove(0);
			s2.add(d);
		}
		List<Feature> features = new ArrayList<Feature>();
		for (Feature f:this.features()) {
			features.add(f);
		}
		
		DataSet[] ds = new DataSet[2];
		ds[0] = new DataSet(this , s1);
		ds[1] = new DataSet(this, s2);
		return ds;
	}
	
	public void saveToFile(String fileName) throws Exception {
		BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
		out.write(this.toString());
		out.close();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if (_relation!=null && !_relation.isEmpty()) {
			sb.append("@relation ").append(Utils.formatString(this.relation())).append("\n");
		}
		
		for (Feature f:this.features()) {
			sb.append(f.toString()).append("\n");
		}
		
		sb.append("@data\n");
		for (Instance q:this.instances()) {
			sb.append(q.toString()).append("\n");
		}
		
		return sb.toString();
	}
}
