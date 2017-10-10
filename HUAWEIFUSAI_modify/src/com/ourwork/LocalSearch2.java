package com.ourwork;


import java.util.Collections;
import java.util.LinkedList;

import com.filetool.util.FileUtil;

public class LocalSearch2 {
	
	private static int  repeatCost=Integer.MAX_VALUE;
	public int totalCost;  //最优总成本
	public LinkedList<Integer> servers;
	private boolean flag=true;
	
	public LocalSearch2(Graph g,LinkedList<Integer> teservers)
	{
		this.servers=new LinkedList<Integer>(teservers);
		LinkedList<Integer> copySer=new LinkedList<Integer>(teservers);
		ZKW ps=new ZKW(g, copySer);
		totalCost=ps.getCost();
		long time = System.currentTimeMillis();
		while(flag){
		for(int i:copySer)     //对每个服务器进行邻域搜索
		{
			for (Edge e : g.graph[i]) {   //访问每一个服务器的邻接点
				int j=e.endID;
				if(!copySer.contains(j)){     //如果服务器中包含这个邻接点，则继续下一个搜索
					copySer.set(copySer.indexOf(i), j);
					g.finalServers[j].rank=g.finalServers[i].rank;
					g.finalServers[j].ableFlow=g.finalServers[i].ableFlow;
					ZKW temp=new ZKW(g, copySer);
					int tempCost=temp.getCost();
					if(tempCost<totalCost)
					{
						totalCost=tempCost;
						this.servers=new LinkedList<Integer>(copySer);
					}
					copySer.set(copySer.indexOf(j), i);
				}
			}
			
		}
		long time2 = System.currentTimeMillis();
		if (time2 - time >15000)
			break;
		copySer=new LinkedList<Integer>(this.servers);
//		Collections.sort(this.servers);    //排序之后再输出传递
		if(repeatCost>totalCost)
			repeatCost=totalCost;
		else
			flag=false;
		}
		
	}
	
//	public void Swap(LinkedList<Integer> temp,int x,int y)
//	{
//		temp.remove(new Integer(x));
//		temp.add(y);
//	}
	
	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		String graphFilePath = args[0];
		String[] graphContent = FileUtil.read(graphFilePath, null);
		Graph g = new Graph(graphContent);
		LinkedList<Integer> ser=new LinkedList<Integer>();
		int[] originSer={212, 218, 113, 299, 171, 168, 220, 229, 144, 193, 277, 292, 34, 257, 228, 157, 246, 45, 0, 207, 182};
		for(int i:originSer)
			ser.add(i);
		LocalSearch2 an=new LocalSearch2(g, ser);
		System.out.println(an.servers.size() + " " + an.totalCost+" "+an.servers);
		long time2 = System.currentTimeMillis();
		System.out.println("Time:" + (time2 - time) + "ms");
	}

}
