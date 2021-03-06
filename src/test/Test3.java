package test;

import java.util.ArrayList;
import java.util.List;

import iterationThreadPool.IterationThreadPool;

public class Test3 {// 测试分批执行任务
	public static int counter[] = new int[20000];
	public static void main(String[] args) {
		IterationThreadPool itp = IterationThreadPool.get_instance();
		List<Runnable> tasks = new ArrayList<Runnable>();
		for(int i=0; i<20000; ++i) {
			tasks.add(new Task3(i));
		}
		long begin = System.currentTimeMillis();
		for(int i=0; i<1000; ++i) {
			itp.add_taskList(tasks);
			for(int j=0; j<20000; ++j) {
				if(counter[j] != 1) {
					System.out.println(j);
				}
			}
			counter = new int[20000];
		}
		long cost = System.currentTimeMillis() - begin;
		System.out.println("cost: "+cost);
	}
}
