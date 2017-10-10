package com.cacheserverdeploy.deploy;

import java.util.LinkedList;

import com.ourwork.ChangeServerRankByDij;
import com.ourwork.ChangeServerRankSP;
import com.ourwork.Graph;
import com.ourwork.LocalSearch;
import com.ourwork.LocalSearch2;
import com.ourwork.PathSearchPlus;
import com.ourwork.SAAN1;
import com.ourwork.SAAN2;
import com.ourwork.SAnnealing;
import com.ourwork.SAnnealing1;
import com.ourwork.ServerSearchSP;
import com.ourwork.ZKW;

//import com.ourwork.Graph;

public class Deploy {
	/**
	 * 你需要完成的入口 <功能详细描述>
	 * 
	 * @param graphContent
	 *            用例信息文件
	 * @return [参数说明] 输出结果信息
	 * @see [类、类#方法、类#成员]
	 */
	public static String[] deployServer(String[] graphContent) {
		/** do your work here **/
		Graph g = new Graph(graphContent);
		ServerSearchSP ss = new ServerSearchSP(g);
		LinkedList<Integer> servers = ss.getServers();
		SAAN1 an = new SAAN1(g, servers);
		PathSearchPlus ps2 = new PathSearchPlus(g, an.servers);
		ChangeServerRankByDij ch0 = new ChangeServerRankByDij(g, ps2.getCost(), an.servers);
		System.out.println(ch0.getCost());
		SAnnealing1 ans3 = new SAnnealing1(g, ch0.servers, ch0.getCost());
		ZKW ps3 = new ZKW(g, ans3.servers);
		ChangeServerRankSP ch = new ChangeServerRankSP(g, ps3.getCost(), ans3.servers);
		System.out.println("modify rank "+ch.getCost());
		return ch.route;
	}

}
