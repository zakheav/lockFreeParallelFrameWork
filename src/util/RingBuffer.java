package util;

public final class RingBuffer {

	private final SequenceNum getReadLock;// 同步对readPtr的操作
	private final SequenceNum getWriteLock;// 同步对writePtr的操作

	private final int SIZE;// 一定是2^n
	private Object[] ringBuffer;
	public final SequenceNum readPtr;// 可以读的第一个下标
	public final SequenceNum writePtr;// 可以写的第一个下标
	public SequenceNum block;// 阻塞在ringBuffer的线程数目
	
	// writePtr 在 readPtr后面
	// readPtr == writePtr时ringBuffer为空
	// readPtr == (writePtr+1)%size时ringBuffer为满

	// 下面的所有操作都不保证原子性

	public RingBuffer(int size) {
		this.SIZE = size;
		this.ringBuffer = new Object[size];
		this.readPtr = new SequenceNum();
		this.writePtr = new SequenceNum();
		this.getReadLock = new SequenceNum();
		this.getWriteLock = new SequenceNum();
		this.block = new SequenceNum();
	}

	public final boolean add_element(Object o) {// 会同步多个线程的同时写
		while (!getWriteLock.compareAndSet(0, 1))
			;// 获取写锁

		int readIdx = readPtr.get();
		int writeIdx = writePtr.get();

		if (((writeIdx + 1) & (SIZE - 1)) == readIdx) {// buffer已经满了
			getWriteLock.set(0);// 释放写锁
			return false;
		}

		ringBuffer[writeIdx] = o;
		writePtr.increase(SIZE);// 写元素

		getWriteLock.set(0);// 释放写锁
		return true;
	}

	public final Object get_element() {// 会同步多个线程的同时读
		while (!getReadLock.compareAndSet(0, 1))
			;// 获取读锁

		int readIdx = readPtr.get();
		int writeIdx = writePtr.get();

		if (readIdx == writeIdx) {// buffer已经空了
			getReadLock.set(0);// 释放读锁
			return null;
		}

		Object o = ringBuffer[readIdx];
		readPtr.increase(SIZE);// 读取元素

		getReadLock.set(0);// 释放读锁
		return o;
	}

	public final boolean isEmpty() {
		int readIdx = readPtr.get();
		int writeIdx = writePtr.get();
		return readIdx == writeIdx;
	}
}
