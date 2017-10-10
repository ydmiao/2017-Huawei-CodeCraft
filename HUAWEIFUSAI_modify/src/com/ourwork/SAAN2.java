package com.ourwork;

import java.util.LinkedList;

import com.filetool.util.FileUtil;

public class SAAN2 {
	private LadderSP lsp;// 节点作为服务器的优先级
	private DeleteServers ds;
	private int totalCost;// 最后的总成本
	public LinkedList<Integer> servers;// 最后找到的服务器
	private int count=0;//add from mandy
	private int repeatCount=0;
	private int curCost;
	private static int  repeatCost=Integer.MAX_VALUE;

	public SAAN2(Graph g, LinkedList<Integer> curSer) {
		int vsize = g.netNodesArray.length;
		lsp = new LadderSP(g);
		ZKW rs = new ZKW(g, curSer);
		int curCost = rs.getCost();
		totalCost = curCost;
		long time = System.currentTimeMillis();
//		lsp = new LadderSP(g);
		servers =new LinkedList<Integer>(curSer);
		while(repeatCount<2){
//			System.out.println("---------------------------------");
		while(curCost<=totalCost){
			count=0;
			totalCost = curCost;
			servers = new LinkedList<Integer>(curSer);  //3.30
			LinkedList<Integer> tempser =new LinkedList<Integer>(curSer);
			ZKW ss = new ZKW(g, curSer);
			curCost = ss.getCost();
//			System.out.println("##########"+curCost);
		    lsp.setCostSumNode(g, ss.getRoutes());// 处理初始解的路径
		    IndexMaxPQ<Double> pq = lsp.getLadder(g);
		    int newCost=ss.getCost();
		    while(newCost>=curCost){
		    	if (count > vsize / 4)
					break;
		    	tempser =new LinkedList<Integer>(curSer);
		    	int k = pq.delMax();
		    	count++;
		    	if(k == -1){
					break;
				}
				else
					while (tempser.contains(k) && (!pq.isEmpty())){
						k = pq.delMax();
						count++;
					}
		    	tempser.add(k);
		    	lsp.getCostSumNode()[k] = -50000;// 这样写也行？
		    	ZKW ps = new ZKW(g, tempser);
			    newCost=ps.getCost();
		    }
		    curCost=newCost;    //when k==-1,the newCost is not the best
		    curSer=tempser;
//		    System.out.println(curCost);
		}
		curCost=totalCost;
		if(totalCost==repeatCost)
			break;
		curSer=new LinkedList<Integer>(servers);   //3.30
		System.out.println(servers.size() + " " + totalCost);
		//**********************************************************、//
//		if(repeatCost == totalCost)
//			break;
//		System.out.println(curCost);
		while(curCost<=totalCost){
			totalCost = curCost;
			servers = new LinkedList<Integer>(curSer);   //3.30
			LinkedList<Integer> tempser =new LinkedList<Integer>(curSer);
			ZKW ss = new ZKW(g, curSer);
			curCost = ss.getCost();
		    lsp.setCostSumNode(g, ss.getRoutes());// 处理初始解的路径
		    ds = new DeleteServers(g, curSer, ss.getRoutes());
		    int newCost=ss.getCost();
		    while(newCost>=curCost){
		    	tempser =new LinkedList<Integer>(curSer);
		    	for(int ij:ds.getServers()){
					tempser.remove(tempser.indexOf(ij));
				}
				int aa=ds.getServer();
				if(aa == -1){
					break;
				}
				else
					tempser.remove(tempser.indexOf(aa));
				ZKW ps = new ZKW(g, tempser);
				newCost=ps.getCost();
				
		    }
		    curCost=newCost;   //when aa==-1,the newCost is not the best                    
		    curSer=tempser;   
//		    System.out.println(newCost);
		}
		ZKW ps = new ZKW(g, servers);
		curCost=ps.getCost();
//		totalCost=curCost;
//		curSer=new LinkedList<Integer>(servers);
//		Collections.sort(servers);
		long time2 = System.currentTimeMillis();
		if (time2 - time >20000)
			break;
		if(repeatCost!=totalCost)
			repeatCost=totalCost;
		else
			repeatCount++;
		}
	}

	public LinkedList<Integer> getServers() {
		return servers;
	}

	public int getTotalCost() {
		return totalCost;
	}

	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		String graphFilePath = args[0];
		String[] graphContent = FileUtil.read(graphFilePath, null);
		Graph g = new Graph(graphContent);
		ServerSearchSP ss = new ServerSearchSP(g);
		LinkedList<Integer> servers = ss.getServers();
		
		System.out.println("origin servers: "+servers.size()+" "+servers);         //add from mandy   
		SAAN2 an = new SAAN2(g, servers);
		System.out.println(an.servers.size() + " " + an.totalCost+" "+an.servers);
//		LocalSearch2 ans=new LocalSearch2(g, an.servers);
//		System.out.println(ans.servers.size() + " " + ans.totalCost+" "+ans.servers);
		//以上第一轮大搜索
//		SAAN2 an2 = new SAAN2(g, ans.servers);   //再来一次SAAN搜索
//		System.out.println(an2.servers.size() + " " + an2.totalCost+" "+an2.servers);
//		LocalSearch2 ans2=new LocalSearch2(g, an2.servers);  //再来一次领域搜索
//		System.out.println(ans2.servers.size() + " " + ans2.totalCost+" "+ans2.servers);
		SAnnealing ans3 = new SAnnealing(g, an.servers, an.totalCost);
		System.out.println(ans3.servers.size() + " " + ans3.totalCost + "" + ans3.servers);
		
		ZKW ps2 = new ZKW(g, ans3.servers);
		System.out.println("ZKW " + ps2.getCost());
		
		ChangeServerRankSP answer=new ChangeServerRankSP(g, ps2.getCost(),ans3.servers);
		System.out.println("modify rank "+answer.getCost());
		//以上第二轮大搜索
		long time2 = System.currentTimeMillis();
		System.out.println("Time:" + (time2 - time) + "ms");
	}
}
