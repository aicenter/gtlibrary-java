package cz.agents.gtlibrary.experimental.stochastic.characteristics;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kail
 * Date: 12/11/13
 * Time: 9:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class RPCommitmentGenerator implements Iterator<Map<Path,Integer>> {
    public int DEPTH = 2;
    public int discretizations = 10;
    public int nodes = 3;
    public int startingNode = 0;

    protected LinkedHashMap<Path, Integer> currentCommitment = null;
    protected boolean firstRun = true;

    public RPCommitmentGenerator(int DEPTH, int discretizations, int nodes, int startingNode) {
        this.DEPTH = DEPTH;
        this.discretizations = discretizations;
        this.nodes = nodes;
        this.startingNode = startingNode;
        generateAllPaths();
    }

    public int getCurrentProbability(Path path) {
        return currentCommitment.get(path);
    }

    private void generateAllPaths() {
        currentCommitment = new LinkedHashMap<Path, Integer>();
        ArrayList<Path> queue = new ArrayList<Path>();
        Path root = new Path(new int[] {});
        queue.add(root);
        while (!queue.isEmpty()) {
            Path currentNode = queue.remove(queue.size()-1);
            if (currentNode.getHistory().size() < DEPTH)
                for (int i=0; i<nodes; i++) {
                    List newNodeHist = new ArrayList(currentNode.getHistory());
                    newNodeHist.add(i);
                    queue.add(new Path(newNodeHist));
                }
            else {
                int value = 0;
                if (queue.isEmpty()) value = discretizations;
                currentCommitment.put(currentNode, value);
            }
        }
    }

    @Override
    public boolean hasNext() {
        if (firstRun) {
            return true;
        } else {
            if (currentCommitment.get(currentCommitment.keySet().iterator().next()) < discretizations) {
                return true;
            }
        }
        // all probabilities are set to 1 for the first action -- the last commitment
        return false;
    }

    @Override
    public Map<Path, Integer> next() {
        if (!hasNext()) return null;
        if (firstRun) {
            firstRun = false;
            return currentCommitment;
        }

        int degrees = discretizations;
        Iterator<Path> i = currentCommitment.keySet().iterator();
        LinkedList<Path> tmpList = new LinkedList<Path>();
        Path last = null;

        while (i.hasNext()) {
            Path p = i.next();
            if (i.hasNext()) {
                degrees = degrees - currentCommitment.get(p);
                tmpList.addFirst(p);
            } else {
                last = p;
            }
        }


        if (degrees == 0) {
            boolean increaseNext = false;
            for (Path p : tmpList) {
                if (increaseNext) {
                    currentCommitment.put(p, currentCommitment.get(p) + 1);
                    increaseNext = false;
                    break;
                }
                if (getCurrentProbability(p) > 0) {
                    degrees = currentCommitment.get(p);
                    currentCommitment.put(p,0);
                    increaseNext = true;
                    continue;
                }
            }
            assert (!increaseNext);
        } else {
            Path p = tmpList.getFirst();
            currentCommitment.put(p, currentCommitment.get(p) + 1);
        }
        currentCommitment.put(last, degrees - 1);
        return currentCommitment;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    static public void main(String[] args) {
        RPCommitmentGenerator test = new RPCommitmentGenerator(2,5,3,0);
        HashSet<Characteristic> chars = new HashSet<Characteristic>();
//        for (Path p : test.currentCommitment.keySet()) {
//            System.out.println(p);
//        }
        int commitments = 0;

        int[] charValues = new int[test.discretizations+1];

        while (test.hasNext()) {
//            System.out.println("****************************************");
            Map<Path, Integer> map = test.next();
            commitments++;
            Characteristic c = new Characteristic(test.startingNode, test.discretizations, test.DEPTH, test.nodes, map);
            if (chars.add(c)) {
                charValues[c.getValue()]++;
                if (chars.size() % 100000 == 0) System.out.println("Characteristics : " + chars.size());
                if (c.getValue() == 3)
                    System.out.println(c);
            }

//            System.out.println(c);
//            for (Path p : map.keySet()) {
//                System.out.println(p + "   -->   " + map.get(p));
//            }
//            try {
//                System.in.read();
//            } catch (IOException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
        }
        System.out.println("Commitments : " + commitments);
        System.out.println("Characteristics : " + chars.size());
        System.out.println("Char Values : " + Arrays.toString(charValues));
    }
}
