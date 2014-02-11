package cz.agents.gtlibrary.experimental.stochastic.characteristics;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: kail
 * Date: 12/4/13
 * Time: 7:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class Characteristic {
    int startingNode; //for this characteristic
    int discretization; //how much different values there are between 0 and 1
    int d; // length of the attack
    int nodes;
    int[][] probabilities;

    public Characteristic(int startingNode, int discretization, int d, int nodes) {
        this.startingNode = startingNode;
        this.discretization = discretization;
        this.d = d;
        this.nodes = nodes;
        this.probabilities = new int[d][nodes];
    }

    public Characteristic(int startingNode, int discretization, int d, int nodes, Map<Path, Integer> commitment) {
        this(startingNode,discretization,d,nodes);
        for (Path p : commitment.keySet()) {
            int probability = commitment.get(p);
            List<Integer> history = p.getHistory();
            boolean[] alreasyVisited = new boolean[nodes];
            for (int dd=0; dd<history.size(); dd++) {
                int node = history.get(dd);
                if (!alreasyVisited[node]) {
                    probabilities[dd][node] += probability;
                    alreasyVisited[node] = true;
                }
            }
        }
    }

    public int getValue() {
        int result = Integer.MAX_VALUE;
        for (int node = 0; node < nodes; node++) {
            int curNodeVal = 0;
            for (int dd=0; dd<d; dd++) {
                curNodeVal += probabilities[dd][node];
            }
            if (curNodeVal < result) {
                result = curNodeVal;
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Characteristic)) return false;

        Characteristic that = (Characteristic) o;

        if (d != that.d) return false;
        if (discretization != that.discretization) return false;
        if (nodes != that.nodes) return false;
        if (startingNode != that.startingNode) return false;
        if (!Arrays.deepEquals(probabilities, that.probabilities))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = startingNode;
        result = 31 * result + discretization;
        result = 31 * result + d;
        result = 31 * result + nodes;
        result = 31 * result + Arrays.deepHashCode(probabilities);
        return result;
    }

    @Override
    public String toString() {
        return "Characteristic{" +
                "startingNode=" + startingNode +
                ", discretization=" + discretization +
                ", d=" + d +
                ", nodes=" + nodes +
                ", probabilities=" + (probabilities == null ? null : Arrays.deepToString(probabilities)) +
                '}';
    }
}
