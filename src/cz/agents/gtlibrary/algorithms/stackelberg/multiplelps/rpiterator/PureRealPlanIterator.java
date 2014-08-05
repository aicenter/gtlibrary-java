package cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.rpiterator;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.FeasibilitySequenceFormLP;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;

import java.util.*;

public abstract class PureRealPlanIterator implements Iterator<Set<Sequence>> {

    protected boolean first = true;
    protected GameState rootState;
    protected StackelbergConfig config;
    protected Set<Sequence> currentSet;
    protected List<Pair<SequenceInformationSet, List<Action>>> stack;
    protected Player player;
    protected Expander<SequenceInformationSet> expander;
    protected FeasibilitySequenceFormLP solver;

    private Map<InformationSet, List<Action>> actions;
    private Map<Action, Double> maxActionValues;
//    protected int minIndex = Integer.MAX_VALUE;

    public PureRealPlanIterator(Player player, StackelbergConfig config, Expander<SequenceInformationSet> expander, FeasibilitySequenceFormLP solver) {
        this.currentSet = new HashSet<>();
        this.player = player;
        this.stack = new ArrayList<>();
        this.expander = expander;
        this.solver = solver;
        this.config = config;
        this.rootState = config.getRootState();
        actions = new HashMap<>();
        maxActionValues = new HashMap<>();
        initActions(rootState);
//        sortActions();
        initStack();
        initRealizationPlan();
    }

    protected void sortActions() {
        for (List<Action> actionList : actions.values()) {
            Collections.sort(actionList, new Comparator<Action>() {
                @Override
                public int compare(Action o1, Action o2) {
                    return Double.compare(maxActionValues.get(o2), maxActionValues.get(o1));
                }
            });
        }
    }

    private double initActions(GameState state) {
        if (state.isGameEnd())
            return state.getUtilities()[player.getId()];

        List<Action> currentActions = getActions(state);
        double maxValue = Double.NEGATIVE_INFINITY;

        for (Action currentAction : currentActions) {
            double currentValue = initActions(state.performAction(currentAction));
            Double oldValue = maxActionValues.get(currentAction);

            if (oldValue == null)
                maxActionValues.put(currentAction, currentValue);
            else
                maxActionValues.put(currentAction, Math.max(currentValue, oldValue));
            if(maxValue < currentValue)
                maxValue = currentValue;
        }
        return maxValue;
    }

    private List<Action> getActions(GameState state) {
        if (!player.equals(state.getPlayerToMove()))
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
        currentSet.add(rootState.getSequenceFor(player));
        solver.removeSlackFor(rootState.getSequenceFor(player));
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
            if (currentState.getPlayerToMove().equals(player)) {
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
            return currentSet;
        }
        int index = getIndexOfReachableISWithActionsLeftFrom(stack.size() - 1);

        updateRealizationPlan(index);
        return currentSet;
    }

    protected void updateRealizationPlan(int index) {
        for (int i = index; i < stack.size(); i++) {
            Pair<SequenceInformationSet, List<Action>> setActionPair = stack.get(i);

            if (currentSet.contains(setActionPair.getLeft().getPlayersHistory())) {
                Sequence continuation = new ArrayListSequenceImpl(setActionPair.getLeft().getPlayersHistory());

                continuation.addLast(setActionPair.getRight().get(setActionPair.getRight().size() - 1));
                currentSet.add(continuation);
                solver.removeSlackFor(continuation);
                if (StackelbergConfig.USE_FEASIBILITY_CUT && !solver.checkFeasibilityFor(currentSet)) {
                    i = getIndexOfReachableISWithActionsLeftFrom(i) - 1;
                }
            }
        }
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

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not implemented.");
    }
}
