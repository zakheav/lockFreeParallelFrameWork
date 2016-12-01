package util;

public class RingBuffer {
	private final int SIZE;
	private Object[] ringBuffer;
	public SequenceNum readPtr;// 可以读的第一个下标
	public SequenceNum writePtr;// 可以写的第一个下标

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

	public boolean addElement(Object task) {// 由于只有一个生产者，所以下面的过程不用加锁同步
		int writeIdx = writePtr.get();
		ringBuffer[writeIdx] = task;
		writePtr.increase(SIZE);
		return true;
	}

	public Object getElement() {// 由于只有一个消费者，所以下面的过程不用加锁同步
		int readIdx = readPtr.get();
		Object task = ringBuffer[readIdx];
		readPtr.increase(SIZE);
		return task;
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
