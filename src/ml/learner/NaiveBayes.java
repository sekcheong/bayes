package ml.learner;

import ml.data.*;

public class NaiveBayes extends AbstractLearner {
	private int[][][] _counts;
	private double[][][] _probXgY;
	private double[] _pY;
	private DataSet _dataSet;

	@Override
	public void train(DataSet train) {
		int t = train.target();
		int t_len = train.features()[t].labels().length;

		_counts = new int[train.features().length][][];
		_probXgY = new double[train.features().length][][];
		_pY = new double[t_len];
		_dataSet = train;
		
		for (int i = 0; i < train.features().length; i++) {
			Feature f = train.features()[i];

			int f_len = f.labels().length;
			_counts[i] = new int[f_len][t_len];
			_probXgY[i] = new double[f_len][t_len];

			for (Instance x : train.instances()) {
				int j = x.valueAt(i).intValue();
				int k = x.valueAt(t).intValue();
				_counts[i][j][k]++;
				if (i == 0) _pY[k]++;
			}

		}

		for (int i = 0; i < train.features().length; i++) {
			Feature f = train.features()[i];
			int[] c = new int[t_len];
			for (int j = 0; j < f.labels().length; j++) {
				for (int k = 0; k < t_len; k++) {
					c[k] = c[k] + _counts[i][j][k];
				}
			}
			for (int j = 0; j < f.labels().length; j++) {
				for (int k = 0; k < t_len; k++) {
					// Use Laplace estimate +1 pseudo-count
					_probXgY[i][j][k] = (double) (_counts[i][j][k] + 1) / (c[k] + f.labels().length);
				}
			}
		}

		for (int i = 0; i < _pY.length; i++) {
			_pY[i] = (double) (_pY[i] + 1) / (train.instances().size() + _pY.length);
		}

	}

	@Override
	public double test(DataSet test) {
		int correct = 0;

		for (Feature f : _dataSet.features()) {
			if (f.index() == _dataSet.target()) continue;
			printInfo("%s class\n", f.name());
		}
		printInfo("\n");
		
		for (Instance instance : test.instances()) {
			int p = this.classify(instance);
			if (p == instance.valueAt(test.target()).intValue()) {
				correct++;
			}
		}

		printInfo("\n%d\n", correct);
		
		double acc = ((double) correct) / test.instances().size();
		printDebug("Accuracy: %f\n",acc);
		return acc;
	}

	@Override
	public int classify(Instance instance) {
		int target = _dataSet.target();
		int tlen = _dataSet.features()[target].labels().length;
		int maxK = 0;
		double totalP = 0;
		double maxP = Double.NEGATIVE_INFINITY;
		double logP[] = new double[tlen];

		for (int k = 0; k < tlen; k++) {
			for (int i = 0; i < instance.length(); i++) {
				if (i == target) continue;
				int j = instance.valueAt(i).intValue();
				logP[k] = logP[k] + Math.log(_probXgY[i][j][k]);
			}
			logP[k] = logP[k] + Math.log(_pY[k]);
			if (logP[k] > maxP) {
				maxP = logP[k];
				maxK = k;
			}
			totalP += Math.exp(logP[k]);
		}

		double post = Math.exp(logP[maxK]) / totalP;
		printInfo("%s %s %.16f\n", _dataSet.features()[target].labels()[maxK], instance.valueAt(target), post);

		return maxK;
	}

}
