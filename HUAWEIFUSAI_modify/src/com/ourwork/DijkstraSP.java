package com.ourwork;

import java.util.LinkedList;

import com.filetool.util.FileUtil;

public class DijkstraSP {
	private Edge[] edgeTo;
	private int[] distTo;
	private IndexMinPQ<Integer> pq;
	private int[] minBands;
	private final int vSize;

	public DijkstraSP(final Graph g, int s) {
		vSize = g.netNodesArray.length + 2;
		edgeTo = new Edge[vSize];
		distTo = new int[vSize];
		minBands = new int[vSize];
		pq = new IndexMinPQ<Integer>(vSize);

		for (int v = 0; v < vSize; v++) {
			distTo[v] = Integer.MAX_VALUE;
		}
		distTo[s] = 0;
		minBands[s] = Integer.MAX_VALUE;
		pq.insert(s, distTo[s]);
		while (!pq.isEmpty()) {
			visit(g, pq.delMin());
		}
	}

	private void visit(Graph g, int v) {
		for (Edge e : g.graph[v]) {
			int w = e.endID;
			if ((distTo[w] > distTo[v] + e.price) && (e.leftBandwidth > 0)) {
				distTo[w] = distTo[v] + e.price;
				edgeTo[w] = e;
				minBands[w] = Math.min(minBands[v], e.leftBandwidth);
				if (pq.contains(w))
					pq.changeKey(w, distTo[w]);
				else
					pq.insert(w, distTo[w]);
			}
			if ((distTo[w] == distTo[v] + e.price) && (minBands[w] < Math.min(minBands[v], e.leftBandwidth))) {
				edgeTo[w] = e;
				minBands[w] = Math.min(minBands[v], e.leftBandwidth);
				if (pq.contains(w))
					pq.changeKey(w, distTo[w]);
				else
					pq.insert(w, distTo[w]);
			}
		}
	}

	public int dist(int end) {
		return distTo[end];
	}

	public int getBandWidth(int end) {
		return minBands[end];
	}

	public LinkedList<Edge> getPath(int end) {
		LinkedList<Edge> lin = new LinkedList<Edge>();
		int v = end;
		while (edgeTo[v] != null) {
			lin.addFirst(edgeTo[v]);
			v = edgeTo[v].startID;
		}
		return lin;
	}

	public void printPath(int end) {
		for (Edge e : getPath(end))
			if (e.startID != end)
				System.out.print(e.startID + "-->");
		System.out.println(end);
	}

	public static void main(String[] args) {
		String graphFilePath = args[0];
		String[] graphContent = FileUtil.read(graphFilePath, null);
		Graph g = new Graph(graphContent);
		DijkstraSP sp = new DijkstraSP(g, 3);
		System.out.println(sp.dist(26) + "---" + sp.getBandWidth(26));
		sp.printPath(26);
		System.out.println(sp.getPath(26));// this LinkedList is empty
	}
}
