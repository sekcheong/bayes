package edu.wisc.ml.learner;

import edu.wisc.ml.data.DataSet;
import edu.wisc.ml.data.Instance;

public  interface Learner {
	
	public enum VerboseMode {
		NONE,
		INFO,
		WARNING,
		DEBUG;
	}

	
	public void train(DataSet train);

	public double test(DataSet test);

	public int classify(Instance instance);
	
	public void setVerboseMode(VerboseMode m);
	public VerboseMode verboseMode();
	
	
}
