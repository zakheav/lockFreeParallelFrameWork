package test;

import threadPool.ThreadPool;

public class Test2 {// 测试线程池会不会丢任务
	public static int counter[] = new int[20000];

	public static void main(String[] args) {
		ThreadPool tp = ThreadPool.get_instance();
		long begin = System.currentTimeMillis();
		for (int j = 1; j <= 1000; ++j) {
			for (int i = 0; i < 20000; ++i) {
				tp.add_task(new Task2(i));
			}

			while (true) {
				int n = 0;// 完成任务数
				for (int i = 0; i < 20000; ++i) {
					if (counter[i] != 0) {
						++n;
					}
				}
				if (n == 20000) {
					break;
				} else {
					// System.out.println("20000......" + n);
				}
			}

			counter = new int[20000];
		}
		long cost = System.currentTimeMillis() - begin;
		System.out.println("cost: " + cost);
	}
}
