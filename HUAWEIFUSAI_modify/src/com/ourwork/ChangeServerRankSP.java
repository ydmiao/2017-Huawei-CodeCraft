package com.ourwork;

import java.util.LinkedList;

import com.filetool.util.FileUtil;

public class ChangeServerRankSP {
	private int[] rank;//服务器等级数组
	private int minCost;//最小成本
	public LinkedList<Integer> servers ;//服务器位置数组
	public String[] route;//最终路径
	
	public ChangeServerRankSP(Graph G,int cost,LinkedList<Integer> ser){
		rank = new int[G.netNodesArray.length];//初始化rank数组
		
		minCost = cost;
		LinkedList<Integer> newSer = new LinkedList<Integer>(ser);//服务器位置的临时数组
		servers = new LinkedList<Integer>(newSer);
		int time = 0;
		for(int i = 0;time<newSer.size();i++){//调整服务器的先后顺序
			time++;
			Integer j = newSer.remove(i);
			newSer.addLast(j);
			ZKW z = new ZKW(G, newSer);
			int m = z.getCost();
			if(m<minCost){
				minCost = m;
				servers = new LinkedList<Integer>(newSer);
//				System.out.println(minCost);
				i--;
			}
			else{
				newSer.removeLast();//恢复
				newSer.add(i, j);
			}
		}//服务器的顺序调整完毕
		
		ZKW z1 = new ZKW(G, servers);
		rank = z1.rankOfServer;
		for(Integer s : servers){//将最终的服务器级别写入fanalServers
			Graph.finalServers[s].rank= rank[s];
			Graph.finalServers[s].ableFlow = ServerClass.serverFlow[rank[s]];
		}
//		依次降低某个服务器的档次，测试是否有较优解

		for(Integer s : servers){//rank保存最优服务器等级
			if(rank[s] <= 0){//该点没有服务器或者为最低档服务器,无需降档直接跳过；
				continue;
			}
			else{
				Graph.finalServers[s].rank--;
				Graph.finalServers[s].ableFlow = ServerClass.serverFlow[Graph.finalServers[s].rank];
				ZKW zkw = new ZKW(G, servers);
				int m = zkw.getCost();
				if(m<minCost){//降档成功
					rank[s]--;//记录新的服务器等级和最小费用流
					minCost = m;
				}
				else{//降档不成功，恢复
					Graph.finalServers[s].rank++;
					Graph.finalServers[s].ableFlow = ServerClass.serverFlow[Graph.finalServers[s].rank];
				}
			}
		}
		
		//最后计算一次zkw，获取路径
		ZKW z2 = new ZKW(G, servers);
		route = z2.writeRoutes();
	}
	public int getCost(){
		return minCost;
	}

public static void main(String[] args) {
	long time = System.currentTimeMillis();
	String graphFilePath = args[0];
	String[] graphContent = FileUtil.read(graphFilePath, null);
	Graph g = new Graph(graphContent);
	int[] data={242, 125, 459, 276, 552, 495, 579, 238, 186, 113, 260, 236, 72,  306, 150, 399, 335, 378, 152, 398, 376, 293, 75, 160, 510, 34, 358, 92, 62, 457, 61, 548, 444, 227, 300, 31, 506, 411, 446};
    LinkedList<Integer> servers=new LinkedList<Integer>();
    for(int i:data)
    	servers.add(i);
    ZKW an=new ZKW(g,servers);
    System.out.println(an.getCost());
    ChangeServerRankSP ans=new ChangeServerRankSP(g, an.getCost(), servers);
    System.out.println(ans.getCost());
}
}


