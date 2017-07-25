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


package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.improvedBR;

import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;

import java.util.*;


public class BestMinmaxValueOracle extends SQFBestResponseAlgorithm {

    private BestMinmaxCoreLP coreLP;

    public BestMinmaxValueOracle(Expander expander, int searchingPlayerIndex, Player[] actingPlayers, SequenceFormConfig algConfig, GameInfo gameInfo, BestMinmaxCoreLP coreLP) {
        super(expander, searchingPlayerIndex, actingPlayers, algConfig, gameInfo);
        this.coreLP = coreLP;
    }

    @Override
    protected Double calculateEvaluation(Map<Player, Sequence> currentHistory, GameState gameState) {
        double utRes = 0;
        if (algConfig.getActualNonzeroUtilityValues(gameState) != null) {
            utRes = algConfig.getActualNonzeroUtilityValues(gameState);
        } else {
            utRes = gameState.getUtilities()[0] * gameState.getNatureProbability();
            if (utRes != 0) {
                algConfig.setUtility(gameState, utRes);
            }
        }
        if (searchingPlayerIndex == 1) {
            utRes *= -1; // a zero sum game
        }
        Double weight = getOpponentRealizationPlan().get(currentHistory.get(players[opponentPlayerIndex]));
        if (weight == null || weight == 0) {
            weight = 1d;
        }

        Double mm = coreLP.calculateMinmaxImprovement(currentHistory.get(players[searchingPlayerIndex]), (SequenceFormConfig)algConfig);
        if (mm == null) return utRes * weight; // weighting with opponent's realization plan
        else return mm;
    }



    protected Double bestResponse(GameState gameState, double lowerBound) {

        Map<Player, Sequence> currentHistory = new HashMap<Player, Sequence>();
        currentHistory.put(players[searchingPlayerIndex], gameState.getSequenceFor(players[searchingPlayerIndex]));
        currentHistory.put(players[opponentPlayerIndex], gameState.getSequenceFor(players[opponentPlayerIndex]));

        nodes++;
        Double returnValue = null;

        if (gameState.isGameEnd()) { // we are in a leaf
            return calculateEvaluation(currentHistory, gameState);
        }

        Double tmpVal = cachedValuesForNodes.get(gameState);
        if (tmpVal != null) { // we have already solved this node as a part of an evaluated information set
            //maybe we could remove the cached reward at this point? No in double-oracle -> we are using it in restricted game
            return tmpVal;
        }

        Player currentPlayer = gameState.getPlayerToMove();


        if (currentPlayer.equals(players[searchingPlayerIndex])) { // searching player to move
            List<GameState> alternativeNodes = new ArrayList<GameState>();

            boolean nonZeroOppRP = (getOpponentRealizationPlan().get(gameState.getHistory().getSequenceOf(players[opponentPlayerIndex])) != null && getOpponentRealizationPlan().get(gameState.getHistory().getSequenceOf(players[opponentPlayerIndex])) > 0);
            boolean nonZeroOppRPAlt = false;

            InformationSet currentIS = algConfig.getInformationSetFor(gameState);
            if (currentIS != null) {
                alternativeNodes.addAll(currentIS.getAllStates());
                if (!alternativeNodes.contains(gameState)) {
                    alternativeNodes.add(gameState);
                }
            } // if we do not have alternative nodes stored in the currentIS, there is no RP leading to these nodes --> we do not need to consider them
            else {
                alternativeNodes.add(gameState);
            }

            assert (alternativeNodes.contains(gameState));
            HashMap<GameState, Double> alternativeNodesProbs = new HashMap<GameState, Double>();

            double ISProbability = 0;

            for (GameState currentNode : alternativeNodes) {
                double currentNodeProb = currentNode.getNatureProbability();
                if (nonZeroOppRP) {
                    if (getOpponentRealizationPlan().containsKey(currentNode.getHistory().getSequenceOf(players[opponentPlayerIndex]))) {
                        double altProb = getOpponentRealizationPlan().get(currentNode.getHistory().getSequenceOf(players[opponentPlayerIndex]));
                        currentNodeProb *= altProb;
                        if (altProb > 0) {
                            nonZeroOppRPAlt = true;
                        }
                    } else {
                        currentNodeProb = 0;
                    }
                }
                ISProbability += currentNodeProb;
                alternativeNodesProbs.put(currentNode, currentNodeProb);
            }

            if (!nonZeroOppRP && !nonZeroOppRPAlt && ISProbability > gameState.getNatureProbability()) {
                // if there is zero OppRP prob we keep only those nodes in IS that are caused by the moves of nature
                // i.e., -> we keep all the nodes that share the same history of the opponent
                for (GameState state : new ArrayList<GameState>(alternativeNodes)) {
                    if (!state.getHistory().getSequenceOf(players[opponentPlayerIndex]).equals(gameState.getHistory().getSequenceOf(players[opponentPlayerIndex]))) {
                        alternativeNodes.remove(state);
                        alternativeNodesProbs.remove(state);
                    }
                }
            }

            BRSimpleMaxSelection sel = new BRSimpleMaxSelection(lowerBound);
            Collections.sort(alternativeNodes, comparator);

//            for (GameState currentNode : alternativeNodes) {
//                sel.setCurrentNode(currentNode);
                selectAction(alternativeNodes.get(0), sel, expander.getActions(gameState));
//                sel.abandonCurrentNode();
//                if (sel.allNodesProbability < EPS_CONSTANT) {
//                    break;
//                }
//                if ((sel.getResult().getRight() + sel.allNodesProbability * MAX_UTILITY_VALUE) < lowerBound) { //
//                    break;
//                }
//            }

            Action resultAction = sel.getResult().getLeft(); //selected action for the searching player
            returnValue = sel.getResult().getRight();

//            for (GameState currentNode : alternativeNodes) { // storing the results based on the action
//                if (sel.actionRealValues.get(currentNode) == null) {
//                    if (currentNode.equals(gameState)) {
//                        returnValue = -MAX_UTILITY_VALUE;
//                    }
//                    continue;
//                }
//                double v;
//                if (resultAction == null) {
//                    v = -MAX_UTILITY_VALUE;
//                } else {
//                    v = sel.actionRealValues.get(currentNode).get(resultAction);
//                }
//
//                cachedValuesForNodes.put(currentNode, v);
//                if (currentNode.equals(gameState)) {
//                    returnValue = v;
//                }
//            }

//            if (returnValue == null) {
//                System.out.println();
//            }
            assert (returnValue != null);

            Sequence resultSequence = new LinkedListSequenceImpl(currentHistory.get(players[searchingPlayerIndex]));
            resultSequence.addLast(resultAction);

            HashSet<Sequence> tmpBRSet = BRresult.get(currentHistory.get(players[searchingPlayerIndex]));
            if (tmpBRSet == null) {
                tmpBRSet = new HashSet<Sequence>();
            }

            tmpBRSet.add(resultSequence);
            BRresult.put(currentHistory.get(players[searchingPlayerIndex]), tmpBRSet);

        } else { // nature player or the opponent is to move
            double nodeProbability = gameState.getNatureProbability();
            boolean nonZeroORP = false;
            BRFirstActionSelection sel = new BRFirstActionSelection(lowerBound);
            selectAction(gameState, sel, expander.getActions(gameState));
            returnValue = sel.getResult().getRight();
        }

        assert (returnValue != null);
//        assert (returnValue <= MAX_UTILITY_VALUE*(1.5));
        return returnValue;
    }


    public class BRSimpleMaxSelection extends BRActionSelection {

        protected double max = Double.NEGATIVE_INFINITY;
        protected Action maxAction = null;

        public BRSimpleMaxSelection(double lowerBound) {
            super(lowerBound);
        }

        @Override
        public void addValue(Action action, double value, double natureProb, double orpProb) {
             if (value > max) {
                 max = value;
                 maxAction = action;
             }
        }

        @Override
        public Pair<Action, Double> getResult() {
            return new Pair<Action, Double>(maxAction, max);
        }

        @Override
        public List<Action> sortActions(GameState state, List<Action> actions) {
            return actions;
        }

        @Override
        public double calculateNewBoundForAction(Action action, double natureProb, double orpProb, GameState state) {
            return Double.NEGATIVE_INFINITY;
        }
    }

    public class BRFirstActionSelection extends BRSimpleMaxSelection {

        public BRFirstActionSelection(double lowerBound) {
            super(lowerBound);
        }

        @Override
        public double calculateNewBoundForAction(Action action, double natureProb, double orpProb, GameState state) {
            if (maxAction == null) return Double.NEGATIVE_INFINITY;
            else return Double.POSITIVE_INFINITY;
        }
    }

}
