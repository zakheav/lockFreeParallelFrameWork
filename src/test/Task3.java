package test;

public class Task3 implements Runnable {

	public int no;

	public Task3(int no) {
		this.no = no;
	}

	@Override
	public void run() {
		Test3.counter[no] = 1;
	}

}
