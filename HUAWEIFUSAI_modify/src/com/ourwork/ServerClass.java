package com.ourwork;

public class ServerClass {
	public static int[] serverPrice;
	public static int[] serverFlow;

	public int connectedNode = -1;// 服务器连接的普通节点
	public int rank;// 服务器等级
	public int ableFlow;// 所能提供的流量
	public int leftFlow;// 剩余的流量
	public int useFlow;// 已经使用的流量

	public ServerClass() {
		rank = serverPrice.length - 1;
		leftFlow = ableFlow = serverFlow[rank];
	}

	public static int getRankFromFlow(int f) {// 根据流量定服务器的等级
		int r = serverPrice.length;
		int f1 = r - 1;
		for (int i = r - 1; i >= 0 && f <= serverFlow[i]; i--)
			f1 = i;
		return f1;
	}
}
