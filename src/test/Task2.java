package test;

public class Task2 implements Runnable {
	public int no;

	public Task2(int no) {
		this.no = no;
	}

	@Override
	public void run() {
//		int a = 0;
//		for(int i=0; i<10000; ++i) {
//			for(int j=0; j<10000; ++j) {
//				++a;
//			}
//		}
		Test2.counter[no] = 1;
	}
}
