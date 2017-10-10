package com.ourwork;

import java.util.LinkedList;

import com.filetool.util.FileUtil;

public class ServerSearchSP {
	private LinkedList<Integer> servers;// 存储找到的服务器
	private final int cSize;

	public ServerSearchSP(Graph G) {
		Graph g = G.graphCopy();
		cSize = g.consumptionNodesArray.length;

		servers = new LinkedList<Integer>();
		Ladder ladder = new Ladder(g);// get the ladder of all vertices

		Components cps = new Components(g);// 预先对图做联通分量检查
		for (Graph graph : cps.getGraphList())// preprocess graph g
			if (graph.consumptionNodes > 0) {
				int s = ladder.getServer(graph);// get a server
				servers.add(s);
			}

		g.setSource(servers);// 肯定有至少一个初始服务器了
		int numOfConsumption = g.consumptionNodes;
		while (numOfConsumption > 0) {
			if (!calculateGraph(g)) {// 如果最小流不成功，做连通分量检查
				Components cs = new Components(g);// 加了源，数组会越界
				// System.out.println(cs.count());
				for (Graph graph : cs.getGraphList())
					if (graph.consumptionNodes > 0) {
						int s = ladder.getServer(graph);// get a server
						servers.add(s);
					}
				g = G.graphCopy();
				g.setSource(servers);
			}
			numOfConsumption = g.consumptionNodes;
		}
		for (Integer i : ladder.getServers())
			if (!servers.contains(i))
				servers.add(i);
	}

	@SuppressWarnings("unchecked")
	private boolean calculateGraph(Graph g) {
		int[] dist = new int[cSize];
		LinkedList<Edge>[] paths = (LinkedList<Edge>[]) new LinkedList[cSize];
		IndexMinPQ<Integer> pq = new IndexMinPQ<Integer>(cSize);

		DijkstraSP dsp = new DijkstraSP(g, g.netNodes - 1);
		for (ConsumptionNode cn : g.consumptionNodesArray) {
			if (cn == null)
				continue;
			int v = cn.connectedNetNodeID;
			dist[cn.ID] = dsp.dist(v);
			if (dist[cn.ID] == Integer.MAX_VALUE)
				return false;
			paths[cn.ID] = dsp.getPath(v);
			pq.insert(cn.ID, dist[cn.ID]);
		}

		while (!pq.isEmpty()) {
			int index = pq.delMin();
			int minPathBand = Math.min(getPathBands(paths[index]), g.consumptionNodesArray[index].needBandWidth);
			if (minPathBand > 0)
				for (Edge e : paths[index]) {
					reviseEdge(g, e, minPathBand);
				}
			g.consumptionNodesArray[index].needBandWidth -= minPathBand;
			if (g.consumptionNodesArray[index].needBandWidth == 0) {
				int v = g.consumptionNodesArray[index].connectedNetNodeID;
				g.netNodesArray[v].isConsumption = false;
				g.consumptionNodesArray[index] = null;
				g.consumptionNodes--;
			}
		}
		return true;
	}

	private void reviseEdge(Graph g, Edge e, int delta) {
		e.leftBandwidth -= delta;
		if (e.leftBandwidth == 0)
			if (e.revEdge > 0 && g.graph[e.endID].get(e.revEdge - 1).endID == e.startID)
				g.graph[e.endID].get(e.revEdge - 1).leftBandwidth = 0;
			else if ((e.revEdge < g.graph[e.endID].size() - 1
					&& g.graph[e.endID].get(e.revEdge + 1).endID == e.startID))
				g.graph[e.endID].get(e.revEdge + 1).leftBandwidth = 0;// 两边都断，连通分量才能正确
	}

	private int getPathBands(LinkedList<Edge> path) {
		int min = Integer.MAX_VALUE;
		for (Edge e : path)
			if (min > e.leftBandwidth)
				min = e.leftBandwidth;
		return min;
	}

	public LinkedList<Integer> getServers() {
		return servers;
	}

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		String graphFilePath = args[0];
		String[] graphContent = FileUtil.read(graphFilePath, null);
		Graph g = new Graph(graphContent);
		ServerSearchSP ss = new ServerSearchSP(g);
		for (int i : ss.servers)
			System.out.print(i + ",");
		long time2 = System.currentTimeMillis();
		System.out.println("Time:" + (time2 - time) + "ms");
	}
}
