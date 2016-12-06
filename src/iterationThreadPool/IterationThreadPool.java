package iterationThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import util.RingBuffer;
import util.SequenceNum;

public class IterationThreadPool {
	private List<Worker> workerList;
	private ConcurrentLinkedQueue<Runnable> overFlowTasks;// 任务溢出区
	private final int WORK_NUM;
	private static IterationThreadPool instance = new IterationThreadPool();
	private SequenceNum finishTaskNum;// 本批任务的完成数
	
	private volatile boolean memoryBarrier = true;// 提供内存屏障支持
	@SuppressWarnings("unused")
	private volatile boolean mb = true;// 提供内存屏障支持
	
	private IterationThreadPool() {
		this.WORK_NUM = 10;
		this.workerList = new ArrayList<Worker>();
		this.overFlowTasks = new ConcurrentLinkedQueue<Runnable>();
		for (int i = 0; i < WORK_NUM; ++i) {
			add_worker();
		}
	}

	public static IterationThreadPool getInstance() {
		return instance;
	}

	private void add_worker() {
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
					Runnable task = (Runnable)taskBuffer.get_element();
					task.run();
					finishTaskNum.increase();
				}
				while (!overFlowTasks.isEmpty()) {// 检查溢出区是否存在任务
					Runnable task = overFlowTasks.poll();
					if (task != null) {
						task.run();
						finishTaskNum.increase();
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

	private void add_task(Runnable task) {
		
		int idx = (int) (Math.random() * WORK_NUM);
		if (idx == WORK_NUM)
			--idx;
		Worker worker = workerList.get(idx);
		if (!worker.taskBuffer.isFull()) {
			worker.taskBuffer.add_element(task);
			
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
	
	public void add_taskList(List<Runnable> tasks) {// 一次性投放一批任务，保证任务全部完成后返回
		finishTaskNum = new SequenceNum();
		for (Runnable task : tasks) {
			add_task(task);
		}
		while (finishTaskNum.get() < tasks.size()) {
		}
	}
}
