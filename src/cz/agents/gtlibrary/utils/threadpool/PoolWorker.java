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
