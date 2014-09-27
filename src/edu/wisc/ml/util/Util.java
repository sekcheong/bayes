package edu.wisc.ml.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.wisc.ml.data.DataSet;
import edu.wisc.ml.data.Instance;


public class Util {
	public static String formatString(String str) {
		str = str.replace("\n", "\\n");
		str = str.replace("\r", "\\r");
		str = str.replace("\t", "\\t");
		str = str.replace("\f", "\\f");
		str = str.replace("\"", "\\\"");
		str = str.replace("'", "\\'");
		str = str.replace("\\", "\\\\");
		if (str.contains("\\") || str.contains(" ")) {
			return "'" + str + "'";
		}
		return str;
	}
	
	@SuppressWarnings("unchecked")
	private static List<Instance>[] partitionDataSet(DataSet data) {
		List<Instance>[] s = new ArrayList[2];
		s[0] = new ArrayList<Instance>();
		s[1] = new ArrayList<Instance>();
		for (Instance d : data.instances()) {
			int index = d.valueAt(data.target()).intValue();
			s[index].add(d);
		}
		return s;
	} 

	
	public static DataSet[] randomSamples(DataSet ds, int numSets, int size) {

		if (size >= ds.instances().size()) {
			DataSet[] sets = new DataSet[1];
			sets[0] = ds;
			return sets;
		}
		
		DataSet[] sets= new DataSet[numSets];
		List<Instance> samples = new ArrayList<Instance>(ds.instances());
		for (int i = 0; i < numSets; i++) {
			Collections.shuffle(samples);
			List<Instance> ins = new ArrayList<Instance>();
			for (int j=0; j<size; j++) {
				ins.add(samples.get(j));
			}
			sets[i]=new DataSet(ds,ins);
		}
		return sets;
	}
	
	
	public static DataSet[] stratifySamples(DataSet ds, int numSets, int size) {

		if (size >= ds.instances().size()) {
			DataSet[] sets = new DataSet[1];
			sets[0] = ds;
			return sets;
		}

		List<Instance>[] s = partitionDataSet(ds);
		
		int total = (s[0].size() + s[1].size());
		double negRate = (double) s[0].size() / total;
		int n = (int) (size * negRate);
		
		DataSet[] sets= new DataSet[numSets];

		for (int i = 0; i < numSets; i++) {
			List<Instance> ins = new ArrayList<Instance>();
			Collections.shuffle(s[0]);
			Collections.shuffle(s[1]);

			// select negative examples
			for (int j = 0; j < n; j++) {
				ins.add(s[0].get(j));
			}

			// select positive examples
			for (int j = 0; j < size - n; j++) {
				ins.add(s[1].get(j));
			}

			Collections.shuffle(ins);

			sets[i] = new DataSet(ds, ins);
		}
		return sets;
	}
	
}
