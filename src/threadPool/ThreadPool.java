package threadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import util.RingBuffer;
import util.SequenceNum;

public class ThreadPool {
	private List<Worker> workerList;
	private ConcurrentLinkedQueue<Runnable> overFlowTasks;// 任务溢出区
	private final int WORKER_NUM;// 必须是2^n
	private static ThreadPool instance = new ThreadPool();
	private final int noBlockTimes = 10000;
	private SequenceNum workerIdx;

	private ThreadPool() {
		this.WORKER_NUM = 1;// 必须是2^n
		this.workerList = new ArrayList<Worker>();
		this.overFlowTasks = new ConcurrentLinkedQueue<Runnable>();
		for (int i = 0; i < WORKER_NUM; ++i) {
			add_worker();
		}
		this.workerIdx = new SequenceNum();
	}

	public static ThreadPool get_instance() {
		return instance;
	}

	private void add_worker() {
		RingBuffer taskBuffer = new RingBuffer(65536);
		Worker worker = new Worker(taskBuffer);
		worker.start();
		workerList.add(worker);
	}

	class Worker extends Thread {
		public RingBuffer taskBuffer;

		public Worker(RingBuffer taskBuffer) {
			this.taskBuffer = taskBuffer;
		}

		public void run() {
			int noBlockTimer = noBlockTimes;// 用于减少不必要的线程阻塞,尤其在大量简单的小任务加入线程池的时候
			Object task = null;
			while (true) {
				do {
					task = taskBuffer.get_element();
					if (task != null) {
						((Runnable) task).run();
					}
				} while (task != null);

				do {// 检查溢出区是否存在任务
					task = overFlowTasks.poll();
					if (task != null) {
						((Runnable) task).run();
					}
				} while (task != null);

				if (noBlockTimer > 0) {
					--noBlockTimer;
				} else {
					noBlockTimer = noBlockTimes;
					taskBuffer.block.increase();// 设置这个线程将要阻塞

					synchronized (taskBuffer) {
						while (taskBuffer.isEmpty()) {
							try {
								taskBuffer.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						taskBuffer.block.decrease();
					}
				}
			}
		}
	}

	public void add_task(Runnable task) {
		int idx = workerIdx.increase(WORKER_NUM);
		Worker worker = workerList.get(idx);

		if (!worker.taskBuffer.add_element(task)) {// 无法向buffer中添加任务（buffer满）
			overFlowTasks.offer(task);
		}

		if (worker.taskBuffer.block.get() == 1) {// 这个worker在阻塞等待新的任务
			synchronized (worker.taskBuffer) {
				worker.taskBuffer.notify();
			}
		}
	}
}
