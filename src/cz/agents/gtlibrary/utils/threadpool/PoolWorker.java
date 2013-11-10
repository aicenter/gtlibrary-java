package cz.agents.gtlibrary.utils.threadpool;

import java.util.concurrent.BlockingQueue;

public class PoolWorker extends Thread {

	private BlockingQueue<Runnable> queue;
	private boolean kill = false;

	public PoolWorker(BlockingQueue<Runnable> queue) {
		this.queue = queue;
	}

	@Override
	public void run() {
		super.run();
		Runnable task = null;

		while (!kill) {
			try {
//				System.out.println("Thread " + getId() + " is waiting for task");
				task = queue.take();
			} catch (InterruptedException e1) {
//				e1.printStackTrace();
			}
//			System.out.println("Thread " + getId() + " took task, queue size is " + queue.size());
			try {
				task.run();
//				System.out.println("Thread " + getId() + " finnished");
			} catch (RuntimeException e) {
				e.printStackTrace();
				System.err.println("Fail during task execution.");
			}
		}
	}
	
	public void kill() {
		kill = true;
	}
}
