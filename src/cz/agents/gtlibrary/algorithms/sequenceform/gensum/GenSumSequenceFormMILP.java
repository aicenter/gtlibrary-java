package cz.agents.gtlibrary.algorithms.sequenceform.gensum;

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameInfo;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.bpg.GenSumBPGGameState;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMExpander;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMGameInfo;
import cz.agents.gtlibrary.domain.mpochm.MPoCHMGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.domain.randomgame.GeneralSumRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.stacktest.StackTestExpander;
import cz.agents.gtlibrary.domain.stacktest.StackTestGameInfo;
import cz.agents.gtlibrary.domain.stacktest.StackTestGameState;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import ilog.concert.IloException;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

public class GenSumSequenceFormMILP {

    public static void main(String[] args) {
//        runStackelbergTest();
//        runKuhnPoker();
//        runGP();
//        runBPG();
//        runGenSumBPG();
        runAoS();
//        runMPoCHM();
//        runGenSumRandomGame();
    }

    protected static void runGenSumRandomGame() {
        GameState root = new GeneralSumRandomGameState();
        GenSumSequenceFormConfig config = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new RandomGameExpander<>(config);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, new RandomGameInfo(), config);

        builder.generateCompleteGame();
        GenSumSequenceFormMILP solver = new GenSumSequenceFormMILP(config, root.getAllPlayers(), new RandomGameInfo());

        solver.compute();
    }

    protected static void runAoS() {
        GameState root = new AoSGameState();
        GenSumSequenceFormConfig config = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new AoSExpander<>(config);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, new AoSGameInfo(), config);

        builder.generateCompleteGame();
        GenSumSequenceFormMILP solver = new GenSumSequenceFormMILP(config, root.getAllPlayers(), new AoSGameInfo());

        solver.compute();
    }

    protected static void runMPoCHM() {
        GameState root = new MPoCHMGameState();
        GenSumSequenceFormConfig config = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new MPoCHMExpander<>(config);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, new MPoCHMGameInfo(), config);

        builder.generateCompleteGame();
        GenSumSequenceFormMILP solver = new GenSumSequenceFormMILP(config, root.getAllPlayers(), new MPoCHMGameInfo());

        solver.compute();
    }

    protected static void runStackelbergTest() {
        GameState root = new StackTestGameState();
        GenSumSequenceFormConfig config = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new StackTestExpander<>(config);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, new StackTestGameInfo(), config);

        builder.generateCompleteGame();
        GenSumSequenceFormMILP solver = new GenSumSequenceFormMILP(config, root.getAllPlayers(), new StackTestGameInfo());

        solver.compute();
    }

    protected static void runGP() {
        GameState root = new GenericPokerGameState();
        GenSumSequenceFormConfig config = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new GenericPokerExpander<>(config);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, new GPGameInfo(), config);

        builder.generateCompleteGame();
        GenSumSequenceFormMILP solver = new GenSumSequenceFormMILP(config, root.getAllPlayers(), new GPGameInfo());

        solver.compute();
    }

    protected static void runKuhnPoker() {
        GameState root = new KuhnPokerGameState();
        GenSumSequenceFormConfig config = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new KuhnPokerExpander<>(config);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, new KPGameInfo(), config);

        builder.generateCompleteGame();
        GenSumSequenceFormMILP solver = new GenSumSequenceFormMILP(config, root.getAllPlayers(), new KPGameInfo());

        solver.compute();

    }

    protected static void runBPG() {
        GameState root = new BPGGameState();
        GenSumSequenceFormConfig config = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new BPGExpander<>(config);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, new BPGGameInfo(), config);

        builder.generateCompleteGame();
        GenSumSequenceFormMILP solver = new GenSumSequenceFormMILP(config, root.getAllPlayers(), new BPGGameInfo());

        solver.compute();
        GambitEFG efg = new GambitEFG();

        efg.write("BPG.gbt", root, expander);
    }

    protected static void runGenSumBPG() {
        GameState root = new GenSumBPGGameState();
        GenSumSequenceFormConfig config = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new BPGExpander<>(config);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, new BPGGameInfo(), config);

        builder.generateCompleteGame();
        GenSumSequenceFormMILP solver = new GenSumSequenceFormMILP(config, root.getAllPlayers(), new BPGGameInfo());

        solver.compute();
        GambitEFG efg = new GambitEFG();

        efg.write("GenSumBPG.gbt", root, expander);
    }

    protected final double M = 1e6;
    protected MILPTable lpTable;
    protected GenSumSequenceFormConfig config;
    protected GameInfo info;
    protected Player[] players;
    protected ThreadMXBean threadMXBean;
    protected long lpTime;
    private double objectiveValue;

    public GenSumSequenceFormMILP(GenSumSequenceFormConfig config, Player[] players, GameInfo info) {
        lpTable = new MILPTable();
        this.info = info;
        this.config = config;
        this.players = players;
        threadMXBean = ManagementFactory.getThreadMXBean();
    }

    public SolverResult compute() {
        generateSequenceConstraints();
        generateISConstraints();
//        addObjective();
//        addMaxValueConstraints();
        return solve();
    }

    protected void addMaxValueConstraints() {
        addMaxValueConstraintFor(players[0]);
        addMaxValueConstraintFor(players[1]);
    }

    protected void addMaxValueConstraintFor(Player player) {
        lpTable.setConstraint("uMax_" + player, new Pair<>("v", player.getId()), 1);
//        lpTable.setConstant("uMax_" + player, info.getMaxUtility());
        lpTable.setConstant("uMax_" + player, getMaxUtility(player));
        lpTable.setConstraintType("uMax_" + player, 0);
    }

    protected double getMaxUtility(Player player) {
        double maxUtility = Double.NEGATIVE_INFINITY;

        for (Double[] utilities : config.getUtilityForSequenceCombinationGenSum().values()) {
            if (maxUtility < utilities[player.getId()])
                maxUtility = utilities[player.getId()];
        }
        return maxUtility;
    }

    protected void setCplex(LPData data) throws IloException {
//        data.getSolver().setParam(IloCplex.IntParam.NodeSel, IloCplex.NodeSelect.BestEst);
    }

    protected SolverResult solve() {
        try {
            LPData data = lpTable.toCplex();

            System.out.println("p0 sequence count: " + config.getSequencesFor(players[0]).size());
            System.out.println("p1 sequence count: " + config.getSequencesFor(players[1]).size());
            System.out.println("IS count: " + config.getAllInformationSets().size());
//            data.getSolver().exportModel("milp.lp");
            long start = threadMXBean.getCurrentThreadCpuTime();

            setCplex(data);
            data.getSolver().solve();
            lpTime = (long) ((threadMXBean.getCurrentThreadCpuTime() - start) / 1e6);
            System.out.println("LP time: " + lpTime);
            System.out.println(data.getSolver().getStatus());
            System.out.println("p0 reward: " + data.getSolver().getValue(data.getVariables()[lpTable.getVariableIndex(new Pair<>("v", 0))]) / info.getUtilityStabilizer());
            System.out.println("p1 reward: " + data.getSolver().getValue(data.getVariables()[lpTable.getVariableIndex(new Pair<>("v", 1))]) / info.getUtilityStabilizer());
            System.out.println("obj. reward: " + data.getSolver().getObjValue());
            objectiveValue =  data.getSolver().getObjValue();
//            System.out.println("Strategies: ");
            for (Map<Sequence, Double> realPan : getStrategyProfile(data).values()) {
                printNonZero(System.out, realPan);
            }
//            System.out.println("Slacks: ");
//            print(System.out, getSlacks(data, players[0]));
//            print(System.out, getSlacks(data, players[1]));
//            System.out.println("Values: ");
//            print(System.out, getISValues(data, players[0]));
//            print(System.out, getISValues(data, players[1]));

            return new SolverResult(getRealPlan(data, players[0]), getRealPlan(data, players[1]), lpTime);
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected Map<InformationSet, Double> getISValues(LPData data, Player player) {
        Map<InformationSet, Double> isValues = new HashMap<>();

        for (SequenceInformationSet informationSet : config.getAllInformationSets().values()) {
            if (informationSet.getPlayer().equals(player))
                try {
                    if (!informationSet.getOutgoingSequences().isEmpty())
                        isValues.put(informationSet, data.getSolver().getValue(data.getVariables()[lpTable.getVariableIndex(new Pair<>("v", informationSet))]));
                } catch (IloException e) {
                    e.printStackTrace();
                }
        }
        return isValues;
    }

    protected Map<Sequence, Double> getSlacks(LPData data, Player player) {
        Map<Sequence, Double> slacks = new HashMap<>();

        for (Sequence sequence : config.getSequencesFor(player)) {
            try {
                slacks.put(sequence, data.getSolver().getValue(data.getVariables()[lpTable.getVariableIndex(new Pair<>("s", sequence))]));
            } catch (IloException e) {
                e.printStackTrace();
            }
        }
        return slacks;
    }

    private Map<Player, Map<Sequence, Double>> getStrategyProfile(LPData data) {
        Map<Player, Map<Sequence, Double>> strategyProfile = new HashMap<>(2);

        strategyProfile.put(players[0], getRealPlan(data, players[0]));
        strategyProfile.put(players[1], getRealPlan(data, players[1]));
        return strategyProfile;
    }

    protected Map<Sequence, Double> getRealPlan(LPData data, Player player) {
        Map<Sequence, Double> realPlan = new HashMap<>();

        for (Sequence sequence : config.getSequencesFor(player)) {
            try {
                realPlan.put(sequence, data.getSolver().getValue(data.getVariables()[lpTable.getVariableIndex(sequence)]));
            } catch (IloException e) {
                e.printStackTrace();
            }
        }
        return realPlan;
    }

    protected void generateISConstraints() {
        for (SequenceInformationSet informationSet : config.getAllInformationSets().values()) {
            createConstraintFor(informationSet);
        }
    }

    protected void generateSequenceConstraints() {
        initRPConstraint(new ArrayListSequenceImpl(players[0]));
        initRPConstraint(new ArrayListSequenceImpl(players[1]));
        for (Sequence sequence : config.getAllSequences()) {
            createValueConstraintFor(sequence);
            createIntegerConstraint(sequence);
            createSlackConstraint(sequence);
        }
    }

    protected void createSlackConstraint(Sequence sequence) {
        Object key = new Pair<>("s", sequence);

        lpTable.setConstraint(key, key, 1);
        lpTable.setConstraint(key, new Pair<>("b", sequence), M);
        lpTable.setConstant(key, M);
        lpTable.setConstraintType(key, 0);
    }

    protected void createIntegerConstraint(Sequence sequence) {
        Object key = new Pair<>("b", sequence);

        lpTable.setConstraint(key, sequence, 1);
        lpTable.setConstraint(key, key, -1);
        lpTable.setConstraintType(key, 0);
        lpTable.markAsBinary(key);
    }

    protected void initRPConstraint(Sequence emptySequence) {
        lpTable.setConstraint(emptySequence.getPlayer(), emptySequence, 1);
        lpTable.setConstant(emptySequence.getPlayer(), 1);
        lpTable.setConstraintType(emptySequence.getPlayer(), 1);
    }

    protected void createConstraintFor(SequenceInformationSet informationSet) {
        if (!informationSet.getOutgoingSequences().isEmpty()) {
            lpTable.setConstraint(informationSet, informationSet.getPlayersHistory(), 1);
            for (Sequence outgoingSequence : informationSet.getOutgoingSequences()) {
                lpTable.setConstraint(informationSet, outgoingSequence, -1);
            }
            lpTable.setConstraintType(informationSet, 1);
        }
    }

    protected void createValueConstraintFor(Sequence sequence) {
        Object infSetVarKey = new Pair<>("v", (sequence.size() == 0 ? sequence.getPlayer().getId() : sequence.getLastInformationSet()));

        lpTable.setConstraint(sequence, infSetVarKey, 1);
        lpTable.setLowerBound(infSetVarKey, Double.NEGATIVE_INFINITY);
        lpTable.setConstraint(sequence, new Pair<>("s", sequence), -1);
        for (Sequence compatibleSequence : config.getCompatibleSequencesFor(sequence)) {
            Double utility = config.getUtilityFor(sequence, compatibleSequence, sequence.getPlayer());

            if (utility != null)
                lpTable.setConstraint(sequence, compatibleSequence, -info.getUtilityStabilizer() * utility);
        }
        for (SequenceInformationSet reachableIS : config.getReachableSets(sequence)) {
            if (!reachableIS.getOutgoingSequences().isEmpty())
                lpTable.setConstraint(sequence, new Pair<>("v", reachableIS), -1);
        }
        lpTable.setConstraintType(sequence, 1);
    }

    protected void printNonZero(PrintStream stream, Map<? extends Object, Double> realPlan) {
        for (Map.Entry<? extends Object, Double> entry : realPlan.entrySet()) {
            if (entry.getValue() > 0)
                stream.println(entry);
        }
    }

    protected void print(PrintStream stream, Map<? extends Object, Double> realPlan) {
        for (Map.Entry<? extends Object, Double> entry : realPlan.entrySet()) {
            stream.println(entry);
        }
    }

    public long getLpTime() {
        return lpTime;
    }

    public double getObjectiveValue() {
        return objectiveValue;
    }
}
