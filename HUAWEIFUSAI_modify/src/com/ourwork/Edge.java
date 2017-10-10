package com.ourwork;

public class Edge implements Comparable<Edge> {
	public int startID;// 起始点ID
	public int endID;//
	public int bandWidth;// 边的总带宽
	public int price;// 带宽单价
	public int leftBandwidth;// 剩余可用带宽
	public int flow;// 已经使用的带宽
	public int revEdge = -1;// 反向边引用
	public int iniPrice;

	public Edge() {
		super();
	}

	public Edge(int startID, int endID, int bandWidth, int price) {
		super();
		this.startID = startID;
		this.endID = endID;
		this.leftBandwidth = this.bandWidth = bandWidth;
		this.price = this.iniPrice = price;
	}

	public Edge reverse() {// 产生反向边
		Edge e = new Edge();
		e.startID = endID;
		e.endID = startID;
		e.bandWidth = bandWidth;
		e.leftBandwidth = 0;// 反向边起始可用带宽为0
		e.iniPrice = -iniPrice;
		e.price = -price;// 反向边 价格为 负
		return e;
	}

	@Override
	public int compareTo(Edge e) {
		int num = price - e.price;
		int num2 = num == 0 ? e.bandWidth - bandWidth : num;
		return num2;
	}

	public Edge deepCopy() {
		Edge e1 = new Edge();
		e1.startID = this.startID;
		e1.endID = this.endID;
		e1.bandWidth = this.bandWidth;
		e1.price = this.price;

		e1.leftBandwidth = this.leftBandwidth;
		e1.revEdge = this.revEdge;
		e1.flow = this.flow;
		e1.iniPrice = this.iniPrice;
		return e1;
	}

}
