package com.ourwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import com.filetool.util.FileUtil;

public class Ladder {// 和以前对比无误
	private final int vSize;
	private final double alpha = 1, beta = 2, gamma = 2, zeta = 2.0;
	private double[] averagePB;// 单位带宽成本 不变
	private double maxAPB = 0.0;
	private int[] degrees;// 网络节点的度 会变
	private int maxDegree = 0;
	private int[] bandSum;// 带宽总和 不变
	private int maxBandSum = 0;
	private double[] minCostRatio;// 提供消费节点所需带宽的成本与服务器成本的比值
	private double maxRatio;
	private double[] ladder;
	private LinkedList<Integer> servers;

	public Ladder(Graph g) {
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
				bandSum[i] += e.leftBandwidth;
				pricesum += e.leftBandwidth * e.price; // 不变
			}
			if (g.netNodesArray[i].isConsumption) {
				int cost = getMinCost(g, i);

				// cost大于总服务器成本
				int sumServerPrice = g.serverPrice + g.netNodesArray[i].locationCost;
				if (cost > sumServerPrice)// is this condition common?
					servers.add(i);
				minCostRatio[i] = (double) cost / sumServerPrice;
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

	public int getMinCost(Graph g, int v) {// v is a consumptionnode
		int c = g.netNodesArray[v].connectedConsumptionID;
		int leftBand = g.consumptionNodesArray[c].needBandWidth;

		int minCost = 0;
		ArrayList<Edge> adj = new ArrayList<Edge>(g.graph[v]);
		Collections.sort(adj);// 不许动图的位置，这里要改，先复制一份再排序
		for (Edge e : adj) {
			if (e.price > 0) {
				minCost += e.price * (leftBand < e.bandWidth ? leftBand : e.bandWidth);
				leftBand -= e.bandWidth;
				if (leftBand <= 0)
					break;
			}
		}
		// if (leftBand > 0)
		// servers.add(v);// v will undoubtedly be found
		return minCost;
	}

	public int getServer(Graph g) {// g may be not a complete graph
		int num = -1;
		double max = Integer.MIN_VALUE;
		for (int i = 0; i < vSize; i++) {
			if (g.netNodesArray[i] == null)
				continue;
			ladder[i] = -averagePB[i] / maxAPB * alpha + (double) degrees[i] / maxDegree * beta
					+ (double) bandSum[i] / maxBandSum * gamma + minCostRatio[i] / maxRatio * zeta;
			if (max < ladder[i]) {
				max = ladder[i];
				num = i;
			}
		}
		// if num is selected,it won't be selected again,case 3
		// averagePB[num] = Integer.MAX_VALUE;// won't overflow
		minCostRatio[num] = Integer.MIN_VALUE;
		return num;
	}

	public LinkedList<Integer> getServers() {
		return servers;
	}

	// Test
	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		String graphFilePath = args[0];
		String[] graphContent = FileUtil.read(graphFilePath, null);
		Graph g = new Graph(graphContent);
		Ladder l = new Ladder(g);
		for (NetNode nn : g.netNodesArray)
			if (nn.isConsumption)
				System.out.println(nn.ID + ": " + l.getMinCost(g, nn.ID));
		System.out.println(l.getServers());
		System.out.println(l.getServer(g));
		long time2 = System.currentTimeMillis();
		System.out.println("Time:" + (time2 - time) + "ms");
	}
}
