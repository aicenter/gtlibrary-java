package cz.agents.gtlibrary.domain.stochastic.experimental;

import java.util.ArrayList;
import java.util.Arrays;
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
