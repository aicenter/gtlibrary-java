package cz.agents.gtlibrary.utils.threadpool;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool {

	protected LinkedBlockingQueue<Runnable> queue;
	protected LinkedList<PoolWorker> workers;

	public ThreadPool(int capacity) {
		queue = new LinkedBlockingQueue<Runnable>();
		workers = new LinkedList<PoolWorker>();
		for (int i = 0; i < capacity; i++) {
			PoolWorker worker = new PoolWorker(queue);

			worker.start();
			workers.add(worker);
		}
	}

	public void addTask(Runnable task) {
		try {
			queue.put(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void killAll() {
		for (PoolWorker worker : workers) {
			worker.kill();
			worker.interrupt();
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		killAll();
	}

	public boolean isFinnished() {
		for (PoolWorker worker : workers) {
			if(!worker.getState().equals(Thread.State.WAITING))
				return false;
		}
		return true;
	}
}
