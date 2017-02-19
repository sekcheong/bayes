package ml.learner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ml.data.DataSet;
import ml.data.Feature;
import ml.data.Instance;

public class TanNaiveBayes extends AbstractLearner {
	private DataSet _dataSet;
	int[] _parents;

	@Override
	public void train(DataSet train) {

		printDebug("size:%d\n", train.instances().size());

		_dataSet = train;

		double[][] weights = new double[train.features().length][train.features().length];
		for (int i = 0; i < train.features().length - 1; i++) {
			for (int j = 0; j < train.features().length - 1; j++) {
				weights[i][j] = computeCMI(train, i, j);
				if (i == j) weights[i][j] = 0;
				printDebug("%f ", weights[i][j]);
			}
			printDebug("\n");
		}

		ArrayList<Node> nodes = findMST(train, weights);
	   _parents = new int[nodes.size()];
		for (Node v : nodes) {
			if (v.feature == 0) {
				_parents[0] = 0;
				continue;
			}
			_parents[v.feature] = v.parent.feature;
			printDebug("(%d,%d) w=%f\n", v.parent.feature, v.feature, v.weight);
		}

		for (Node n : nodes) {
			if (n.feature == 0) {
				Feature fx = train.features()[0];
				Feature fz = train.features()[train.target()];
				for (int vx = 0; vx < fx.labels().length; vx++) {
					for (int vz = 0; vz < fz.labels().length; vz++) {
						double prob = prob_x_given_z(train, fx.index(), vx, vz);
						printDebug("Pr(%d=%d | %d=%d)=%f\n", fx.index(), vx, fz.index(), vz, prob);
					}
				}
				continue;
			}

			computeCPT(train, n.feature, _parents[n.feature]);
		}

		Feature fz = train.features()[train.target()];
		for (int vz = 0; vz < fz.labels().length; vz++) {
			double prob = prob_z(train, vz);
			printDebug("Pr(%d=%d)=%.16f\n", fz.index(), vz, prob);
		}
	}

	@Override
	public double test(DataSet test) {
		int correct = 0;
		for (Feature f : _dataSet.features()) {
			if (f.index() != _dataSet.target()) {
				if (f.index() == 0) {
					printInfo("%s class\n", f.name());
					continue;
				}
				printInfo("%s %s class\n", f.name(), _dataSet.features()[_parents[f.index()]].name());
			}
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
		printDebug("Accuracy: %f\n", acc);
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

		for (int y = 0; y < tlen; y++) {
			logP[y] = logSumProb(instance, y, _parents);
			if (logP[y] > maxP) {
				maxP = logP[y];
				maxK = y;
			}
			totalP += Math.exp(logP[y]);
		}

		double post = Math.exp(logP[maxK]) / totalP;
		printInfo("%s %s %.16f\n", _dataSet.features()[target].labels()[maxK], instance.valueAt(target), post);
		
		return maxK;
	}

	private double logSumProb(Instance d, int z, int[] parents) {
		double logSum;
		Feature t = _dataSet.features()[_dataSet.target()];

		logSum = Math.log(prob_z(_dataSet, z));
		logSum = logSum + Math.log(prob_x_given_z(_dataSet, 0, d.valueAt(0).intValue(), z));

		for (int x = 1; x < d.length(); x++) {
			if (x == t.index()) continue;
			double prob = 0;
			int y = parents[x];

			prob = prob_x_given_yz(_dataSet, x, d.valueAt(x).intValue(), y, d.valueAt(y).intValue(), z);

			logSum = logSum + Math.log(prob);
		}
		return logSum;
	}

	private class Node implements Comparable<Node> {
		public int feature = -1;
		public double weight;
		public List<Node> adjecent = new ArrayList<Node>();
		public Node parent;

		@Override
		public int compareTo(Node o) {
			if (this.weight > o.weight) return -1;
			if (this.weight < o.weight) return 1;
			return 0;
		}
	}

	public ArrayList<Node> findMST(DataSet train, double[][] weights) {
		ArrayList<Node> graph = new ArrayList<Node>();

		for (int i = 0; i < train.features().length - 1; i++) {
			Node u = new Node();
			u.feature = i;
			graph.add(u);
		}

		for (Node u : graph) {
			for (int i = 0; i < train.features().length - 1; i++) {
				if (u.feature == i) continue;
				u.adjecent.add(graph.get(i));
			}
		}

		ArrayList<Node> nodes = new ArrayList<Node>();
		for (Node u : graph) {
			u.weight = Double.NEGATIVE_INFINITY;
			nodes.add(u);
		}

		Node r = graph.get(0);
		r.weight = Double.POSITIVE_INFINITY;
		r.parent = null;

		while (graph.size() > 0) {
			Node u = Collections.min(graph);
			graph.remove(u);
			for (Node v : u.adjecent) {
				if (graph.contains(v) && weights[u.feature][v.feature] > v.weight) {
					v.parent = u;
					v.weight = weights[u.feature][v.feature];
				}
			}
		}

		return nodes;
	}

	private double prob_z(DataSet ds, int zVal) {
		int count = 0;
		for (Instance i : ds.instances()) {
			if (i.valueAt(ds.target()).intValue() == zVal) {
				count++;
			}
		}
		return ((double) count + 1) / (ds.instances().size() + ds.features()[ds.target()].labels().length);
	}

	private double computeCMI(DataSet train, int i, int j) {
		double p = 0;
		Feature fi = train.features()[i];
		Feature fj = train.features()[j];
		for (int xi = 0; xi < fi.labels().length; xi++) {
			for (int xj = 0; xj < fj.labels().length; xj++) {
				for (int y = 0; y < train.features()[train.target()].labels().length; y++) {
					p += prob_xyz(train, i, xi, j, xj, y)
							* log2(prob_xy_given_z(train, i, xi, j, xj, y), prob_x_given_z(train, i, xi, y) * prob_x_given_z(train, j, xj, y));
				}
			}
		}
		return p;
	}

	private double prob_xyz(DataSet ds, int x, int xVal, int y, int yVal, int zVal) {
		int count = 0;
		double prob;

		for (Instance d : ds.instances()) {
			if (d.valueAt(x).intValue() == xVal) {
				if (d.valueAt(y).intValue() == yVal) {
					if (d.valueAt(ds.target()).intValue() == zVal) {
						count = count + 1;
					}
				}
			}
		}
		prob = count;
		int n1 = ds.features()[x].labels().length;
		int n2 = ds.features()[y].labels().length;
		int n3 = ds.features()[ds.target()].labels().length;
		return (prob + 1) / (ds.instances().size() + (n1 * n2 * n3));
	}

	private double prob_x_given_z(DataSet ds, int x, int xVal, int zVal) {
		int count = 0;
		ArrayList<Instance> s = new ArrayList<Instance>();

		for (Instance d : ds.instances()) {
			if (d.valueAt(ds.target()).intValue() == zVal) {
				s.add(d);
			}
		}

		for (Instance d : s) {
			if (d.valueAt(x).intValue() == xVal) {
				count++;
			}
		}
		int n1 = ds.features()[x].labels().length;
		return ((double) (count + 1)) / (s.size() + n1);
	}

	private double prob_xy_given_z(DataSet ds, int x, int xVal, int y, int yVal, int zVal) {
		double prob = 0;
		int count = 0;

		ArrayList<Instance> s = new ArrayList<Instance>();

		for (Instance d : ds.instances()) {
			if (d.valueAt(ds.target()).intValue() == zVal) {
				s.add(d);
			}
		}

		for (Instance d : s) {
			if (d.valueAt(x).intValue() == xVal) {
				if (d.valueAt(y).intValue() == yVal) {
					count = count + 1;
				}
			}
		}
		int n1 = ds.features()[x].labels().length;
		int n2 = ds.features()[y].labels().length;
		prob = count;
		return (prob + 1) / (s.size() + (n1 * n2));
	}

	private double prob_x_given_yz(DataSet ds, int x, int xVal, int y, int yVal, int zVal) {
		ArrayList<Instance> s = new ArrayList<Instance>();
		int count = 0;
		int t = ds.target();

		for (Instance d : ds.instances()) {
			if (d.valueAt(y).intValue() == yVal) {
				if (d.valueAt(t).intValue() == zVal) {
					s.add(d);
				}
			}
		}

		for (Instance d : s) {
			if (d.valueAt(x).intValue() == xVal) {
				count++;
			}
		}
		// return ((double) count) / s.size();
		int l1 = ds.features()[x].labels().length;
		return ((double) (count + 1)) / (s.size() + l1);
	}

	// CPT for P(x | y,z)
	private void computeCPT(DataSet ds, int x, int y) {
		Feature fx = ds.features()[x];
		Feature fy = ds.features()[y];
		Feature fz = ds.features()[ds.target()];

		for (int vz = 0; vz < fz.labels().length; vz++) {
			for (int vx = 0; vx < fx.labels().length; vx++) {
				for (int vy = 0; vy < fy.labels().length; vy++) {
					double prob = prob_x_given_yz(ds, fx.index(), vx, fy.index(), vy, vz);
					printDebug("Pr(%d=%d | %d=%d,%d=%d)=%.16f\n", fx.index(), vx, fy.index(), vy, fz.index(), vz, prob);
				}
			}
		}
	}

	private double log2(double a, double b) {
		if (a == 0) return 0;
		return Math.log(a / b) / Math.log(2);
	}

}