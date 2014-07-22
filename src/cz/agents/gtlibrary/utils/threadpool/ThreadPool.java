/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


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
