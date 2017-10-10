package com.ourwork;

import java.util.ArrayList;
import java.util.LinkedList;

import com.filetool.util.FileUtil;

public class Graph {
	public int consumptionNodes;// 消费节点总数
	public int netNodes;// 网络节点总数
	public int netEdges;// 边总数
	public int serverPrice;// 最高级服务器价格
	public ArrayList<Edge>[] graph;// 边 链表
	public ConsumptionNode[] consumptionNodesArray;// 消费节点数组
	public NetNode[] netNodesArray;// 网络节点数组

	public static ServerClass[] finalServers;
	public static int totalNeedFlow;// 消费节点需求的总带宽

	@SuppressWarnings("unchecked")
	public Graph(String[] graphContent) {

		String[] temp = graphContent[0].split(" ");
		netNodes = Integer.parseInt(temp[0]);
		netEdges = Integer.parseInt(temp[1]);
		consumptionNodes = Integer.parseInt(temp[2]);

		int serverRank = 0;// 服务器等级数量
		for (int i = 2; graphContent[i].length() != 0; i++)
			serverRank++;
		ServerClass.serverPrice = new int[serverRank];
		ServerClass.serverFlow = new int[serverRank];
		for (int i = 0; i < serverRank; i++) {
			String[] temp1 = graphContent[i + 2].split(" ");
			int rank = Integer.parseInt(temp1[0]);
			int flow = Integer.parseInt(temp1[1]);
			int cost = Integer.parseInt(temp1[2]);
			ServerClass.serverFlow[rank] = flow;
			ServerClass.serverPrice[rank] = cost;
		}

		finalServers = new ServerClass[netNodes];
		for (int i = 0; i < netNodes; i++) {
			finalServers[i] = new ServerClass();
			finalServers[i].connectedNode = i;
		}
		serverPrice = ServerClass.serverPrice[serverRank-1];// 初始化为最低档服务器价格

		netNodesArray = new NetNode[netNodes];// 初始化网络节点
		int t = serverRank + 3;
		for (int i = 0; i < netNodes; i++) {
			String[] temp2 = graphContent[i + t].split(" ");
			int id = Integer.parseInt(temp2[0]);
			int cost = Integer.parseInt(temp2[1]);
			netNodesArray[id] = new NetNode();
			netNodesArray[id].ID = id;
			netNodesArray[id].locationCost = cost;
		}

		graph = (ArrayList<Edge>[]) new ArrayList[netNodes + 2];
		for (int i = 0; i < netNodes + 2; i++)
			graph[i] = new ArrayList<Edge>();

		int t1 = serverRank + netNodes + 4;
		for (int i = 0; i < netEdges; i++) {
			String[] s1 = graphContent[i + t1].split(" ");
			int sta_id = Integer.parseInt(s1[0]);
			int end_id = Integer.parseInt(s1[1]);
			int band = Integer.parseInt(s1[2]);
			int price = Integer.parseInt(s1[3]);

			Edge tempEdge = new Edge(sta_id, end_id, band, price);
			Edge tempEdge1 = tempEdge.reverse();
			tempEdge.revEdge = graph[end_id].size();
			tempEdge1.revEdge = graph[sta_id].size();
			graph[sta_id].add(tempEdge);
			graph[end_id].add(tempEdge1);

			Edge tempEdge2 = new Edge(end_id, sta_id, band, price);
			Edge tempEdge3 = tempEdge2.reverse();
			tempEdge2.revEdge = graph[sta_id].size();
			tempEdge3.revEdge = graph[end_id].size();
			graph[end_id].add(tempEdge2);
			graph[sta_id].add(tempEdge3);
		}

		consumptionNodesArray = new ConsumptionNode[consumptionNodes];// 初始化消费节点数组
		int t2 = serverRank + netEdges + netNodes + 5;
		for (int j = 0; j < consumptionNodes; j++) {
			String[] s2 = graphContent[j + t2].split(" ");
			consumptionNodesArray[j] = new ConsumptionNode(Integer.parseInt(s2[0]), Integer.parseInt(s2[1]),
					Integer.parseInt(s2[2]));
			netNodesArray[consumptionNodesArray[j].connectedNetNodeID].isConsumption = true;
			netNodesArray[consumptionNodesArray[j].connectedNetNodeID].connectedConsumptionID = consumptionNodesArray[j].ID;
		}
		
		for (ConsumptionNode cn : consumptionNodesArray)
			totalNeedFlow += cn.needBandWidth;
	}

	public Graph() {

	}

	@SuppressWarnings("unchecked")
	public Graph graphCopy() {
		Graph tempGraph = new Graph();
		tempGraph.netEdges = this.netEdges;
		tempGraph.netNodes = this.netNodes;
		tempGraph.serverPrice = this.serverPrice;
		tempGraph.consumptionNodes = this.consumptionNodes;

		tempGraph.netNodesArray = new NetNode[this.netNodesArray.length];
		for (int i = 0; i < this.netNodesArray.length; i++)
			if (this.netNodesArray[i] != null)
				tempGraph.netNodesArray[i] = this.netNodesArray[i].deepCopy();

		tempGraph.consumptionNodesArray = new ConsumptionNode[this.consumptionNodesArray.length];
		for (int i = 0; i < this.consumptionNodesArray.length; i++)
			if (this.consumptionNodesArray[i] != null)
				tempGraph.consumptionNodesArray[i] = this.consumptionNodesArray[i].deepCopy();

		tempGraph.graph = (ArrayList<Edge>[]) new ArrayList[this.netNodesArray.length + 2];
		for (int i = 0; i < this.netNodesArray.length + 2; i++) {
			ArrayList<Edge> c1 = new ArrayList<Edge>();
			for (Edge e : this.graph[i]) {
				Edge e1 = e.deepCopy();
				c1.add(e1);
			}
			tempGraph.graph[i] = c1;
		}
		return tempGraph;
	}

	// netNodes+1为汇点，netNodes+2为源点，汇点不会变，源点会变，所以先设置汇点，再根据服务器位置设置源点
	public void setSource(LinkedList<Integer> servers) {
		netNodes = netNodesArray.length + 2;
		for (int s : servers) {
			Edge e = new Edge(netNodes - 1, s, finalServers[s].ableFlow, 0);// 边流量为服务器能提供的流量
			e.revEdge = graph[finalServers[s].connectedNode].size();
			Edge tempe = e.reverse();
			tempe.revEdge = graph[netNodes - 1].size();
			graph[netNodes - 1].add(e);// 正向边
			graph[finalServers[s].connectedNode].add(tempe);// 反向边
		}
	}

	// netNodes+1为汇点，netNodes+2为源点
	public void setSink() {
		netNodes = netNodesArray.length + 1;
		for (ConsumptionNode cn : consumptionNodesArray) {
			// 必须添加反向边,节点数组不变，边数量不加
			Edge e = new Edge(cn.connectedNetNodeID, netNodes - 1, cn.needBandWidth, 0);
			e.revEdge = graph[netNodes - 1].size();
			Edge tempe = e.reverse();
			tempe.revEdge = graph[cn.connectedNetNodeID].size();
			graph[cn.connectedNetNodeID].add(e);// 正向边
			graph[netNodes - 1].add(tempe);// 反向边
		}
	}

	public void printGraph(Graph g) {
		for (int j = 0; j < g.graph.length; j++) {
			if (g.graph[j] != null) {
				for (Edge e : g.graph[j]) {
					if (e.price > 0) {
						System.out.print(e.startID + " ");
						System.out.print(e.endID + " ");
						System.out.print(e.bandWidth + " ");
						System.out.println(e.price);
					}
				}
			}
		}
		System.out.println();
		for (ConsumptionNode con : g.consumptionNodesArray) {
			System.out.print(con.ID + " ");
			System.out.print(con.connectedNetNodeID + " ");
			System.out.println(con.needBandWidth);
		}
		for (NetNode nn : g.netNodesArray) {
			if (nn != null)
				System.out.println(nn.ID + "");
		}
	}

	public static void main(String[] args) {
		String graphFilePath = args[0];
		String[] graphContent = FileUtil.read(graphFilePath, null);
		Graph g = new Graph(graphContent);
		g.printGraph(g);
	}
}
