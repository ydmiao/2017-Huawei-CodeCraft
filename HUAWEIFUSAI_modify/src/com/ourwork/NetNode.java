package com.ourwork;

public class NetNode {
	public int ID;// 网络节点ID
	public boolean isConsumption;// 是否连接消费节点
	public int connectedConsumptionID = -1;// 与之相连的消费节点的ID

	public boolean isService;// 是否放置服务器
	public ServerClass sc;
	public int locationCost;// 部署成本

	public NetNode deepCopy() {
		NetNode n = new NetNode();
		n.ID = this.ID;
		n.isConsumption = this.isConsumption;
		n.connectedConsumptionID = this.connectedConsumptionID;
		n.isService = this.isService;
		n.locationCost = this.locationCost;
		n.sc = this.sc;// 直接给引用就可以了
		return n;
	}

}
