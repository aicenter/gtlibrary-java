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

import cz.agents.gtlibrary.utils.Pair;
import org.jacop.constraints.Sum;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.*;

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

    private boolean generateCSP(Characteristic c, int currentStep, ArrayList<Map<Path, Set<Integer>>> possiblePlans) {
        if (currentStep == c.probabilities.length) return true;
        Set<Path> toCheck = new HashSet<Path>();
        Set<Path>[] constraints = new HashSet[nodes];
        if (possiblePlans.size() == currentStep) {
            Map<Path, Set<Integer>> currentStepMap = new HashMap<Path, Set<Integer>>();
            for (Map.Entry<Path, Set<Integer>> e : possiblePlans.get(currentStep-1).entrySet()) {
                for (int i=0; i<nodes; i++) {
                    List<Integer> history = new ArrayList<Integer>(e.getKey().getHistory());
                    Path p = null;
                    if (!history.contains(i)) {
                        history.add(i);
                        p = new Path(history);
                        Set<Path> csSet = constraints[i];
                        if (csSet == null) csSet = new HashSet<Path>();
                        csSet.add(p);
                        constraints[i] = csSet;
                    } else {
                        history.add(i);
                        p = new Path(history);
                    }

                    Set<Integer> newPlans = new HashSet<Integer>();
                    for (int possibleStrategy = 0; possibleStrategy<Collections.max(e.getValue()); possibleStrategy++) {
                        newPlans.add(possibleStrategy);
                    }
                    currentStepMap.put(p,newPlans);
                    toCheck.add(p);
                }
            }
        }  else assert false;

        while (!toCheck.isEmpty()) {
           toCheck.iterator().next();
        }


        return true;
    }

    private void generateCSP(Characteristic c) {
        Map<Path, Variable> allVariables = new HashMap<Path, Variable>();
        Map<Path, SumConstraint> realizationPlanConstraints = new HashMap<Path, SumConstraint>();
        Map<Pair<Integer, Integer>, SumConstraint> characteristicsConstraints = new HashMap<Pair<Integer, Integer>, SumConstraint>();
        Map<Variable, Set<SumConstraint>> varToConstMap = new HashMap<Variable, Set<SumConstraint>>();

        ArrayList<Path> queue = new ArrayList<Path>();
        for (int n=0; n<nodes; n++) {
            Path root = new Path(new int[] {n});
            queue.add(root);
        }
        while (!queue.isEmpty()) {
            Path currentPath = queue.remove(queue.size()-1);
            int historySize = currentPath.getHistory().size();
            int lastNode = currentPath.getHistory().get(historySize-1);

            boolean isFirstVisitToThisNode = currentPath.getHistory().indexOf(lastNode) == historySize-1;
            Variable currentVar;

            Set<SumConstraint> varConstraints;

            if (historySize == 1) {
//                if (c.startingNode == lastNode) {
//                    isFirstVisitToThisNode = false;
//                    int loopValue = discretizations;
//                    for (int vv : c.probabilities[0]) {
//                        loopValue = loopValue - vv;
//                    }
//                    assert loopValue >= 0;
//                    ArrayList<Integer> allowedValues = new ArrayList<Integer>(1);
//                    allowedValues.add(loopValue);
//                    currentVar = new Variable(currentPath, allowedValues);
//                } else {
                    ArrayList<Integer> allowedValues = new ArrayList<Integer>(1);
                    allowedValues.add(c.probabilities[0][lastNode]);
                    currentVar = new Variable(currentPath, allowedValues);

                    varConstraints = varToConstMap.get(currentVar);
                    if (varConstraints == null) varConstraints = new HashSet<SumConstraint>();
//                }
            }  else {
                Path parent = new Path(currentPath.getHistory().subList(0,historySize-1));
                Variable parentVar = allVariables.get(parent);
                ArrayList<Integer> allowedValues = new ArrayList<Integer>();
                for (int v=0; v<=Math.min(discretizations, Collections.max(parentVar.getDomain())); v++) {
                    allowedValues.add(v);
                }
                currentVar = new Variable(currentPath, allowedValues);
                SumConstraint rpConst = realizationPlanConstraints.get(parent);
                if (rpConst == null) {
                    rpConst = new SumConstraint(new ArrayList<Variable>(), new ArrayList<Variable>());
                    rpConst.addToLeftSide(parentVar);
                }
                rpConst.addToRightSide(currentVar);
                realizationPlanConstraints.put(parent, rpConst);

                varConstraints = varToConstMap.get(currentVar);
                if (varConstraints == null) varConstraints = new HashSet<SumConstraint>();
                varConstraints.add(rpConst);

                if (characteristicsConstraints.containsKey(new Pair<Integer, Integer>(parent.getHistory().get(historySize-2), historySize-1))) {
                    SumConstraint chConst = characteristicsConstraints.get(new Pair<Integer, Integer>(parent.getHistory().get(historySize-2), historySize-1));
                    chConst.addToRightSide(currentVar);
                    characteristicsConstraints.put(new Pair<Integer, Integer>(parent.getHistory().get(historySize-2), historySize-1),chConst);
                    varConstraints.add(chConst);
                }
            }

            if (isFirstVisitToThisNode) {
                SumConstraint chConst = characteristicsConstraints.get(new Pair<Integer, Integer>(lastNode, historySize));
                if (chConst == null) {
                    chConst = new SumConstraint(new ArrayList<Variable>(), new ArrayList<Variable>(), c.probabilities[historySize-1][lastNode]);
                }
                chConst.addToLeftSide(currentVar);
                characteristicsConstraints.put(new Pair<Integer, Integer>(lastNode, historySize), chConst);
                varConstraints.add(chConst);
            }

            allVariables.put(currentPath, currentVar);
            varToConstMap.put(currentVar, varConstraints);

            if (currentPath.getHistory().size() < DEPTH) {
                for (int i=0; i<nodes; i++) {
                    List newNodeHist = new ArrayList(currentPath.getHistory());
                    newNodeHist.add(i);
                    Path newPath = new Path(newNodeHist);
                    queue.add(newPath);
                }
            }
        }

        ArrayList<SumConstraint> allConstraints = new ArrayList<SumConstraint>();
        allConstraints.addAll(realizationPlanConstraints.values());
        allConstraints.addAll(characteristicsConstraints.values());

        ArrayList<Variable> allVars = new ArrayList<Variable>();
        allVars.addAll(allVariables.values());

        Store store = new Store();
        IntVar[] cspVariables = new IntVar[allVars.size()];
        for (int i=0; i<cspVariables.length; i++) {
            cspVariables[i] = new IntVar(store, "v"+i, Collections.min(allVars.get(i).getDomain()), Collections.max(allVars.get(i).getDomain()));
        }
        for (SumConstraint s : allConstraints) {
            IntVar[] ls = new IntVar[s.getLeftSide().size()];
            for (int l=0; l<ls.length; l++) {
                ls[l] = cspVariables[allVars.indexOf(s.getLeftSide().get(l))];
            }
            IntVar[] rs = new IntVar[s.getRightSide().size()];
            for (int l=0; l<rs.length; l++) {
                rs[l] = cspVariables[allVars.indexOf(s.getRightSide().get(l))];
            }
            if (s.getSumValue() != null) {
                IntVar charValue = new IntVar(store, "c"+s.hashCode(), s.getSumValue(), s.getSumValue());
                store.impose(new Sum(ls, charValue));
                if (rs.length > 0)
                    store.impose(new Sum(rs, charValue));
            } else {
                assert (ls.length == 1 && rs.length > 0);
                store.impose(new Sum(rs, ls[0]));
            }
        }

        Search<IntVar> search = new DepthFirstSearch<IntVar>();
        SelectChoicePoint<IntVar> select = new InputOrderSelect<IntVar>(store, cspVariables, new IndomainMin<IntVar>());
        search.getSolutionListener().searchAll(true);
        search.getSolutionListener().recordSolutions(true);
        search.setSolutionListener(new PrintOutListener<IntVar>());
        boolean result = search.labeling(store, select);

        if ( result )
            System.out.println("Yes");
        else
            System.out.println("No");

    }


    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    static public void main(String[] args) {
        RPCommitmentGenerator test = new RPCommitmentGenerator(2,10,3,0);
//        HashSet<Characteristic> chars = new HashSet<Characteristic>();

        HashMap<Integer, Set<Characteristic>> chars = new HashMap<Integer, Set<Characteristic>>();

//        for (Path p : test.currentCommitment.keySet()) {
//            System.out.println(p);
//        }
        long commitments = 0;

        int[] charValues = new int[test.discretizations+1];

        while (test.hasNext()) {
//            System.out.println("****************************************");
            Map<Path, Integer> map = test.next();
            commitments++;
            if (commitments % 1e7 == 0) System.out.println("Commitments: " + commitments);
            Characteristic c = new Characteristic(test.startingNode, test.discretizations, test.DEPTH, test.nodes, map);
            int value = c.getValue();

            Set<Characteristic> ch = chars.get(value);
            if (ch == null) ch = new HashSet<Characteristic>();
            if (ch.add(c)) {
                charValues[c.getValue()]++;
            }
            chars.put(value, ch);

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

        for (int value = test.discretizations; value >= 0; value--) {
            if (chars.get(value) == null) continue;
            for (Characteristic c : chars.get(value)) {
                System.out.println("Characteristics: " + c);
                test.generateCSP(c);
                System.out.println("*****************");
            }
            break;
//            System.out.print("Testing set for reward " + reward + " ..... " );
//            Map<Characteristic,Set<Characteristic>> evidence = Characteristic.isClosed(chars.get(reward));
//            if (!evidence.isEmpty()) {
//                System.out.println("closed");
//
//                for (Map.Entry<Characteristic, Set<Characteristic>> e : evidence.entrySet()) {
//                    System.out.println(e);
//                }
//
//                break;
//            } else {
//                System.out.println("open");
//            }
        }


        System.out.println("Commitments : " + commitments);
        System.out.println("Characteristics : " + chars.size());
        System.out.println("Char Values : " + Arrays.toString(charValues));
    }
}
