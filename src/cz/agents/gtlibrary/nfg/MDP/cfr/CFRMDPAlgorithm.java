/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.nfg.MDP.cfr;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.DoubleOracleCostPairedMDP;
import cz.agents.gtlibrary.nfg.MDP.FullCostPairedMDP;
import cz.agents.gtlibrary.nfg.MDP.core.MDPBestResponse;
import cz.agents.gtlibrary.nfg.MDP.core.MDPCoreLP;
import cz.agents.gtlibrary.nfg.MDP.domain.paws.PAWSConfig;
import cz.agents.gtlibrary.nfg.MDP.domain.paws.PAWSExpander;
import cz.agents.gtlibrary.nfg.MDP.domain.tig.TIGConfig;
import cz.agents.gtlibrary.nfg.MDP.domain.tig.TIGExpander;
import cz.agents.gtlibrary.nfg.MDP.domain.tig.TIGPassangerState;
import cz.agents.gtlibrary.nfg.MDP.domain.transitgame.TGConfig;
import cz.agents.gtlibrary.nfg.MDP.domain.transitgame.TGExpander;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author viliam
 */
public class CFRMDPAlgorithm {
    private ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    
    private MDPConfig config;
    private Player player;
    private MDPExpander expander;
    
    public static boolean USE_CURRENT_STRATEGY = false;
    public static boolean USE_RMPLUS = true;
    public static boolean CACHE_RELATED_ACTIONS = true;
    public static boolean REVERSE_UPDATE_ORDER = false;
    
    HashMap<Player, ArrayList<CFRMDPState>> allStates = new HashMap<>();
    
    public CFRMDPAlgorithm(MDPConfig config, Player player, MDPExpander expander) {
        this.config = config;
        this.player = player;
        this.expander = expander;
        
        String revOrd = System.getProperty("CFR_REV_ORD");
        if (revOrd != null)
            REVERSE_UPDATE_ORDER = Boolean.parseBoolean(revOrd);
        String nInit = System.getProperty("NOISY_INIT");
        if (nInit != null){
            CFRMDPState.NOISY_INIT = true;
            CFRMDPState.noiseRandom = new HighQualityRandom(Integer.parseInt(nInit));
        }
        
        long bulidStart = threadBean.getCurrentThreadCpuTime();
        for (Player p : config.getAllPlayers()) {
            buildMDP(p, config.getDomainRootState(p));
            int actions = 0;
            System.out.println("MDP " + p.getId() + " states: " + allStates.get(p).size());
        }
        System.out.println("MDP bulding time: " + (threadBean.getCurrentThreadCpuTime()-bulidStart)/1000000l);
        if (CACHE_RELATED_ACTIONS){ 
            long linkStart = threadBean.getCurrentThreadCpuTime();
            initUtilityLinks();
            System.out.println("MDP linking time: " + (threadBean.getCurrentThreadCpuTime()-linkStart)/1000000l);
        }
    }
    
    final void buildMDP(Player pl, MDPState rootState){
        HashMap<MDPState,CFRMDPState> statesMap = new HashMap<>();
        CFRMDPState rootCFRState = new CFRMDPState(rootState, expander.getActions(rootState));
        statesMap.put(rootState, rootCFRState);
        
        ArrayDeque<CFRMDPState> q = new ArrayDeque<>();
        q.add(rootCFRState);
        
        while (!q.isEmpty()){
            CFRMDPState state = q.remove();
            
            int i=0;
            for (MDPStateActionMarginal a : state.allActions){
                Map<MDPState, Double> outcomes = expander.getSuccessors(a);
                FixedSizeMap<CFRMDPState, Double> newOutcomes = new FixedSizeMap<>(outcomes.size());
                for (Map.Entry<MDPState, Double> en : outcomes.entrySet()){
                    CFRMDPState newState = statesMap.get(en.getKey());
                    if (newState == null){
                        newState = new CFRMDPState(en.getKey(),expander.getActions(en.getKey()));
                        statesMap.put(en.getKey(), newState);
                        q.add(newState);
                    }
                    newOutcomes.put(newState, en.getValue());
                }
                state.outcomes[i++]=newOutcomes;
            }
        }
        ArrayList<CFRMDPState> postOrderList = new ArrayList<>();
        postOrder(rootCFRState,postOrderList,new HashSet<CFRMDPState>());
        postOrderList.get(postOrderList.size()-1).stateProb=1;
        allStates.put(pl, postOrderList);
        System.out.println("MDP for " + pl + " built");
    }
    
    private void postOrder(CFRMDPState state, ArrayList<CFRMDPState> output, HashSet<CFRMDPState> closed){
        for (int i=0;i<state.allActions.length;i++){
            for (CFRMDPState outcome : state.outcomes[i].keySet()){
                if (!closed.contains(outcome)) postOrder(outcome, output, closed);
            }
        }
        closed.add(state);
        output.add(state);
    }
    
    final void initUtilityLinks(){
        for (CFRMDPState s1 : allStates.get(config.getAllPlayers().get(0))){
           for (CFRMDPState s2 : allStates.get(config.getAllPlayers().get(1))){
               int a1i=0;
               for (MDPStateActionMarginal a1 : s1.allActions){
                   int a2i=0;
                   for (MDPStateActionMarginal a2 : s2.allActions){
                       double u = config.getUtility(a1, a2);
                       if (u != 0){
                           s1.relatedOpActions[a1i].add(new Triplet<>(s2,a2i,u));
                           s2.relatedOpActions[a2i].add(new Triplet<>(s1,a1i,-u));
                       }
                       a2i++;
                   }
                   a1i++;
               }
           }
        }
        System.out.println("Utilities linked");
    }
    
    
    public void updateStateProbsMeanStrat(Player pl, int it){
        ArrayList<CFRMDPState> states = allStates.get(pl);
        for(int i=0;i<states.size()-1;i++) states.get(i).stateProb = 0;
        for(int i=states.size()-1;i>=0;i--){
            CFRMDPState s = states.get(i);
            if (s.stateProb != 0) for (int ai=0; ai<s.allActions.length; ai++){
                for (Map.Entry<CFRMDPState,Double> o : s.outcomes[ai].entrySet()){
                    o.getKey().stateProb += s.stateProb*s.curStrategy[ai]*o.getValue();
                }
                s.meanStrategy[ai]+= s.stateProb*s.curStrategy[ai]*(USE_RMPLUS||it==0 ? it : 1);
            }
        }
//        if (it % 100 == 0){
//            double count=0;
//            for (CFRMDPState s : states) count += (s.stateProb == 0 ? 1 : 0);
//            System.out.println("Zero states" + count / states.size());
//        }
    }
    
    public void updateStateProbsBasedOnMean(Player pl){
        ArrayList<CFRMDPState> states = allStates.get(pl);
        for(int i=0;i<states.size()-1;i++) states.get(i).stateProb = 0;
        for(int i=states.size()-1;i>=0;i--){
            CFRMDPState s = states.get(i);
            double meanSum = 0;
            for (double d : s.meanStrategy) meanSum += d;
            if (meanSum==0) continue; 
            if (s.stateProb != 0) for (int ai=0; ai<s.allActions.length; ai++){
                for (Map.Entry<CFRMDPState,Double> o : s.outcomes[ai].entrySet()){
                    o.getKey().stateProb += s.stateProb*s.meanStrategy[ai]*o.getValue()/meanSum;
                }
            }
        }
    }
    
    public void updateEVsRegretsStrategies(Player pl){
        for (CFRMDPState state : allStates.get(pl)){
            state.stateValue=0;
            double actVal[] = new double[state.allActions.length];
            //compute values
            for (int ai=0; ai<state.allActions.length; ai++){
                actVal[ai]=0;
                for (Map.Entry<CFRMDPState,Double> outcome : state.outcomes[ai].entrySet()){
                    actVal[ai] += outcome.getKey().stateValue*outcome.getValue();
                }
                if (CACHE_RELATED_ACTIONS){
                    for (Triplet<CFRMDPState, Integer, Double> trip : state.relatedOpActions[ai]){
                        actVal[ai] += trip.getFirst().stateProb*trip.getFirst().curStrategy[trip.getSecond()]*trip.getThird();
                    }
                } else {
                    for (CFRMDPState s2 : allStates.get(config.getOtherPlayer(pl))){
                        if (s2.stateProb==0) continue;
                        for (int ai2=0; ai2 < s2.allActions.length;ai2++) {
                            if (s2.curStrategy[ai2]==0) continue;
                            actVal[ai] += s2.stateProb*s2.curStrategy[ai2]*config.getUtility(state.allActions[ai], s2.allActions[ai2])*(pl.getId()==0 ? 1 : -1);
                        }
                    }
                }
                state.stateValue += state.curStrategy[ai]*actVal[ai];
            }
            //update regrets
            double regSum=0;
            for (int ai=0; ai<state.allActions.length; ai++){
                if (USE_RMPLUS) state.regrets[ai] = Math.max(0, state.regrets[ai]+actVal[ai]-state.stateValue);
                else state.regrets[ai] += actVal[ai]-state.stateValue;
                regSum += Math.max(0,state.regrets[ai]);
            }
            //update strategy
            for (int ai=0; ai<state.allActions.length; ai++){
                if (regSum <= 0) state.curStrategy[ai]=1.0/state.allActions.length;
                else state.curStrategy[ai]=Math.max(0,state.regrets[ai])/regSum;
            }
        }
    }
    
    public double computeBR(Player pl){
        for (CFRMDPState state : allStates.get(pl)){
            state.stateValue = 0;
            double max = Double.NEGATIVE_INFINITY;
            for (int ai=0; ai<state.allActions.length; ai++){
                double av=0;
                for (Map.Entry<CFRMDPState,Double> outcome : state.outcomes[ai].entrySet()){
                    av += outcome.getKey().stateValue*outcome.getValue();
                }
                if (CACHE_RELATED_ACTIONS){
                    for (Triplet<CFRMDPState, Integer, Double> trip : state.relatedOpActions[ai]){
                        double[] opStr = (USE_CURRENT_STRATEGY ? trip.getFirst().curStrategy : trip.getFirst().meanStrategy);
                        double sum=0;
                        for (double d : opStr) sum += d;
                        if (sum == 0){
                          av +=  trip.getFirst().stateProb*trip.getThird()/opStr.length;
                        } else {
                          av += trip.getFirst().stateProb*opStr[trip.getSecond()]*trip.getThird()/sum;
                        }
                    }
                } else {
                    for (CFRMDPState s2 : allStates.get(config.getOtherPlayer(pl))){
                        if (s2.stateProb==0) continue;
                        double[] opStr = (USE_CURRENT_STRATEGY ? s2.curStrategy : s2.meanStrategy);
                        double sum=0;
                        for (double d : s2.meanStrategy) sum += d;
                        for (int ai2=0; ai2 < s2.allActions.length;ai2++) {
                            if (opStr[ai2]==0) continue;
                            if (sum==0){
                                av += s2.stateProb*config.getUtility(state.allActions[ai], s2.allActions[ai2])*(pl.getId()==0 ? 1 : -1)/opStr.length;
                            } else {
                                av += s2.stateProb*opStr[ai2]*config.getUtility(state.allActions[ai], s2.allActions[ai2])*(pl.getId()==0 ? 1 : -1)/sum;
                            }
                        }
                    }
                }
                max = Math.max(max,av);
            }
            if (state.allActions.length > 0) state.stateValue = max;
        }
        return allStates.get(pl).get(allStates.get(pl).size()-1).stateValue*(pl.getId()==0 ? 1 : -1);
    }
    
    public MDPStrategy getMeanStrategy(Player pl){
        updateStateProbsBasedOnMean(pl);
        MDPStrategy strategy = new MDPStrategy(pl, config, expander);
        for (CFRMDPState state : allStates.get(pl)){
            double meanSum = 0;
            for (double d : state.meanStrategy) meanSum += d;
            if (meanSum == 0) continue;
            for (int ai=0;ai<state.allActions.length;ai++){
                strategy.putStrategy(state.allActions[ai], state.stateProb*state.meanStrategy[ai]/meanSum);
            }
        }
        return strategy;
    }
    
    public MDPStrategy getCurrentStrategy(Player pl){
        updateStateProbsMeanStrat(pl,0);
        MDPStrategy strategy = new MDPStrategy(pl, config, expander);
        for (CFRMDPState state : allStates.get(pl)){
            for (int ai=0;ai<state.allActions.length;ai++){
                strategy.putStrategy(state.allActions[ai], state.stateProb*state.curStrategy[ai]);
            }
        }
        return strategy;
    }
    
    public void runIterations(int num){
        long timeUsed = 0;
        long brTime = 0;
        int printIt = 10;
        
        long t0 = threadBean.getCurrentThreadCpuTime();
        System.out.println("Convergence: Iter; Time; P1BR; P2BR");
        
        long startTime = threadBean.getCurrentThreadCpuTime();
        for (int it=1;it<=num;it++){
            if (!REVERSE_UPDATE_ORDER){
                updateStateProbsMeanStrat(config.getOtherPlayer(player),it);
                updateEVsRegretsStrategies(player);
                updateStateProbsMeanStrat(player,it);
                updateEVsRegretsStrategies(config.getOtherPlayer(player));
            } else {
                updateStateProbsMeanStrat(player,it);
                updateEVsRegretsStrategies(config.getOtherPlayer(player));
                updateStateProbsMeanStrat(config.getOtherPlayer(player),it);
                updateEVsRegretsStrategies(player);
            }
            
            //if (num < 100 || it % (num/100) == 0) System.out.println("Iteration " + it + " completed");
            if (it == printIt || it==num){
                timeUsed += threadBean.getCurrentThreadCpuTime() - startTime;
                startTime = threadBean.getCurrentThreadCpuTime();
                Pair<Double,Double> res = computeExploitability();
                brTime += threadBean.getCurrentThreadCpuTime() - startTime;
                //System.out.println("BR Time: " + (brTime/1e6));
                System.out.println("Convergence: " + it + "; " + (timeUsed/1e6) + "; " + res.getLeft() + "; " + res.getRight());
                printIt *= 1.1;
                if (res.getLeft()-res.getRight() < 0.001) break;
                startTime = threadBean.getCurrentThreadCpuTime();
            }
        }
        System.out.println("Full thread time: " + (threadBean.getCurrentThreadCpuTime() - t0));
    }
    
    private Pair<Double,Double> computeExploitability_old(){
        MDPStrategy s1,s2;
        if (USE_CURRENT_STRATEGY){
            s1 = getCurrentStrategy(config.getAllPlayers().get(0));
            s2 = getCurrentStrategy(config.getAllPlayers().get(1));
        } else {
            s1 = getMeanStrategy(config.getAllPlayers().get(0));
            s2 = getMeanStrategy(config.getAllPlayers().get(1));
        } 
        s1.recalculateExpandedStrategy();
        s2.recalculateExpandedStrategy();
        
        MDPBestResponse br = new MDPBestResponse(config, config.getAllPlayers().get(0));
        double v1 = br.calculateBR(s1, s2);
        double vv1 = computeBR(config.getAllPlayers().get(0));
        System.out.println(v1 + " " + vv1);
        
        br = new MDPBestResponse(config, config.getAllPlayers().get(1));
        double v2 = br.calculateBR(s2, s1);
        double vv2 = computeBR(config.getAllPlayers().get(1));
        System.out.println(v2 + " " + vv2);
        System.out.println((v1-v2) + " " + (vv1-vv2));
        
        return new Pair(v1,v2);
    }
    
    private Pair<Double,Double> computeExploitability(){
        if (USE_CURRENT_STRATEGY){
            updateStateProbsMeanStrat(config.getAllPlayers().get(0),0);
            updateStateProbsMeanStrat(config.getAllPlayers().get(1),0);
        } else {
            updateStateProbsBasedOnMean(config.getAllPlayers().get(0));
            updateStateProbsBasedOnMean(config.getAllPlayers().get(1));
        }
        
        double v1 = computeBR(config.getAllPlayers().get(0));
        double v2 = computeBR(config.getAllPlayers().get(1));
        return new Pair(v1,v2);
    }
    
    // [TG10x20d24 | ... ] [ CFRP_Cur | CFRP_Mean | DO ]
    
    public static void main(String[] args){
        
        assert args.length == 2;
        MDPExpander expander = null;
        MDPConfig config = null;
      
        if (args[0].startsWith("TG")){
            int xPos = args[0].indexOf('x');
            TGConfig.WIDTH_OF_GRID = Integer.parseInt(args[0].substring(2, xPos));
            int dPos = args[0].indexOf('d');
            TGConfig.LENGTH_OF_GRID = Integer.parseInt(args[0].substring(xPos+1, dPos));
            int pPos = args[0].indexOf('p');
            if (pPos == -1){
                TGConfig.MAX_TIME_STEP = Integer.parseInt(args[0].substring(dPos+1));
            } else {
                TGConfig.MAX_TIME_STEP = Integer.parseInt(args[0].substring(dPos+1,pPos));
                TGConfig.PATROLLER_NOT_RETURNED_PENALTY = Double.parseDouble(args[0].substring(pPos+1));
            }
            expander = new TGExpander();
            config = new TGConfig();
            CACHE_RELATED_ACTIONS = true;
        } else  if (args[0].startsWith("TIG")){
            assert args[0].charAt(3)=='s';
            int tPos = args[0].indexOf('t');
            int pPos = args[0].indexOf('p');
            TIGConfig.NUM_STOPS = Integer.parseInt(args[0].substring(4, tPos));
            TIGConfig.NUM_TRAINS = Integer.parseInt(args[0].substring(tPos+1, pPos));
            TIGConfig.APPROXIMATE_PASSANGERS = Integer.parseInt(args[0].substring(pPos+1));
            
            expander = new TIGExpander();
            config = new TIGConfig();
            CACHE_RELATED_ACTIONS = true;
            REVERSE_UPDATE_ORDER = false;
            if (args[1].startsWith("LP")){
                TIGExpander.MAINTAIN_PREDECESSORS = true;
            }
        } else if (args[0].startsWith("PAWS")){
            expander = new PAWSExpander();
            config = new PAWSConfig();
            CACHE_RELATED_ACTIONS = true;
            REVERSE_UPDATE_ORDER = true;
            if (args[1].startsWith("LP")){
                PAWSExpander.MAINTAIN_PREDECESSORS = true;
            }
        } else {
             throw new IllegalArgumentException("Wrong domain definition");
        }
       
        if (args[1].startsWith("DO")){
            DoubleOracleCostPairedMDP.testGame(expander, config);
            return;
        } else if (args[1].startsWith("LP")) {
            if (args[1].length()>2 && args[1].charAt(2)=='B') MDPCoreLP.USE_BARRIER = true;
            FullCostPairedMDP.testGame(expander, config);
            return;
        } else if (args[1].startsWith("CFRP")) {
            if (args[1].equals("CFRP_Cur")) USE_CURRENT_STRATEGY = true;
            else if (!args[1].equals("CFRP_Mean")){
                throw new IllegalArgumentException("Wrong CFRP variant");
            } 
        } else if (args[1].equals("CFR")) {
                USE_RMPLUS=false;
        }else {
            throw new IllegalArgumentException("Wrong algorithm definition");
        }
        
       
        //MDPExpander expander = new BPExpander();
        //MDPConfig config = new BPConfig();
        
        System.out.println(config.toString());
        
        CFRMDPAlgorithm alg = new CFRMDPAlgorithm(config, config.getAllPlayers().get(0), expander);
        alg.runIterations(2000);
        
        Pair<Double,Double> res = alg.computeExploitability();
        
        if (args[0].startsWith("TIG")){
            int tickets = 0;
            int allTypes = 0;
            for (CFRMDPState s : alg.allStates.get(config.getAllPlayers().get(1))){
                if (s.mdpState instanceof TIGPassangerState){
                    if (s.allActions.length == 2 && s.meanStrategy[0] > 10*s.meanStrategy[1])
                        tickets += 1;
                    allTypes += 1;
                }
            }
            System.out.println("Tickets: " + tickets + " out of " + (allTypes-2) + " bought tickets.");
        }
        System.out.println("P1 BR: " + res.getLeft());
        System.out.println("P2 BR: " + res.getRight());
        System.out.println("Result: " + (res.getLeft()-res.getRight()));
        
    }
    
}
