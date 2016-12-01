package threadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import util.RingBuffer;

public class ThreadPool {
	private List<Worker> workerList;
	private ConcurrentLinkedQueue<Runnable> overFlowTasks;// 任务溢出区
	private final int WORK_NUM;
	private static ThreadPool instance = new ThreadPool();
	
	private volatile boolean memoryBarrier = true;// 提供内存屏障支持
	@SuppressWarnings("unused")
	private volatile boolean mb = true;// 提供内存屏障支持
	
	private ThreadPool() {
		this.WORK_NUM = 10;
		this.workerList = new ArrayList<Worker>();
		this.overFlowTasks = new ConcurrentLinkedQueue<Runnable>();
		for (int i = 0; i < WORK_NUM; ++i) {
			addWorker();
		}
	}

	public static ThreadPool getInstance() {
		return instance;
	}

	private void addWorker() {
		RingBuffer taskBuffer = new RingBuffer(40000);
		Worker worker = new Worker(taskBuffer);
		worker.start();
		workerList.add(worker);
	}

	class Worker extends Thread {
		private volatile boolean block;// 用于判断这个worker是否已经阻塞等待新的任务
		public RingBuffer taskBuffer;

		public Worker(RingBuffer taskBuffer) {
			this.taskBuffer = taskBuffer;
			this.block = false;
		}

		public boolean isBlock() {
			block = block;// 在block变量之前添加内存屏障，该函数前的指令不会被重排序到前面
			return block;
		}

		public void run() {
			
			while (true) {
				while (!taskBuffer.isEmpty()) {
					Runnable task = (Runnable)taskBuffer.getElement();
					task.run();
				}
				while (!overFlowTasks.isEmpty()) {// 检查溢出区是否存在任务
					Runnable task = overFlowTasks.poll();
					if (task != null) {
						task.run();
					}
				}
				
				this.block = true;
				mb = memoryBarrier;// 在block变量之后添加内存屏障，该指令后面的指令不会被重排序到前面
				
				synchronized (taskBuffer) {
					while (taskBuffer.isEmpty()) {
						try {
							taskBuffer.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					this.block = false;
					mb = memoryBarrier;// 在block变量之后添加内存屏障，该指令后面的指令不会被重排序到前面
				}
			}
		}
	}

	public void addTask(Runnable task) {
		
		int idx = (int) (Math.random() * WORK_NUM);
		if (idx == WORK_NUM)
			--idx;
		Worker worker = workerList.get(idx);
		if (!worker.taskBuffer.isFull()) {
			worker.taskBuffer.addElement(task);
			
			memoryBarrier = true;// 内存屏障，保证之前的指令不会重排序到后面
			if (worker.block) {// 这个worker在阻塞等待新的任务
				synchronized (worker.taskBuffer) {
					worker.taskBuffer.notify();
				}
			}
		} else {
			overFlowTasks.offer(task);
		}
	}
}
