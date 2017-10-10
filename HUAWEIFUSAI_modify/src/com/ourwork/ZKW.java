package com.ourwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

import com.filetool.util.FileUtil;

/**
 * 
 * @author yxiang
 *
 */
public class ZKW {
	private Graph g;
	private int source, sink;
	private int answer, cost, allFlow;
	private boolean[] visited;
	private ArrayList<Route> allRoutes;// 存放路径
	private Stack<Edge> stack;
	private int allServserPrice;//所有服务器的总成本
	public LinkedList<Integer> server;
	public int[] rankOfServer;

	public int getAllServserPrice() {
		return allServserPrice;
	}

	public ZKW(Graph G, LinkedList<Integer> servers) {
		g = G.graphCopy();
		server = new LinkedList<Integer>(servers);
		rankOfServer = new int[g.netNodesArray.length];
		Arrays.fill(rankOfServer, -1);
		g.setSink();// 先汇点，再源点
		g.setSource(servers);
		source = g.netNodes - 1;
		sink = g.netNodes - 2;
		int totalFlow = 0;
		for (ConsumptionNode cn : g.consumptionNodesArray)
			totalFlow += cn.needBandWidth;
		do
			do
				visited = new boolean[g.netNodes];
			while (augment(source, Integer.MAX_VALUE) != 0);
		while (relabel());
		if (allFlow < totalFlow)
			answer = Integer.MAX_VALUE;
		else {
			setServersRank();//服务器降档
			answer += allServserPrice;
			getAllRoutes();
		}
	}

	public int augment(int u, int flow) {
		if (u == sink) {
			answer += cost * flow;
			allFlow += flow;
			return flow;
		}
		visited[u] = true;
		int left_flow = flow;
		for (Edge e : g.graph[u]) {
			if (left_flow <= 0)
				break;
			if (e.leftBandwidth > 0 && e.price == 0 && !visited[e.endID]) {
				int delta = augment(e.endID, Math.min(left_flow, e.leftBandwidth));
				e.leftBandwidth -= delta;
				e.flow += delta;
				g.graph[e.endID].get(e.revEdge).leftBandwidth += delta;
				left_flow -= delta;
			}
		}
		flow -= left_flow;
		return flow;
	}

	public boolean relabel() {// 根据最后一次寻找增广路不成功的 DFS, 找到 d = Min{dis[v]+w[u][v]-dis[u]}
		int delta = Integer.MAX_VALUE;
		for (int u = 0; u < g.netNodes; ++u) {
			if (visited[u]) {
				for (Edge e : g.graph[u]) {
					if (e.leftBandwidth > 0 && !visited[e.endID] && e.price < delta)// 容量不为零，没访问过，代价小于delta
						delta = e.price;
				}
			}
		}
		if (delta == Integer.MAX_VALUE)
			return false;
		for (int u = 0; u < g.netNodes; ++u) {
			if (visited[u]) {
				for (Edge e : g.graph[u]) {
					e.price -= delta;
					g.graph[e.endID].get(e.revEdge).price += delta;
				}
			}
		}
		cost += delta;
		return true;
	}

	private void process() {
		for (int i = 0; i < g.netNodes - 2; i++) {
			for (Edge e : g.graph[i]) {
				Edge d = g.graph[e.endID].get(e.revEdge);
				int min = Math.min(e.flow, d.flow);
				e.flow -= min;
				d.flow -= min;
			}
		}
	}

	private int dfs(int u, int flow) {
		if (u == sink) {
			Route r = new Route();// 没有路径的price
			r.route = new LinkedList<Edge>(stack);
			r.route.pollFirst();
//			r.serverLevel = Graph.finalServers[e1.endID].rank;// 输出服务器档次
			Edge e2 = r.route.pollLast();
			r.end = e2.startID;
			r.destination = g.netNodesArray[r.end].connectedConsumptionID;
			r.bandWidth = flow;
			for (Edge e : r.route)
				r.price += e.iniPrice;
			allRoutes.add(r);
		} else {
			int left_flow = flow;
			for (Edge e : g.graph[u]) {// 改
				if (left_flow == 0)
					break;
				if (e.flow > 0) {
					stack.push(e);
					int delta = dfs(e.endID, Math.min(left_flow, e.flow));
					stack.pop();
					e.flow -= delta;
					left_flow -= delta;
				}
			}
			flow -= left_flow;
		}
		return flow;
	}

	private void getAllRoutes() {
		allRoutes = new ArrayList<Route>();
		stack = new Stack<Edge>();
		process();
		dfs(source, Integer.MAX_VALUE);
	}

	public ArrayList<Route> getRoutes() {
		return allRoutes;
	}

	public void printRoutes() {
		for (Route r : allRoutes)
			System.out.println(r.toString());
	}

	public String[] writeRoutes() {
//		writeServer();
		int m = allRoutes.size();
		String[] s = new String[m + 2];
		s[0] = Integer.toString(m);
		s[1] = "\r\n";
		for (int i = 0; i < m; i++) {
			s[i + 2] = allRoutes.get(i).toString();
		}
		return s;
	}

	public int getCost() {
		return answer;
	}
	private void setServersRank(){//最后服务器降档
		for(Edge e : g.graph[source]){//扫描超级源点边的流量
			int  r= ServerClass.getRankFromFlow(e.flow);
			rankOfServer[e.endID] = r;
			allServserPrice += ServerClass.serverPrice[r]+g.netNodesArray[e.endID].locationCost;
		}
	}
//	private void writeServer(){//写入最终服务器
//		for(int i = 0;i<server.size();i++){
//			int j = server.get(i);
//			Graph.finalServers[j].rank = rankOfServer[i];
//			Graph.finalServers[j].ableFlow = ServerClass.serverFlow[rankOfServer[i]];
//		}
//	}

	public static void main(String[] args) {
		String graphFilePath = args[0];
		String[] graphContent = FileUtil.read(graphFilePath, null);
		Graph g = new Graph(graphContent);
		LinkedList<Integer> servers = new LinkedList<Integer>();
		int[] a = {665, 570, 105, 297, 330, 45, 1000, 176, 575, 880, 398, 771, 435, 396, 1150, 445, 557, 35, 536, 416, 643, 917, 55, 1171, 465};
		for (int i = 0; i < a.length; i++) {
			servers.add(a[i]);
		}
		long time = System.currentTimeMillis();
		ZKW zkw = new ZKW(g, servers);
		System.out.println(zkw.answer);
		ChangeServerRankSP ch = new ChangeServerRankSP(g, zkw.answer,servers);
//		for(String s : ch.route)
//			System.out.println(s);
		long time2 = System.currentTimeMillis();
		System.out.println("耗时：" + (time2 - time) + "ms");
	}
}
