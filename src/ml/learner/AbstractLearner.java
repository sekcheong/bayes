package ml.learner;

import ml.data.DataSet;
import ml.data.Instance;

public abstract class AbstractLearner implements Learner {
	private VerboseMode _verboseMode = VerboseMode.NONE;

	public abstract void train(DataSet train);

	public abstract double test(DataSet test);

	public abstract int classify(Instance instance);

	public void setVerboseMode(VerboseMode m) {
		_verboseMode = m;
	}

	public VerboseMode verboseMode() {
		return _verboseMode;
	}

	protected void printf(String format, Object... args) {
		System.out.printf(format, args);
	}

	protected void printDebug(String format, Object... args) {
		if (_verboseMode.ordinal()<VerboseMode.DEBUG.ordinal()) return;
		System.out.printf(format, args);
	}

	protected void printWarning(String format, Object... args) {
		if (_verboseMode.ordinal() < VerboseMode.WARNING.ordinal()) return;
		System.out.printf(format, args);
	}

	protected void printInfo(String format, Object... args) {
		if (_verboseMode.ordinal() < VerboseMode.INFO.ordinal()) return;
		System.out.printf(format, args);
	}
}
