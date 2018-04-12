package cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.rpiterator;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.FeasibilitySequenceFormLP;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;

import java.util.*;

public abstract class PureRealPlanIterator implements Iterator<Set<Sequence>> {

    protected GameState rootState;
    protected StackelbergConfig config;
    protected Set<Sequence> currentSet;
    protected List<Pair<SequenceInformationSet, List<Action>>> stack;
    protected Player follower;
    protected Expander<SequenceInformationSet> expander;
    protected FeasibilitySequenceFormLP solver;
    protected boolean first = true;
    protected double leaderUpperBound;
    protected double bestValue;

//    protected Set<GameState> currentlyReachableLeafs;
    protected PriorityQueue<GameState> currentlyReachableLeafs;
    protected Map<InformationSet, Map<Sequence, List<GameState>>> leafAccessibility;
    protected Map<InformationSet, List<Action>> actions;
    protected Map<Action, Double> maxFollowerValues;
    protected Map<Action, Double> maxLeaderValues;

    public PureRealPlanIterator(final Player follower, StackelbergConfig config, Expander<SequenceInformationSet> expander, FeasibilitySequenceFormLP solver) {
        this.currentSet = new HashSet<>();
        this.follower = follower;
        this.stack = new ArrayList<>();
        this.expander = expander;
        this.solver = solver;
        this.config = config;
        this.rootState = config.getRootState();
        actions = new HashMap<>();
        maxFollowerValues = new HashMap<>();
        maxLeaderValues = new HashMap<>();
        leafAccessibility = new HashMap<>();
//        currentlyReachableLeafs = new HashSet<>();
        currentlyReachableLeafs = new PriorityQueue<>(100, new Comparator<GameState>() {
            @Override
            public int compare(GameState o1, GameState o2) {
                return Double.compare(o2.getUtilities()[1 - follower.getId()], o1.getUtilities()[1 - follower.getId()]);
            }
        });
        leaderUpperBound = Double.NEGATIVE_INFINITY;
        bestValue = Double.NEGATIVE_INFINITY;
        initLeafAccessibility(rootState);
        initActions(rootState);
        sortActions();
        initStack();
        initRealizationPlan();
    }

    private List<GameState> initLeafAccessibility(GameState rootState) {
        if (rootState.isGameEnd())
            return getListForLeaf(rootState);
        List<GameState> allLeafs = new ArrayList<>();

        for (Action action : expander.getActions(rootState)) {
            GameState nextState = rootState.performAction(action);
            List<GameState> leafs = initLeafAccessibility(nextState);

            allLeafs.addAll(leafs);
            if (rootState.getPlayerToMove().equals(follower))
                updateLeafAccessibility(rootState, nextState, leafs);
        }
        return allLeafs;
    }

    private void updateLeafAccessibility(GameState rootState, GameState nextState, List<GameState> leafs) {
        InformationSet set = config.getInformationSetFor(rootState);
        Map<Sequence, List<GameState>> sequenceLeafs = getSequenceContinuationMap(set);
        List<GameState> continuationLeafs = sequenceLeafs.get(nextState.getSequenceFor(rootState.getPlayerToMove()));

        if (continuationLeafs == null) {
            continuationLeafs = new ArrayList<>();
            sequenceLeafs.put(nextState.getSequenceFor(rootState.getPlayerToMove()), continuationLeafs);
        }
        continuationLeafs.addAll(leafs);
    }

    private Map<Sequence, List<GameState>> getSequenceContinuationMap(InformationSet set) {
        Map<Sequence, List<GameState>> sequenceLeafs = leafAccessibility.get(set);

        if (sequenceLeafs == null) {
            sequenceLeafs = new HashMap<>();
            leafAccessibility.put(set, sequenceLeafs);
        }
        return sequenceLeafs;
    }

    private List<GameState> getListForLeaf(GameState rootState) {
        List<GameState> utilityList = new ArrayList<>();

        utilityList.add(rootState);
        return utilityList;
    }

    protected void sortActions() {
        for (List<Action> actionList : actions.values()) {
            Collections.sort(actionList, new Comparator<Action>() {
                @Override
                public int compare(Action o1, Action o2) {
                    return Double.compare(maxLeaderValues.get(o1), maxLeaderValues.get(o2));
                }
            });
        }
    }

    private double[] initActions(GameState state) {
        if (state.isGameEnd())
            return state.getUtilities();

        List<Action> currentActions = getActions(state);
        double followerMaxValue = Double.NEGATIVE_INFINITY;
        double leaderMaxValue = Double.NEGATIVE_INFINITY;

        for (Action currentAction : currentActions) {
            double[] currentValues = initActions(state.performAction(currentAction));

            followerMaxValue = update(state, followerMaxValue, currentAction, currentValues[follower.getId()], maxFollowerValues);
            leaderMaxValue = update(state, leaderMaxValue, currentAction, currentValues[1 - follower.getId()], maxLeaderValues);
        }
        return getArray(followerMaxValue, leaderMaxValue);
    }

    private double update(GameState state, double maxValue, Action currentAction, double value, Map<Action, Double> maxActionValues) {
        Double oldValue = maxActionValues.get(currentAction);

        if (state.getPlayerToMove().equals(follower)) {
            if (oldValue == null)
                maxActionValues.put(currentAction, value);
            else
                maxActionValues.put(currentAction, Math.max(value, oldValue));
        }
        if (maxValue < value)
            return value;
        return maxValue;
    }

    private double[] getArray(double followerValue, double leaderValue) {
        double[] array = new double[2];

        array[follower.getId()] = followerValue;
        array[1 - follower.getId()] = leaderValue;
        return array;
    }

    private List<Action> getActions(GameState state) {
        if (!follower.equals(state.getPlayerToMove()))
            return expander.getActions(state);
        InformationSet set = config.getInformationSetFor(state);
        List<Action> currentActions = actions.get(set);

        if (currentActions == null) {
            currentActions = expander.getActions(state);
            actions.put(set, currentActions);
        }
        return currentActions;
    }

    protected void initRealizationPlan() {
        currentSet.add(rootState.getSequenceFor(follower));
        solver.removeSlackFor(rootState.getSequenceFor(follower));
        updateRealizationPlan(0);
    }

    protected void initStack() {
        Deque<GameState> queue = new ArrayDeque<>();
        Set<InformationSet> assignedIS = new HashSet<>();

        queue.add(rootState);
        while (queue.size() > 0) {
            GameState currentState = queue.poll();

            if (currentState.isGameEnd())
                continue;
            if (currentState.getPlayerToMove().equals(follower)) {
                SequenceInformationSet set = config.getInformationSetFor(currentState);
                Pair<SequenceInformationSet, List<Action>> setActionPair = new Pair<SequenceInformationSet, List<Action>>(set, new ArrayList<>(actions.get(set)));

                if (!assignedIS.contains(set))
                    stack.add(setActionPair);
                assignedIS.add(set);
                for (Action action : setActionPair.getRight()) {
                    addToQueue(queue, currentState.performAction(action));
                }
            } else {
                for (Action action : expander.getActions(currentState)) {
                    addToQueue(queue, currentState.performAction(action));
                }
            }
        }
    }

    protected abstract void addToQueue(Deque<GameState> queue, GameState state);

    @Override
    public boolean hasNext() {
        for (Pair<SequenceInformationSet, List<Action>> p : stack) {
            if (p.getRight().size() > 1)
                return true;
        }
        return false;
    }

    @Override
    public Set<Sequence> next() {
        if (first) {
            first = false;
            return new HashSet<>(currentSet);
        }
        int index = getIndexOfReachableISWithActionsLeftFrom(stack.size() - 1);

        updateRealizationPlan(index);
        return new HashSet<>(currentSet);
    }

    protected void updateRealizationPlan(int index) {
        for (int i = index; i < stack.size(); i++) {
            Pair<SequenceInformationSet, List<Action>> setActionPair = stack.get(i);

            if (currentSet.contains(setActionPair.getLeft().getPlayersHistory())) {
                Sequence continuation = new ArrayListSequenceImpl(setActionPair.getLeft().getPlayersHistory());
                Action lastAction = setActionPair.getRight().get(setActionPair.getRight().size() - 1);

                continuation.addLast(lastAction);
                currentSet.add(continuation);
                updateReachableLeafsAfterAddition(setActionPair.getLeft(), continuation);
                solver.removeSlackFor(continuation);
                if (leaderUpperBound <= bestValue + 1e-8 || (StackelbergConfig.USE_FEASIBILITY_CUT && !solver.checkFeasibilityFor(currentSet))) {
                    i = getIndexOfReachableISWithActionsLeftFrom(i) - 1;
                    assert !currentlyReachableLeafs.isEmpty();
                }
            }
        }
    }

    private void updateReachableLeafsAfterAddition(InformationSet set, Sequence continuation) {
        Map<Sequence, List<GameState>> sequenceLeafs = leafAccessibility.get(set);

        for (List<GameState> gameStates : sequenceLeafs.values()) {
            currentlyReachableLeafs.removeAll(gameStates);
        }
        currentlyReachableLeafs.addAll(sequenceLeafs.get(continuation));
        leaderUpperBound = getLeaderUpperBound();
    }

    protected int getIndexOfReachableISWithActionsLeftFrom(int index) {
        for (; index >= 0; index--) {
            SequenceInformationSet set = stack.get(index).getLeft();
            List<Action> actions = stack.get(index).getRight();

            if (currentSet.contains(set.getPlayersHistory())) {
                Action lastAction = actions.remove(actions.size() - 1);
                Sequence sequence = new ArrayListSequenceImpl(set.getPlayersHistory());

                sequence.addLast(lastAction);
                currentSet.remove(sequence);
                updateReachableLeafsAfterRemoval(set, actions, sequence);
                solver.addSlackFor(sequence);
                if (!actions.isEmpty()) {
//                    if (minIndex >= index) {
//                        minIndex = index;
//                            System.out.println(index);
//                            System.out.println(set.getPlayersHistory());
//                            System.out.println("Actions left: " + actions.size());
//                        if (minIndex == 0) {
//                            minIndex = Integer.MAX_VALUE;
//                        }
//                    }
                    break;
                }
                if (index == 0)
                    throw new NoSuchElementException();
            }
            stack.set(index, new Pair<SequenceInformationSet, List<Action>>(set, new ArrayList<>(this.actions.get(set))));
        }
        return index;
    }

    private void updateReachableLeafsAfterRemoval(SequenceInformationSet set, List<Action> actions, Sequence sequence) {
        if(actions.isEmpty())
            updateLeafsFor(set, this.actions.get(set), sequence);
        else
            updateLeafsFor(set, actions, sequence);
        leaderUpperBound = getLeaderUpperBound();
    }

    private void updateLeafsFor(SequenceInformationSet set, List<Action> actions, Sequence sequence) {
        Map<Sequence, List<GameState>> reachableLeafs = leafAccessibility.get(set);

        currentlyReachableLeafs.removeAll(reachableLeafs.get(sequence));
        for (Action action : actions) {
            Sequence continuation = new ArrayListSequenceImpl(set.getPlayersHistory());

            continuation.addLast(action);
            currentlyReachableLeafs.addAll(reachableLeafs.get(continuation));
        }
    }

    public void setBestValue(double bestValue) {
        this.bestValue = bestValue;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public double getLeaderUpperBound() {
        if(currentlyReachableLeafs.isEmpty())
            return Double.NEGATIVE_INFINITY;
        return currentlyReachableLeafs.peek().getUtilities()[1 - follower.getId()];
//        double upperBound = Double.NEGATIVE_INFINITY;
//
//        for (GameState leaf : currentlyReachableLeafs) {
//            double[] utilities = leaf.getUtilities();
//
//            if (utilities[1 - follower.getId()] > upperBound)
//                upperBound = utilities[1 - follower.getId()];
//        }
//        return upperBound;
    }

    public double getCurrentUpperBound() {
        return leaderUpperBound;
    }
}
