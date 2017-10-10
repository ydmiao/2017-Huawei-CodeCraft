package com.ourwork;

import java.util.ArrayList;

import com.filetool.util.FileUtil;

public class Components {
	private boolean[] marked;
	private int[] id;// 联通分量标识符
	private int count;
	private ArrayList<Graph> components;
	private Graph tempGraph;
	private final int vSize;
	private final int cSize;

	@SuppressWarnings("unchecked")
	public Components(Graph g) {// 都做的是全图的连通分量检查，没有子图了
		vSize = g.netNodes;// 防止数组越界
		cSize = g.consumptionNodesArray.length;// 消费节点满足后会被删掉，而普通节点永远不会
		id = new int[vSize];//
		marked = new boolean[vSize];
		components = new ArrayList<Graph>();
		for (int i = 0; i < vSize; i++) {
			if (!marked[i]) {// 汇点永远会是一个单独的连通分量，不影响，源点也一样
				tempGraph = new Graph();
				tempGraph.graph = (ArrayList<Edge>[]) new ArrayList[vSize];
				for (int v = 0; v < vSize; v++) {
					tempGraph.graph[v] = new ArrayList<Edge>();
				}
				dfs(g, i);
				tempGraph.serverPrice = g.serverPrice;
				tempGraph.netEdges /= 2;
				components.add(tempGraph);
				count++;
			}
		}
		supGraph(g);
	}

	private void dfs(Graph g, int i) {
		marked[i] = true;
		id[i] = count;
		for (Edge e : g.graph[i]) {
			if (e.leftBandwidth > 0) {// leftBandwidth为0默认为断开，所以源点是单独连通分量
				tempGraph.graph[e.startID].add(e);
				tempGraph.netEdges++;
				int w = e.endID;
				if (!marked[w])
					dfs(g, w);
			}
		}
	}

	private void supGraph(Graph g) {

		for (int i = 0; i < count; i++) {
			components.get(i).consumptionNodesArray = new ConsumptionNode[cSize];
			components.get(i).netNodesArray = new NetNode[vSize];
		}

		for (NetNode nn : g.netNodesArray) {// 子图为原图浅复制
			if (nn == null)
				continue;
			int v = nn.ID;
			components.get(id[v]).netNodesArray[v] = nn;
			components.get(id[v]).netNodes++;

			if (nn.isConsumption) {
				int c = nn.connectedConsumptionID;
				components.get(id[v]).consumptionNodesArray[c] = g.consumptionNodesArray[c];
				components.get(id[v]).consumptionNodes++;
			}
		}
	}

	public boolean connected(int v, int w) {
		return id[v] == id[w];
	}

	public int id(int v) {
		return id[v];
	}

	public int count() {
		return count;
	}

	public ArrayList<Graph> getGraphList() {
		return components;
	}

	// Test
	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		String graphFilePath = args[0];
		String[] graphContent = FileUtil.read(graphFilePath, null);
		Graph g = new Graph(graphContent);
		Components cp = new Components(g);
		System.out.println(cp.count);
		long time2 = System.currentTimeMillis();
		System.out.println("Time:" + (time2 - time) + "ms");
	}
}
