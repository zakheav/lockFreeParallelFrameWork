package test;

import util.SequenceNum;

public class Test1 {// 测试SequenceNum的increase原子性
	public static int counter[] = new int[200000];
	public static SequenceNum s = new SequenceNum();

	public static void main(String[] args) {
		new Thread(new Task1(s)).start();
		new Thread(new Task1(s)).start();
		try {
			Thread.sleep(1000);
		} catch(Exception e) {
			
		}
		for(int i=0; i<200000; ++i) {
			if(counter[i] != 1) {
				System.out.println(i);
			}
		}
	}
}
