package preprocess;

import producer.Producer;
import producer.SpillTaskQueue;

public abstract class TaskDistributor extends Thread {
	protected Producer producer;
	public volatile boolean block;// 用于判断是否已经阻塞等待新的任务

	private volatile boolean memoryBarrier = true;// 提供内存屏障支持
	@SuppressWarnings("unused")
	private volatile boolean mb = true;// 提供内存屏障支持

	public TaskDistributor(Producer producer) {
		this.producer = producer;
		this.block = false;
	}

	public void run() {

		while (true) {
			Object task = null;
			do {
				task = producer.taskBuffer.get_element();
				if (task != null)
					distribute_task(task);
			} while (task != null);

			do {// 检查溢出区是否存在任务
				task = SpillTaskQueue.overFlowTasks.poll();
				if (task != null) {
					distribute_task(task);
				}
			} while (task != null);

			this.block = true;
			mb = memoryBarrier;// 在block变量之后添加内存屏障，该指令后面的指令不会被重排序到前面

			synchronized (producer.taskBuffer) {
				while (producer.taskBuffer.isEmpty()) {
					try {
						producer.taskBuffer.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				this.block = false;
				mb = memoryBarrier;// 在block变量之后添加内存屏障，该指令后面的指令不会被重排序到前面
			}
		}
	}// 获取任务

	protected abstract void distribute_task(Object o);// 分发任务
}
