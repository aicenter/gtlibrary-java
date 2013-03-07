package cz.agents.gtlibrary.algorithms.sequenceform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.ValueComparator;

/**
 * 
 * Best-response algorithm with pruning. It calculates best-response value for a game
 * described by the root state and the expander.
 * 
 */

public class SQFBestResponseAlgorithm {
	
	public long nodes = 0;
	
	protected Expander<SequenceInformationSet> expander;
	
    protected Map<GameState, Double> cachedValuesForNodes = new HashMap<GameState, Double>();
	private Map<Sequence, Double> opponentRealizationPlan = new HashMap<Sequence, Double>();

	protected HashMap<Sequence, HashSet<Sequence>> BRresult = new HashMap<Sequence, HashSet<Sequence>>();
	protected HashSet<Sequence> bestResponseSequences = new HashSet<Sequence>();
	
	final protected int searchingPlayerIndex;
	final protected int opponentPlayerIndex;
	final protected Player[] players;
	final protected SequenceFormConfig<SequenceInformationSet> algConfig;
	final protected GameInfo gameInfo;
	
	protected double MAX_UTILITY_VALUE;
	final protected double EPS_CONSTANT = 0.000000001; // zero for numerical-stability reasons 

	protected ORComparator comparator;
	
	
	protected GameState gameTreeRoot = null;

	public SQFBestResponseAlgorithm(Expander<SequenceInformationSet> expander, int searchingPlayerIndex, Player[] actingPlayers, SequenceFormConfig<SequenceInformationSet> algConfig, GameInfo gameInfo) {
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

		this.opponentRealizationPlan = opponentRealizationPlan;
		this.BRresult.clear();
		this.bestResponseSequences.clear();
		this.cachedValuesForNodes.clear();
		this.gameTreeRoot = root;
		
		comparator = new ORComparator(opponentRealizationPlan);
		
		return bestResponse(root, -MAX_UTILITY_VALUE);
	}

	public Double calculateBRNoClear(GameState root) {
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
				if (utRes != 0) algConfig.setUtility(gameState, utRes);
			}			  			
			if (searchingPlayerIndex == 1) utRes *= -1; // a zero sum game
			Double weight = getOpponentRealizationPlan().get(currentHistory.get(players[opponentPlayerIndex]));
			if (weight == null || weight == 0) weight = 1d;			
			return utRes * weight; // weighting with opponent's realization plan
		}

		Double tmpVal = cachedValuesForNodes.get(gameState);
		if (tmpVal != null) { // we have already solved this node as a part of an evaluated information set
			//maybe we could remove the cached value at this point? No in double-oracle -> we are using it in restricted game
			return tmpVal;
		} 

		Player currentPlayer = gameState.getPlayerToMove();

		
		if (currentPlayer.equals(players[searchingPlayerIndex])) { // searching player to move
			List<GameState> alternativeNodes = new ArrayList<GameState>();
			
			boolean nonZeroOppRP = (getOpponentRealizationPlan().get(gameState.getHistory().getSequenceOf(players[opponentPlayerIndex])) != null && getOpponentRealizationPlan().get(gameState.getHistory().getSequenceOf(players[opponentPlayerIndex])) > 0);

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
			HashMap<GameState,Double> alternativeNodesProbs = new HashMap<GameState, Double>();

			double ISProbability = 0;
			for (GameState currentNode : alternativeNodes) {
				double currentNodeProb = currentNode.getNatureProbability();
				if (nonZeroOppRP) {
					if (getOpponentRealizationPlan().containsKey(currentNode.getHistory().getSequenceOf(players[opponentPlayerIndex]))) {
						currentNodeProb *= getOpponentRealizationPlan().get(currentNode.getHistory().getSequenceOf(players[opponentPlayerIndex]));
					} else currentNodeProb = 0;
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
				if ((sel.getResult().getRight() + sel.allNodesProbability*MAX_UTILITY_VALUE) < lowerBound) { // 
					
				}
			}				

			Action resultAction = sel.getResult().getLeft(); //selected action for the searching player
				
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
			Double currentOppRealizationPlan = getOpponentRealizationPlan().get(currentHistory.get(players[opponentPlayerIndex]));
			if (currentOppRealizationPlan != null && currentOppRealizationPlan > 0) {
				nodeProbability *= currentOppRealizationPlan;
				nonZeroORP = true;
			}
			BROppSelection sel = new BROppSelection(lowerBound, nodeProbability, nonZeroORP);
			selectAction(gameState, sel, lowerBound);
			returnValue = sel.getResult().getRight();
			if (nonZeroORP && !sel.nonZeroContinuation) {
				returnValue *= currentOppRealizationPlan;
			}
		} 
			
		assert (returnValue != null);
		assert (returnValue <= MAX_UTILITY_VALUE*(1+EPS_CONSTANT));
		return returnValue;
	}
	
	public void selectAction(GameState state, BRActionSelection selection, double lowerBound) {
		boolean changed = false;
		List<Action> actionsToExplore = expander.getActions(state);
		actionsToExplore = selection.sortActions(state, actionsToExplore);
		for (Action act : actionsToExplore) {
			Action action = act;
		
			GameState newState = (GameState)state.performAction(action);
			
			double natureProb = newState.getNatureProbability(); // TODO extract these probabilities from selection Map
			Double oppRP = getOpponentRealizationPlan().get(newState.getHistory().getSequenceOf(players[opponentPlayerIndex]));
			if (oppRP == null) oppRP = 0d;
			 
			double newLowerBound = selection.calculateNewBoundForAction(action, natureProb, oppRP);
			if (newLowerBound <= MAX_UTILITY_VALUE) {
				double value = bestResponse(newState, newLowerBound);
				selection.addValue(action, value, natureProb, oppRP);
				changed = true;
			}
		}
		if (!changed) {
			assert false; // DEBUG -> remove
		}
	}

    public Map<Sequence, Double> getOpponentRealizationPlan() {
        return opponentRealizationPlan;
    }

    public abstract class BRActionSelection {
		protected double lowerBound;
		public abstract void addValue(Action action, double value, double natureProb, double orpProb);
		public abstract Pair<Action, Double> getResult();		
		
		public BRActionSelection(double lowerBound) {
			this.lowerBound = lowerBound;
		}
		
		public abstract List<Action> sortActions(GameState state, List<Action> actions);
		
		public abstract double calculateNewBoundForAction(Action action, double natureProb, double orpProb);
	}
	
	public class BROppSelection extends BRActionSelection {
		
		protected double nodeProbability;		
		protected double value = 0;		
		protected boolean nonZeroORP;
		protected boolean nonZeroContinuation = false;
		protected Double tempValue = null; 
		
		
		public BROppSelection(double lowerBound, double nodeProbability, boolean nonZeroORP) {
			super(lowerBound);
			this.nodeProbability = nodeProbability;
			this.nonZeroORP = nonZeroORP;
		}
		
		@Override
		public void addValue(Action action, double value, double natureProb, double orpProb) {
			double probability = natureProb;
			if (tempValue == null) tempValue = value;
			if (nonZeroORP) {
				if (orpProb == 0) {
					return;
				}
//				nonZeroContinuation = true;
				probability *= orpProb;
			}
			this.nodeProbability -= probability;
			this.value += value;
		}

		@Override
		public Pair<Action, Double> getResult() {
			if (nonZeroORP && !nonZeroContinuation)
				return new Pair<Action, Double>(null, tempValue);
			else
				return new Pair<Action, Double>(null, value);
		}

		@Override
		public double calculateNewBoundForAction(Action action, double natureProb, double orpProb) {
			double probability = natureProb;
			if (nonZeroORP) {
				probability *= orpProb;
				if (orpProb == 0) {
					if (tempValue == null) return -MAX_UTILITY_VALUE;
					else return Double.POSITIVE_INFINITY;
				}
			}
			if (nodeProbability < EPS_CONSTANT) {
				if (tempValue == null) return -MAX_UTILITY_VALUE;
				else return Double.POSITIVE_INFINITY;
			}
			return (lowerBound - (value + (nodeProbability - probability)*MAX_UTILITY_VALUE));
		}

		@Override
		public List<Action> sortActions(GameState state, List<Action> actions) {
			List<Action> result = new ArrayList<Action>();
			if (state.isPlayerToMoveNature()) {
				nonZeroContinuation = nonZeroORP;
				// sort according to the nature probability
				Map<Action, Double> actionMap = new FixedSizeMap<Action, Double>(actions.size());
				for (Action a : actions) {
					actionMap.put(a, state.getProbabilityOfNatureFor(a)); // the standard way is to sort ascending; hence, we store negative probability
				}
				ValueComparator<Action> comp = new ValueComparator<Action>(actionMap);
		        TreeMap<Action,Double> sortedMap = new TreeMap<Action,Double>(comp);
		        sortedMap.putAll(actionMap);
				result.addAll(sortedMap.keySet());
			} else {
				// sort according to the opponent realizaiton plan
				Sequence currentSequence = state.getSequenceFor(players[opponentPlayerIndex]);
				Map<Action, Double> sequenceMap = new FixedSizeMap<Action, Double>(actions.size());
				for (Action a : actions) {
					Sequence newSeq = new LinkedListSequenceImpl(currentSequence);
					newSeq.addLast(a);
					Double prob = getOpponentRealizationPlan().get(newSeq);
					if (prob == null) prob = 0d;
					if (prob > 0) nonZeroContinuation = true;
					sequenceMap.put(a, prob); // the standard way is to sort ascending; hence, we store negative probability
				}
				ValueComparator<Action> comp = new ValueComparator<Action>(sequenceMap);
		        TreeMap<Action,Double> sortedMap = new TreeMap<Action,Double>(comp);
		        sortedMap.putAll(sequenceMap);
				result.addAll(sortedMap.keySet());
			}
			return result;
		}
	}

	public class BRSrchSelection extends BRActionSelection {
		
		protected double allNodesProbability;
		
		protected HashMap<Action, Double> actionExpectedValues = new HashMap<Action, Double>();
		protected HashMap<GameState, HashMap<Action, Double>> actionRealValues = new HashMap<GameState, HashMap<Action,Double>>();
		protected double maxValue = Double.NEGATIVE_INFINITY;
		protected double previousMaxValue = Double.NEGATIVE_INFINITY;
		protected Action maxAction = null;
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
			actionRealValues.put(currentNode, new HashMap<Action, Double>());
			maxValue = Double.NEGATIVE_INFINITY;
		}

		public void abandonCurrentNode() {
			allNodesProbability -= alternativeNodesProbs.get(currentNode);
			this.currentNode = null;
			previousMaxValue = actionExpectedValues.get(maxAction);
		}

		@Override
		public void addValue(Action action, double value, double natureProb, double orpProb) {
			assert (currentNode != null);
			
			HashMap<Action, Double> currentNodeActionValues = actionRealValues.get(currentNode);
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
					maxAction = action;
				}
			}
		}
	
		@Override
		public Pair<Action, Double> getResult() {
			return new Pair<Action, Double>(maxAction, actionExpectedValues.get(maxAction));
		}
		
		@Override
		public double calculateNewBoundForAction(Action action, double natureProb, double orpProb) {
			if (nonZeroORP && orpProb <= 0) return Double.POSITIVE_INFINITY;
			if (previousMaxValue == Double.NEGATIVE_INFINITY) {
				return Double.NEGATIVE_INFINITY;
			} else {
				if (this.allNodesProbability < EPS_CONSTANT) {
					return Double.POSITIVE_INFINITY;
				} else	{
//					double probability = natureProb;
//					if (nonZeroORP) probability *= orpProb;
					return ((previousMaxValue + this.allNodesProbability * (-MAX_UTILITY_VALUE)) -
								(actionExpectedValues.get(action) + ( this.allNodesProbability - alternativeNodesProbs.get(currentNode) ) * MAX_UTILITY_VALUE));
				}
			}
		}

		@Override
		public List<Action> sortActions(GameState state, List<Action> actions) {
			// TODO implement actions sorting for the searching player 
			return actions;
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
	
	public class ORComparator implements Comparator<GameState> {

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
	
	public Double getCachedValueForState(GameState state) {
		return cachedValuesForNodes.get(state);
	}
}
