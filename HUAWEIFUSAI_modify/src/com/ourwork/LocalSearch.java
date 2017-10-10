package com.ourwork;


import java.util.Collections;
import java.util.LinkedList;

import com.filetool.util.FileUtil;

public class LocalSearch {
	
	private static int  repeatCost=Integer.MAX_VALUE;
	public int totalCost;  //最优总成本
	public LinkedList<Integer> servers;
	private boolean flag=true;
	
	public LocalSearch(Graph g,LinkedList<Integer> teservers)
	{
		this.servers=new LinkedList<Integer>(teservers);
		LinkedList<Integer> copySer=new LinkedList<Integer>(teservers);
		PathSearchPlus ps=new PathSearchPlus(g, copySer);
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
					PathSearchPlus temp=new PathSearchPlus(g, copySer);
					int tempCost=temp.getCost();
					if(tempCost<totalCost)
					{
						totalCost=tempCost;
						this.servers=new LinkedList<Integer>(copySer);
//						System.out.println(servers.size()+" "+totalCost);
					}
					copySer.set(copySer.indexOf(j), i);
				}
			}
			
		}
		long time2 = System.currentTimeMillis();
		if (time2 - time >10000)
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
		int[] originSer={9, 15, 19, 21, 26, 31, 33, 34, 35, 38, 40, 44, 56, 59, 61, 64, 70, 74, 87, 88, 95, 98, 100, 102, 103, 105, 109, 113, 114, 120, 127, 133, 134, 139, 145, 163, 165, 168, 173, 177, 179, 180, 182, 184, 195, 198, 199, 203, 207, 210, 214, 229, 230, 237, 244, 251, 253, 256, 263, 265, 269, 274, 275, 280, 282, 284, 287, 288, 291, 296, 299, 306, 307, 309, 320, 324, 326, 330, 333, 337, 340, 345, 349, 352, 369, 371, 376, 387, 392, 397, 416, 417, 419, 424, 428, 429, 433, 437, 440, 446, 448, 450, 454, 458, 462, 466, 471, 472, 480, 496, 511, 514, 516, 519, 529, 534, 535, 552, 562, 563, 569, 572, 576, 577, 578, 582, 586, 588, 598, 600, 603, 604, 609, 611, 612, 615, 619, 626, 627, 633, 636, 637, 642, 644, 651, 659, 668, 674, 679, 685, 686, 689, 691, 694, 701, 702, 703, 705, 709, 717, 718, 727, 729, 732, 734, 745, 747, 750, 759, 760, 762, 763, 767, 768, 772, 773, 776, 779, 784, 792, 796, 797, 798};
		for(int i:originSer)
			ser.add(i);
		LocalSearch an=new LocalSearch(g, ser);
		System.out.println(an.servers.size() + " " + an.totalCost+" "+an.servers);
		long time2 = System.currentTimeMillis();
		System.out.println("Time:" + (time2 - time) + "ms");
	}

}
