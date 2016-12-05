package test;

public class Task2 implements Runnable {
	public int no;

	public Task2(int no) {
		this.no = no;
	}

	@Override
	public void run() {
		Test2.counter[no] = 1;
	}
}
