package cz.agents.gtlibrary.algorithms.flipit.bestresponse;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleBestResponse;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.domain.flipit.FlipItGameInfo;
import cz.agents.gtlibrary.domain.flipit.NodePointsFlipItGameState;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;

import java.util.*;

/**
 * Created by Jakub Cerny on 01/06/17.
 */
public class FlipItBestResponseAlgorithm extends DoubleOracleBestResponse {


    protected static final boolean useBoundInSearchingPlayerNodes = false;
    protected static boolean useCustomBounds = true;

    public FlipItBestResponseAlgorithm(Expander<DoubleOracleInformationSet> expander, int searchingPlayerIndex, Player[] actingPlayers, DoubleOracleConfig algConfig, GameInfo gameInfo) {
        super(expander, searchingPlayerIndex, actingPlayers, algConfig, gameInfo);
        this.useOriginalBRFormulation = false;
//        if (FlipItGameInfo.gameVersion == FlipItGameInfo.FlipItInfo.REVEALED_NODE_POINTS)
//            useCustomBounds = true;
    }


    @Override
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

            boolean nonZeroOppRP = (opponentRealizationPlan.get(gameState.getSequenceFor(players[opponentPlayerIndex])) != null && opponentRealizationPlan.get(gameState.getSequenceFor(players[opponentPlayerIndex])) > 0);
            boolean nonZeroOppRPAlt = false;

            InformationSet currentIS = algConfig.getInformationSetFor(gameState);
            if (currentIS != null) {
                alternativeNodes.addAll(currentIS.getAllStates());

                if (!alternativeNodes.contains(gameState)) {
                    alternativeNodes.add(gameState);
                }

                if (alternativeNodes.size() == 1 && ((!useOriginalBRFormulation || !nonZeroOppRP) && (useOriginalBRFormulation || nonZeroOppRP))) {
                    alternativeNodes.addAll(getAlternativeNodesOutsideRG(gameState));
                }
            } // if we do not have alternative nodes stored in the currentIS, there is no RP leading to these nodes --> we do not need to consider them
            else {
                alternativeNodes.addAll(getAlternativeNodesOutsideRG(gameState));
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
//                if (currentNode.equals(gameState)) {
//                    System.out.println("contains " + currentNodeProb + " " + currentNode.hashCode() + " " + gameState.hashCode());
//                }
//                System.out.println(alternativeNodesProbs.get(gameState));
            }

            if (!useOriginalBRFormulation) {
//                System.out.println(alternativeNodes.contains(gameState));
//                System.out.println(alternativeNodesProbs.containsKey(gameState));
                Double stateProb = alternativeNodesProbs.get(gameState);
                if (stateProb == null){
                    System.err.println("NULL AlternativeNodes state : " + gameState);
                    stateProb = 1.0d;
                }
                if (useCustomBounds && lowerBound > stateProb * ((NodePointsFlipItGameState) gameState).getUpperBoundForUtilityFor(searchingPlayerIndex))
                    return Double.NEGATIVE_INFINITY;
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
            new ArrayList<GameState>(alternativeNodes).stream().filter(state -> !state.getSequenceForPlayerToMove().equals(gameState.getSequenceForPlayerToMove())).forEach(state -> {
                alternativeNodes.remove(state);
                alternativeNodesProbs.remove(state);
            });

            BRSrchSelection sel = new BRSrchFlipItSelection(lowerBound, ISProbability, alternativeNodesProbs, nonZeroOppRP);
            Collections.sort(alternativeNodes, comparator);

//            if (!useOriginalBRFormulation) {
//                if (lowerBound > ISProbability * ((NodePointsFlipItGameState) gameState).getUpperBoundForUtilityFor(searchingPlayerIndex))
//                    return Double.NEGATIVE_INFINITY;
//            }

            List<Action> actionsToExplore = expander.getActions(gameState);
            actionsToExplore = sel.sortActions(gameState, actionsToExplore);

            for (GameState currentNode : alternativeNodes) {
                sel.setCurrentNode(currentNode);
                selectAction(currentNode, sel, actionsToExplore);
                sel.abandonCurrentNode();
                if (sel.allNodesProbability < EPS_CONSTANT) {
                    break;
                }
//                if ((sel.getResult().getRight() + sel.allNodesProbability * MAX_UTILITY_VALUE) < lowerBound) { //
//                    break;
//                }
                if (currentNode.equals(gameState)) {
                    if (Collections.max(sel.actionRealValues.get(currentNode).values()) < lowerBound)
                        break;
                }
            }

            Action resultAction = sel.getResult().getLeft(); //selected action for the searching player

            for (GameState currentNode : alternativeNodes) { // storing the results based on the action
                if (sel.actionRealValues.get(currentNode) == null) {
                    if (currentNode.equals(gameState)) {
//                        returnValue = -MAX_UTILITY_VALUE*alternativeNodesProbs.get(currentNode);
                        returnValue = Double.NEGATIVE_INFINITY;
                    }
                    continue;
                }
                double v;
                if (resultAction == null) {
//                    v = -MAX_UTILITY_VALUE*alternativeNodesProbs.get(currentNode);
                    v = Double.NEGATIVE_INFINITY;
                } else {
                    v = sel.actionRealValues.get(currentNode).get(resultAction);
                }

                cachedValuesForNodes.put(currentNode, v);
                if (currentNode.equals(gameState)) {
                    returnValue = v;
                }
            }

//            if (returnValue == null) {
//                System.out.println();
//            }
            assert (returnValue != null);

            Sequence resultSequence = new ArrayListSequenceImpl(currentHistory.get(players[searchingPlayerIndex]));
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
            Double currentOppRealizationPlan = getOpponentRealizationPlan().get(currentHistory.get(players[opponentPlayerIndex]));
            if (currentOppRealizationPlan != null && currentOppRealizationPlan > 0) {
                nodeProbability *= currentOppRealizationPlan;
                nonZeroORP = true;

                if (!useOriginalBRFormulation) {
                    if (useCustomBounds && lowerBound > nodeProbability * ((NodePointsFlipItGameState) gameState).getUpperBoundForUtilityFor(searchingPlayerIndex))
                        return Double.NEGATIVE_INFINITY;
                }

            }
            if (algConfig.getActualNonzeroUtilityValues(gameState) != null) {
                returnValue = algConfig.getActualNonzeroUtilityValues(gameState);
                if (nonZeroORP) {
                    returnValue *= currentOppRealizationPlan;
                }
                if (searchingPlayerIndex != 0) {
                    returnValue *= -1;
                }
            } else {
                BROppSelection sel = new BROppFlipItSelection(lowerBound, nodeProbability, nonZeroORP);
                List<Action> actionsToExplore = expander.getActions(gameState);
                actionsToExplore = sel.sortActions(gameState, actionsToExplore);
                selectAction(gameState, sel, actionsToExplore);
                returnValue = sel.getResult().getRight();
                if (nonZeroORP && !sel.nonZeroContinuation) {
                    returnValue *= currentOppRealizationPlan;
                }
            }
        }

        assert (returnValue != null);
        assert (returnValue <= MAX_UTILITY_VALUE * (1.01));
        return returnValue;
    }

    @Override
    public void selectAction(GameState state, BRActionSelection selection, List<Action> actionsToExplore) {
//        List<Action> actionsToExplore = expander.getActions(state);
//        actionsToExplore = selection.sortActions(state, actionsToExplore);

        Double rP = getOpponentRealizationPlan().get(state.getHistory().getSequenceOf(players[opponentPlayerIndex]));
        if (rP == null) {
            rP = 0d;
        }

        double stateProb = rP * state.getNatureProbability();

        for (Action act : actionsToExplore) {
            Action action = act;

            GameState newState = state.performAction(action);

            double natureProb = newState.getNatureProbability(); // TODO extract these probabilities from selection Map
            Double oppRP = getOpponentRealizationPlan().get(newState.getHistory().getSequenceOf(players[opponentPlayerIndex]));
            if (oppRP == null) {
                oppRP = 0d;
            }

            if (state.getPlayerToMove().getId() != searchingPlayerIndex) stateProb = natureProb * oppRP;

            double newLowerBound = selection.calculateNewBoundForAction(action, natureProb, oppRP, state);
            double upperBound;
            double bound = ((NodePointsFlipItGameState)state).getUpperBoundForUtilityFor(searchingPlayerIndex);
            if (bound < 0.0) bound = 0.0;
            if (useCustomBounds && !useOriginalBRFormulation)// && state.getPlayerToMove().getId() != searchingPlayerIndex)
                upperBound = Math.min(MAX_UTILITY_VALUE, bound);
            else
                upperBound = MAX_UTILITY_VALUE;


//            if (newLowerBound > stateProb * upperBound && newLowerBound < 1000 * MAX_UTILITY_VALUE) {
//                System.err.println(newLowerBound + " " +stateProb*upperBound + " " + upperBound + " " + MAX_UTILITY_VALUE );
////                System.exit(0);
//            }

            // nasobeni stateProb tady nebyvalo

            if (newLowerBound <= stateProb * MAX_UTILITY_VALUE && newLowerBound > stateProb * upperBound){

//                System.out.println(newLowerBound + " " + stateProb + " " + upperBound + " " + stateProb * upperBound);
//                System.out.println(newLowerBound + " " + stateProb + " " + upperBound + " " + stateProb * upperBound);
            }

            if (newLowerBound <= stateProb * upperBound) {
                double value = bestResponse(newState, newLowerBound);
                selection.addValue(action, value, natureProb, oppRP);
            }
        }
    }

    public class BROppFlipItSelection extends BROppSelection {

        public BROppFlipItSelection(double lowerBound, double nodeProbability, boolean nonZeroORP) {
            super(lowerBound, nodeProbability, nonZeroORP);
        }

        @Override
        public double calculateNewBoundForAction(Action action, double natureProb, double orpProb, GameState state) {
            double probability = natureProb;
            if (nonZeroORP) {
                probability *= orpProb;
                if (orpProb == 0) {
                    if (tempValue == null) {
                        return -MAX_UTILITY_VALUE;
                    } else {
                        return Double.POSITIVE_INFINITY;
                    }
                }
            }
            if (nodeProbability < EPS_CONSTANT) {
                if (tempValue == null) {
                    return -MAX_UTILITY_VALUE;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
            if (useCustomBounds && !useOriginalBRFormulation) {
                return (lowerBound - (value + (nodeProbability - probability) * ((NodePointsFlipItGameState) state).getUpperBoundForUtilityFor(searchingPlayerIndex)));
            }
            return (lowerBound - (value + (nodeProbability - probability) * MAX_UTILITY_VALUE));
        }
    }

    public class BRSrchFlipItSelection extends BRSrchSelection {
        public BRSrchFlipItSelection(double lowerBound, double allNodesProbability, HashMap<GameState, Double> alternativeNodesProbs, boolean nonZeroORP) {
            super(lowerBound, allNodesProbability, alternativeNodesProbs, nonZeroORP);
        }

        @Override
        public double calculateNewBoundForAction(Action action, double natureProb, double orpProb, GameState state) {
            if (nonZeroORP && orpProb <= 0) {
                return Double.POSITIVE_INFINITY;
            }
            if (previousMaxValue == Double.NEGATIVE_INFINITY) {
                return Double.NEGATIVE_INFINITY;
            } else {
                if (this.allNodesProbability < EPS_CONSTANT) {
                    return Double.POSITIVE_INFINITY;
                } else {
//					double probability = natureProb;
//					if (nonZeroORP) probability *= orpProb;

                    if (useCustomBounds && !useOriginalBRFormulation && useBoundInSearchingPlayerNodes) {
                        return ((previousMaxValue + this.allNodesProbability * (((NodePointsFlipItGameState) state).getLowerBoundForUtilityFor(searchingPlayerIndex)))
                                - (actionExpectedValues.get(action) + (this.allNodesProbability - alternativeNodesProbs.get(currentNode)) * ((NodePointsFlipItGameState) state).getUpperBoundForUtilityFor(searchingPlayerIndex)));
                    } else {
                        return ((previousMaxValue + this.allNodesProbability * (-MAX_UTILITY_VALUE))
                                - (actionExpectedValues.get(action) + (this.allNodesProbability - alternativeNodesProbs.get(currentNode)) * MAX_UTILITY_VALUE));
                    }
                }
            }
        }
    }
}
