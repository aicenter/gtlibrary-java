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


/*
 * Generator taken from: http://www.javamex.com/tutorials/random_numbers/numerical_recipes.shtml
 */
package cz.agents.gtlibrary.utils;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HighQualityRandom extends Random {
  private Lock l = new ReentrantLock();
  private long u;
  private long v = 4101842887655102017L;
  private long w = 1;
  
  public HighQualityRandom() {
    this(System.nanoTime());
  }
  public HighQualityRandom(long seed) {
    l.lock();
    u = seed ^ v;
    nextLong();
    v = u;
    nextLong();
    w = v;
    nextLong();
    l.unlock();
  }
  
  public long nextLong() {
    l.lock();
    try {
      u = u * 2862933555777941757L + 7046029254386353087L;
      v ^= v >>> 17;
      v ^= v << 31;
      v ^= v >>> 8;
      w = 4294957665L * (w & 0xffffffff) + (w >>> 32);
      long x = u ^ (u << 21);
      x ^= x >>> 35;
      x ^= x << 4;
      long ret = (x + v) ^ w;
      return ret;
    } finally {
      l.unlock();
    }
  }
  
  protected int next(int bits) {
    return (int) (nextLong() >>> (64-bits));
  }

}
