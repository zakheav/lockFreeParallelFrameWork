package preprocess;

import producer.Producer;
import producer.SpillTaskQueue;

public abstract class TaskDistributor extends Thread {
	protected Producer producer;
	public volatile boolean block;// 用于判断是否已经阻塞等待新的任务

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
			synchronized (producer.taskBuffer) {
				while (producer.taskBuffer.isEmpty()) {
					try {
						producer.taskBuffer.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				this.block = false;
			}
		}
	}// 获取任务

	protected abstract void distribute_task(Object o);// 分发任务
}
