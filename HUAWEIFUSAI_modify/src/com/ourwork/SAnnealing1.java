package com.ourwork;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import com.filetool.util.FileUtil;

public class SAnnealing1 {
	private final int T = 60;
	private final double EPS = 1e-8;
	private final double DELTA = 0.98;
	private final int LIMIT = 14800;
	private final int OLOOP = 300;
	private final int ILOOP = 15000;
	private HashMap<LinkedList<Integer>, Integer> tabuTable;
	public int totalCost;
	public LinkedList<Integer> servers;
	public int N;

	public SAnnealing1(Graph g, LinkedList<Integer> curSer, int curCost) {
		int longTime;
		if(g.netNodesArray.length>700)
			longTime=45000;
		else
			longTime=80000;

		tabuTable = new HashMap<LinkedList<Integer>, Integer>(OLOOP * ILOOP);
//		Collections.sort(curSer);
		tabuTable.put(curSer, curCost);
		totalCost = curCost;
		servers = curSer;
		long time = System.currentTimeMillis();

		double t = T;
		int oloop = 0, limit = 0;
		while (t > EPS && oloop < OLOOP) {
			int iloop = 0;
			while (limit < LIMIT && iloop < ILOOP) {
				LinkedList<Integer> newSer = getNext(g, curSer);
//				Collections.sort(newSer);
				int newCost;
				if (tabuTable.containsKey(newSer))
					newCost = tabuTable.get(newSer);
				else {
					PathSearchPlus ps = new PathSearchPlus(g, newSer);
					newCost = ps.getCost();
					tabuTable.put(newSer, newCost);
				}

				double dE = newCost - curCost;
				if (dE < 0) {
					curCost = newCost;
					curSer = newSer;
					if (totalCost > curCost) {
						totalCost = curCost;
						servers = new LinkedList<Integer>(curSer);
						System.out.println(servers.size()+" "+totalCost+" limit:"+limit);
					}
					limit = 0;
				} else {
					double rd = Math.random();
					if (Math.pow(Math.E, -dE / t) > rd) {
						curCost = newCost;
						curSer = newSer;
					}
					limit++;
				}
				iloop++;
//				System.out.println("iloop " + iloop + " " + newCost + " " + totalCost + " " + newSer.size());
				long time2 = System.currentTimeMillis();
				if (time2 - time > longTime)
					return;
			}
			oloop++;
			t *= DELTA;
		}
	}

	private LinkedList<Integer> getNext(Graph g, LinkedList<Integer> servers) {
		LinkedList<Integer> ser = new LinkedList<Integer>(servers);
		if(g.netNodesArray.length>700)
			N=2;
		else 
			N=3;
		int w = (int) (Math.random() * N);
		switch (w) {
		case 0:// delete
			int i = (int) (Math.random() * ser.size());
			ser.remove(i);
			break;
		case 1:
			int j = (int) (Math.random() * ser.size());
			int e = (int) (Math.random() * g.graph[ser.get(j)].size());
			int v = g.graph[ser.get(j)].get(e).endID;
			while(ser.contains(v)){
			    j = (int) (Math.random() * ser.size());
				e = (int) (Math.random() * g.graph[ser.get(j)].size());
				v = g.graph[ser.get(j)].get(e).endID;
			}
			ser.set(j, v);
			
			break;
		case 2:
			if (ser.size() == g.consumptionNodesArray.length) {
				int s = (int) (Math.random() * ser.size());
				ser.remove(s);
				break;
			}
			int x = (int) (Math.random() * g.netNodesArray.length);
			while (ser.contains(x)) {
				x = (int) (Math.random() * g.netNodesArray.length);
			}
			ser.add(x);
			break;
		}
//		int sum = 0;
//		for (Integer i : ser)
//			for (Edge e : g.graph[i])
//				sum += e.bandWidth;
//		if (sum < Graph.totalNeedFlow) {
//			int x = (int) (Math.random() * g.netNodesArray.length);
//			while (ser.contains(x)) {
//				x = (int) (Math.random() * g.netNodesArray.length);
//			}
//			ser.add(x);
//		}
		return ser;
	}

	public LinkedList<Integer> getServers() {
		return servers;
	}

	public int getTotalCost() {
		return totalCost;
	}

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		String graphFilePath = args[0];
		String[] graphContent = FileUtil.read(graphFilePath, null);
		Graph g = new Graph(graphContent);
		ServerSearchSP ss = new ServerSearchSP(g);
		LinkedList<Integer> servers = ss.getServers();
		PathSearchPlus ps = new PathSearchPlus(g, servers);
		SAnnealing an = new SAnnealing(g, servers, ps.getCost());
		System.out.println(an.getServers().size() + " " + an.getTotalCost() + " " + an.getServers());
		long time2 = System.currentTimeMillis();
		System.out.println("Time:" + (time2 - time) + "ms");
	}
}
