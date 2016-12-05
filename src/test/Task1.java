package test;

import util.SequenceNum;

public class Task1 implements Runnable {
	private SequenceNum s;
	public Task1(SequenceNum s) {
		this.s = s;
	}
	@Override
	public void run() {
		for(int i=0; i<100000; ++i) {
			Test1.counter[s.increase()-1] = 1;
		}
	}
}
