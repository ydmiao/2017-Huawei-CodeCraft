package com.ourwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class LadderSP {
	private final int vSize;
	private double alpha = 1, beta = 1, gamma = 2, zeta = 0, theta = 3;
	private double[] averagePB;
	private double maxAPB = 0.0;
	private int[] degrees;
	private int maxDegree = 0;
	private int[] bandSum;
	private int maxBandSum = 0;
	private double[] minCostRatio;
	private double maxRatio;
	private int[] costSumNode;
	private double[] ladder;
	private IndexMaxPQ<Double> pq;
	private LinkedList<Integer> servers;

	public LadderSP(final Graph g) {
		vSize = g.netNodesArray.length;
		averagePB = new double[vSize];
		degrees = new int[vSize];
		bandSum = new int[vSize];
		minCostRatio = new double[vSize];
		ladder = new double[vSize];
		servers = new LinkedList<Integer>();
		for (int i = 0; i < vSize; i++) {

			degrees[i] = g.graph[i].size() / 2;// 存图方式变了

			int pricesum = 0;
			if (g.netNodesArray[i] == null)
				continue;
			for (Edge e : g.graph[i]) {
				bandSum[i] += e.leftBandwidth;// 高处的节点会不会受到影响？？？？？？？？
				pricesum += e.leftBandwidth * e.iniPrice;// 不变
			}
			if (g.netNodesArray[i].isConsumption) {
				int cost = getMinCost(g, i);
				minCostRatio[i] = (double) cost / g.serverPrice;
				int c = g.netNodesArray[i].connectedConsumptionID;
				bandSum[i] += g.consumptionNodesArray[c].needBandWidth;
				degrees[i]++;
			}
			averagePB[i] = (double) pricesum / bandSum[i];
			if (maxAPB < averagePB[i])
				maxAPB = averagePB[i];
			if (maxDegree < degrees[i])
				maxDegree = degrees[i];
			if (maxBandSum < bandSum[i])
				maxBandSum = bandSum[i];
			if (maxRatio < minCostRatio[i])
				maxRatio = minCostRatio[i];
		}
	}

	// get min cost of every consumption node
	private int getMinCost(Graph g, int v) {// v is a consumptionnode
		int c = g.netNodesArray[v].connectedConsumptionID;
		int leftBand = g.consumptionNodesArray[c].needBandWidth;

		int minCost = 0;

		ArrayList<Edge> adj = new ArrayList<Edge>(g.graph[v]);
		Collections.sort(adj);
		for (Edge e : adj) {
			if (e.iniPrice > 0) {
				minCost += e.iniPrice * (leftBand < e.bandWidth ? leftBand : e.bandWidth);
				leftBand -= e.bandWidth;
				if (leftBand <= 0)
					break;
			}
		}
		// if (leftBand > 0)
		// servers.add(v);// v will undoubtedly be found
		return minCost;
	}

	// check every route;
	public void setCostSumNode(Graph g, ArrayList<Route> routes) {// 高处的节点代价为0
		// initialize everytime
		costSumNode = new int[vSize];
		for (Route ro : routes) {
			int sum = 0;
			for (Edge e : ro.route) {
				sum += e.iniPrice * ro.bandWidth;
				costSumNode[e.endID] += sum;
				// 要把大于服务器成本的放进服务器吗？
				// if (costSumNode[e.endID] > 2.5*g.serverPrice) {
				// servers.add(e.endID);
				// if(vSize>500)
				// costSumNode[e.endID] = -50000;
				// }
			}
		}
	}

	// get the ladder and get a server
	public IndexMaxPQ<Double> getLadder(Graph g) {// g may be not a complete
													// graph
		pq = new IndexMaxPQ<Double>(vSize);
		for (int i = 0; i < vSize; i++) {
			if (g.netNodesArray[i] == null)
				continue;
			// double a = -averagePB[i] / maxAPB * alpha;
			// double b = (double) degrees[i] / maxDegree * beta;
			// double c = (double) bandSum[i] / maxBandSum * gamma;
			// double d = minCostRatio[i] / maxRatio * zeta;
			// double e = (double) costSumNode[i] / g.serverPrice * theta
			ladder[i] = -averagePB[i] / maxAPB * alpha + (double) degrees[i] / maxDegree * beta
					+ (double) bandSum[i] / maxBandSum * gamma + minCostRatio[i] / maxRatio * zeta
					+ (double) costSumNode[i] / g.serverPrice * theta;
			pq.insert(i, ladder[i]);
		}
		return pq;
	}

	public int[] getCostSumNode() {
		return costSumNode;
	}

	public LinkedList<Integer> getServers() {
		return servers;
	}

	// Test
}
