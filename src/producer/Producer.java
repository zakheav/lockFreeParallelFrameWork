package producer;

import preprocess.TaskDistributor;
import util.RingBuffer;

public class Producer {// 生产者
	public RingBuffer taskBuffer;// 对应一个任务队列
	private TaskDistributor taskDistributor;// 对应一个任务分发者
	@SuppressWarnings("unused")
	private volatile boolean memoryBarrier = true;// 提供内存屏障支持

	public Producer() {
		this.taskBuffer = new RingBuffer(2000);
	}

	public void set_taskDistributor(TaskDistributor distributor) {
		this.taskDistributor = distributor;
	}

	public void add_Object(Object o) {
		if (!taskBuffer.isFull()) {
			taskBuffer.add_element(o);
			memoryBarrier = true;// 内存屏障，保证之前的指令不会重排序到后面
			if (taskDistributor.block) {
				synchronized (taskBuffer) {
					taskBuffer.notify();
				}
			}
		} else {
			SpillTaskQueue.overFlowTasks.offer(o);
		}
	}
}
