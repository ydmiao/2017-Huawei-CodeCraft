package com.ourwork;

import java.util.Collections;
import java.util.LinkedList;

import com.filetool.util.FileUtil;

public class SAAN1 {
	private LadderSP lsp;// 节点作为服务器的优先级
	private DeleteServers ds;
	public int totalCost;// 最后的总成本
	public LinkedList<Integer> servers;// 最后找到的服务器
	private int count = 0;// add from mandy
	private int repeatCount = 0;
	private int curCost;
	private static int repeatCost = Integer.MAX_VALUE;

	public SAAN1(Graph g, LinkedList<Integer> curSer) {
		int vsize = g.netNodesArray.length;
		lsp = new LadderSP(g);
		PathSearchPlus rs = new PathSearchPlus(g, curSer);
		int curCost = rs.getCost();
		totalCost = curCost;
		long time = System.currentTimeMillis();
		servers = new LinkedList<Integer>(curSer);
		while (repeatCount < 2) {
			// System.out.println("---------------------------------");
			while (curCost <= totalCost) {
				count = 0;
				totalCost = curCost;
				servers = new LinkedList<Integer>(curSer); // 3.30
				LinkedList<Integer> tempser = new LinkedList<Integer>(curSer);
				PathSearchPlus ss = new PathSearchPlus(g, curSer);
				curCost = ss.getCost();
				lsp.setCostSumNode(g, ss.getRoutes());// 处理初始解的路径
				IndexMaxPQ<Double> pq = lsp.getLadder(g);
				int newCost = ss.getCost();
				while (newCost >= curCost) {
					if (count > vsize / 2)
						break;
					tempser = new LinkedList<Integer>(curSer);
					int k = pq.delMax();
					count++;
					if (k == -1) {
						break;
					} else
						while (tempser.contains(k) && (!pq.isEmpty())) {
							k = pq.delMax();
							count++;
						}
					tempser.add(k);
					lsp.getCostSumNode()[k] = -50000;// 这样写也行？
					PathSearchPlus ps = new PathSearchPlus(g, tempser);
					newCost = ps.getCost();
				}
				curCost = newCost; // when k==-1,the newCost is not the best
				curSer = tempser;
				
			}
			curCost = totalCost;
			if (totalCost == repeatCost)
				break;
			curSer = new LinkedList<Integer>(servers); // 3.30

			// **********************************************************、//
			System.out.println(servers.size() + " " + totalCost);

			while (curCost <= totalCost) {
				totalCost = curCost;
				servers = new LinkedList<Integer>(curSer); // 3.30
				LinkedList<Integer> tempser = new LinkedList<Integer>(curSer);
				PathSearchPlus ss = new PathSearchPlus(g, curSer);
				curCost = ss.getCost();
				lsp.setCostSumNode(g, ss.getRoutes());// 处理初始解的路径
				ds = new DeleteServers(g, curSer, ss.getRoutes());
				int newCost = ss.getCost();
				while (newCost >= curCost) {
					tempser = new LinkedList<Integer>(curSer);
					for (int ij : ds.getServers()) {
						tempser.remove(tempser.indexOf(ij));
					}
					int aa = ds.getServer();
					if (aa == -1) {
						break;
					} else
						tempser.remove(tempser.indexOf(aa));
					PathSearchPlus ps = new PathSearchPlus(g, tempser);
					newCost = ps.getCost();

				}
				curCost = newCost; // when aa==-1,the newCost is not the best
				curSer = tempser;
			}
			PathSearchPlus ps = new PathSearchPlus(g, servers);
			curCost = ps.getCost();

			long time2 = System.currentTimeMillis();
			 if (time2 - time > 30000)
				break;
			if (repeatCost != totalCost)
				repeatCost = totalCost;
			else
				repeatCount++;
		}
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
		System.out.println("origin servers: " + servers.size() + " " + servers); // add
		 
		 SAAN1 an = new SAAN1(g, servers);
//		 System.out.println(an.servers.size() + " " + an.totalCost + " " + an.servers);
//		 LocalSearch an1=new LocalSearch(g, an.servers);
		 
        PathSearchPlus ps2=new PathSearchPlus(g,an.servers);
        System.out.println("dij: "+ps2.getCost());
        
        ChangeServerRankByDij ch0=new ChangeServerRankByDij(g, ps2.getCost(), an.servers);
        System.out.println("rank: "+ch0.getCost());
        
		SAnnealing1 ans3 = new SAnnealing1(g, ch0.servers, ch0.getCost());
		System.out.println(ans3.servers.size() + " " + ans3.totalCost + "" + ans3.servers);
		
		ZKW ps3 = new ZKW(g, ans3.servers);
		System.out.println("ZKW	 " + ps3.getCost());
		ChangeServerRankSP ch = new ChangeServerRankSP(g, ps3.getCost(), ans3.servers);
		System.out.println("modify rank "+ch.getCost());
		long time2 = System.currentTimeMillis();
		System.out.println("Time:" + (time2 - time) + "ms");
	}
}
