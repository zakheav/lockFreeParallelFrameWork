package util;

import sun.misc.Unsafe;

@SuppressWarnings("restriction")
public class SequenceNum {
	private static final Unsafe unsafe;
	@SuppressWarnings("unused")
	private int p1, p2, p3, p4, p5, p6, p7;// 缓冲行填充
	private volatile int value;
	@SuppressWarnings("unused")
	private long p8, p9, p10, p11, p12, p13, p14;// 缓冲行填充
	private static final long VALUE_OFFSET;

	static {
		unsafe = Util.get_unsafe();
		try {
			VALUE_OFFSET = unsafe.objectFieldOffset(SequenceNum.class.getDeclaredField("value"));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public SequenceNum() {
		value = 0;
	}

	public final boolean compareAndSet(final int expectedValue, final int newValue) {
		return unsafe.compareAndSwapInt(this, VALUE_OFFSET, expectedValue, newValue);
	}

	public final int get() {
		return value;
	}
	
	public final void set(int newValue) {
		value = newValue;
	}

	public final int increase(int size) {// size 必须是2^n
		while (true) {
			int now = get();
			int newNum = (now + 1) & (size - 1);
			if (compareAndSet(now, newNum)) {
				return newNum;
			}
		}
	}

	public final int increase() {
		while (true) {
			int now = get();
			int newNum = now + 1;
			if (compareAndSet(now, newNum)) {
				return newNum;
			}
		}
	}
	
	public final int decrease() {
		while (true) {
			int now = get();
			int newNum = now - 1;
			if (compareAndSet(now, newNum)) {
				return newNum;
			}
		}
	}
}
