package util;

public class RingBuffer {
	private volatile int writeFinish = 0;// 生产者线程向消费者线程发消息

	private final int SIZE;
	private Object[] ringBuffer;
	public SequenceNum readPtr;// 可以读的第一个下标
	public SequenceNum writePtr;// 可以写的第一个下标

	private volatile boolean memoryBarrier = true;// 提供内存屏障支持
	@SuppressWarnings("unused")
	private volatile boolean mb = true;// 提供内存屏障支持

	// writePtr 在 readPtr后面
	// readPtr == writePtr时ringBuffer为空
	// readPtr == (writePtr+1)%size时ringBuffer为满

	// 下面的所有操作都不保证原子性

	public RingBuffer(int size) {
		this.SIZE = size;
		this.ringBuffer = new Object[size];
		this.readPtr = new SequenceNum();
		this.writePtr = new SequenceNum();
	}

	public boolean add_element(Object o) {// 支持多生产者访问
		writeFinish = 0;
		mb = memoryBarrier;// 内存屏障, 保证后面的指令不会重排序到这条指令之前
		int writeIdx = writePtr.getAndIncrease(SIZE);
		ringBuffer[writeIdx] = o;
		writeFinish = 1;// 生产者写结束，向消费者发出消息
		return true;
	}

	public Object get_element() {// 支持多消费者访问
		int readIdx = readPtr.getAndIncrease(SIZE);
		while (writeFinish == 0)
			;// 等待生产者写结束
		Object o = ringBuffer[readIdx];
		return o;
	}

	public boolean isFull() {
		int readIdx = readPtr.get();
		int writeIdx = writePtr.get();
		return readIdx == (writeIdx + 1) % SIZE;
	}

	public boolean isEmpty() {
		int readIdx = readPtr.get();
		int writeIdx = writePtr.get();
		return readIdx == writeIdx;
	}
}
