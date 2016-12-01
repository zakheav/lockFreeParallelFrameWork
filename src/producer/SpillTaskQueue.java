package producer;

import java.util.concurrent.ConcurrentLinkedQueue;

public class SpillTaskQueue {
	static public ConcurrentLinkedQueue<Object> overFlowTasks = new ConcurrentLinkedQueue<Object>();
}
