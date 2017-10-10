package com.ourwork;

import java.util.LinkedList;

public class Route {
	public LinkedList<Edge> route;
	public int bandWidth;
	public int end;
	public int destination;
	public int price;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Edge e : route)
			sb.append(e.startID).append(' ');
		sb.append(end).append(' ');
		sb.append(destination).append(' ');
		sb.append(bandWidth).append(' ');
		if(route.size()==0)
			sb.append(Graph.finalServers[end].rank);
		else 
			sb.append(Graph.finalServers[route.get(0).startID].rank);
		return sb.toString();
	}

}
