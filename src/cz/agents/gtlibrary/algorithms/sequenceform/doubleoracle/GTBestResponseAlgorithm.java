package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public class GTBestResponseAlgorithm {
	
	public long nodes = 0;
	
	protected Expander<SequenceInformationSet> expander;
	
    protected Map<GameState, Double> cachedValuesForNodes = new HashMap<GameState, Double>();
	protected Map<Sequence, Double> opponentRealizationPlan = new HashMap<Sequence, Double>();

	protected HashMap<Sequence, HashSet<Sequence>> BRresult = new HashMap<Sequence, HashSet<Sequence>>();
	protected HashSet<Sequence> bestResponseSequences = new HashSet<Sequence>();
	
	final protected int searchingPlayerIndex;
	final protected int opponentPlayerIndex;
	final protected Player[] players;
	final protected SequenceFormConfig algConfig;
	final protected GameInfo gameInfo;
	
	private double MAX_UTILITY_VALUE;
	final private double EPS_CONSTANT = 0.00001; // zero for numerical-stability reasons 

	protected ORComparator comparator;
	
	
	protected GameState gameTreeRoot = null;

	public GTBestResponseAlgorithm(Expander<SequenceInformationSet> expander, int searchingPlayerIndex, Player[] actingPlayers, SequenceFormConfig algConfig, GameInfo gameInfo) {
		this.searchingPlayerIndex = searchingPlayerIndex;
		this.opponentPlayerIndex = (1 + searchingPlayerIndex) % 2;
		this.players = actingPlayers;	
		assert players.length == 2;		
		this.expander = expander;
		this.algConfig = algConfig;
		this.gameInfo = gameInfo;
		this.MAX_UTILITY_VALUE = gameInfo.getMaxUtility();
	}
	
	public Double calculateBR(GameState root, Map<Sequence, Double> opponentRealizationPlan) {
		
		nodes = 0;
		
		Map<Player, Sequence> currentHistory = new HashMap<Player, Sequence>();
		currentHistory.put(players[searchingPlayerIndex], new LinkedListSequenceImpl(players[searchingPlayerIndex]));
		currentHistory.put(players[opponentPlayerIndex], new LinkedListSequenceImpl(players[opponentPlayerIndex]));
		
		this.opponentRealizationPlan = opponentRealizationPlan;
		this.BRresult.clear();
		this.bestResponseSequences.clear();
		this.cachedValuesForNodes.clear();
		this.gameTreeRoot = root;
		
		comparator = new ORComparator(opponentRealizationPlan);
		
		return bestResponse(root, -MAX_UTILITY_VALUE);
	}
	
	protected Double bestResponse(GameState gameState, double lowerBound) {
		
		Map<Player, Sequence> currentHistory = new HashMap<Player, Sequence>();
		currentHistory.put(players[searchingPlayerIndex], gameState.getSequenceFor(players[searchingPlayerIndex]));
		currentHistory.put(players[opponentPlayerIndex], gameState.getSequenceFor(players[opponentPlayerIndex]));
		
		nodes++;
		Double returnValue = null;  
		
		if (gameState.isGameEnd()){ // we are in a leaf
			double utRes = 0;
			if (algConfig.getActualNonzeroUtilityValues(gameState) != null) utRes = algConfig.getActualNonzeroUtilityValues(gameState);
			else {
				utRes = gameState.getUtilities()[0] * gameState.getNatureProbability();
				if (utRes != 0) algConfig.setUtility(gameState);
			}			  			
			if (searchingPlayerIndex == 1) utRes *= -1; // a zero sum game
			Double weight = opponentRealizationPlan.get(currentHistory.get(players[opponentPlayerIndex]));
			if (weight == null || weight == 0) weight = 1d;			
			return utRes * weight; // weighting with opponent's realization plan
		}

		Double tmpVal = cachedValuesForNodes.get(gameState);
		if (tmpVal != null) { // we have already solved this node as a part of an evaluated information set
			//TODO maybe we could remove the cached value at this point?
			return tmpVal;
		} 

		Player currentPlayer = gameState.getPlayerToMove();

		
		if (currentPlayer.equals(players[searchingPlayerIndex])) { // searching player to move
			List<GameState> alternativeNodes = new ArrayList<GameState>();
			
			boolean nonZeroOppRP = (opponentRealizationPlan.get(gameState.getHistory().getSequenceOf(players[opponentPlayerIndex])) != null && opponentRealizationPlan.get(gameState.getHistory().getSequenceOf(players[opponentPlayerIndex])) > 0); 

			InformationSet currentIS = algConfig.getInformationSetFor(gameState); 				
			if (currentIS != null) {
				alternativeNodes.addAll(currentIS.getAllStates());
			} // if we do not have alternative nodes stored in the currentIS, there is no RP leading to these nodes --> we do not need to consider them
			else {
				alternativeNodes.add(gameState);
			}
				
			assert (alternativeNodes.contains(gameState));
			HashMap<GameState,Double> alternativeNodesProbs = new HashMap<GameState, Double>();

			double ISProbability = 0;
			for (GameState currentNode : alternativeNodes) {
				double currentNodeProb = currentNode.getNatureProbability();
				if (nonZeroOppRP && opponentRealizationPlan.containsKey(currentNode.getHistory().getSequenceOf(players[opponentPlayerIndex]))) { 
					currentNodeProb *= opponentRealizationPlan.get(currentNode.getHistory().getSequenceOf(players[opponentPlayerIndex]));					
				}
				ISProbability += currentNodeProb;
				alternativeNodesProbs.put(currentNode, currentNodeProb);
			}
				
			BRSrchSelection	sel = new BRSrchSelection(lowerBound, ISProbability, alternativeNodesProbs, nonZeroOppRP);
			Collections.sort(alternativeNodes, comparator);
				
			for (GameState currentNode : alternativeNodes) {
				sel.setCurrentNode(currentNode);
				selectAction(currentNode, sel, lowerBound);
				sel.abandonCurrentNode();
				if (sel.allNodesProbability < EPS_CONSTANT) break;
				//TODO add pruning with lower bound
			}				

			ActionImpl resultAction = sel.getResult().getLeft(); //selected action for the searching player
				
			for (GameState currentNode : alternativeNodes) { // storing the results based on the action
				if (sel.actionRealValues.get(currentNode) == null) {
					continue;
				}
				double v = sel.actionRealValues.get(currentNode).get(resultAction);
				cachedValuesForNodes.put(currentNode, v);
				if (currentNode.equals(gameState)) returnValue = v;
			}
			
			assert (returnValue != null);
			
			Sequence resultSequence = new LinkedListSequenceImpl(currentHistory.get(players[searchingPlayerIndex]));
			resultSequence.addLast(resultAction);
			
			HashSet<Sequence> tmpBRSet = BRresult.get(currentHistory.get(players[searchingPlayerIndex]));
			if (tmpBRSet == null) tmpBRSet = new HashSet<Sequence>();
			
			tmpBRSet.add(resultSequence);
			BRresult.put(currentHistory.get(players[searchingPlayerIndex]), tmpBRSet);
			
		} else { // nature player or the opponent is to move
			double nodeProbability = gameState.getNatureProbability();
			boolean nonZeroORP = false;
			Double currentOppRealizationPlan = opponentRealizationPlan.get(currentHistory.get(players[opponentPlayerIndex]));
			if (currentOppRealizationPlan != null && currentOppRealizationPlan > 0) {
				nodeProbability *= currentOppRealizationPlan;
				nonZeroORP = true;
			}
			BROppSelection sel = new BROppSelection(lowerBound, nodeProbability, nonZeroORP);
			selectAction(gameState, sel, lowerBound);
			returnValue = sel.getResult().getRight();
		} 
			
		assert (returnValue != null);
		assert (returnValue <= MAX_UTILITY_VALUE*(1+EPS_CONSTANT));
		return returnValue;
	}
	
	public void selectAction(GameState state, BRActionSelection selection, double lowerBound) {
		boolean changed = false;
		List<Action> actionsToExplore = expander.getActions(state);
		
		for (Action act : actionsToExplore) {
			ActionImpl action = (ActionImpl)act;
		
			GameState newState = (GameState)state.performAction(action);
			
			double natureProb = newState.getNatureProbability();
			Double oppRP = opponentRealizationPlan.get(newState.getHistory().getSequenceOf(players[opponentPlayerIndex]));
			if (oppRP == null) oppRP = 0d;
			 
			double newLowerBound = selection.calculateNewBoundForAction(action, natureProb, oppRP);
			if (newLowerBound <= MAX_UTILITY_VALUE) {
				double value = bestResponse(newState, newLowerBound);
				selection.addValue(action, value, natureProb, oppRP);
				changed = true;
			}
		}
		if (!changed) {
			assert false;
		}
	}
	
	public abstract class BRActionSelection {
		protected double lowerBound;
		public abstract void addValue(ActionImpl action, double value, double natureProb, double orpProb);
		public abstract Pair<ActionImpl, Double> getResult();		
		
		public BRActionSelection(double lowerBound) {
			this.lowerBound = lowerBound;
		}
		
		public abstract double calculateNewBoundForAction(ActionImpl action, double natureProb, double orpProb);
	}
	
	public class BROppSelection extends BRActionSelection {
		
		protected double nodeProbability;		
		protected double value = 0;		
		protected boolean nonZeroORP;
		
		
		
		public BROppSelection(double lowerBound, double nodeProbability, boolean nonZeroORP) {
			super(lowerBound);
			this.nodeProbability = nodeProbability;
			this.nonZeroORP = nonZeroORP;
		}
		
		@Override
		public void addValue(ActionImpl action,	double value, double natureProb, double orpProb) {
			double probability = natureProb;
			if (nonZeroORP) {
				probability *= orpProb;
				if (orpProb == 0) value = 0;
			}
			this.nodeProbability -= probability;
			this.value += value;
		}
	
		@Override
		public Pair<ActionImpl, Double> getResult() {
			return new Pair<ActionImpl, Double>(null, value);
		}
		
		@Override
		public double calculateNewBoundForAction(ActionImpl action, double natureProb, double orpProb) {
			double probability = natureProb;
			if (nonZeroORP) {
				probability *= orpProb;
			}
			if (nodeProbability < EPS_CONSTANT) {
				if (action == null) return -MAX_UTILITY_VALUE; 
				else return Double.POSITIVE_INFINITY;
			}
			return Math.max(probability * (-MAX_UTILITY_VALUE), lowerBound - (value + (nodeProbability - probability)*MAX_UTILITY_VALUE));
		}
	}

	public class BRSrchSelection extends BRActionSelection {
		
		protected double allNodesProbability;
		
		protected HashMap<ActionImpl, Double> actionExpectedValues = new HashMap<ActionImpl, Double>();
		protected HashMap<GameState, HashMap<ActionImpl, Double>> actionRealValues = new HashMap<GameState, HashMap<ActionImpl,Double>>();
		protected double maxValue = Double.NEGATIVE_INFINITY;
		protected ActionImpl maxAction = null;
		protected ActionImpl maxActionCurrentRun = null;
		protected GameState currentNode = null;	
		protected HashMap<GameState,Double> alternativeNodesProbs = null;
		protected boolean nonZeroORP;
		
		
		public BRSrchSelection(double lowerBound, double allNodesProbability, HashMap<GameState,Double> alternativeNodesProbs, boolean nonZeroORP) {
			super(lowerBound);
			this.allNodesProbability = allNodesProbability;			
			this.alternativeNodesProbs = alternativeNodesProbs;
			this.nonZeroORP = nonZeroORP;
		}
		
		public void setCurrentNode(GameState currentNode) {
//			allNodesProbability -= nodeProbability;
			this.currentNode = currentNode;
			actionRealValues.put(currentNode, new HashMap<ActionImpl, Double>());
			maxValue = Double.NEGATIVE_INFINITY;
		}

		public void abandonCurrentNode() {
			allNodesProbability -= alternativeNodesProbs.get(currentNode);
			this.currentNode = null;
			this.maxAction = maxActionCurrentRun;
		}

		@Override
		public void addValue(ActionImpl action,	double value, double natureProb, double orpProb) {
			assert (currentNode != null);
			
			HashMap<ActionImpl, Double> currentNodeActionValues = actionRealValues.get(currentNode);
			assert (currentNodeActionValues != null);
			assert (!currentNodeActionValues.containsKey(action));			
			currentNodeActionValues.put(action, value);
			
			if (orpProb > 0 || !nonZeroORP) {
				Double currValue = actionExpectedValues.get(action);
				if (currValue == null) currValue = 0d;
				currValue += value;
				actionExpectedValues.put(action, currValue);
				
				if (currValue > maxValue) {
					maxValue = currValue;
					maxActionCurrentRun = action;
				}
			}
		}
	
		@Override
		public Pair<ActionImpl, Double> getResult() {
			return new Pair<ActionImpl, Double>(maxAction, actionExpectedValues.get(maxAction));
		}
		
		@Override
		public double calculateNewBoundForAction(ActionImpl action, double natureProb, double orpProb) {
			if (nonZeroORP && orpProb <= 0) return MAX_UTILITY_VALUE+EPS_CONSTANT;
			if (maxAction == null) {
				if (maxActionCurrentRun == null) return -MAX_UTILITY_VALUE;
				else return actionExpectedValues.get(maxActionCurrentRun);
			} else {
				if (this.allNodesProbability < EPS_CONSTANT) {
					if (action.equals(maxAction)) return -MAX_UTILITY_VALUE;
					else return MAX_UTILITY_VALUE+EPS_CONSTANT;
				} else	return (actionExpectedValues.get(maxAction) + this.allNodesProbability * (-MAX_UTILITY_VALUE)) -
							(actionExpectedValues.get(action) + ( this.allNodesProbability - alternativeNodesProbs.get(currentNode) ) * MAX_UTILITY_VALUE);
			}
		}
	}
	


	public HashSet<Sequence> getBRSequences() {
		if (BRresult == null) return null;
		if (bestResponseSequences.size() != 0) return bestResponseSequences;
		HashSet<Sequence> result = new HashSet<Sequence>();
		LinkedList<Sequence> queue = new LinkedList<Sequence>();
		queue.add(new LinkedListSequenceImpl(this.players[searchingPlayerIndex]));
		result.addAll(queue);
		
		while (queue.size() > 0) {
			Sequence s = queue.removeFirst();
			Set<Sequence> res = BRresult.get(s);
			if (res != null) {
				result.addAll(res);
				queue.addAll(res);
			}
		}
		
		bestResponseSequences = result;
		return result;
	}
	
	public HashSet<Sequence> getFullBRSequences() {
		HashSet<Sequence> result = new HashSet<Sequence>();
		Set<Sequence> keysInBR = BRresult.keySet();
		
		for (Sequence c : getBRSequences()) {
			if (!keysInBR.contains(c)) result.add(c);
		}
		
		return result;
	}
	
	public int getSearchingPlayerIndex() {
		return searchingPlayerIndex;
	}
	
	public int getOpponentPlayerIndex() {
		return opponentPlayerIndex;
	}
	
	protected class ORComparator implements Comparator<GameState> {

		final protected Map<Sequence, Double> probability;
 
		
		public ORComparator(Map<Sequence, Double> prob) {
			this.probability = prob;
		}
		
		@Override
		public int compare(GameState arg0, GameState arg1) {
			Double or0 = (probability.get(arg0.getHistory().getSequenceOf(players[opponentPlayerIndex])) == null) ? Double.NEGATIVE_INFINITY : probability.get(arg0.getHistory().getSequenceOf(players[opponentPlayerIndex]))*arg0.getNatureProbability();
			Double or1 = (probability.get(arg1.getHistory().getSequenceOf(players[opponentPlayerIndex])) == null) ? Double.NEGATIVE_INFINITY : probability.get(arg1.getHistory().getSequenceOf(players[opponentPlayerIndex]))*arg1.getNatureProbability();
			if (or0 < or1) return 1;
			if (or0 > or1) return -1;
			return 0;
		}
		
	}
}
