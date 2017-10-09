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


package cz.agents.gtlibrary.domain.pursuit;

import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.graph.Edge;
import cz.agents.gtlibrary.utils.graph.Graph;
import cz.agents.gtlibrary.utils.graph.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PursuitGraph extends Graph {

    private static final long serialVersionUID = -3128524115361864271L;

    private Node evaderStart;
    private Node p1Start;
    private Node p2Start;
    private Node evaderGoal;
    private double[][] distanceMatrix;

    public PursuitGraph(String graphFile) {
        super(graphFile);
    }

    protected void init() {
        super.init();
        computeDistanceMatrix();
        if (PursuitGameInfo.randomizeStartPositions) {
            Random random = new HighQualityRandom(PursuitGameInfo.seed);

            while (p1Start == null || p2Start == null) {
                int nodeCount = getAllNodes().size();
                double correctDistance = Math.floor(2 / 3. * PursuitGameInfo.depth);
                List<Node> nodes = new ArrayList<>(getAllNodes().values());

                evaderStart = getAllNodes().get("ID" + random.nextInt(nodeCount));
                Collections.shuffle(nodes, random);
                for (Node node : nodes) {
                    if (distanceMatrix[evaderStart.getIntID()][node.getIntID()] <= correctDistance)
                        if (p1Start == null) {
                            p1Start = node;
                        } else {
                            p2Start = node;
                            break;
                        }
                }
                if (p1Start == null || p2Start == null) {
                    p1Start = null;
                    p2Start = null;
                    System.out.println("Impossible to place patrolers, moving evader");
                }
            }
//            int nodes = getAllNodes().size();
//            int tmp = (nodes)*(nodes-1)*(nodes-2);
//            HighQualityRandom rnd = new HighQualityRandom(PursuitGameInfo.seed);
//            int choice = rnd.nextInt(tmp);
//            int en = choice / ((nodes-1)*(nodes-2));
//            choice = choice % ((nodes-1)*(nodes-2));
//            int p1n = choice / (nodes - 2);
//            if (p1n == en) p1n++;
//            int p2n = choice % (nodes-2);
//            while (p2n == p1n || p2n == en)
//                p2n++;
//            evaderStart = allNodes.get("ID" + en);
//            p1Start = allNodes.get("ID" + p1n);
//            p2Start = allNodes.get("ID" + p2n);
//            PursuitGameInfo.evaderStart = en;
//            PursuitGameInfo.p1Start = p1n;
//            PursuitGameInfo.p2Start = p2n;
            PursuitGameInfo.p1Start = p1Start.getIntID();
            PursuitGameInfo.p2Start = p2Start.getIntID();
            PursuitGameInfo.evaderStart = evaderStart.getIntID();

        } else {
            evaderStart = allNodes.get("ID" + PursuitGameInfo.evaderStart);
            p1Start = allNodes.get("ID" + PursuitGameInfo.p1Start);
            p2Start = allNodes.get("ID" + PursuitGameInfo.p2Start);
            if (PursuitGameInfo.evaderGoal != -1)
                evaderGoal = allNodes.get("ID" + PursuitGameInfo.evaderGoal);
        }
    }

    private void computeDistanceMatrix() {
        distanceMatrix = new double[getAllNodes().size()][getAllNodes().size()];

        for (int i = 0; i < distanceMatrix.length; i++) {
            for (int j = 0; j < distanceMatrix[0].length; j++) {
                if (i != j)
                    distanceMatrix[i][j] = Double.POSITIVE_INFINITY;
                else
                    distanceMatrix[i][j] = 0;
            }
        }
        for (Edge edge : graph.edgeSet()) {
            distanceMatrix[edge.getSource().getIntID()][edge.getTarget().getIntID()] = 1;
        }
        for (int k = 0; k < distanceMatrix.length; k++) {
            for (int i = 0; i < distanceMatrix.length; i++) {
                for (int j = 0; j < distanceMatrix.length; j++) {
                    double newDistance = distanceMatrix[i][k] + distanceMatrix[k][j];

                    if (distanceMatrix[i][j] > newDistance)
                        distanceMatrix[i][j] = newDistance;
                }
            }
        }
    }

    public Node getEvaderStart() {
        return evaderStart;
    }

    public Node getP1Start() {
        return p1Start;
    }

    public Node getP2Start() {
        return p2Start;
    }

    public Node getEvaderGoal() {
        return evaderGoal;
    }

    public double getDistance(Node start, Node goal) {
        return distanceMatrix[start.getIntID()][goal.getIntID()];
    }

}
