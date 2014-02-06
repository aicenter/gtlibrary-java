package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;

public class MCTSRunner implements Serializable{
        protected ThreadMXBean threadBean;
	private final int MCTS_ITERATIONS_PER_CALL = 1000;
	private final int SAME_STRATEGY_CHECK_COUNT = 20;

	protected MCTSConfig algConfig;
	protected GameState gameState;
	protected Expander<MCTSInformationSet> expander;
        protected InnerNode rootNode;

	public MCTSRunner(MCTSConfig algConfig, GameState gameState, Expander<MCTSInformationSet> expander) {
		this.algConfig = algConfig;
		this.gameState = gameState;
		this.expander = expander;
                threadBean = ManagementFactory.getThreadMXBean();
	}
        
        public void runMCTStime(int miliseconds, Player player){
            if (rootNode == null)
                    rootNode = createRootNode(gameState, expander, algConfig);
            long start = threadBean.getCurrentThreadCpuTime();
            for (;(threadBean.getCurrentThreadCpuTime()-start)/1e6 < miliseconds;) {
                    iteration();
            }
        }
        
        
        public void runMCTS(int iterations, Player player){
            if (rootNode == null)
                    rootNode = createRootNode(gameState, expander, algConfig);
            for (int i = 0; i < iterations; i++) {
                    iteration();
            }
        }
        
        private void iteration(){
            //includes expansion
            Node selectedLeaf = rootNode;
            selectedLeaf = rootNode.selectRecursively();
            Action a = null;
            Node child = selectedLeaf;
            if (selectedLeaf instanceof InnerNode) {
                child = ((InnerNode)selectedLeaf).selectChild();
                a = child.getLastAction();
            }
            selectedLeaf.backPropagate(a, child.simulate());
        }

	public Strategy runMCTS(int iterations, Player player, Distribution distribution) {
		runMCTS(iterations, player);
                if (distribution == null) return null;
		return getCurrentStrategyFor(player, distribution);
	}
        
        public Strategy getCurrentStrategyFor(Player player, Distribution distribution){
            return getCurrentStrategyFor(player, distribution, Integer.MAX_VALUE);
        }
        
        public Strategy getCurrentStrategyFor(Player player, Distribution distribution, int cutOffDepth){
                Strategy strategy = rootNode.getStrategyFor(player, distribution, cutOffDepth);
		for (Map.Entry<Sequence, Double> en : strategy.entrySet()){
                    if (en.getValue() < 1e-6){
                        en.setValue(0d);
                    }
                }
                
		strategy.put(new LinkedListSequenceImpl(player), 1d);
		return strategy;
        }

        public double[] getEV(){
            return rootNode.getEV();
        }

	public Strategy runMCTS(Player player, Distribution distribution) {
		Strategy lastPureStrategy = null;
		Strategy strategy = null;
		int counter = 0;
 
		while (true) {
			strategy = runMCTS(MCTS_ITERATIONS_PER_CALL, player, distribution);
			if (lastPureStrategy != null && strategy.maxDifferenceFrom(lastPureStrategy) < 0.001) {
//			if(strategy.equals(lastPureStrategy)) {
				counter++;
			} else {
				counter = 0;
			}
			if (counter == SAME_STRATEGY_CHECK_COUNT) {
				break;
			}
			lastPureStrategy = strategy;
		}
		return strategy;
	}

//
//	/**
//	 * Runs MCTS until given +- epsilon is reached
//	 * 
//	 * @param player
//	 * @param value
//	 * @param epsilon
//	 * @return
//	 */
//	public Map<Sequence, Double> runMcts(Player player, double value, double epsilon) {
//		if (rootNode == null)
//			rootNode = createRootNode(gameState, expander, algConfig);
//		Node selectedLeaf = rootNode;
//		int iterationCount = 0;
//
//		while (Math.abs(rootNode.getEV()[player.getId()] - value) > epsilon) {
//			selectedLeaf = rootNode.selectRecursively();
//			selectedLeaf.expand();
//			selectedLeaf.backPropagate(selectedLeaf.simulate());
//			iterationCount++;
//		}
//		System.out.println("Iterations of MCTS: " + iterationCount);
//		return rootNode.getPureStrategyFor(player);
//	}
//
//	public Map<Sequence, Double> runMctsWithIncreasingFixedDepth(int iterations, Player player) {
//		if (rootNode == null)
//			rootNode = createRootNode(gameState, expander, algConfig);
//		Node selectedLeaf = rootNode;
//		int depth = 0;
//		int iterationsLeft = iterations;
//
//		for (int i = 0; i < iterations; i++) {
//			selectedLeaf = rootNode.selectRecursively(depth);
//
//			if (i >= iterations - iterationsLeft / 3.) {
//				iterationsLeft = iterations - i;
//				if (selectedLeaf.getDepth() > depth + 1)
//					depth++;
//			}
//			selectedLeaf.expand();
//			selectedLeaf.backPropagate(selectedLeaf.simulate());
//		}
//		System.out.println("Expected value: " + Arrays.toString(rootNode.getEV()));
//		return rootNode.getPureStrategyFor(player);
//	}

	protected InnerNode createRootNode(GameState gameState, Expander<MCTSInformationSet> expander, MCTSConfig algConfig) {
		if (gameState.isPlayerToMoveNature())
			return new ChanceNode(expander, algConfig, gameState);
		return new InnerNode(expander, algConfig, gameState);
	}

    private InnerNode oldParent = null;
    public void setRootNode(InnerNode rootNode) {
        if (oldParent != null) rootNode.setParent(oldParent);
        this.rootNode = rootNode;
        oldParent = rootNode.getParent();
        rootNode.setParent(null);
    }

    public InnerNode getRootNode() {
        return rootNode;
    }

    public void saveToFile(String fileName) throws Exception{
        FileOutputStream file = new FileOutputStream(fileName);
        ObjectOutputStream stream = new ObjectOutputStream(file);
        stream.writeObject(this);
        stream.close();
        file.close();
    }
    
    public static MCTSRunner loadFromFile(String fileName) throws Exception {
        FileInputStream file = new FileInputStream(fileName);
        ObjectInputStream stream = new ObjectInputStream(file);
        MCTSRunner runner = (MCTSRunner) stream.readObject();
        stream.close();
        file.close();
        return runner;
    }
    
    public void setCurrentIS(MCTSInformationSet currentIS) {
           assert false; //not implemented
    }
        
}
    