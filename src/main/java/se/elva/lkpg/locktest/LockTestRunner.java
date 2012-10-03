package se.elva.lkpg.locktest;

public class LockTestRunner {
	
	private static final int THREAD_COUNT = 1;
	private static Thread[] threads = new Thread[THREAD_COUNT];
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for (int i = 0; i < THREAD_COUNT; i++) {
			threads[i] = new Thread(new Runner());
			threads[i].start();			
		}
		
	}
	
	private static class Runner implements Runnable {
		public void run() {
			LockTest lockTest = new LockTest();
			lockTest.initializeCache();
			//for (int i = 0; i < 10; i++) {
			for (;;) {
				lockTest.runTestDefaultCache();
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
			}
		}
	}
}
