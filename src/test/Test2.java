package test;

import threadPool.ThreadPool;

public class Test2 {// 测试线程池会不会丢任务
	public static int counter[] = new int[20000];

	public static void main(String[] args) {
		ThreadPool tp = ThreadPool.get_instance();
		for(int j=1; j<=100; ++j) {
			for (int i = 0; i < 20000; ++i) {
				tp.add_task(new Task2(i));
			}
			try {
				Thread.sleep(500);
			} catch (Exception e) {

			}
			int n = 0;// 完成任务数
			for (int i = 0; i < 20000; ++i) {
				if (counter[i] != 1) {
					System.out.println(i);
				} else {
					++n;
				}
			}
			if (n != 20000)
				System.out.println("......" + n);
		}
		
		System.out.println("finish");
	}
}
