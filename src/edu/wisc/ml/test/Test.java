package edu.wisc.ml.test;

import java.util.ArrayList;
import java.util.Collections;

import edu.wisc.ml.data.DataSet;
import edu.wisc.ml.learner.*;
import edu.wisc.ml.reader.ArffReader;
import edu.wisc.ml.util.Util;

public class Test {

	public static void showHelpScreen() {
		System.out.printf("Usage: \n");
		System.out.printf("  bayes [train file] [test file] [n|t]\n");
	}
	
	private static double[] stratifiedTest(Learner learner, DataSet train, DataSet test, int numSets, int setSize) {
		ArrayList<Double> acc = new ArrayList<Double>();
		
		//DataSet[] dataSets = Util.stratifySamples(train, numSets, setSize);
		DataSet[] dataSets = Util.randomSamples(train, numSets, setSize);
		for (int i = 0; i < dataSets.length; i++) {
			learner.train(dataSets[i]);
			double a = learner.test(test);
			acc.add(a);
		}
		
		double[] results = new double[3];
		//min accuracy
		results[0] = Collections.min(acc);
		//max accuracy
		results[1] = Collections.max(acc);
		//average accuracy
		results[2] = 0;
		
		for (int i = 0; i < acc.size(); i++) {
			results[2] = results[2] + acc.get(i);
		}
		results[2] = results[2] / acc.size();
		return results;
	}	
		
	
	public static void main(String[] args) {
		
		DataSet train = null;
		DataSet test = null;
		Learner l;

		if (args.length < 3) {
			showHelpScreen();
			return;
		}
		
		try {
			ArffReader reader = new ArffReader(args[0]);
			train = reader.readDataSet();	
		}
		catch (Exception ex) {
			System.out.printf("Error reading the train file '" + args[0] +"'.\nDetails:", ex.getMessage());
		}
		
		try {
			ArffReader reader = new ArffReader(args[1]);
		  test =reader.readDataSet();
		}
		catch (Exception ex) {
			System.out.printf("Error reading the test file '" + args[0] +"'.\nDetails:", ex.getMessage());
		}
		
		
		if (args[2].equals("n")) {
			l = new NaiveBayes();
		}
		else if (args[2].equals("t")) {
			l = new TanNaiveBayes();
		}
		else {
			System.out.printf("Invalid classifier option.\n");
			showHelpScreen();
			return;
		}
		
		if (args.length>3 && args[3].equals("s")) {
			l.setVerboseMode(Learner.VerboseMode.NONE);
			int[] sizes = { 25, 50, 75, 100 };
			for (int i:sizes) {
				double[] accuracy = stratifiedTest(l, train, test, 4, i);
				System.out.printf("%3d %f %f %f\n", i, accuracy[0], accuracy[1], accuracy[2]);
			}
			return;
		}
		
		try {
			l.setVerboseMode(Learner.VerboseMode.INFO);
			l.train(train);
			l.test(test);
		}
		catch (Exception ex) {
			System.out.printf("Error classifing the data.\nDetails:" + ex.getMessage());
		}
	}

}
