package com.ourwork;

import java.util.ArrayList;
import java.util.LinkedList;

import com.filetool.util.FileUtil;

public class PathSearchPlus {
	private Graph g;
	private ArrayList<Route> allRoutes;
	private final int cSize;
	private int cost;
	private int source;
	private ServerClass[] serversArray;
	private int allServserPrice;// 所有服务器的总成本
	public int[] rankOfServer;

	public int getAllServserPrice() {
		return allServserPrice;
	}

	public PathSearchPlus(Graph G, final LinkedList<Integer> servers) {
		g = G.graphCopy();
		serversArray = new ServerClass[g.netNodes];
		rankOfServer = new int[g.netNodesArray.length];
		for (Integer j : servers) {// 设置服务器
			serversArray[j] = new ServerClass();
			serversArray[j].connectedNode = j;
		}
		g.setSource(servers);// 设置源点，服务器位置不能变
		source = g.netNodes - 1;
		cSize = g.consumptionNodesArray.length;
		allRoutes = new ArrayList<Route>();

		while (g.consumptionNodes > 0) {
			if (!calculateGraph(g, servers)) {
				cost = Integer.MAX_VALUE;
				return;
			}
		}
		setServersRank();// 服务器降档
		cost += allServserPrice;
	}

	// Path search
	@SuppressWarnings("unchecked")
	private boolean calculateGraph(Graph g, LinkedList<Integer> servers) {

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
			if (minPathBand > 0) {
				for (Edge e : paths[index])
					cost += reviseEdge(g, e, minPathBand);
				Route ro = new Route();
				paths[index].poll();
				ro.route = paths[index];
				ro.bandWidth = minPathBand;
				ro.end = g.consumptionNodesArray[index].connectedNetNodeID;
				ro.destination = index;
				ro.price = dist[index];
				allRoutes.add(ro);
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

	private int reviseEdge(Graph g, Edge e, int delta) {
		e.leftBandwidth -= delta;
		e.flow += delta;
		int cost = e.price * delta;
		return cost;
	}

	private int getPathBands(LinkedList<Edge> path) {
		int min = Integer.MAX_VALUE;
		for (Edge e : path)
			if (min > e.leftBandwidth)
				min = e.leftBandwidth;
		return min;
	}

	private void setServersRank() {// 最后服务器降档
		for (Edge e : g.graph[source]) {// 扫描超级源点边的流量
			int r = ServerClass.getRankFromFlow(e.flow);
			rankOfServer[e.endID] = r;
			serversArray[e.endID].rank = r;
			serversArray[e.endID].ableFlow = ServerClass.serverFlow[r];
			allServserPrice += ServerClass.serverPrice[r] + g.netNodesArray[e.endID].locationCost;
		}
	}

	public void writeServer() {// 写入最终服务器
		for (int i = 0; i < serversArray.length; i++)
			Graph.finalServers[i] = serversArray[i];
	}

	public int getCost() {
		return cost;
	}

	public ArrayList<Route> getRoutes() {
		return allRoutes;
	}

	public String[] printResult() {
		int resultSize = allRoutes.size() + 2;
		String[] result = new String[resultSize];
		result[0] = new String(Integer.toString(allRoutes.size()));
		result[1] = new String("\r\n");
		for (int i = 2; i < resultSize; i++) {
			Route p = allRoutes.get(i - 2);
			result[i] = new String(p.toString());
		}
		// System.out.println(cost);
		return result;
	}

	// Test
	public static void main(String[] args) {
		String graphFilePath = args[0];
		String[] graphContent = FileUtil.read(graphFilePath, null);
		Graph g = new Graph(graphContent);
		// ServerSearchSP ss = new ServerSearchSP(g);
		// LinkedList<Integer> servers = ss.getServers();
		LinkedList<Integer> servers = new LinkedList<Integer>();
		// servers.add(0);
		// servers.add(3);
		// servers.add(22);
		int[] a = { 87, 104, 31, 27, 133, 107,153, 89, 64, 14, 7, 80 };
		for (int i = 0; i < a.length; i++) {
			servers.add(a[i]);
		}
		long time = System.currentTimeMillis();
		PathSearchPlus ps = new PathSearchPlus(g, servers);
		ps.writeServer();
		for (Route ro : ps.getRoutes()) {
			System.out.println(ro);
		}
		System.out.println("total cost: " + ps.getCost());
		long time2 = System.currentTimeMillis();
		System.out.println("耗时：" + (time2 - time) + "ms");
	}
}
