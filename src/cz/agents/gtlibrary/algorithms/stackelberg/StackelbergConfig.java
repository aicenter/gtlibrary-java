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


package cz.agents.gtlibrary.algorithms.stackelberg;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
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
public class StackelbergConfig extends SequenceFormConfig<SequenceInformationSet> {

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

    public GameState getRootState() {
        return rootState;
    }

    public class PureRealizationPlanIterator implements Iterator<Set<Sequence>> {

        private boolean first = true;
        private Set<Sequence> currentSet;
        private List<Pair<InformationSet, List<Action>>> stack;
        private Player player;
        private Expander<SequenceInformationSet> expander;


        public PureRealizationPlanIterator(Player player, Expander expander) {
            this.currentSet = new HashSet<>();
            this.player = player;
            this.stack = new ArrayList<>();
            this.expander = expander;
            recursive(rootState);
        }

        private void recursive(GameState state) {
            ArrayDeque<GameState> queue = new ArrayDeque<>();
            Set<InformationSet> assignedIS = new HashSet<>();

            queue.add(state);
            while (queue.size() > 0) {
                GameState currentState = queue.removeFirst();

                if (currentState.isGameEnd()) {
                    currentSet.addAll(currentState.getSequenceFor(player).getAllPrefixes());
                    continue;
                }

                if (currentState.getPlayerToMove().equals(player)) {
                    InformationSet set = getInformationSetFor(currentState);

                    if (assignedIS.contains(set))
                        continue;
                    Pair<InformationSet, List<Action>> actionsInThisSet = new Pair<>(set, expander.getActions(currentState));
                    Action lastAction = actionsInThisSet.getRight().get(actionsInThisSet.getRight().size() - 1);

                    stack.add(actionsInThisSet);
                    addToQueue(queue, currentState.performAction(lastAction));
                    assignedIS.add(getInformationSetFor(currentState));
                } else {
                    for (Action action : expander.getActions(currentState)) {
                        addToQueue(queue, currentState.performAction(action));
                    }
                }
            }

        }

        private void addToQueue(ArrayDeque<GameState> queue, GameState state) {
            if (state.getPlayerToMove().equals(player))
                queue.addLast(state);
            else
                queue.addFirst(state);
        }

        @Override
        public boolean hasNext() {
            for (Pair<InformationSet, List<Action>> p : stack) {
                if (p.getRight().size() > 1)
                    return true;
            }
            return false;
        }

        @Override
        public Set<Sequence> next() {
            if (!hasNext())
                throw new NoSuchElementException();
            if (first) {
                first = false;
                return currentSet;
            }
            int index = stack.size() - 1;

            for (; index >= 0; index--) {
                Action lastAction = stack.get(index).getRight().remove(stack.get(index).getRight().size() - 1);
                Sequence sequence = new ArrayListSequenceImpl(stack.get(index).getLeft().getPlayersHistory());

                sequence.addLast(lastAction);
                currentSet.remove(sequence);
                if (!stack.get(index).getRight().isEmpty()) {
                    break;
                }

            }
            stack.subList(index + 1, stack.size()).clear();
            Pair<InformationSet, List<Action>> lastActions = stack.get(stack.size() - 1);

            for (GameState state : lastActions.getLeft().getAllStates()) {//mam zaručený že se takhle updatně i IS ve stejný vrstvě? podle mě ne
                recursive(state.performAction(lastActions.getRight().get(lastActions.getRight().size() - 1)));
            }
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
            for (int i = 0; i < existingUtility.length; i++) {
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
