import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class knapsack {

	static ArrayList<ArrayList<Card>> problems = new ArrayList<ArrayList<Card>>();
	static ArrayList<Integer> capacities = new ArrayList<Integer>();
	static int maxProfit = 0;
	static int numbest = 0;
	static int[] weights;
	static int[] profits;
	static String[] include;
	static String[] bestset;

	static public class Card implements Comparable<Card>{

		Card(int weight, int profit, float frac){
			this.weight = weight;
			this.profit = profit;
			this.frac = frac;
		}
		int weight;
		int profit;
		float frac;

		@Override
		public int compareTo(Card i) {
			return Float.compare(this.frac, i.frac);
		}

	}

	//Greedy Algorithm
	static int greedy1(int problem){
		int maxProfit = 0;
		int weight = 0;
		for(int i = 0; i < problems.get(problem).size(); i++) {
			if(weight + problems.get(problem).get(i).weight < capacities.get(problem)) {
				bestset[i + 1] = "yes";
				maxProfit += problems.get(problem).get(i).profit;
				weight += problems.get(problem).get(i).weight;
			}
		}
		return maxProfit;
	}

	//Improved Greedy Algorithm
	static int greedy2(int problem){
		int g1profit = greedy1(problem);
		float pMax = -1;
		int found = 0;
		for(int i = 0; i < problems.get(problem).size(); i++) {
			if(problems.get(problem).get(i).profit > pMax && problems.get(problem).get(i).weight <= capacities.get(problem)) {
				pMax = problems.get(problem).get(i).profit;
				found = i;
			}
		}

		if(g1profit > pMax){
			return g1profit;
		}
		else {
			Arrays.fill(bestset, "null");
			bestset[found + 1] = "yes";
			return Math.round(pMax);
		}
	}

	//Helper function for computing the upper bound for Backtracking Algorithm
	static double KWF2(int problem, int i, int currWeight, int profit) {

		double bound = profit;
		int n = problems.get(problem).size()+1;
		int capacity = capacities.get(problem);

		double[] x = new double[n];
		for(int j = i; j < n; j++) x[j] = 0;

		while( (currWeight < capacity) && (i <= n - 1)) {
			if(currWeight + weights[i] <= capacity) {
				x[i] = 1;
				currWeight += weights[i];
				bound += profits[i];
			} else {
				x[i] = (capacity - currWeight) / weights[i];
				currWeight = capacity;
				bound += profits[i] * x[i];
			}
			i++;
		}
		return bound;
	}

	//Helper function to parse through the state space tree of the Backtracking Algorithm
	static void knapsack(int problem, int i, int profit, int weight) {
		if(weight <= capacities.get(problem) && profit > maxProfit) {
			maxProfit = profit;
			numbest = i;
			for(int j = 0; j < include.length; j++){
				bestset[j] = include[j];
			}
		}

		if(promising(problem, weight, profit, i)) {
			include[i+1] = "yes";
			knapsack(problem, i+1, profit + profits[i+1], weight + weights[i+1]);
			include[i+1] = "no";
			knapsack(problem, i+1, profit, weight);
		}
	}

	//Helper function to determine if a node is promising in the pruned state space tree of the Backtracking Algorithm
	static boolean promising(int problem, int weight, int profit, int i) {
		if(weight >= capacities.get(problem)) return false;
		double bound = KWF2(problem, i+1, weight, profit);
		return (bound > maxProfit);
	}

	//Backtracking Algorithm
	static int backTracking(int problem){
		int size = problems.get(problem).size() + 1;

		int j = 0;
		for(int i = 1; i < size; i++, j++) {
			weights[i] = problems.get(problem).get(j).weight;
			profits[i] = problems.get(problem).get(j).profit;
		}

		knapsack(problem, 0, 0, 0);
		if(maxProfit == 0) return greedy2(problem);
		return maxProfit;
	}

	//Dynamic Programming Algorithm -- Modified to only use two lines of storage within the table at any given time. (-1 entry in the table denotes memory not in use)
	static int dynamicProgramming(int problem) {
		int items = problems.get(problem).size();
		int cap = capacities.get(problem);

		int x = 0;
		for(int i = 1; i < items + 1; i++, x++) {
			weights[i] = problems.get(problem).get(x).weight;
			profits[i] = problems.get(problem).get(x).profit;
		}

		int[][] P = new int[items + 1][cap + 1];

		for(int i = 0; i < items + 1; i++) {
			for(int j = 0; j < cap + 1; j++) {
				P[i][j] = -1;
			}
		}

		for(int c = 0; c < cap + 1; c++) {
			P[0][c] = 0;
		}

		for(int i = 1; i < items + 1; i++) {
			P[i][0] = 0;
			for(int c = 1; c < cap + 1; c++) {
				if(i >= 2 && c >= 2) {
					P[i-2][c-2] = -1;
				}
				if(weights[i] <= c && P[i-1][c-weights[i]] + profits[i] > P[i-1][c]) {
					P[i][c] = P[i-1][c-weights[i]] + profits[i];
				} else {
					P[i][c] = P[i-1][c];
				}
			}
		}
		return P[items][cap];
	}

	public static void main(String[] args) throws IOException {
			String inputFile = args[0];
			String outputFile = args[1];
			int selectedAlg = Integer.parseInt(args[2]);
			File input = new File(inputFile);

      BufferedReader reader = new BufferedReader(new FileReader(inputFile));
      int i = 0;
      int probLines = 0;
      String line = "";
      int problemCount = -1;
      int currI = 0;
      boolean hitSpace = false;
      StringBuilder n1 = new StringBuilder();
      StringBuilder n2 = new StringBuilder();

			//Parsing the input file
	    while (reader.ready()) {
	    	line = reader.readLine();
	    	if(i == 0 || i == currI + probLines + 1) {
	    		currI = i;
	    		hitSpace = false;
	    		problems.add(new ArrayList<Card>());
	    		problemCount++;
	    		for(char x : line.toCharArray()) {
	    			if(x == ' ') {
	    				hitSpace = true;
	    				continue;
	    			}
	    			if(hitSpace) n1.append(x);
	    			else n2.append(x);
	    		}
	    		probLines = Integer.parseInt(n2.toString());
	    		capacities.add(Integer.parseInt(n1.toString()));
	    		n1.setLength(0);
	    		n2.setLength(0);
	    		i++;
	    	} else {
	    		hitSpace = false;
	    		for(char x : line.toCharArray()) {
	    			if(x == ' ') {
	    				hitSpace = true;
	    				continue;
	    			}
	    			if(hitSpace) n1.append(x);
	    			else n2.append(x);
	    		}
	    		problems.get(problemCount).add(new Card(Integer.parseInt(n2.toString()), Integer.parseInt(n1.toString()), (Integer.parseInt(n1.toString()))/Integer.parseInt(n2.toString())));
	    		n1.setLength(0);
	    		n2.setLength(0);
		    	i++;
	    	}
	    }

	    reader.close();

			//Sorting input items in non-increasing order
	    for(ArrayList<Card> x : problems) {
				Collections.sort(x, Collections.reverseOrder());
	    }

			//Writing to output file
	    long startTime = 0;
	    long endTime = 0;
	    double elapsed = 0;
	    int maxP = 0;
	    ArrayList<Double> times = new ArrayList<Double>();
	    ArrayList<Integer> max = new ArrayList<Integer>();
			ArrayList<String[]> bestsets = new ArrayList<String[]>();
	    for(i = 0; i <= problemCount; i++) {
				weights = new int[problems.get(i).size()+1];
				profits = new int[problems.get(i).size()+1];
				include = new String[problems.get(i).size()+1];
				bestset = new String[problems.get(i).size()+1];
	    	maxProfit = 0;
				numbest = 0;
	    	Arrays.fill(weights, -1);
	    	Arrays.fill(profits, -1);
				Arrays.fill(bestset, "null");
				Arrays.fill(include, "null");
				switch(selectedAlg) {
					case 0:
						startTime = System.nanoTime();
						maxP = greedy1(i);
						endTime = System.nanoTime();
						elapsed = (endTime - startTime) / 1e6;
						times.add(elapsed);
						max.add(maxP);
						bestsets.add(bestset);
						break;
					case 1:
						startTime = System.nanoTime();
						maxP = greedy2(i);
						endTime = System.nanoTime();
						elapsed = (endTime - startTime) / 1e6;
						times.add(elapsed);
						max.add(maxP);
						bestsets.add(bestset);
						break;
					case 2:
						startTime = System.nanoTime();
						maxP = backTracking(i);
						endTime = System.nanoTime();
						elapsed = (endTime - startTime) / 1e6;
						times.add(elapsed);
						max.add(maxP);
						bestsets.add(bestset);
						break;
					case 3:
						startTime = System.currentTimeMillis();
						maxP = dynamicProgramming(i);
						endTime = System.currentTimeMillis();
						elapsed = endTime = startTime;
						times.add(elapsed);
						max.add(maxP);
						break;
					default:
						break;
			}
	   }
			int y = 0;
	    FileWriter writer = new FileWriter(outputFile);
	    for(i = 0; i <= problemCount; i++) {
				y = 0;
	    	writer.write(Integer.toString(problems.get(i).size()));
	    	writer.write(" ");
	    	writer.write(Integer.toString(max.get(i)));
	    	writer.write(" ");
	    	writer.write(Double.toString(times.get(i)));
				writer.write(" ");
				if(selectedAlg != 3) {
					for(String x : bestsets.get(i)){
						if(x.compareTo("yes") == 0) {
							writer.write(Integer.toString(y));
				    	writer.write(" ");
						}
						y++;
				}
			}
				writer.write("\n");
	    }
	    writer.close();
	}
}
