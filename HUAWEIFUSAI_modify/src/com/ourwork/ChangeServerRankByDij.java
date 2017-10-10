package com.ourwork;

import java.util.LinkedList;

import com.filetool.util.FileUtil;

public class ChangeServerRankByDij {
	private int[] rank;//服务器等级数组
	private int minCost;//最小成本
	public LinkedList<Integer> servers ;//服务器位置数组
	public String[] route;//最终路径
	public ChangeServerRankByDij(Graph G,int cost,LinkedList<Integer> ser){
		rank = new int[G.netNodesArray.length];//初始化rank数组
		minCost = cost;
		LinkedList<Integer> newSer = new LinkedList<Integer>(ser);//服务器位置的临时数组
		servers = new LinkedList<Integer>(newSer);
		
		PathSearchPlus ps = new PathSearchPlus(G,ser);
//		System.out.println(servers);
		rank = ps.rankOfServer;
//		for(Integer i : servers)
//		System.out.print(rank[i] +" ");
//		System.out.println();
		for(Integer s : servers){//将最终的服务器级别写入fanalServers
			Graph.finalServers[s].rank= rank[s];
			Graph.finalServers[s].ableFlow = ServerClass.serverFlow[rank[s]];
		}
		//依次降低某个服务器的档次，测试是否有较优解
		for(Integer s : servers){//rank保存最优服务器等级
			if(rank[s] <= 0){//该点没有服务器或者为最低档服务器,无需降档直接跳过；
				continue;
			}
			else{
				Graph.finalServers[s].rank--;
				Graph.finalServers[s].ableFlow = ServerClass.serverFlow[Graph.finalServers[s].rank];
				PathSearchPlus pps = new PathSearchPlus(G, servers);
				int m = pps.getCost();
				if(m<minCost){//降档成功
					rank[s]--;//记录新的服务器等级和最小费用流
//					for(Integer i : servers)
//						System.out.print(rank[i] +" ");
//					System.out.println();
					minCost = m;
				}
				else{//降档不成功，恢复
					Graph.finalServers[s].rank++;
					Graph.finalServers[s].ableFlow = ServerClass.serverFlow[Graph.finalServers[s].rank];
				}
			}
		}
		//最后计算一次zkw，获取路径
		PathSearchPlus ppss = new PathSearchPlus(G, servers);
		route = ppss.printResult();
	}
	public int getCost(){
		return minCost;
	}
	public static void main(String[] args) {
		String graphFilePath = args[0];
		String[] graphContent = FileUtil.read(graphFilePath, null);
		Graph g = new Graph(graphContent);
		long time = System.currentTimeMillis();
		ServerSearchSP ss = new ServerSearchSP(g);
		PathSearchPlus p = new PathSearchPlus(g, ss.getServers());
		System.out.println(p.getCost());
		ChangeServerRankByDij ch = new ChangeServerRankByDij(g, p.getCost(),ss.getServers());
		System.out.println(ch.getCost());
//		for(String s : ch.route)
//			System.out.println(s);
		long time2 = System.currentTimeMillis();
		System.out.println("耗时：" + (time2 - time) + "ms");
	}

}
