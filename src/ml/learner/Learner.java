package ml.learner;

import ml.data.DataSet;
import ml.data.Instance;

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
