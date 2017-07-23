package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.memeff;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.domain.randomabstraction.IDObservation;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.CFRBRData;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.ImperfectRecallISKey;
import cz.agents.gtlibrary.iinodes.Observations;
import cz.agents.gtlibrary.interfaces.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AutomatedAbstractionAlgorithm {

    protected final GameState rootState;
    protected final Expander<? extends InformationSet> expander;
    protected final GameInfo gameInfo;
    protected final Map<ISKey, IRCFRInformationSet> currentAbstractionInformationSets;
    protected final InformationSetKeyMap currentAbstractionISKeys;
    protected int iteration = 1;
    protected int isKeyCounter = 0;

//    public static void main(String[] args) {
//        runGenericPoker();
//    }
//
//    protected static void runGenericPoker() {
//        GameState root = new GenericPokerGameState();
//        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(new MCTSConfig());
//
//        System.out.println("Abstracted IS count: " + expander.getAlgorithmConfig().getAllInformationSets().size());
//        AutomatedAbstractionAlgorithm cfr = new AutomatedAbstractionAlgorithm(root, expander, new GPGameInfo());
//
//        cfr.runIterations(1000000);
//        GambitEFG gambit = new GambitEFG();
//
//        gambit.write("cfrbrtest.gbt", root, expander);
//    }


    public AutomatedAbstractionAlgorithm(GameState rootState, Expander<? extends InformationSet> expander, GameInfo info) {
        this.rootState = rootState;
        this.expander = expander;
        this.gameInfo = info;
        currentAbstractionInformationSets = new HashMap<>();
        currentAbstractionISKeys = new InformationSetKeyMap();
        buildInitialAbstraction();
    }

    protected void buildInitialAbstraction() {
        buildInformationSets(rootState);
        addData(currentAbstractionInformationSets.values());
    }

    protected void buildInformationSets(GameState state) {
        if (state.isGameEnd())
            return;
        ImperfectRecallISKey key = getAbstractionISKey(state);
        IRCFRInformationSet set = currentAbstractionInformationSets.computeIfAbsent(key, k -> new IRCFRInformationSet(state, key));

        set.addStateToIS(state);//is it really necessary to store the states here? maybe one for expander is enough
        expander.getActions(state).stream().map(a -> state.performAction(a)).forEach(s -> buildInformationSets(s));
    }


    protected void addData(Collection<IRCFRInformationSet> informationSets) {
        informationSets.forEach(i -> i.setData(new CFRBRData(this.expander.getActions(i.getAllStates().stream().findAny().get()))));
    }

    public void runIterations(int iterations) {
        for (int i = 0; i < iterations; i++) {
            this.iteration++;
            iteration(rootState.getAllPlayers()[1]);
            iteration(rootState.getAllPlayers()[0]);
            if (i % 20 == 0 || iteration == 1)
                printStatistics();
        }
    }

    protected Map<ISKey, double[]> getBehavioralStrategyFor(Player player) {
        Map<ISKey, double[]> strategy = new HashMap<>(currentAbstractionInformationSets.size() / 2);

        currentAbstractionInformationSets.values().stream().filter(is -> is.getPlayer().equals(player)).forEach(is ->
                strategy.put(is.getISKey(), is.getData().getMp())
        );
        return strategy;
    }

    protected ImperfectRecallISKey getAbstractionISKey(GameState state) {
        return currentAbstractionISKeys.get(state, expander);
    }

    protected ImperfectRecallISKey createCounterISKey(Player player) {
        Observations observations = new Observations(player, player);

        observations.add(new IDObservation(isKeyCounter++));
        return new ImperfectRecallISKey(observations, null, null);
    }

    protected abstract void printStatistics();

    protected abstract void iteration(Player player);

}
