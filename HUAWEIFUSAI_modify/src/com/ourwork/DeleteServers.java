package com.ourwork;

import java.util.ArrayList;
import java.util.LinkedList;

public class DeleteServers {
	private double alpha = 1.5, beta = 0.5;
	LinkedList<Integer> servers;// 肯定被删除的服务器
	IndexMinPQ<Double> pq2;
	int[] affordBand;
	int maxAfforBand;
	int[] priceSum;
	double[] pricePerBand;
	double maxPricePerBand;

	public DeleteServers(Graph g, LinkedList<Integer> servers, ArrayList<Route> routes) {
		this.servers = new LinkedList<Integer>();
		affordBand = new int[g.netNodes];
		priceSum = new int[g.netNodes];
		pricePerBand = new double[g.netNodes];
		pq2 = new IndexMinPQ<Double>(g.netNodes);
		for (Route ro : routes) {
			int server;
			if (ro.route.size() == 0)
				server = ro.end;
			else
				server = ro.route.get(0).startID;
			affordBand[server] += ro.bandWidth;
			priceSum[server] += ro.price * ro.bandWidth;
		}
		for (Integer s : servers) {
			if (affordBand[s] == 0) {
				this.servers.add(s);
				continue;
			}
			pricePerBand[s] = (double) priceSum[s] / affordBand[s];
			if (maxAfforBand < affordBand[s])
				maxAfforBand = affordBand[s];
			if (maxPricePerBand < pricePerBand[s])
				maxPricePerBand = pricePerBand[s];
		}
		for (Integer s : servers) {
			if (affordBand[s] > 0) {
				double val = (double) affordBand[s] / maxAfforBand * alpha - pricePerBand[s] / maxPricePerBand * beta;
				pq2.insert(s, val);
			}
		}
	}

	public int getServer() {
		if (!pq2.isEmpty())
			return pq2.delMin();
		return -1;
	}

	public LinkedList<Integer> getServers() {
		return servers;
	}
}
