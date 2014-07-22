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


package cz.agents.gtlibrary.experimental.stochastic.characteristics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kail
 * Date: 12/11/13
 * Time: 8:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class Path {
    List<Integer> history = new ArrayList<Integer>();

    public Path(List<Integer> history) {
        this.history = history;
    }

    public Path(int[] history) {
        this.history = new ArrayList<Integer>();
        for (int i : history)
            this.history.add(i);
    }

    public List<Integer> getHistory() {
        return history;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Path)) return false;

        Path path = (Path) o;

        if (history != null ? !history.equals(path.history) : path.history != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return history != null ? history.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Path{" +
                "history=" + history +
                '}';
    }
}
