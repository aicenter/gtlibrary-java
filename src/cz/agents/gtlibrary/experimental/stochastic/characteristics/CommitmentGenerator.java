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

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kail
 * Date: 12/11/13
 * Time: 9:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class CommitmentGenerator  implements Iterator<Map<Path,int[]>> {
    public int DEPTH = 2;
    public int discretizations = 10;
    public int nodes = 3;
    public int startingNode = 0;

    protected LinkedHashMap<Path, int[]> currentCommitment = null;
    protected boolean firstRun = true;

    public CommitmentGenerator(int DEPTH, int discretizations, int nodes, int startingNode) {
        this.DEPTH = DEPTH;
        this.discretizations = discretizations;
        this.nodes = nodes;
        this.startingNode = startingNode;
        generateAllPaths();
    }

    public int getCurrentProbability(Path node, int action) {
        if (action == nodes-1) {
            int result = discretizations;
            for (int p : currentCommitment.get(node)) {
                result = result - p;
            }
            return result;
        }  else {
            return currentCommitment.get(node)[action];
        }
    }

    private void generateAllPaths() {
        currentCommitment = new LinkedHashMap<Path, int[]>();
        ArrayList<Path> queue = new ArrayList<Path>();
        Path root = new Path(new int[] {});
        queue.add(root);
        while (!queue.isEmpty()) {
            Path currentNode = queue.remove(queue.size()-1);
            currentCommitment.put(currentNode, new int[nodes-1]);
            if (currentNode.getHistory().size() < DEPTH)
                for (int i=0; i<nodes; i++) {
                    List newNodeHist = new ArrayList(currentNode.getHistory());
                    newNodeHist.add(i);
                    queue.add(new Path(newNodeHist));
                }
        }
    }

    @Override
    public boolean hasNext() {
        if (firstRun) {
            firstRun = false;
            return true;
        } else {
            for (Path p : currentCommitment.keySet()) {
                if (currentCommitment.get(p)[0] < discretizations) {
                    return true;
                }
            }
        }
        // all probabilities are set to 1 for the first action -- the last commitment
        return false;
    }

    @Override
    public Map<Path, int[]> next() {
        if (!hasNext()) return null;

        Iterator<Path> i = currentCommitment.keySet().iterator();
        outerloop:
        while (i.hasNext()) {
            Path p = i.next();
            int[] commitmentInNode = currentCommitment.get(p);

//            int degrees;
//            if (p.getHistory().size() == 0) {
//                degrees = discretizations;
//            } else {
//                List<Integer> history = p.getHistory();
//                int histSize = history.size();
//                Path p2;
//                int curNode = history.get(histSize-1);
//                if (histSize == 1)
//                    p2 = new Path(new int[] {});
//                else p2 = new Path(new ArrayList<Integer>(history.subList(0,histSize-1)));
//                degrees = getCurrentProbability(p2,curNode);
//            }

            int degrees = discretizations;

            for (int actions = 0; actions < nodes-1; actions++) {
                degrees = degrees - commitmentInNode[actions];
            }

            if (degrees == 0) {
                for (int actions  = nodes - 2; actions >= 0; actions--) {
                    if (commitmentInNode[actions] > 0) {
                        if (actions == 0) {
                            commitmentInNode[actions] = 0;
                            continue outerloop;
                        }

                        degrees += commitmentInNode[actions];
                        commitmentInNode[actions] = 0;

                        commitmentInNode[actions-1]++;
                        break outerloop;
                    }
                }
            } else {
                commitmentInNode[nodes-2]++;
                break outerloop;
            }

        }
        return currentCommitment;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    static public void main(String[] args) {
        CommitmentGenerator test = new CommitmentGenerator(2,5,3,0);

//        for (Path p : test.currentCommitment.keySet()) {
//            System.out.println(p);
//        }
        int commitments = 0;
        while (test.hasNext()) {
            System.out.println("****************************************");
            Map<Path, int[]> map = test.next();
            commitments++;
            for (Path p : map.keySet()) {
                System.out.println(p + "   -->   " + Arrays.toString(map.get(p)));
            }
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        System.out.println(commitments);
    }
}
