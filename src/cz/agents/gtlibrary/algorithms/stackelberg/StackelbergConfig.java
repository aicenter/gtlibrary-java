package cz.agents.gtlibrary.algorithms.stackelberg;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.Pair;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 12/2/13
 * Time: 2:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class StackelbergConfig<I extends SequenceInformationSet> extends SequenceFormConfig<I> {

    protected Map<GameState, Double[]> actualNonZeroUtilityValuesInLeafs = new HashMap<GameState, Double[]>();
    protected Map<Map<Player, Sequence>, Double[]> utilityForSequenceCombination = new HashMap<Map<Player, Sequence>, Double[]>();



    private GameState rootState;

    public StackelbergConfig(GameState rootState) {
        super();
        this.rootState = rootState;
    }

    public PureRealizationPlanIterator getIterator(Player player, Expander<SequenceInformationSet> expander) {
        return new PureRealizationPlanIterator(player, expander);
    }

    public class PureRealizationPlanIterator implements Iterator<Set<Sequence>> {

        private boolean first = true;
        private Set<Sequence> currentSet;
        private List<Pair<GameState, List<Action>>> stack;
        private Player player;
        private Expander<SequenceInformationSet> expander;


        public PureRealizationPlanIterator(Player player, Expander expander) {
            this.currentSet = new HashSet<Sequence>();
            this.player = player;
            this.stack = new ArrayList<Pair<GameState, List<Action>>>();
            this.expander = expander;
            recursive(rootState);
        }

        private void recursive(GameState state) {
            LinkedList<GameState> queue = new LinkedList<GameState>();
            queue.add(state);
            Set<InformationSet> assignedIS = new HashSet<InformationSet>();

            while (queue.size() > 0) {
                GameState currentState = queue.removeFirst();

                if (currentState.isGameEnd()) {
                    currentSet.addAll(currentState.getHistory().getSequenceOf(player).getAllPrefixes());
                    continue;
                }

                if (currentState.getPlayerToMove().equals(player)) {
                    if (assignedIS.contains(getInformationSetFor(currentState)))
                        continue;
                    Pair<GameState, List<Action>> actionsInThisState = new Pair<GameState, List<Action>>(currentState, expander.getActions(currentState));
                    stack.add(actionsInThisState);
                    Action a = actionsInThisState.getRight().get(actionsInThisState.getRight().size() - 1);
                    queue.add(currentState.performAction(a));
                    assignedIS.add(getInformationSetFor(currentState));
                } else {
                    for (Action action : expander.getActions(currentState)) {
                        queue.add(currentState.performAction(action));
                    }
                }
            }
        }

        @Override
        public boolean hasNext() {
            for (Pair<GameState, List<Action>> p : stack) {
                if (p.getRight().size() > 1)
                    return true;
            }
            return false;
        }

        @Override
        public Set<Sequence> next() {
            if (!hasNext()) throw new NoSuchElementException();
            if (first) {
                first = false;
                return currentSet;
            }
            int index=stack.size()-1;
            for (; index>=0; index--) {
                Action lastAction = stack.get(index).getRight().remove(stack.get(index).getRight().size()-1);
                Sequence removingSequence = stack.get(index).getLeft().performAction(lastAction).getSequenceFor(player);
                currentSet.remove(removingSequence);
                if (!stack.get(index).getRight().isEmpty()) {
                    break;
                }
            }
            stack = stack.subList(0, index+1);
            Pair<GameState, List<Action>> lastActions = stack.get(stack.size()-1);
            GameState s = lastActions.getLeft().performAction(lastActions.getRight().get(lastActions.getRight().size()-1));
            recursive(s);
            return currentSet;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not implemented.");
        }
    }

    public void setUtility(GameState leaf) {
        final double[] utilities = leaf.getUtilities();
        Double[] u = new Double[utilities.length];
        for (Player p : leaf.getAllPlayers())
            u[p.getId()] = utilities[p.getId()] * leaf.getNatureProbability();
        setUtility(leaf, u);
    }

    public void setUtility(GameState leaf, Double[] utility) {
        if (actualNonZeroUtilityValuesInLeafs.containsKey(leaf)) {
            assert (actualNonZeroUtilityValuesInLeafs.get(leaf) == utility);
            return; // we have already stored this leaf
        }
        boolean allZeros = true;
        for (Player p : leaf.getAllPlayers()) {
            if (utility[p.getId()] != 0) {
                allZeros = false;
                break;
            }
        }

        if (allZeros) return; // we do not store zero-utility

        FixedSizeMap<Player, Sequence> activePlayerMap = createActivePlayerMap(leaf);
        Double[] existingUtility = utility;

        if (utilityForSequenceCombination.containsKey(activePlayerMap)) {
            Double[] storedUV = utilityForSequenceCombination.get(activePlayerMap);
            for (int i=0; i<existingUtility.length; i++) {
                existingUtility[i] += storedUV[i];
            }
        }

        actualNonZeroUtilityValuesInLeafs.put(leaf, utility);
        utilityForSequenceCombination.put(activePlayerMap, existingUtility);
    }

    public Double getActualNonzeroUtilityValues(GameState leaf, Player player) {
        return actualNonZeroUtilityValuesInLeafs.get(leaf)[player.getId()];
    }

    public Double getUtilityFor(Map<Player, Sequence> sequenceCombination, Player player) {
        if (!utilityForSequenceCombination.containsKey(sequenceCombination)) return null;
        return utilityForSequenceCombination.get(sequenceCombination)[player.getId()];
    }
}
