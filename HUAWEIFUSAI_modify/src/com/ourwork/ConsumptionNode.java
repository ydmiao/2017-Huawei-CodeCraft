package com.ourwork;

public class ConsumptionNode {
	public int ID;
	public int connectedNetNodeID;
	public int needBandWidth;

	public ConsumptionNode() {

	}
	public ConsumptionNode(int id, int con, int band) {
		this.ID = id;
		this.needBandWidth = band;
		this.connectedNetNodeID = con;
	}

	public ConsumptionNode deepCopy() {
		ConsumptionNode c = new ConsumptionNode();
		c.ID = this.ID;
		c.connectedNetNodeID = this.connectedNetNodeID;
		c.needBandWidth = this.needBandWidth;
		return c;
	}
}
