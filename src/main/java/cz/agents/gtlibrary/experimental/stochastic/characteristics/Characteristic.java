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

import java.util.*;

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

    public static  Map<Characteristic,Set<Characteristic>> isClosed(Set<Characteristic> characteristicSet) {
        boolean result = false;


        Set<Characteristic> toRemove = new HashSet<Characteristic>();
        Map<Characteristic,Set<Characteristic>> support = new HashMap<Characteristic, Set<Characteristic>>();
        for (Characteristic c : characteristicSet)
            support.put(c, new HashSet<Characteristic>());

        Iterator<Characteristic> i = support.keySet().iterator();
        while (i.hasNext()) {
            Characteristic currentChar = i.next();
            for (Characteristic c : support.keySet()) {
                if (c.isPrefixOf(currentChar) && !toRemove.contains(c)) {
                    support.get(currentChar).add(c);
                }
            }
            if (support.get(currentChar).isEmpty())
                toRemove.add(currentChar);
        }

        for (Characteristic c : toRemove) {
            support.remove(c);
        }


        boolean change = true;
        while (change) {
            change = false;
            toRemove.clear();
            for (Characteristic c : support.keySet()) {
                Set<Characteristic> thisCharSupport = support.get(c);
                for (Characteristic cc : new HashSet<Characteristic>(thisCharSupport)) {
                    if (!support.containsKey(cc))
                        thisCharSupport.remove(cc);
                }
                if (thisCharSupport.isEmpty()) {
                    toRemove.add(c);
                    change = true;
                }
            }
            for (Characteristic c : toRemove) {
                support.remove(c);
            }
        }

        return support;
    }

    public boolean isPrefixOf(Characteristic otherCharacteristic) {
        for (int l = 1; l<otherCharacteristic.probabilities.length; l++) {
            if (!Arrays.equals(this.probabilities[l-1], otherCharacteristic.probabilities[l]))
                return false;
        }
        return true;
    }
}
