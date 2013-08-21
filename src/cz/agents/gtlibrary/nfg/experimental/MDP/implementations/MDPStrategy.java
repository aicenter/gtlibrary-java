package cz.agents.gtlibrary.nfg.experimental.MDP.implementations;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 6/27/13
 * Time: 1:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class MDPStrategy extends MixedStrategy<MDPStateActionMarginal>{

    private MDPConfig config;
    private Player player;
    private MDPExpander expander;

//    private Map<Set<MDPStateActionMarginal>, Double> utilityCache = new HashMap<Set<MDPStateActionMarginal>, Double>();

    private MDPState root;
    private Set<MDPState> strategyStates = new HashSet<MDPState>();
    private Map<MDPStateActionMarginal, Double> strategy = new HashMap<MDPStateActionMarginal, Double>();

    static private Map<Set<MDPStateActionMarginal>, Double> utilityCache = null;

    public MDPStrategy(Player player, MDPConfig config, MDPExpander expander) {
        if (utilityCache == null) {
            utilityCache = new HashMap<Set<MDPStateActionMarginal>, Double>();
        }
        this.config = config;
        this.player = player;
        this.expander = expander;
        root = new MDPRootState(player);
        HashSet<MDPState> rootStates = new HashSet<MDPState>();
        strategyStates.add(root);
    }

    @Override
    public void sanityCheck() {
          for (MDPState s : strategyStates) {
              if (s.isRoot()) continue;
              if (hasStateASuccessor(s)) {
                  double ls = 0;
                  double rs = 0;
                  Map<MDPStateActionMarginal, Double> m = getPredecessors(s);
                  for (MDPStateActionMarginal pred : m.keySet()) {
                      if (strategy.containsKey(pred))
                        ls += strategy.get(pred)*m.get(pred);
                  }
                  for (MDPAction a : getActions(s)) {
                      MDPStateActionMarginal map2 = new MDPStateActionMarginal(s,a);
                      if (strategy.containsKey(map2)) {
                          rs += strategy.get(map2);
                      }
                  }
                  if (Math.abs(ls - rs) > MDPConfigImpl.getEpsilon())
                      assert false;
              }
          }
    }

    public Map<MDPStateActionMarginal, Double> getStrategy() {
        return strategy;
    }

    public Set<MDPState> getStates() {
        return strategyStates;
    }

    public Set<MDPStateActionMarginal> getActionStates() {
        return strategy.keySet();
    }

    public Set<MDPStateActionMarginal> getAllActionStates() {
        return strategy.keySet();
    }

    public MDPState getRootState() {
        return root;
    }

    public Map<MDPState, Double> getAllSuccessors(MDPStateActionMarginal action) {
        return expander.getSuccessors(action);
    }

    public Map<MDPState, Double> getSuccessors(MDPStateActionMarginal action) {
        return getAllSuccessors(action);
    }

    public Map<MDPStateActionMarginal, Double> getPredecessors(MDPState state) {
        Map<MDPStateActionMarginal, Double> result = expander.getPredecessors(state);
        if (result == null) {
            result = new HashMap<MDPStateActionMarginal, Double>();
            result.put(new MDPStateActionMarginal(getRootState(), getActions(getRootState()).get(0)), 1d);
        }
        return result;
    }

    public List<MDPAction> getAllActions(MDPState state) {
        return expander.getActions(state);
    }

    public List<MDPAction> getActions(MDPState state) {
        return getAllActions(state);
    }

    public void generateCompleteStrategy() {
        LinkedList<MDPState> queue = new LinkedList<MDPState>();
        queue.add(getRootState());
        while (!queue.isEmpty()) {
            MDPState state = queue.poll();
            addStrategyState(state);
            List<MDPAction> actions = getActions(state);
            for (MDPAction a : actions) {
                MDPStateActionMarginal mdpsam = new MDPStateActionMarginal(state, a);
                putStrategy(mdpsam,0d);
                for (Map.Entry<MDPState, Double> e : getSuccessors(mdpsam).entrySet()) {
                    queue.addLast(e.getKey());

                }
            }
        }
    }


    public boolean hasStateASuccessor(MDPState state) {
        List<MDPAction> actions = getActions(state);
        if (actions == null || actions.isEmpty())
            return false;
        for (MDPAction a : actions) {
            MDPStateActionMarginal map = new MDPStateActionMarginal(state, a);
            if (!getSuccessors(map).isEmpty())
                return true;
        }
        return false;
    }

    public boolean hasAllStateASuccessor(MDPState state) {
        List<MDPAction> actions = getAllActions(state);
        if (actions == null || actions.isEmpty())
            return false;
        for (MDPAction a : actions) {
            MDPStateActionMarginal map = new MDPStateActionMarginal(state, a);
            if (!getAllSuccessors(map).isEmpty())
                return true;
        }
        return false;
    }

    public void putStrategy(MDPStateActionMarginal map, Double prob) {
        strategy.put(map, prob);

    }

    protected void addStrategyState(MDPState state) {
        strategyStates.add(state);
    }

    public double getUtility(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction) {
        return config.getUtility(firstPlayerAction,secondPlayerAction);
    }


    public double getUtility(MDPStateActionMarginal firstPlayerAction, MDPStrategy secondPlayerStrategy) {
        double result = 0;

        for (MDPStateActionMarginal mdp : secondPlayerStrategy.getStrategy().keySet()) {
            result += getUtility(firstPlayerAction, mdp) * secondPlayerStrategy.getStrategy().get(mdp);
        }

        return result;
    }

    public Double getUtilityFromCache(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction) {
        Set<MDPStateActionMarginal> mdps = new HashSet<MDPStateActionMarginal>();
        mdps.add(firstPlayerAction);
        mdps.add(secondPlayerAction);
        Double v = utilityCache.get(mdps);
        if (v == null) v = 0d;
        return v;
    }

    public void storeUtilityToCache(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction, Double value) {
        Set<MDPStateActionMarginal> mdps = new HashSet<MDPStateActionMarginal>();
        mdps.add(firstPlayerAction);
        mdps.add(secondPlayerAction);
        utilityCache.put(mdps, value);
    }

    public void removeUtilityFromCache(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction) {
        Set<MDPStateActionMarginal> mdps = new HashSet<MDPStateActionMarginal>();
        mdps.add(firstPlayerAction);
        mdps.add(secondPlayerAction);
        utilityCache.remove(mdps);
    }

    public void storeAllUtilityToCache(Set<MDPStateActionMarginal> firstPlayerSet, Set<MDPStateActionMarginal> secondPlayerSet) {
        for (MDPStateActionMarginal a1 : firstPlayerSet) {
            for (MDPStateActionMarginal a2 : secondPlayerSet) {
                double v = getUtility(a1, a2);
                if (v != 0) {
                    storeUtilityToCache(a1, a2, v);
                }
            }
        }
    }

    public class MDPRootState extends MDPStateImpl {

        private int hash;

        public MDPRootState(Player player) {
            super(player);
            hash = "MDPRootState".hashCode() * 31 + player.hashCode();
        }

        @Override
        public MDPState performAction(MDPAction action) {
            return config.getDomainRootState(getPlayer()).performAction(action);
        }

        @Override
        public MDPState copy() {
            return new MDPRootState(getPlayer());
        }

        @Override
        public boolean isRoot() {
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            return o == this;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return "MDPRootState:"+getPlayer();
        }
    }

}
