package producer;

import preprocess.TaskDistributor;
import util.RingBuffer;

public class Producer {// 生产者
	public RingBuffer taskBuffer;// 对应一个任务队列
	protected TaskDistributor taskDistributor;// 对应一个任务分发者

	public Producer() {
		this.taskBuffer = new RingBuffer(65536);
	}

	public void set_taskDistributor(TaskDistributor distributor) {
		this.taskDistributor = distributor;
	}

	public void add_Object(Object o) {
		if (!taskBuffer.add_element(o)) {
			SpillTaskQueue.overFlowTasks.offer(o);
		}

		if (taskDistributor.block) {
			synchronized (taskBuffer) {
				taskBuffer.notify();
			}
		}
	}
}
