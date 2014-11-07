package cz.agents.gtlibrary.algorithms.sequenceform.gensum.experiments;

import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.*;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTBackPropFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.*;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.quantalresponse.QREResult;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.quantalresponse.QRESolver;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom.DataBuilder;
import cz.agents.gtlibrary.algorithms.stackelberg.*;
import cz.agents.gtlibrary.algorithms.stackelberg.milp.StackelbergSequenceFormMILP;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.GenSumBPGGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenSumGPGameState;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.GenSumKuhnPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.pursuit.GenSumPursuitGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.randomgame.GeneralSumRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class StrategyStrengthExperiments {

    public static BufferedWriter neWriter;
    public static BufferedWriter undomWriter;
    public static BufferedWriter qpWriter;
    public static BufferedWriter p1MaxWriter;
    public static BufferedWriter welfareWriter;
    public static BufferedWriter stackWriter;
    public static BufferedWriter brWriter;
    public static BufferedWriter wrWriter;
    public static BufferedWriter cfrWriter;
    public static BufferedWriter mctsWriter;

    /**
     * bf, depth, corr, observations, seed
     *
     * @param args
     */
    public static void main(String[] args) {
//        runGenSumKuhnPoker(0.1);
//        runGenSumBPG(Integer.parseInt(args[0]));
//        runGenSumGenericPoker(0.1);
//        runGenSumPursuit(1);
        runGenSumRandomGames(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Double.parseDouble(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
    }

    private static void runGenSumPursuit(int depth) {
        PursuitGameInfo.depth = depth;
        GameState root = new GenSumPursuitGameState();
        GameInfo info = new PursuitGameInfo();
        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new PursuitExpander<>(algConfig);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, info, algConfig);

        builder.generateCompleteGame();

        GenSumSequenceFormMILP undomSolver = new UndomGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root, expander, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP p1MaxSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
        GenSumSequenceFormMILP welfareSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0], root.getAllPlayers()[1]);
        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;

        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
        SolverResult undomResult = undomSolver.compute();
        SolverResult neResult = neSolver.compute();
        SolverResult p1MaxResult = p1MaxSolver.compute();
        SolverResult welfareResult = welfareSolver.compute();
        MCTSConfig mctsP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> mctsP1Expander = new RandomGameExpander<>(mctsP1Config);
        MCTSConfig cfrP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> cfrP1Expander = new RandomGameExpander<>(cfrP1Config);

        Map<Sequence, Double> cfrP1RealPlan = getCFRStrategy(root, cfrP1Expander);
        Map<Sequence, Double> mctsP1RealPlan = getMCTSStrategy(root, mctsP1Expander, info);

        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
        Expander<SequenceInformationSet> stackelbergExpander = new PursuitExpander<>(stackelbergConfig);
        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver);


        if (qpResult != null && undomResult != null && neResult != null) {
            StackelbergConfig stackConfig = new StackelbergConfig(root);
            Expander<SequenceInformationSet> stackExpander = new PursuitExpander<>(stackConfig);
            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);

            stackBuilder.generateCompleteGame();
            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> mctsExpander = new PursuitExpander<>(mctsConfig);

            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> cfrExpander = new PursuitExpander<>(cfrConfig);
//            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
            evaluateAgainstWRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateAgainstBRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstMCTS(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstCFR(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstQRE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
        }
    }

    private static void runGenSumBPG(int depth) {
        BPGGameInfo.DEPTH = depth;
        GameState root = new GenSumBPGGameState();
        GameInfo info = new BPGGameInfo();
        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new BPGExpander<>(algConfig);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, info, algConfig);

        builder.generateCompleteGame();

        GenSumSequenceFormMILP undomSolver = new UndomGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root, expander, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP p1MaxSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
        GenSumSequenceFormMILP welfareSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0], root.getAllPlayers()[1]);
        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;

        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
        SolverResult undomResult = undomSolver.compute();
        SolverResult neResult = neSolver.compute();
        SolverResult p1MaxResult = p1MaxSolver.compute();
        SolverResult welfareResult = welfareSolver.compute();
        MCTSConfig mctsP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> mctsP1Expander = new RandomGameExpander<>(mctsP1Config);
        MCTSConfig cfrP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> cfrP1Expander = new RandomGameExpander<>(cfrP1Config);

        Map<Sequence, Double> cfrP1RealPlan = getCFRStrategy(root, cfrP1Expander);
        Map<Sequence, Double> mctsP1RealPlan = getMCTSStrategy(root, mctsP1Expander, info);

        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
        Expander<SequenceInformationSet> stackelbergExpander = new BPGExpander<>(stackelbergConfig);
        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver);


        if (qpResult != null && undomResult != null && neResult != null) {
            StackelbergConfig stackConfig = new StackelbergConfig(root);
            Expander<SequenceInformationSet> stackExpander = new BPGExpander<>(stackConfig);
            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);

            stackBuilder.generateCompleteGame();
            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> mctsExpander = new BPGExpander<>(mctsConfig);

            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> cfrExpander = new BPGExpander<>(cfrConfig);
//            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
            evaluateAgainstWRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateAgainstBRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstMCTS(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstCFR(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstQRE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
        }
    }


    private static void runGenSumKuhnPoker(double rake) {
        GameState root = new GenSumKuhnPokerGameState();
        GameInfo info = new KPGameInfo();
        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new KuhnPokerExpander<>(algConfig);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, info, algConfig);

        builder.generateCompleteGame();

        GenSumSequenceFormMILP undomSolver = new UndomGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root, expander, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP p1MaxSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
        GenSumSequenceFormMILP welfareSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0], root.getAllPlayers()[1]);
        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;

        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
        SolverResult undomResult = undomSolver.compute();
        SolverResult neResult = neSolver.compute();
        SolverResult p1MaxResult = p1MaxSolver.compute();
        SolverResult welfareResult = welfareSolver.compute();
        MCTSConfig mctsP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> mctsP1Expander = new RandomGameExpander<>(mctsP1Config);
        MCTSConfig cfrP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> cfrP1Expander = new RandomGameExpander<>(cfrP1Config);

        Map<Sequence, Double> cfrP1RealPlan = getCFRStrategy(root, cfrP1Expander);
        Map<Sequence, Double> mctsP1RealPlan = getMCTSStrategy(root, mctsP1Expander, info);

        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
        Expander<SequenceInformationSet> stackelbergExpander = new KuhnPokerExpander<>(stackelbergConfig);
        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver);


        if (qpResult != null && undomResult != null && neResult != null) {
            StackelbergConfig stackConfig = new StackelbergConfig(root);
            Expander<SequenceInformationSet> stackExpander = new KuhnPokerExpander<>(stackConfig);
            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);

            stackBuilder.generateCompleteGame();
            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> mctsExpander = new KuhnPokerExpander<>(mctsConfig);

            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> cfrExpander = new KuhnPokerExpander<>(cfrConfig);
//            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
            evaluateAgainstWRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateAgainstBRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstMCTS(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstCFR(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstQRE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
        }
    }

    private static void runGenSumGenericPoker(double rake) {
        GameState root = new GenSumGPGameState();
        GameInfo info = new GPGameInfo();
        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new GenericPokerExpander<>(algConfig);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, info, algConfig);

        builder.generateCompleteGame();

        GenSumSequenceFormMILP undomSolver = new UndomGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root, expander, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP p1MaxSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
        GenSumSequenceFormMILP welfareSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0], root.getAllPlayers()[1]);
        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;

        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
        SolverResult undomResult = undomSolver.compute();
        SolverResult neResult = neSolver.compute();
        SolverResult p1MaxResult = p1MaxSolver.compute();
        SolverResult welfareResult = welfareSolver.compute();
        MCTSConfig mctsP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> mctsP1Expander = new RandomGameExpander<>(mctsP1Config);
        MCTSConfig cfrP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> cfrP1Expander = new RandomGameExpander<>(cfrP1Config);

        Map<Sequence, Double> cfrP1RealPlan = getCFRStrategy(root, cfrP1Expander);
        Map<Sequence, Double> mctsP1RealPlan = getMCTSStrategy(root, mctsP1Expander, info);
        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
        Expander<SequenceInformationSet> stackelbergExpander = new GenericPokerExpander<>(stackelbergConfig);
        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver);


        if (qpResult != null && undomResult != null && neResult != null) {
            StackelbergConfig stackConfig = new StackelbergConfig(root);
            Expander<SequenceInformationSet> stackExpander = new GenericPokerExpander<>(stackConfig);
            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);

            stackBuilder.generateCompleteGame();
            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> mctsExpander = new GenericPokerExpander<>(mctsConfig);

            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> cfrExpander = new GenericPokerExpander<>(cfrConfig);
//            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
            evaluateAgainstWRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateAgainstBRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstMCTS(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstCFR(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstQRE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
        }
    }

    private static Player[] getActingPlayers(GameState root) {
        return new Player[]{root.getAllPlayers()[0], root.getAllPlayers()[1]};
    }


    private static void runGenSumRandomGames(int bf, int depth, double correlation, int observations, int seed) {
        RandomGameInfo.seed = seed;
        RandomGameInfo.MAX_BF = bf;
        RandomGameInfo.MAX_DEPTH = depth;
        RandomGameInfo.CORRELATION = correlation;
        RandomGameInfo.MAX_OBSERVATION = observations;
        GeneralSumRandomGameState root = new GeneralSumRandomGameState();
        GameInfo info = new RandomGameInfo();
        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new RandomGameExpander<>(algConfig);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, info, algConfig);

        builder.generateCompleteGame();

        GenSumSequenceFormMILP undomSolver = new UndomGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root, expander, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP p1MaxSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
        GenSumSequenceFormMILP welfareSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0], root.getAllPlayers()[1]);
        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;

        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "GenSumRndGameRepr");
        SolverResult undomResult = undomSolver.compute();
        SolverResult neResult = neSolver.compute();
        SolverResult p1MaxResult = p1MaxSolver.compute();
        SolverResult welfareResult = welfareSolver.compute();
        MCTSConfig mctsP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> mctsP1Expander = new RandomGameExpander<>(mctsP1Config);
        MCTSConfig cfrP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> cfrP1Expander = new RandomGameExpander<>(cfrP1Config);

        Map<Sequence, Double> cfrP1RealPlan = getCFRStrategy(root, cfrP1Expander);
        Map<Sequence, Double> mctsP1RealPlan = getMCTSStrategy(root, mctsP1Expander, info);

        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
        Expander<SequenceInformationSet> stackelbergExpander = new RandomGameExpander<>(stackelbergConfig);
        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(root.getAllPlayers(), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver);


        if (qpResult != null && undomResult != null && neResult != null) {
            StackelbergConfig stackConfig = new StackelbergConfig(root);
            Expander<SequenceInformationSet> stackExpander = new RandomGameExpander<>(stackConfig);
            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);

            stackBuilder.generateCompleteGame();
            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> mctsExpander = new RandomGameExpander<>(mctsConfig);

            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> cfrExpander = new RandomGameExpander<>(cfrConfig);
//            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
            evaluateAgainstWRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateAgainstBRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstMCTS(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstCFR(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstQRE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
        }
    }

    private static Map<Sequence, Double> getMCTSStrategy(GameState root, Expander<MCTSInformationSet> expander, GameInfo info) {
        ISMCTSExploitability.rootState = root;
        ISMCTSExploitability.expander = expander;
        BackPropFactory factory = new UCTBackPropFactory(Math.sqrt(2) * info.getMaxUtility());
        GenSumISMCTSNestingRunner.alg = new GenSumISMCTSAlgorithm(root.getAllPlayers()[1], new DefaultSimulator(expander), factory, root, expander);
//            GenSumISMCTSAlgorithm mcts = new GenSumISMCTSAlgorithm(root.getAllPlayers()[1], new DefaultSimulator(expander), new UCTBackPropFactory(Math.sqrt(2) * info.getMaxUtility()), root, expander);
//        GenSumISMCTSNestingRunner.alg.runMiliseconds(300);
//        GenSumISMCTSNestingRunner.buildStichedStrategy(root.getAllPlayers()[1], GenSumISMCTSNestingRunner.alg.getRootNode().getInformationSet(),
//                GenSumISMCTSNestingRunner.alg.getRootNode(), 50);
//        Strategy strategy = StrategyCollector.getStrategyFor(GenSumISMCTSNestingRunner.alg.getRootNode(), root.getAllPlayers()[1], new MeanStratDist());
//
//        if (strategy.size() <= 2) {
//            GenSumISMCTSNestingRunner.alg.runMiliseconds(150);
//            GenSumISMCTSNestingRunner.buildStichedStrategy(root.getAllPlayers()[1], GenSumISMCTSNestingRunner.alg.getRootNode().getInformationSet(),
//                    GenSumISMCTSNestingRunner.alg.getRootNode(), 50);
//
//            strategy = StrategyCollector.getStrategyFor(GenSumISMCTSNestingRunner.alg.getRootNode(), root.getAllPlayers()[1], new MeanStratDist());
//            if (strategy.size() <= 2) {
//                GenSumISMCTSNestingRunner.alg.runMiliseconds(150);
//            }
//        }
        buildMCTSCompleteTree(GenSumISMCTSNestingRunner.alg.getRootNode(), factory);
        InnerNode rootNode =  GenSumISMCTSNestingRunner.alg.getRootNode();
//        GenSumISMCTSNestingRunner.alg.runMiliseconds(300);
        GenSumISMCTSNestingRunner.buildStichedStrategy(root.getAllPlayers()[0], GenSumISMCTSNestingRunner.alg.getRootNode().getInformationSet(),
                GenSumISMCTSNestingRunner.alg.getRootNode(), 100000);
        GenSumISMCTSNestingRunner.alg.setCurrentIS(rootNode.getInformationSet());
        return StrategyCollector.getStrategyFor(GenSumISMCTSNestingRunner.alg.getRootNode(), root.getAllPlayers()[0], new MeanStratDist());
    }

    private static Map<Sequence, Double> getCFRStrategy(GameState root, Expander<MCTSInformationSet> expander) {
        CFRAlgorithm cfr = new CFRAlgorithm(root.getAllPlayers()[0], root, expander);

        buildCFRCompleteTree(cfr.getRootNode());
        cfr.runIterations(100000);
        return StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[0], new MeanStratDist());
    }

    private static void evaluateAgainstBRNE(SolverResult neResult, SolverResult undomResult, SolverResult qpResult, SolverResult p1MaxResult, SolverResult welfareResult, Map<Player, Map<Sequence, Double>> stackResult, Map<Sequence, Double> cfrP1RealPlan, Map<Sequence, Double> mctsP1RealPlan, GameState root, Expander<SequenceInformationSet> expander, GameInfo info, GenSumSequenceFormConfig algConfig, Player player) {
        try {
            neWriter = new BufferedWriter(new FileWriter("P1NEvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            undomWriter = new BufferedWriter(new FileWriter("P1UndomvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            qpWriter = new BufferedWriter(new FileWriter("P1QPvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            p1MaxWriter = new BufferedWriter(new FileWriter("P1MNEvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            welfareWriter = new BufferedWriter(new FileWriter("P1WNEvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            stackWriter = new BufferedWriter(new FileWriter("P1StackvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            cfrWriter = new BufferedWriter(new FileWriter("P1CFRvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            mctsWriter = new BufferedWriter(new FileWriter("P1MCTSvsBRNEExpVal" + getDomainDependentString() + ".csv", true));

            SolverResult vsNeSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], neResult.p1RealPlan).compute();
            SolverResult vsUndomSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], undomResult.p1RealPlan).compute();
            SolverResult vsQPSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], qpResult.p1RealPlan).compute();
            SolverResult vsStackSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], stackResult.get(root.getAllPlayers()[0])).compute();
            SolverResult vsp1maxSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], p1MaxResult.p1RealPlan).compute();
            SolverResult vswelfareSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], welfareResult.p1RealPlan).compute();
            SolverResult vsCfrSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], cfrP1RealPlan).compute();
            SolverResult vsMctsSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], mctsP1RealPlan).compute();

            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsBRNEvsNEExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsBRNEvsNEExpVal" + getDomainDependentString() + ".csv", true)),
                    neWriter, neResult.p1RealPlan, vsNeSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsBRNEvsUndomExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsBRNEvsUndomExpVal" + getDomainDependentString() + ".csv", true)),
                    undomWriter, undomResult.p1RealPlan, vsUndomSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsBRNEvsQPExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsBRNEvsQPExpVal" + getDomainDependentString() + ".csv", true)),
                    qpWriter, qpResult.p1RealPlan, vsQPSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsBRNEvsMNEExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsBRNEvsMNEExpVal" + getDomainDependentString() + ".csv", true)),
                    p1MaxWriter, p1MaxResult.p1RealPlan, vsp1maxSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsBRNEvsWNEExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsBRNEvsWNEExpVal" + getDomainDependentString() + ".csv", true)),
                    welfareWriter, welfareResult.p1RealPlan, vswelfareSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsBRNEvsStackExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsBRNEvsStackExpVal" + getDomainDependentString() + ".csv", true)),
                    stackWriter, stackResult.get(root.getAllPlayers()[0]), vsStackSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsBRNEvsCFRExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsBRNEvsCFRExpVal" + getDomainDependentString() + ".csv", true)),
                    cfrWriter, cfrP1RealPlan, vsCfrSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsBRNEvsMCTSExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsBRNEvsMCTSExpVal" + getDomainDependentString() + ".csv", true)),
                    mctsWriter, mctsP1RealPlan, vsMctsSolverResult.p2RealPlan, root, expander, info, algConfig);
            neWriter.newLine();
            undomWriter.newLine();
            qpWriter.newLine();
            p1MaxWriter.newLine();
            welfareWriter.newLine();
            stackWriter.newLine();
            cfrWriter.newLine();
            mctsWriter.newLine();

            neWriter.close();
            undomWriter.close();
            qpWriter.close();
            p1MaxWriter.close();
            welfareWriter.close();
            stackWriter.close();
            cfrWriter.close();
            mctsWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void evaluateAgainstWRNE(SolverResult neResult, SolverResult undomResult, SolverResult qpResult, SolverResult p1MaxResult, SolverResult welfareResult, Map<Player, Map<Sequence, Double>> stackResult, Map<Sequence, Double> cfrP1RealPlan, Map<Sequence, Double> mctsP1RealPlan, GameState root, Expander<SequenceInformationSet> expander, GameInfo info, GenSumSequenceFormConfig algConfig, Player player) {
        try {
            neWriter = new BufferedWriter(new FileWriter("P1NEvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            undomWriter = new BufferedWriter(new FileWriter("P1UndomvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            qpWriter = new BufferedWriter(new FileWriter("P1QPvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            p1MaxWriter = new BufferedWriter(new FileWriter("P1MNEvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            welfareWriter = new BufferedWriter(new FileWriter("P1WNEvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            stackWriter = new BufferedWriter(new FileWriter("P1StackvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            cfrWriter = new BufferedWriter(new FileWriter("P1CFRvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            mctsWriter = new BufferedWriter(new FileWriter("P1MCTSvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            SolverResult vsNeSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], neResult.p1RealPlan).compute();
            SolverResult vsUndomSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], undomResult.p1RealPlan).compute();
            SolverResult vsQPSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], qpResult.p1RealPlan).compute();
            SolverResult vsStackSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], stackResult.get(root.getAllPlayers()[0])).compute();
            SolverResult vsp1maxSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], p1MaxResult.p1RealPlan).compute();
            SolverResult vswelfareSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], welfareResult.p1RealPlan).compute();
            SolverResult vsCfrSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], cfrP1RealPlan).compute();
            SolverResult vsMctsSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], mctsP1RealPlan).compute();

            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsWRNEvsNEExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsWRNEvsNEExpVal" + getDomainDependentString() + ".csv", true)),
                    neWriter, neResult.p1RealPlan, vsNeSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsWRNEvsUndomExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsWRNEvsUndomExpVal" + getDomainDependentString() + ".csv", true)),
                    undomWriter, undomResult.p1RealPlan, vsUndomSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsWRNEvsQPExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsWRNEvsQPExpVal" + getDomainDependentString() + ".csv", true)),
                    qpWriter, qpResult.p1RealPlan, vsQPSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsWRNEvsP1MNEExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsWRNEvsP1MNEExpVal" + getDomainDependentString() + ".csv", true)),
                    p1MaxWriter, p1MaxResult.p1RealPlan, vsp1maxSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsWRNEvsWNEExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsWRNEvsWNEExpVal" + getDomainDependentString() + ".csv", true)),
                    welfareWriter, welfareResult.p1RealPlan, vswelfareSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsWRNEvsStackExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsWRNEvsStackExpVal" + getDomainDependentString() + ".csv", true)),
                    stackWriter, stackResult.get(root.getAllPlayers()[0]), vsStackSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsWRNEvsCFRExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsWRNEvsCFRExpVal" + getDomainDependentString() + ".csv", true)),
                    cfrWriter, cfrP1RealPlan, vsCfrSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsWRNEvsMCTSExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsWRNEvsMCTSExpVal" + getDomainDependentString() + ".csv", true)),
                    mctsWriter, mctsP1RealPlan, vsMctsSolverResult.p2RealPlan, root, expander, info, algConfig);

            neWriter.newLine();
            undomWriter.newLine();
            qpWriter.newLine();
            p1MaxWriter.newLine();
            welfareWriter.newLine();
            stackWriter.newLine();
            cfrWriter.newLine();
            mctsWriter.newLine();

            neWriter.close();
            undomWriter.close();
            qpWriter.close();
            p1MaxWriter.close();
            welfareWriter.close();
            stackWriter.close();
            cfrWriter.close();
            mctsWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void evaluateBRWR(BufferedWriter brWriter, BufferedWriter wrWriter, BufferedWriter writer, Map<Sequence, Double> p1Strategy, Map<Sequence, Double> p2Strategy, GameState root, Expander<SequenceInformationSet> expander, GameInfo info, GenSumSequenceFormConfig algConfig) throws IOException {
        GeneralSumWorstResponse wr = new GeneralSumWorstResponse(expander, 0, getActingPlayers(root), algConfig, info);
        GeneralSumBestResponse br = new GeneralSumBestResponse(expander, 0, getActingPlayers(root), algConfig, info);

        br.calculateBR(root, p2Strategy);
        wr.calculateBR(root, p2Strategy);
//        brWriter = new BufferedWriter(new FileWriter("P1BRvsWRNEvsStackExpVal" + getDomainDependentString() + ".csv", true));
        write(brWriter, computeExpectedValue(br.getBRStategy(), p2Strategy, root, expander));
        brWriter.newLine();
        brWriter.close();
//        wrWriter = new BufferedWriter(new FileWriter("P1WRvsWRNEvsStackExpVal" + getDomainDependentString() + ".csv", true));
        write(wrWriter, computeExpectedValue(wr.getBRStategy(), p2Strategy, root, expander));
        wrWriter.newLine();
        wrWriter.close();
        write(writer, computeExpectedValue(p1Strategy, p2Strategy, root, expander));
    }

    private static void evaluateP1StrategiesAgainstQRE(SolverResult neResult, SolverResult undomResult, SolverResult qpResult, SolverResult p1MaxResult, SolverResult welfareResult, Map<Player, Map<Sequence, Double>> stackResult,
                                                       Map<Sequence, Double> cfrP1RealPlan, Map<Sequence, Double> mctsP1RealPlan, GameState root, Expander<SequenceInformationSet> expander, GameInfo info, SequenceFormConfig<SequenceInformationSet> algConfig, Player player) {
        try {
            neWriter = new BufferedWriter(new FileWriter("P1NEvsQREExpVal" + getDomainDependentString() + ".csv", true));
            undomWriter = new BufferedWriter(new FileWriter("P1UndomvsQREExpVal" + getDomainDependentString() + ".csv", true));
            qpWriter = new BufferedWriter(new FileWriter("P1QPvsQREExpVal" + getDomainDependentString() + ".csv", true));
            p1MaxWriter = new BufferedWriter(new FileWriter("P1MNEvsQREExpVal" + getDomainDependentString() + ".csv", true));
            welfareWriter = new BufferedWriter(new FileWriter("P1WNEvsQREExpVal" + getDomainDependentString() + ".csv", true));
            stackWriter = new BufferedWriter(new FileWriter("P1StackvsQREExpVal" + getDomainDependentString() + ".csv", true));
            brWriter = new BufferedWriter(new FileWriter("P1BRvsQREExpVal" + getDomainDependentString() + ".csv", true));
            wrWriter = new BufferedWriter(new FileWriter("P1WRvsQREExpVal" + getDomainDependentString() + ".csv", true));
            cfrWriter = new BufferedWriter(new FileWriter("P1CFRvsQREExpVal" + getDomainDependentString() + ".csv", true));
            mctsWriter = new BufferedWriter(new FileWriter("P1MCTSvsQREExpVal" + getDomainDependentString() + ".csv", true));

            QRESolver solver = new QRESolver(root, expander, info, algConfig);
            QREResult qreResult = solver.solve();

            writeLambdas(neWriter, qreResult.lambdas);
            writeLambdas(undomWriter, qreResult.lambdas);
            writeLambdas(qpWriter, qreResult.lambdas);
            writeLambdas(p1MaxWriter, qreResult.lambdas);
            writeLambdas(welfareWriter, qreResult.lambdas);
            writeLambdas(stackWriter, qreResult.lambdas);
            writeLambdas(brWriter, qreResult.lambdas);
            writeLambdas(wrWriter, qreResult.lambdas);
            writeLambdas(mctsWriter, qreResult.lambdas);
            writeLambdas(cfrWriter, qreResult.lambdas);
            for (Map<Player, Map<Sequence, Double>> quantalResponse : qreResult.quantalResponses) {
                GeneralSumBestResponse br = new GeneralSumBestResponse(expander, 0, getActingPlayers(root), algConfig, info);
                GeneralSumWorstResponse wr = new GeneralSumWorstResponse(expander, 0, getActingPlayers(root), algConfig, info);
                Map<Sequence, Double> qreStrategy = filterLow(quantalResponse.get(player), 1e-6);

                br.calculateBR(root, quantalResponse.get(player));
                wr.calculateBR(root, quantalResponse.get(player));
                write(brWriter, computeExpectedValue(br.getBRStategy(), qreStrategy, root, expander));
                write(wrWriter, computeExpectedValue(wr.getBRStategy(), qreStrategy, root, expander));
                write(neWriter, computeExpectedValue(neResult.p1RealPlan, qreStrategy, root, expander));
                write(undomWriter, computeExpectedValue(undomResult.p1RealPlan, qreStrategy, root, expander));
                write(qpWriter, computeExpectedValue(qpResult.p1RealPlan, qreStrategy, root, expander));
                write(p1MaxWriter, computeExpectedValue(p1MaxResult.p1RealPlan, qreStrategy, root, expander));
                write(welfareWriter, computeExpectedValue(welfareResult.p1RealPlan, qreStrategy, root, expander));
                write(stackWriter, computeExpectedValue(stackResult.get(root.getAllPlayers()[0]), qreStrategy, root, expander));
                write(cfrWriter, computeExpectedValue(cfrP1RealPlan, qreStrategy, root, expander));
                write(mctsWriter, computeExpectedValue(mctsP1RealPlan, qreStrategy, root, expander));
            }
            neWriter.newLine();
            undomWriter.newLine();
            qpWriter.newLine();
            p1MaxWriter.newLine();
            welfareWriter.newLine();
            stackWriter.newLine();
            brWriter.newLine();
            wrWriter.newLine();
            cfrWriter.newLine();
            mctsWriter.newLine();

            neWriter.close();
            undomWriter.close();
            qpWriter.close();
            p1MaxWriter.close();
            welfareWriter.close();
            stackWriter.close();
            brWriter.close();
            wrWriter.close();
            cfrWriter.close();
            mctsWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<Sequence, Double> filterLow(Map<Sequence, Double> realPlan, double filter) {
        Iterator<Map.Entry<Sequence, Double>> iterator = realPlan.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Sequence, Double> entry = iterator.next();

            if (entry.getValue() < filter)
                iterator.remove();
        }
        return realPlan;
    }

    private static void evaluateP1StrategiesAgainstCFR(SolverResult neResult, SolverResult undomResult, SolverResult qpResult, SolverResult p1MaxResult, SolverResult welfareResult, Map<Player, Map<Sequence, Double>> stackResult,
                                                       Map<Sequence, Double> cfrP1RealPlan, Map<Sequence, Double> mctsP1RealPlan, GameState root, Expander<MCTSInformationSet> expander, GameInfo info, MCTSConfig algConfig, Player player) {
        try {
            neWriter = new BufferedWriter(new FileWriter("P1NEvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            undomWriter = new BufferedWriter(new FileWriter("P1UndomvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            qpWriter = new BufferedWriter(new FileWriter("P1QPvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            p1MaxWriter = new BufferedWriter(new FileWriter("P1MNEvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            welfareWriter = new BufferedWriter(new FileWriter("P1WNEvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            stackWriter = new BufferedWriter(new FileWriter("P1StackvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            brWriter = new BufferedWriter(new FileWriter("P1BRvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            wrWriter = new BufferedWriter(new FileWriter("P1WRvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            cfrWriter = new BufferedWriter(new FileWriter("P1CFRvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            mctsWriter = new BufferedWriter(new FileWriter("P1MCTSvsCFRExpVal" + getDomainDependentString() + ".csv", true));

            CFRAlgorithm cfr = new CFRAlgorithm(root.getAllPlayers()[1], root, expander);

            buildCFRCompleteTree(cfr.getRootNode());
            for (int i = 0; i < 500; i++) {
                cfr.runIterations(20);
                Strategy strategy = StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[1], new MeanStratDist());
                GeneralSumBestResponse br = new GeneralSumBestResponse(expander, 0, getActingPlayers(root), algConfig, info);
                GeneralSumWorstResponse wr = new GeneralSumWorstResponse(expander, 0, getActingPlayers(root), algConfig, info);

                br.calculateBR(root, strategy);
                wr.calculateBR(root, strategy);
                write(neWriter, computeExpectedValue(neResult.p1RealPlan, strategy, root, expander));
                write(undomWriter, computeExpectedValue(undomResult.p1RealPlan, strategy, root, expander));
                write(qpWriter, computeExpectedValue(qpResult.p1RealPlan, strategy, root, expander));
                write(p1MaxWriter, computeExpectedValue(p1MaxResult.p1RealPlan, strategy, root, expander));
                write(welfareWriter, computeExpectedValue(welfareResult.p1RealPlan, strategy, root, expander));
                write(stackWriter, computeExpectedValue(stackResult.get(root.getAllPlayers()[0]), strategy, root, expander));
                write(brWriter, computeExpectedValue(br.getBRStategy(), strategy, root, expander));
                write(wrWriter, computeExpectedValue(wr.getBRStategy(), strategy, root, expander));
                write(cfrWriter, computeExpectedValue(cfrP1RealPlan, strategy, root, expander));
                write(mctsWriter, computeExpectedValue(mctsP1RealPlan, strategy, root, expander));
            }
            neWriter.newLine();
            undomWriter.newLine();
            qpWriter.newLine();
            p1MaxWriter.newLine();
            welfareWriter.newLine();
            stackWriter.newLine();
            brWriter.newLine();
            wrWriter.newLine();
            cfrWriter.newLine();
            mctsWriter.newLine();

            neWriter.close();
            undomWriter.close();
            qpWriter.close();
            p1MaxWriter.close();
            welfareWriter.close();
            stackWriter.close();
            brWriter.close();
            wrWriter.close();
            cfrWriter.close();
            mctsWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void evaluateP1StrategiesAgainstMCTS(SolverResult neResult, SolverResult undomResult, SolverResult qpResult, SolverResult p1MaxResult, SolverResult welfareResult, Map<Player, Map<Sequence, Double>> stackResult,
                                                        Map<Sequence, Double> cfrP1RealPlan, Map<Sequence, Double> mctsP1RealPlan, GameState root, Expander<MCTSInformationSet> expander, GameInfo info, MCTSConfig algConfig, Player player) {
        try {
            neWriter = new BufferedWriter(new FileWriter("P1NEvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            undomWriter = new BufferedWriter(new FileWriter("P1UndomvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            qpWriter = new BufferedWriter(new FileWriter("P1QPvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            p1MaxWriter = new BufferedWriter(new FileWriter("P1MNEvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            welfareWriter = new BufferedWriter(new FileWriter("P1WNEvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            stackWriter = new BufferedWriter(new FileWriter("P1StackvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            brWriter = new BufferedWriter(new FileWriter("P1BRvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            wrWriter = new BufferedWriter(new FileWriter("P1WRvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            cfrWriter = new BufferedWriter(new FileWriter("P1CFRvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            mctsWriter = new BufferedWriter(new FileWriter("P1MCTSvsMCTSExpVal" + getDomainDependentString() + ".csv", true));

            ISMCTSExploitability.rootState = root;
            ISMCTSExploitability.expander = expander;
            ISMCTSExploitability.gameInfo = info;
            BackPropFactory factory = new UCTBackPropFactory(Math.sqrt(2) * info.getMaxUtility());
            GenSumISMCTSNestingRunner.alg = new GenSumISMCTSAlgorithm(root.getAllPlayers()[1], new DefaultSimulator(expander), factory, root, expander);
            buildMCTSCompleteTree(GenSumISMCTSNestingRunner.alg.getRootNode(), factory);
            InnerNode rootNode = GenSumISMCTSNestingRunner.alg.getRootNode();
//            GenSumISMCTSAlgorithm mcts = new GenSumISMCTSAlgorithm(root.getAllPlayers()[1], new DefaultSimulator(expander), new UCTBackPropFactory(Math.sqrt(2) * info.getMaxUtility()), root, expander);
            for (int i = 0; i < 500; i++) {
                GenSumISMCTSNestingRunner.buildStichedStrategy(root.getAllPlayers()[1], GenSumISMCTSNestingRunner.alg.getRootNode().getInformationSet(),
                        GenSumISMCTSNestingRunner.alg.getRootNode(), 50);
                GenSumISMCTSNestingRunner.alg.setCurrentIS(rootNode.getInformationSet());
                Strategy strategy = StrategyCollector.getStrategyFor(GenSumISMCTSNestingRunner.alg.getRootNode(), root.getAllPlayers()[1], new MeanStratDist());

                GeneralSumBestResponse br = new GeneralSumBestResponse(expander, 0, getActingPlayers(root), algConfig, info);
                GeneralSumWorstResponse wr = new GeneralSumWorstResponse(expander, 0, getActingPlayers(root), algConfig, info);

                br.calculateBR(root, strategy);
                wr.calculateBR(root, strategy);
                write(neWriter, computeExpectedValue(neResult.p1RealPlan, strategy, root, expander));
                write(undomWriter, computeExpectedValue(undomResult.p1RealPlan, strategy, root, expander));
                write(qpWriter, computeExpectedValue(qpResult.p1RealPlan, strategy, root, expander));
                write(p1MaxWriter, computeExpectedValue(p1MaxResult.p1RealPlan, strategy, root, expander));
                write(welfareWriter, computeExpectedValue(welfareResult.p1RealPlan, strategy, root, expander));
                write(stackWriter, computeExpectedValue(stackResult.get(root.getAllPlayers()[0]), strategy, root, expander));
                write(brWriter, computeExpectedValue(br.getBRStategy(), strategy, root, expander));
                write(wrWriter, computeExpectedValue(wr.getBRStategy(), strategy, root, expander));
                write(cfrWriter, computeExpectedValue(cfrP1RealPlan, strategy, root, expander));
                write(mctsWriter, computeExpectedValue(mctsP1RealPlan, strategy, root, expander));
            }

            neWriter.newLine();
            undomWriter.newLine();
            qpWriter.newLine();
            p1MaxWriter.newLine();
            welfareWriter.newLine();
            stackWriter.newLine();
            brWriter.newLine();
            wrWriter.newLine();
            cfrWriter.newLine();
            mctsWriter.newLine();


            neWriter.close();
            undomWriter.close();
            qpWriter.close();
            p1MaxWriter.close();
            welfareWriter.close();
            stackWriter.close();
            brWriter.close();
            wrWriter.close();
            cfrWriter.close();
            mctsWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void buildCFRCompleteTree(InnerNode r) {
        System.out.println("Building complete tree.");
        int nodes = 0, infosets = 0;
        ArrayDeque<InnerNode> q = new ArrayDeque<InnerNode>();
        q.add(r);
        while (!q.isEmpty()) {
            nodes++;
            InnerNode n = q.removeFirst();
            MCTSInformationSet is = n.getInformationSet();
            if (!(n instanceof ChanceNode))
                if (is.getAlgorithmData() == null) {
                    infosets++;
                    is.setAlgorithmData(new OOSAlgorithmData(n.getActions()));
                }
            for (Action a : n.getActions()) {
                Node ch = n.getChildFor(a);
                if (ch instanceof InnerNode) {
                    q.add((InnerNode) ch);
                }
            }
        }
        System.out.println("Created nodes: " + nodes + "; infosets: " + infosets);
    }

    public static void buildMCTSCompleteTree(InnerNode r, BackPropFactory factory) {
        System.out.println("Building complete tree.");
        int nodes = 0, infosets = 0;
        ArrayDeque<InnerNode> q = new ArrayDeque<InnerNode>();
        q.add(r);
        while (!q.isEmpty()) {
            nodes++;
            InnerNode n = q.removeFirst();
            MCTSInformationSet is = n.getInformationSet();
            if (!(n instanceof ChanceNode))
                if (is.getAlgorithmData() == null) {
                    infosets++;
                    is.setAlgorithmData(factory.createSelector(n.getActions()));
                }
            for (Action a : n.getActions()) {
                Node ch = n.getChildFor(a);
                if (ch instanceof InnerNode) {
                    q.add((InnerNode) ch);
                }
            }
        }
        System.out.println("Created nodes: " + nodes + "; infosets: " + infosets);
    }

    private static void writeLambdas(BufferedWriter writer, List<Double> lambdas) throws IOException {
        writer.write(lambdas.get(0) + "");
        for (int i = 1; i < lambdas.size(); i++) {
            writer.write(", " + lambdas.get(i));
        }
        writer.newLine();
        writer.flush();
    }
//
//    private static void evaluateP1StrategiesAgainstPureRps(SolverResult neResult, SolverResult undomResult, SolverResult qpResult, SolverResult p1MaxResult, SolverResult welfareResult, Map<Player, Map<Sequence, Double>> stackResult, Iterator<Set<Sequence>> iterator, GameState root, Expander<SequenceInformationSet> expander) {
//        try {
//            neWriter = new BufferedWriter(new FileWriter("P1NEvsPureRPsExpVal" + getDomainDependentString() + ".csv", true));
//            undomWriter = new BufferedWriter(new FileWriter("P1UndomvsPureRPsExpVal" + getDomainDependentString() + ".csv", true));
//            qpWriter = new BufferedWriter(new FileWriter("P1QPvsPureRPsExpVal" + getDomainDependentString() + ".csv", true));
//            p1MaxWriter = new BufferedWriter(new FileWriter("P1MNEvsPureRPsExpVal" + getDomainDependentString() + ".csv", true));
//            welfareWriter = new BufferedWriter(new FileWriter("P1WNEvsPureRPsExpVal" + getDomainDependentString() + ".csv", true));
//            stackWriter = new BufferedWriter(new FileWriter("P1StackvsPureRPsExpVal" + getDomainDependentString() + ".csv", true));
//
//            try {
//                while (true) {
//                    Set<Sequence> pureRP = iterator.next();
//
//                    write(neWriter, computeExpectedValue(neResult.p1RealPlan, toRp(pureRP), root, expander));
//                    write(undomWriter, computeExpectedValue(undomResult.p1RealPlan, toRp(pureRP), root, expander));
//                    write(qpWriter, computeExpectedValue(qpResult.p1RealPlan, toRp(pureRP), root, expander));
//                    write(p1MaxWriter, computeExpectedValue(p1MaxResult.p1RealPlan, toRp(pureRP), root, expander));
//                    write(welfareWriter, computeExpectedValue(welfareResult.p1RealPlan, toRp(pureRP), root, expander));
//                    write(stackWriter, computeExpectedValue(stackResult.get(root.getAllPlayers()[0]), toRp(pureRP), root, expander));
//                }
//            } catch (NoSuchElementException e) {
//            }
//            neWriter.newLine();
//            undomWriter.newLine();
//            qpWriter.newLine();
//            p1MaxWriter.newLine();
//            welfareWriter.newLine();
//            stackWriter.newLine();
//
//            neWriter.close();
//            undomWriter.close();
//            qpWriter.close();
//            p1MaxWriter.close();
//            welfareWriter.close();
//            stackWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private static void write(BufferedWriter writer, double[] expVals) throws IOException {
        writer.write(expVals[0] + ", " + expVals[1] + ", ");
        writer.flush();
    }

    private static Map<Sequence, Double> toRp(Set<Sequence> pureRP) {
        Map<Sequence, Double> realPlan = new HashMap<>(pureRP.size());

        for (Sequence sequence : pureRP) {
            realPlan.put(sequence, 1d);
        }
        return realPlan;
    }

    private static double[] computeExpectedValue(Map<Sequence, Double> p1RealPlan, Map<Sequence, Double> p2RealPlan, GameState root, Expander<? extends InformationSet> expander) {
        if (root.isGameEnd())
            return getWeightedUtilities(p1RealPlan, p2RealPlan, root);
        if (root.isPlayerToMoveNature())
            return expectedValuesForNature(p1RealPlan, p2RealPlan, root, expander);
        double[] expectedUtilities = new double[root.getAllPlayers().length];

        for (Action action : expander.getActions(root)) {
            GameState nextState = root.performAction(action);
            double realizationProb = getRealProb(p1RealPlan, p2RealPlan, nextState.getSequenceFor(root.getPlayerToMove()));

            if (realizationProb > 0) {
                double[] actionUtilities = computeExpectedValue(p1RealPlan, p2RealPlan, nextState, expander);

                for (int i = 0; i < actionUtilities.length; i++) {
                    expectedUtilities[i] += actionUtilities[i];
                }
            }
        }
        return expectedUtilities;
    }

    private static double[] expectedValuesForNature(Map<Sequence, Double> p1RealPlan, Map<Sequence, Double> p2RealPlan, GameState root, Expander<? extends InformationSet> expander) {
        double[] expectedUtilities = new double[root.getAllPlayers().length];

        for (Action action : expander.getActions(root)) {
            double[] actionUtilities = computeExpectedValue(p1RealPlan, p2RealPlan, root.performAction(action), expander);

            for (int i = 0; i < actionUtilities.length; i++) {
                expectedUtilities[i] += actionUtilities[i];
            }
        }
        return expectedUtilities;
    }

    private static double getRealProb(Map<Sequence, Double> p1RealPlan, Map<Sequence, Double> p2RealPlan, Sequence sequence) {
        Double prob;

        if (sequence.getPlayer().getId() == 0)
            prob = p1RealPlan.get(sequence);
        else
            prob = p2RealPlan.get(sequence);
        return prob == null ? 0 : prob;
    }

    private static double[] getWeightedUtilities(Map<Sequence, Double> p1RealPlan, Map<Sequence, Double> p2RealPlan, GameState root) {
        double[] utilities = root.getUtilities();
        double probability = root.getNatureProbability() *
                p1RealPlan.get(root.getSequenceFor(root.getAllPlayers()[0])) * p2RealPlan.get(root.getSequenceFor(root.getAllPlayers()[1]));

        for (int i = 0; i < utilities.length; i++) {
            utilities[i] *= probability;
        }
        return utilities;
    }

    public static String getDomainDependentString() {
        return "RndBF" + RandomGameInfo.MAX_BF + "D" + RandomGameInfo.MAX_DEPTH + "COR" + RandomGameInfo.CORRELATION;
    }
}
