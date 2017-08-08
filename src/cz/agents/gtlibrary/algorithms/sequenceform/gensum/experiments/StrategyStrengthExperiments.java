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
import cz.agents.gtlibrary.algorithms.sequenceform.*;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.*;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.quantalresponse.QREResult;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.quantalresponse.QRESolver;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom.DataBuilder;
import cz.agents.gtlibrary.algorithms.stackelberg.*;
import cz.agents.gtlibrary.algorithms.stackelberg.milp.StackelbergSequenceFormMILP;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameInfo;
import cz.agents.gtlibrary.domain.informeraos.InformerAoSExpander;
import cz.agents.gtlibrary.domain.informeraos.InformerAoSGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.ExtendedGenSumKPGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.ExtendedKuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.randomgame.GeneralSumRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.interfaces.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class StrategyStrengthExperiments {

    public static long mctsTime;
    public static long cfrTime;

    /**
     * bf, depth, corr, observations, seed
     *
     * @param args
     */
    public static void main(String[] args) {
        runIAoS();
//        runGenSumKuhnPoker(Double.parseDouble(args[0]));
//        runGenSumBPG(Integer.parseInt(args[0]));
//        runGenSumGenericPoker(0.1);
//        runGenSumPursuit(1);
//        runGenSumRandomGames(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Double.parseDouble(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
//        runGoofspiel(Integer.parseInt(args[0]));
    }

//    public static void runGenSumPursuit(int depth) {
//        PursuitGameInfo.depth = depth;
//        GameState root = new GenSumPursuitGameState();
//        GameInfo info = new PursuitGameInfo();
//        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
//        Expander<SequenceInformationSet> expander = new PursuitExpander<>(algConfig);
//        FullSequenceEFG builder = new FullSequenceEFG(root, expander, info, algConfig);
//
//        builder.generateCompleteGame();
//
//        GenSumSequenceFormMILP undomSolver = new UndomGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root, expander, root.getAllPlayers()[0]);
//        GenSumSequenceFormMILP p1MaxSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0]);
//        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
//        GenSumSequenceFormMILP welfareSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0], root.getAllPlayers()[1]);
//        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;
//
//        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
//        SolverResult undomResult = undomSolver.compute();
//        SolverResult neResult = neSolver.compute();
//        SolverResult p1MaxResult = p1MaxSolver.compute();
//        SolverResult welfareResult = welfareSolver.compute();
//
//        checkIfNE(root, info, algConfig, expander, qpResult);
//        checkIfNE(root, info, algConfig, expander, undomResult);
//        checkIfNE(root, info, algConfig, expander, neResult);
//        checkIfNE(root, info, algConfig, expander, p1MaxResult);
//        checkIfNE(root, info, algConfig, expander, welfareResult);
//
//        MCTSConfig mctsP1Config = new MCTSConfig(new Random(1));
//        Expander<MCTSInformationSet> mctsP1Expander = new RandomGameExpander<>(mctsP1Config);
//        MCTSConfig cfrP1Config = new MCTSConfig(new Random(1));
//        Expander<MCTSInformationSet> cfrP1Expander = new RandomGameExpander<>(cfrP1Config);
//
//        Map<Sequence, Double> cfrP1RealPlan = getCFRStrategy(root, cfrP1Expander);
//        Map<Sequence, Double> mctsP1RealPlan = getMCTSStrategy(root, mctsP1Expander, info);
//
//        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
//        Expander<SequenceInformationSet> stackelbergExpander = new PursuitExpander<>(stackelbergConfig);
//        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
//        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
//        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver);
//
//
//        if (qpResult != null && undomResult != null && neResult != null) {
//            StackelbergConfig stackConfig = new StackelbergConfig(root);
//            Expander<SequenceInformationSet> stackExpander = new PursuitExpander<>(stackConfig);
//            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);
//
//            stackBuilder.generateCompleteGame();
//            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
//            Expander<MCTSInformationSet> mctsExpander = new PursuitExpander<>(mctsConfig);
//
//            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
//            Expander<MCTSInformationSet> cfrExpander = new PursuitExpander<>(cfrConfig);
////            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
//            evaluateAgainstWRNE(maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//            evaluateAgainstBRNE(maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//            evaluateP1StrategiesAgainstMCTS(maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1], algConfig);
//            evaluateP1StrategiesAgainstCFR(maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1], algConfig);
//            evaluateP1StrategiesAgainstQRE(maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1], algConfig);
//        }
//    }

    public static void checkIfNE(GameState root, GameInfo info, GenSumSequenceFormConfig algConfig, Expander<? extends InformationSet> expander, SolverResult qpResult) {
        double[] values = computeExpectedValue(qpResult.p1RealPlan, qpResult.p2RealPlan, expander, algConfig, root);

        if (Math.abs(values[0] - getBRValue(qpResult.p2RealPlan, root, expander, algConfig, info)) > 1e-2 || Math.abs(values[1] - getBRValue(qpResult.p1RealPlan, root, expander, algConfig, info)) < 1e-2)
            System.out.println("Not NE!!!");
        else
            System.out.println("OK");
    }

//    public static void runGenSumBPG(int depth) {
//        BPGGameInfo.DEPTH = depth;
//        GameState root = new GenSumBPGGameState();
//        GameInfo info = new BPGGameInfo();
//        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
//        Expander<SequenceInformationSet> expander = new BPGExpander<>(algConfig);
//        FullSequenceEFG builder = new FullSequenceEFG(root, expander, info, algConfig);
//
//        builder.generateCompleteGame();
//
//        GenSumSequenceFormMILP undomSolver = new UndomGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root, expander, root.getAllPlayers()[0]);
//        GenSumSequenceFormMILP p1MaxSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0]);
//        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
//        GenSumSequenceFormMILP welfareSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0], root.getAllPlayers()[1]);
//
//        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;
//
//        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
//        SolverResult undomResult = undomSolver.compute();
//        SolverResult neResult = neSolver.compute();
//        SolverResult p1MaxResult = p1MaxSolver.compute();
//        SolverResult welfareResult = welfareSolver.compute();
//        MCTSConfig mctsP1Config = new MCTSConfig(new Random(1));
//        Expander<MCTSInformationSet> mctsP1Expander = new RandomGameExpander<>(mctsP1Config);
//        MCTSConfig cfrP1Config = new MCTSConfig(new Random(1));
//        Expander<MCTSInformationSet> cfrP1Expander = new RandomGameExpander<>(cfrP1Config);
//
//        Map<Sequence, Double> cfrP1RealPlan = getCFRStrategy(root, cfrP1Expander);
//        Map<Sequence, Double> mctsP1RealPlan = getMCTSStrategy(root, mctsP1Expander, info);
//
//        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
//        Expander<SequenceInformationSet> stackelbergExpander = new BPGExpander<>(stackelbergConfig);
//        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
//        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
//        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver);
//
//
//        if (qpResult != null && undomResult != null && neResult != null) {
//            StackelbergConfig stackConfig = new StackelbergConfig(root);
//            Expander<SequenceInformationSet> stackExpander = new BPGExpander<>(stackConfig);
//            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);
//
//            stackBuilder.generateCompleteGame();
//            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
//            Expander<MCTSInformationSet> mctsExpander = new BPGExpander<>(mctsConfig);
//
//            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
//            Expander<MCTSInformationSet> cfrExpander = new BPGExpander<>(cfrConfig);
////            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
//            evaluateAgainstWRNE(maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//            evaluateAgainstBRNE(maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//            evaluateP1StrategiesAgainstMCTS(maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1], algConfig);
//            evaluateP1StrategiesAgainstCFR(maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1], algConfig);
//            evaluateP1StrategiesAgainstQRE(maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1], algConfig);
//        }
//    }


    public static void runGenSumKuhnPoker(double rake) {
        GameState root = new ExtendedGenSumKPGameState(rake);
        GameInfo info = new KPGameInfo();
        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new ExtendedKuhnPokerExpander<>(algConfig);

        generateCompleteGame(root, expander, algConfig, info);
        FullSequenceFormLP lp = new SequenceFormLP(getActingPlayers(root));
        GenSumSequenceFormMILP undomSolver = new UndomGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root, expander, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP p1MaxSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
        GenSumSequenceFormMILP welfareSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, getActingPlayers(root), info, root.getAllPlayers()[0], root.getAllPlayers()[1]);
        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;

        GenSumSequenceFormConfig algConfig1 = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander1 = new ExtendedKuhnPokerExpander<>(algConfig1);
        FullSequenceEFG builder1 = new FullSequenceEFG(root, expander1, info, algConfig1);

        builder1.generateCompleteGame();
        lp.calculateBothPlStrategy(root, algConfig);
        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander1, algConfig1, info, "KuhnPokerRepr");
        SolverResult undomResult = undomSolver.compute();
        SolverResult neResult = neSolver.compute();
        SolverResult p1MaxResult = p1MaxSolver.compute();
        SolverResult welfareResult = welfareSolver.compute();
        SolverResult maximinResult = new SolverResult(lp.getResultStrategiesForPlayer(root.getAllPlayers()[0]), lp.getResultStrategiesForPlayer(root.getAllPlayers()[1]), 0);
//        qpResult.p1RealPlan = filterLow(qpResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
//        undomResult.p1RealPlan = filterLow(undomResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
//        neResult.p1RealPlan = filterLow(neResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
//        p1MaxResult.p1RealPlan = filterLow(p1MaxResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
//        welfareResult.p1RealPlan = filterLow(welfareResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
        checkIfNE(root, info, algConfig, expander, qpResult);
        checkIfNE(root, info, algConfig, expander, undomResult);
        checkIfNE(root, info, algConfig, expander, neResult);
        checkIfNE(root, info, algConfig, expander, p1MaxResult);
        checkIfNE(root, info, algConfig, expander, welfareResult);
        MCTSConfig mctsP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> mctsP1Expander = new ExtendedKuhnPokerExpander<>(mctsP1Config);
        MCTSConfig cfrP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> cfrP1Expander = new ExtendedKuhnPokerExpander<>(cfrP1Config);

        OOSAlgorithmData.useEpsilonRM = false;
        Map<Sequence, Double> cfrP1RealPlan = getCFRStrategy(root, cfrP1Expander);
        Map<Sequence, Double> mctsP1RealPlan = getMCTSStrategy(root, mctsP1Expander, info);
        OOSAlgorithmData.useEpsilonRM = true;
        MCTSConfig cfrP1ConfigEps = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> cfrP1ExpanderEps = new ExtendedKuhnPokerExpander<>(cfrP1ConfigEps);
        Map<Sequence, Double> cfrEpsilonP1RealPlan = getCFRStrategy(root, cfrP1ExpanderEps);

        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
        Expander<SequenceInformationSet> stackelbergExpander = new ExtendedKuhnPokerExpander<>(stackelbergConfig);
        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver).getRight();


        if (qpResult != null && undomResult != null && neResult != null) {
            StackelbergConfig stackConfig = new StackelbergConfig(root);
            Expander<SequenceInformationSet> stackExpander = new ExtendedKuhnPokerExpander<>(stackConfig);
            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);

            stackBuilder.generateCompleteGame();
            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> mctsExpander = new ExtendedKuhnPokerExpander<>(mctsConfig);

            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> cfrExpander = new ExtendedKuhnPokerExpander<>(cfrConfig);
//            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
            evaluateAgainstWRNE(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateAgainstBRNE(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstMCTS(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1], algConfig);
            evaluateP1StrategiesAgainstCFR(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1], algConfig);
            evaluateP1StrategiesAgainstQRE(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1], algConfig);
        }
    }

    public static void runIAoS() {
        GameState root = new InformerAoSGameState();
        GameInfo info = new AoSGameInfo();
        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new InformerAoSExpander<>(algConfig);

        generateCompleteGame(root, expander, algConfig, info);
        FullSequenceFormLP lp = new SequenceFormLP(getActingPlayers(root));
        GenSumSequenceFormMILP undomSolver = new UndomGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root, expander, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP p1MaxSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
        GenSumSequenceFormMILP welfareSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, getActingPlayers(root), info, root.getAllPlayers()[0], root.getAllPlayers()[1]);
        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;

        GenSumSequenceFormConfig algConfig1 = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander1 = new InformerAoSExpander<>(algConfig1);
        FullSequenceEFG builder1 = new FullSequenceEFG(root, expander1, info, algConfig1);

        builder1.generateCompleteGame();
        lp.calculateBothPlStrategy(root, algConfig);
        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander1, algConfig1, info, "KuhnPokerRepr");
        SolverResult undomResult = undomSolver.compute();
        SolverResult neResult = neSolver.compute();
        SolverResult p1MaxResult = p1MaxSolver.compute();
        SolverResult welfareResult = welfareSolver.compute();
        SolverResult maximinResult = new SolverResult(lp.getResultStrategiesForPlayer(root.getAllPlayers()[0]), lp.getResultStrategiesForPlayer(root.getAllPlayers()[1]), 0);
//        qpResult.p1RealPlan = filterLow(qpResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
//        undomResult.p1RealPlan = filterLow(undomResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
//        neResult.p1RealPlan = filterLow(neResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
//        p1MaxResult.p1RealPlan = filterLow(p1MaxResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
//        welfareResult.p1RealPlan = filterLow(welfareResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
        checkIfNE(root, info, algConfig, expander, qpResult);
        checkIfNE(root, info, algConfig, expander, undomResult);
        checkIfNE(root, info, algConfig, expander, neResult);
        checkIfNE(root, info, algConfig, expander, p1MaxResult);
        checkIfNE(root, info, algConfig, expander, welfareResult);
        MCTSConfig mctsP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> mctsP1Expander = new InformerAoSExpander<>(mctsP1Config);
        MCTSConfig cfrP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> cfrP1Expander = new InformerAoSExpander<>(cfrP1Config);

        OOSAlgorithmData.useEpsilonRM = false;
        Map<Sequence, Double> cfrP1RealPlan = getCFRStrategy(root, cfrP1Expander);
        Map<Sequence, Double> mctsP1RealPlan = getMCTSStrategy(root, mctsP1Expander, info);
        OOSAlgorithmData.useEpsilonRM = true;
        MCTSConfig cfrP1ConfigEps = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> cfrP1ExpanderEps = new InformerAoSExpander<>(cfrP1ConfigEps);
        Map<Sequence, Double> cfrEpsilonP1RealPlan = getCFRStrategy(root, cfrP1ExpanderEps);

        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
        Expander<SequenceInformationSet> stackelbergExpander = new InformerAoSExpander<>(stackelbergConfig);
        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver).getRight();


        if (qpResult != null && undomResult != null && neResult != null) {
            StackelbergConfig stackConfig = new StackelbergConfig(root);
            Expander<SequenceInformationSet> stackExpander = new InformerAoSExpander<>(stackConfig);
            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);

            stackBuilder.generateCompleteGame();
            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> mctsExpander = new InformerAoSExpander<>(mctsConfig);

            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> cfrExpander = new InformerAoSExpander<>(cfrConfig);
//            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
            evaluateAgainstWRNE(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateAgainstBRNE(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstMCTS(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1], algConfig);
            evaluateP1StrategiesAgainstCFR(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1], algConfig);
            evaluateP1StrategiesAgainstQRE(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1], algConfig);
        }
    }

    public static void generateCompleteGame(GameState rootState, Expander<SequenceInformationSet> expander, GenSumSequenceFormConfig algConfig, GameInfo info) {
        ArrayDeque<GameState> queue = new ArrayDeque<GameState>();

        queue.add(rootState);

        while (queue.size() > 0) {
            GameState currentState = queue.removeLast();

            algConfig.addStateToSequenceForm(currentState);
            if (currentState.isGameEnd()) {
                Double[] utilities = new Double[2];
                double[] stateUtilities = currentState.getUtilities();

                for (int i = 0; i < utilities.length; i++) {
                    utilities[i] = stateUtilities[i] * currentState.getNatureProbability() * info.getUtilityStabilizer();
                }
                algConfig.setUtility(currentState, utilities);
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
    }

//    public static void runGenSumGenericPoker(double rake) {
//        GameState root = new GenSumGPGameState();
//        GameInfo info = new GPGameInfo();
//        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
//        Expander<SequenceInformationSet> expander = new GenericPokerExpander<>(algConfig);
//        FullSequenceEFG builder = new FullSequenceEFG(root, expander, info, algConfig);
//
//        builder.generateCompleteGame();
//
//        GenSumSequenceFormMILP undomSolver = new UndomGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root, expander, root.getAllPlayers()[0]);
//        GenSumSequenceFormMILP p1MaxSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0]);
//        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
//        GenSumSequenceFormMILP welfareSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0], root.getAllPlayers()[1]);
//        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;
//
//        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
//        SolverResult undomResult = undomSolver.compute();
//        SolverResult neResult = neSolver.compute();
//        SolverResult p1MaxResult = p1MaxSolver.compute();
//        SolverResult welfareResult = welfareSolver.compute();
//
//        checkIfNE(root, info, algConfig, expander, qpResult);
//        checkIfNE(root, info, algConfig, expander, undomResult);
//        checkIfNE(root, info, algConfig, expander, neResult);
//        checkIfNE(root, info, algConfig, expander, p1MaxResult);
//        checkIfNE(root, info, algConfig, expander, welfareResult);
//        MCTSConfig mctsP1Config = new MCTSConfig(new Random(1));
//        Expander<MCTSInformationSet> mctsP1Expander = new RandomGameExpander<>(mctsP1Config);
//        MCTSConfig cfrP1Config = new MCTSConfig(new Random(1));
//        Expander<MCTSInformationSet> cfrP1Expander = new RandomGameExpander<>(cfrP1Config);
//
//        Map<Sequence, Double> cfrP1RealPlan = getCFRStrategy(root, cfrP1Expander);
//        Map<Sequence, Double> mctsP1RealPlan = getMCTSStrategy(root, mctsP1Expander, info);
//        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
//        Expander<SequenceInformationSet> stackelbergExpander = new GenericPokerExpander<>(stackelbergConfig);
//        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
//        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
//        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver);
//
//
//        if (qpResult != null && undomResult != null && neResult != null) {
//            StackelbergConfig stackConfig = new StackelbergConfig(root);
//            Expander<SequenceInformationSet> stackExpander = new GenericPokerExpander<>(stackConfig);
//            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);
//
//            stackBuilder.generateCompleteGame();
//            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
//            Expander<MCTSInformationSet> mctsExpander = new GenericPokerExpander<>(mctsConfig);
//
//            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
//            Expander<MCTSInformationSet> cfrExpander = new GenericPokerExpander<>(cfrConfig);
////            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
//            evaluateAgainstWRNE(maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//            evaluateAgainstBRNE(maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//            evaluateP1StrategiesAgainstMCTS(maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1], algConfig);
//            evaluateP1StrategiesAgainstCFR(maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1], algConfig);
//            evaluateP1StrategiesAgainstQRE(maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1], algConfig);
//        }
//    }

    public static Player[] getActingPlayers(GameState root) {
        return new Player[]{root.getAllPlayers()[0], root.getAllPlayers()[1]};
    }


    public static void runGenSumRandomGames(int bf, int depth, double correlation, int observations, int seed) {
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

        FullSequenceFormLP lp = new SequenceFormLP(getActingPlayers(root));
        GenSumSequenceFormMILP undomSolver = new UndomGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root, expander, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP p1MaxSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
        GenSumSequenceFormMILP welfareSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0], root.getAllPlayers()[1]);
        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;
        GenSumSequenceFormConfig algConfig1 = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander1 = new RandomGameExpander<>(algConfig1);
        FullSequenceEFG builder1 = new FullSequenceEFG(root, expander1, info, algConfig1);

        builder1.generateCompleteGame();
        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander1, algConfig1, info, "GenSumRndGameRepr");
        SolverResult undomResult = undomSolver.compute();
        SolverResult neResult = neSolver.compute();
        SolverResult p1MaxResult = p1MaxSolver.compute();
        SolverResult welfareResult = welfareSolver.compute();
        lp.calculateBothPlStrategy(root, algConfig);

        SolverResult maximinResult = new SolverResult(lp.getResultStrategiesForPlayer(root.getAllPlayers()[0]), lp.getResultStrategiesForPlayer(root.getAllPlayers()[1]), 0);
        checkIfNE(root, info, algConfig, expander, qpResult);
        checkIfNE(root, info, algConfig, expander, undomResult);
        checkIfNE(root, info, algConfig, expander, neResult);
        checkIfNE(root, info, algConfig, expander, p1MaxResult);
        checkIfNE(root, info, algConfig, expander, welfareResult);
//        qpResult.p1RealPlan = filterLow(qpResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
//        undomResult.p1RealPlan = filterLow(undomResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
//        neResult.p1RealPlan = filterLow(neResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
//        p1MaxResult.p1RealPlan = filterLow(p1MaxResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
//        welfareResult.p1RealPlan = filterLow(welfareResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
        MCTSConfig mctsP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> mctsP1Expander = new RandomGameExpander<>(mctsP1Config);
        MCTSConfig cfrP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> cfrP1Expander = new RandomGameExpander<>(cfrP1Config);

        OOSAlgorithmData.useEpsilonRM = false;
        Map<Sequence, Double> cfrP1RealPlan = getCFRStrategy(root, cfrP1Expander);
        Map<Sequence, Double> mctsP1RealPlan = getMCTSStrategy(root, mctsP1Expander, info);
        OOSAlgorithmData.useEpsilonRM = true;
        MCTSConfig cfrP1ConfigEps = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> cfrP1ExpanderEps = new RandomGameExpander<>(cfrP1ConfigEps);
        Map<Sequence, Double> cfrEpsilonP1RealPlan = getCFRStrategy(root, cfrP1ExpanderEps);

        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
        Expander<SequenceInformationSet> stackelbergExpander = new RandomGameExpander<>(stackelbergConfig);
        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(root.getAllPlayers(), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver).getRight();

        storeTimes(neResult, undomResult, qpResult, p1MaxResult, welfareResult, runner.getFinalTime(), cfrTime, mctsTime);
        if (qpResult != null && undomResult != null && neResult != null) {
            StackelbergConfig stackConfig = new StackelbergConfig(root);
            Expander<SequenceInformationSet> stackExpander = new RandomGameExpander<>(stackConfig);
            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);

            stackBuilder.generateCompleteGame();
            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> mctsExpander = new RandomGameExpander<>(mctsConfig);

            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> cfrExpander = new RandomGameExpander<>(cfrConfig);
            GenSumSequenceFormConfig freshAlgConfig = new GenSumSequenceFormConfig();
            Expander<SequenceInformationSet> freshExpander = new RandomGameExpander<>(freshAlgConfig);
            FullSequenceEFG freshBuilder = new FullSequenceEFG(root, freshExpander, info, freshAlgConfig);

            freshBuilder.generateCompleteGame();
//            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
            evaluateAgainstWRNE(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateAgainstBRNE(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstMCTS(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1], freshAlgConfig);
            evaluateP1StrategiesAgainstCFR(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1], algConfig);
            evaluateP1StrategiesAgainstQRE(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1], algConfig);
        }
    }
//
//    public static void runGoofspiel(int cardCount) {
//        GSGameInfo.CARDS_FOR_PLAYER = new int[cardCount];
//
//        for (int i = 0; i < cardCount; i++) {
//            GSGameInfo.CARDS_FOR_PLAYER[i] = i + 1;
//        }
//        GSGameInfo.depth = cardCount;
//        GameState root = new GenSumGoofSpielGameState();
//        GameInfo info = new GSGameInfo();
//        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
//        Expander<SequenceInformationSet> expander = new GoofSpielExpander<>(algConfig);
//        FullSequenceEFG builder = new FullSequenceEFG(root, expander, info, algConfig);
//
//        builder.generateCompleteGame();
//
//        FullSequenceFormLP lp = new SequenceFormLP(getActingPlayers(root));
//        GenSumSequenceFormMILP undomSolver = new UndomGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root, expander, root.getAllPlayers()[0]);
//        GenSumSequenceFormMILP p1MaxSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0]);
//        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
//        GenSumSequenceFormMILP welfareSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0], root.getAllPlayers()[1]);
//        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;
//        GenSumSequenceFormConfig algConfig1 = new GenSumSequenceFormConfig();
//        Expander<SequenceInformationSet> expander1 = new GoofSpielExpander<>(algConfig1);
//        FullSequenceEFG builder1 = new FullSequenceEFG(root, expander1, info, algConfig1);
//
//        builder1.generateCompleteGame();
//        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander1, algConfig1, info, "GenSumRndGameRepr");
//        SolverResult undomResult = undomSolver.compute();
//        SolverResult neResult = neSolver.compute();
//        SolverResult p1MaxResult = p1MaxSolver.compute();
//        SolverResult welfareResult = welfareSolver.compute();
//        lp.calculateBothPlStrategy(root, algConfig);
//
//        SolverResult maximinResult = new SolverResult(lp.getResultStrategiesForPlayer(root.getAllPlayers()[0]), lp.getResultStrategiesForPlayer(root.getAllPlayers()[1]), 0);
//        checkIfNE(root, info, algConfig, expander, qpResult);
//        checkIfNE(root, info, algConfig, expander, undomResult);
//        checkIfNE(root, info, algConfig, expander, neResult);
//        checkIfNE(root, info, algConfig, expander, p1MaxResult);
//        checkIfNE(root, info, algConfig, expander, welfareResult);
////        qpResult.p1RealPlan = filterLow(qpResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
////        undomResult.p1RealPlan = filterLow(undomResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
////        neResult.p1RealPlan = filterLow(neResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
////        p1MaxResult.p1RealPlan = filterLow(p1MaxResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
////        welfareResult.p1RealPlan = filterLow(welfareResult.p1RealPlan, root, expander, root.getAllPlayers()[0], 1e-4);
//        MCTSConfig mctsP1Config = new MCTSConfig(new Random(1));
//        Expander<MCTSInformationSet> mctsP1Expander = new GoofSpielExpander<>(mctsP1Config);
//        MCTSConfig cfrP1Config = new MCTSConfig(new Random(1));
//        Expander<MCTSInformationSet> cfrP1Expander = new GoofSpielExpander<>(cfrP1Config);
//
//        Map<Sequence, Double> cfrP1RealPlan = getCFRStrategy(root, cfrP1Expander);
//        Map<Sequence, Double> mctsP1RealPlan = getMCTSStrategy(root, mctsP1Expander, info);
//
//        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
//        Expander<SequenceInformationSet> stackelbergExpander = new GoofSpielExpander<>(stackelbergConfig);
//        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
//        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
//        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver);
//
//        storeTimes(neResult, undomResult, qpResult, p1MaxResult, welfareResult, runner.getFinalTime(), cfrTime, mctsTime);
//        if (qpResult != null && undomResult != null && neResult != null) {
//            StackelbergConfig stackConfig = new StackelbergConfig(root);
//            Expander<SequenceInformationSet> stackExpander = new GoofSpielExpander<>(stackConfig);
//            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);
//
//            stackBuilder.generateCompleteGame();
//            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
//            Expander<MCTSInformationSet> mctsExpander = new GoofSpielExpander<>(mctsConfig);
//
//            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
//            Expander<MCTSInformationSet> cfrExpander = new GoofSpielExpander<>(cfrConfig);
//            GenSumSequenceFormConfig freshAlgConfig = new GenSumSequenceFormConfig();
//            Expander<SequenceInformationSet> freshExpander = new GoofSpielExpander<>(freshAlgConfig);
//            FullSequenceEFG freshBuilder = new FullSequenceEFG(root, freshExpander, info, freshAlgConfig);
//
//            freshBuilder.generateCompleteGame();
////            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
//            evaluateAgainstWRNE(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//            evaluateAgainstBRNE(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//            evaluateP1StrategiesAgainstMCTS(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1], freshAlgConfig);
//            evaluateP1StrategiesAgainstCFR(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1], algConfig);
//            evaluateP1StrategiesAgainstQRE(cfrEpsilonP1RealPlan, maximinResult, neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1], algConfig);
//        }
//    }

    public static void storeTimes(SolverResult neResult, SolverResult undomResult, SolverResult qpResult, SolverResult p1MaxResult, SolverResult welfareResult, long stackTime, long cfrTime, long mctsTime) {
        try {
            BufferedWriter neWriter = new BufferedWriter(new FileWriter("P1NETimes.csv", true));
            BufferedWriter undomWriter = new BufferedWriter(new FileWriter("P1UndomTimes.csv", true));
            BufferedWriter qpWriter = new BufferedWriter(new FileWriter("P1QPTimes.csv", true));
            BufferedWriter p1MaxWriter = new BufferedWriter(new FileWriter("P1MNETimes.csv", true));
            BufferedWriter welfareWriter = new BufferedWriter(new FileWriter("P1WNETimes.csv", true));
            BufferedWriter stackWriter = new BufferedWriter(new FileWriter("P1StackTimes.csv", true));
            BufferedWriter cfrWriter = new BufferedWriter(new FileWriter("P1CFRTimes.csv", true));
            BufferedWriter mctsWriter = new BufferedWriter(new FileWriter("P1MCTSTimes.csv", true));

            neWriter.write("" + neResult.time);
            neWriter.newLine();
            neWriter.close();

            undomWriter.write("" + undomResult.time);
            undomWriter.newLine();
            undomWriter.close();

            qpWriter.write("" + qpResult.time);
            qpWriter.newLine();
            qpWriter.close();

            p1MaxWriter.write("" + p1MaxResult.time);
            p1MaxWriter.newLine();
            p1MaxWriter.close();

            welfareWriter.write("" + welfareResult.time);
            welfareWriter.newLine();
            welfareWriter.close();

            stackWriter.write("" + stackTime);
            stackWriter.newLine();
            stackWriter.close();

            cfrWriter.write("" + cfrTime);
            cfrWriter.newLine();
            cfrWriter.close();

            mctsWriter.write("" + mctsTime);
            mctsWriter.newLine();
            mctsWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<Sequence, Double> getMCTSStrategy(GameState root, Expander<MCTSInformationSet> expander, GameInfo info) {
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
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long start = bean.getCurrentThreadCpuTime();

        buildMCTSCompleteTree(GenSumISMCTSNestingRunner.alg.getRootNode(), factory);
        GenSumISMCTSNestingRunner.buildStichedStrategy(root.getAllPlayers()[0], GenSumISMCTSNestingRunner.alg.getRootNode().getInformationSet(),
                GenSumISMCTSNestingRunner.alg.getRootNode(), 100000);
        GenSumISMCTSNestingRunner.alg.resetRootNode();
        mctsTime = (long) ((bean.getCurrentThreadCpuTime() - start) / 1e6);
        return StrategyCollector.getStrategyFor(GenSumISMCTSNestingRunner.alg.getRootNode(), root.getAllPlayers()[0], new MeanStratDist());
    }

    public static Map<Sequence, Double> getCFRStrategy(GameState root, Expander<MCTSInformationSet> expander) {
        CFRAlgorithm cfr = new CFRAlgorithm(root.getAllPlayers()[0], root, expander);

        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long start = bean.getCurrentThreadCpuTime();

        buildCFRCompleteTree(cfr.getRootNode());
        cfr.runIterations(100000);
        cfrTime = (long) ((bean.getCurrentThreadCpuTime() - start) / 1e6);
        return StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[0], new MeanStratDist());
    }

    public static void evaluateAgainstBRNE(Map<Sequence, Double> cfrEpsilonP1RealPlan, SolverResult maximinResult, SolverResult neResult, SolverResult undomResult, SolverResult qpResult, SolverResult p1MaxResult, SolverResult welfareResult, Map<Player, Map<Sequence, Double>> stackResult, Map<Sequence, Double> cfrP1RealPlan, Map<Sequence, Double> mctsP1RealPlan, GameState root, Expander<SequenceInformationSet> expander, GameInfo info, GenSumSequenceFormConfig algConfig, Player player) {
        try {
            BufferedWriter cfrEpsWriter = new BufferedWriter(new FileWriter("P1CFREpsvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter maximinWriter = new BufferedWriter(new FileWriter("P1MAXIMINvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter neWriter = new BufferedWriter(new FileWriter("P1NEvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter undomWriter = new BufferedWriter(new FileWriter("P1UndomvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter qpWriter = new BufferedWriter(new FileWriter("P1QPvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter p1MaxWriter = new BufferedWriter(new FileWriter("P1MNEvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter welfareWriter = new BufferedWriter(new FileWriter("P1WNEvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter stackWriter = new BufferedWriter(new FileWriter("P1StackvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter cfrWriter = new BufferedWriter(new FileWriter("P1CFRvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter mctsWriter = new BufferedWriter(new FileWriter("P1MCTSvsBRNEExpVal" + getDomainDependentString() + ".csv", true));

            SolverResult vsCfrEpsSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], cfrEpsilonP1RealPlan).compute();
            SolverResult vsMAXIMINSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], maximinResult.p1RealPlan).compute();
            SolverResult vsNeSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], neResult.p1RealPlan).compute();
            SolverResult vsUndomSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], undomResult.p1RealPlan).compute();
            SolverResult vsQPSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], qpResult.p1RealPlan).compute();
            SolverResult vsStackSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], stackResult.get(root.getAllPlayers()[0])).compute();
            SolverResult vsp1maxSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], p1MaxResult.p1RealPlan).compute();
            SolverResult vswelfareSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], welfareResult.p1RealPlan).compute();
            SolverResult vsCfrSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], cfrP1RealPlan).compute();
            SolverResult vsMctsSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], mctsP1RealPlan).compute();

            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsBRNEvsCFREpsExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsBRNEvsCFREpsExpVal" + getDomainDependentString() + ".csv", true)),
                    cfrEpsWriter, cfrEpsilonP1RealPlan, vsCfrEpsSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsBRNEvsP1MAXIMINExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsBRNEvsP1MAXIMINExpVal" + getDomainDependentString() + ".csv", true)),
                    maximinWriter, maximinResult.p1RealPlan, vsMAXIMINSolverResult.p2RealPlan, root, expander, info, algConfig);
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

            cfrEpsWriter.newLine();
            maximinWriter.newLine();
            neWriter.newLine();
            undomWriter.newLine();
            qpWriter.newLine();
            p1MaxWriter.newLine();
            welfareWriter.newLine();
            stackWriter.newLine();
            cfrWriter.newLine();
            mctsWriter.newLine();

            cfrEpsWriter.close();
            maximinWriter.close();
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

    public static void evaluateAgainstWRNE(Map<Sequence, Double> cfrEpsilonP1RealPlan, SolverResult maximinResult, SolverResult neResult, SolverResult undomResult, SolverResult qpResult, SolverResult p1MaxResult, SolverResult welfareResult, Map<Player, Map<Sequence, Double>> stackResult, Map<Sequence, Double> cfrP1RealPlan, Map<Sequence, Double> mctsP1RealPlan, GameState root, Expander<SequenceInformationSet> expander, GameInfo info, GenSumSequenceFormConfig algConfig, Player player) {
        try {
            BufferedWriter cfrEpsWriter = new BufferedWriter(new FileWriter("P1CFREpsvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter maximinWriter = new BufferedWriter(new FileWriter("P1MAXIMINvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter neWriter = new BufferedWriter(new FileWriter("P1NEvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter undomWriter = new BufferedWriter(new FileWriter("P1UndomvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter qpWriter = new BufferedWriter(new FileWriter("P1QPvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter p1MaxWriter = new BufferedWriter(new FileWriter("P1MNEvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter welfareWriter = new BufferedWriter(new FileWriter("P1WNEvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter stackWriter = new BufferedWriter(new FileWriter("P1StackvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter cfrWriter = new BufferedWriter(new FileWriter("P1CFRvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter mctsWriter = new BufferedWriter(new FileWriter("P1MCTSvsWRNEExpVal" + getDomainDependentString() + ".csv", true));

            SolverResult vsCfrEpsSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], cfrEpsilonP1RealPlan).compute();
            SolverResult vsMAXIMINSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], maximinResult.p1RealPlan).compute();
            SolverResult vsNeSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], neResult.p1RealPlan).compute();
            SolverResult vsUndomSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], undomResult.p1RealPlan).compute();
            SolverResult vsQPSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], qpResult.p1RealPlan).compute();
            SolverResult vsStackSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], stackResult.get(root.getAllPlayers()[0])).compute();
            SolverResult vsp1maxSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], p1MaxResult.p1RealPlan).compute();
            SolverResult vswelfareSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], welfareResult.p1RealPlan).compute();
            SolverResult vsCfrSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], cfrP1RealPlan).compute();
            SolverResult vsMctsSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], mctsP1RealPlan).compute();

            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsWRNEvsCFREpsExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsWRNEvsCFREpsExpVal" + getDomainDependentString() + ".csv", true)),
                    cfrEpsWriter, cfrEpsilonP1RealPlan, vsCfrEpsSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsWRNEvsP1MAXIMINExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsWRNEvsP1MAXIMINExpVal" + getDomainDependentString() + ".csv", true)),
                    maximinWriter, maximinResult.p1RealPlan, vsMAXIMINSolverResult.p2RealPlan, root, expander, info, algConfig);
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

            cfrEpsWriter.newLine();
            maximinWriter.newLine();
            neWriter.newLine();
            undomWriter.newLine();
            qpWriter.newLine();
            p1MaxWriter.newLine();
            welfareWriter.newLine();
            stackWriter.newLine();
            cfrWriter.newLine();
            mctsWriter.newLine();

            cfrEpsWriter.close();
            maximinWriter.close();
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

    public static void evaluateBRWR(BufferedWriter brWriter, BufferedWriter wrWriter, BufferedWriter writer, Map<Sequence, Double> p1Strategy, Map<Sequence, Double> p2Strategy, GameState root, Expander<SequenceInformationSet> expander, GameInfo info, GenSumSequenceFormConfig algConfig) throws IOException {
        GeneralSumWorstResponse wr = new GeneralSumWorstResponse(expander, 0, getActingPlayers(root), algConfig, info);
        GeneralSumBestResponse br = new GeneralSumBestResponse(expander, 0, getActingPlayers(root), algConfig, info);

        br.calculateBR(root, p2Strategy);
        wr.calculateBR(root, p2Strategy);
//        brWriter = new BufferedWriter(new FileWriter("P1BRvsWRNEvsStackExpVal" + getDomainDependentString() + ".csv", true));
        write(brWriter, computeExpectedValue(br.getBRStategy(), p2Strategy, expander, algConfig, root));
        brWriter.newLine();
        brWriter.close();
//        wrWriter = new BufferedWriter(new FileWriter("P1WRvsWRNEvsStackExpVal" + getDomainDependentString() + ".csv", true));
        write(wrWriter, computeExpectedValue(wr.getBRStategy(), p2Strategy, expander, algConfig, root));
        wrWriter.newLine();
        wrWriter.close();
        write(writer, computeExpectedValue(p1Strategy, p2Strategy, expander, algConfig, root));
    }

    public static void evaluateP1StrategiesAgainstQRE(Map<Sequence, Double> cfrEpsilonP1RealPlan, SolverResult maximinResult, SolverResult neResult, SolverResult undomResult, SolverResult qpResult, SolverResult p1MaxResult, SolverResult welfareResult, Map<Player, Map<Sequence, Double>> stackResult,
                                                      Map<Sequence, Double> cfrP1RealPlan, Map<Sequence, Double> mctsP1RealPlan, GameState root, Expander<SequenceInformationSet> expander, GameInfo info, SequenceFormConfig<SequenceInformationSet> algConfig, Player player, GenSumSequenceFormConfig seqConfig) {
        try {
            BufferedWriter cfrEpsWriter = new BufferedWriter(new FileWriter("P1CFREpsvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter maximinWriter = new BufferedWriter(new FileWriter("P1MAXIMINvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter neWriter = new BufferedWriter(new FileWriter("P1NEvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter undomWriter = new BufferedWriter(new FileWriter("P1UndomvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter qpWriter = new BufferedWriter(new FileWriter("P1QPvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter p1MaxWriter = new BufferedWriter(new FileWriter("P1MNEvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter welfareWriter = new BufferedWriter(new FileWriter("P1WNEvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter stackWriter = new BufferedWriter(new FileWriter("P1StackvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter brWriter = new BufferedWriter(new FileWriter("P1BRvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter wrWriter = new BufferedWriter(new FileWriter("P1WRvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter cfrWriter = new BufferedWriter(new FileWriter("P1CFRvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter mctsWriter = new BufferedWriter(new FileWriter("P1MCTSvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter bestNEWriter = new BufferedWriter(new FileWriter("P1BestNEvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter worstNEWriter = new BufferedWriter(new FileWriter("P1WorstNEvsQREExpVal" + getDomainDependentString() + ".csv", true));

            QRESolver solver = new QRESolver(root, expander, info, algConfig);
            QREResult qreResult = solver.solve();

            writeLambdas(maximinWriter, qreResult.lambdas);
            writeLambdas(neWriter, qreResult.lambdas);
            writeLambdas(undomWriter, qreResult.lambdas);
            writeLambdas(qpWriter, qreResult.lambdas);
            writeLambdas(p1MaxWriter, qreResult.lambdas);
            writeLambdas(welfareWriter, qreResult.lambdas);
            writeLambdas(stackWriter, qreResult.lambdas);
            writeLambdas(brWriter, qreResult.lambdas);
            writeLambdas(wrWriter, qreResult.lambdas);
            writeLambdas(mctsWriter, qreResult.lambdas);
            writeLambdas(cfrEpsWriter, qreResult.lambdas);
            writeLambdas(cfrWriter, qreResult.lambdas);
            writeLambdas(bestNEWriter, qreResult.lambdas);
            writeLambdas(worstNEWriter, qreResult.lambdas);
            for (Map<Player, Map<Sequence, Double>> quantalResponse : qreResult.quantalResponses) {
                GeneralSumBestResponse br = new GeneralSumBestResponse(expander, 0, getActingPlayers(root), algConfig, info);
                GeneralSumWorstResponse wr = new GeneralSumWorstResponse(expander, 0, getActingPlayers(root), algConfig, info);
                Map<Sequence, Double> qreStrategy = filterLow(quantalResponse.get(player), root, expander, root.getAllPlayers()[1], 1e-3);

                BestGenSumSequenceFormMILP bNE = new BestGenSumSequenceFormMILP(seqConfig, root.getAllPlayers(), info, root.getAllPlayers()[0], qreStrategy);
                WorstGenSumSequenceFormMILP wNE = new WorstGenSumSequenceFormMILP(seqConfig, root.getAllPlayers(), info, root.getAllPlayers()[0], qreStrategy);

                br.calculateBR(root, qreStrategy);
                wr.calculateBR(root, qreStrategy);
                write(maximinWriter, computeExpectedValue(maximinResult.p1RealPlan, qreStrategy, expander, seqConfig, root));
                write(brWriter, computeExpectedValue(br.getBRStategy(), qreStrategy, expander, seqConfig, root));
                write(wrWriter, computeExpectedValue(wr.getBRStategy(), qreStrategy, expander, seqConfig, root));
                write(neWriter, computeExpectedValue(neResult.p1RealPlan, qreStrategy, expander, seqConfig, root));
                write(undomWriter, computeExpectedValue(undomResult.p1RealPlan, qreStrategy, expander, seqConfig, root));
                write(qpWriter, computeExpectedValue(qpResult.p1RealPlan, qreStrategy, expander, seqConfig, root));
                write(p1MaxWriter, computeExpectedValue(p1MaxResult.p1RealPlan, qreStrategy, expander, seqConfig, root));
                write(welfareWriter, computeExpectedValue(welfareResult.p1RealPlan, qreStrategy, expander, seqConfig, root));
                write(stackWriter, computeExpectedValue(stackResult.get(root.getAllPlayers()[0]), qreStrategy, expander, seqConfig, root));
                write(cfrEpsWriter, computeExpectedValue(cfrEpsilonP1RealPlan, qreStrategy, expander, seqConfig, root));
                write(cfrWriter, computeExpectedValue(cfrP1RealPlan, qreStrategy, expander, seqConfig, root));
                write(mctsWriter, computeExpectedValue(mctsP1RealPlan, qreStrategy, expander, seqConfig, root));
                write(bestNEWriter, computeExpectedValue(bNE.compute().p1RealPlan, qreStrategy, expander, seqConfig, root));
                write(worstNEWriter, computeExpectedValue(wNE.compute().p1RealPlan, qreStrategy, expander, seqConfig, root));
            }
            cfrEpsWriter.newLine();
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
            bestNEWriter.newLine();
            worstNEWriter.newLine();
            maximinWriter.newLine();

            cfrEpsWriter.close();
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
            bestNEWriter.close();
            worstNEWriter.close();
            maximinWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Map<Sequence, Double> filterLow(Map<Sequence, Double> realPlan, GameState root, Expander<? extends InformationSet> expander, Player player, double filter) {
//        if(root.isGameEnd())
//            return realPlan;
//        for (Action action : expander.getActions(root)) {
//            GameState nextState = root.performAction(action);
//
//            if (root.getPlayerToMove().equals(player)) {
//                if (realPlan.get(nextState.getSequenceFor(player)) != null)
//                    filterLow(realPlan, nextState, expander, player, filter);
//            } else {
//                filterLow(realPlan, nextState, expander, player, filter);
//            }
//        }
//        if (root.getPlayerToMove().equals(player)) {
//            double removedProbability = 0;
//            double currentProbSum = 0;
//            for (Action action : expander.getActions(root)) {
//                GameState nextState = root.performAction(action);
//                Double probability = realPlan.get(nextState.getSequenceFor(player));
//
//                if (probability != null && probability < filter) {
//                    realPlan.remove(nextState.getSequenceFor(player));
//                    removedProbability += probability;
//                }
//                if(probability != null && probability >= filter)
//                    currentProbSum += probability;
//            }
//
//            for (Action action : expander.getActions(root)) {
//                GameState nextState = root.performAction(action);
//                Double probability = realPlan.get(nextState.getSequenceFor(player));
//
//                if (probability != null && probability > filter)
//                    realPlan.put(nextState.getSequenceFor(player), probability + removedProbability * probability / currentProbSum);
//            }
//        }
//        return realPlan;
        Iterator<Map.Entry<Sequence, Double>> iterator = realPlan.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Sequence, Double> entry = iterator.next();

            if (entry.getValue() < filter)
                iterator.remove();
        }

        for (Sequence sequence : realPlan.keySet()) {
            double value = realPlan.get(sequence);

            value = Math.round(value * 1e3) / 1e3;
            realPlan.put(sequence, value);
        }
        return realPlan;
    }

    public static void evaluateP1StrategiesAgainstCFR(Map<Sequence, Double> cfrEpsilonP1RealPlan, SolverResult maximinResult, SolverResult neResult, SolverResult undomResult, SolverResult qpResult, SolverResult p1MaxResult, SolverResult welfareResult, Map<Player, Map<Sequence, Double>> stackResult,
                                                      Map<Sequence, Double> cfrP1RealPlan, Map<Sequence, Double> mctsP1RealPlan, GameState root, Expander<MCTSInformationSet> expander, GameInfo info, MCTSConfig algConfig, Player player, GenSumSequenceFormConfig seqConfig) {
        try {
            BufferedWriter cfrEpsWriter = new BufferedWriter(new FileWriter("P1CFREpsvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter maximinWriter = new BufferedWriter(new FileWriter("P1MAXIMINvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter neWriter = new BufferedWriter(new FileWriter("P1NEvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter undomWriter = new BufferedWriter(new FileWriter("P1UndomvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter qpWriter = new BufferedWriter(new FileWriter("P1QPvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter p1MaxWriter = new BufferedWriter(new FileWriter("P1MNEvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter welfareWriter = new BufferedWriter(new FileWriter("P1WNEvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter stackWriter = new BufferedWriter(new FileWriter("P1StackvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter brWriter = new BufferedWriter(new FileWriter("P1BRvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter wrWriter = new BufferedWriter(new FileWriter("P1WRvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter cfrWriter = new BufferedWriter(new FileWriter("P1CFRvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter mctsWriter = new BufferedWriter(new FileWriter("P1MCTSvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter bestNEWriter = new BufferedWriter(new FileWriter("P1BestNEvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter worstNEWriter = new BufferedWriter(new FileWriter("P1WorstNEvsCFRExpVal" + getDomainDependentString() + ".csv", true));

            OOSAlgorithmData.useEpsilonRM = false;
            CFRAlgorithm cfr = new CFRAlgorithm(root.getAllPlayers()[1], root, expander);

            buildCFRCompleteTree(cfr.getRootNode());
            for (int i = 0; i < 100; i++) {
                cfr.runIterations(20);
                Map<Sequence, Double> strategy = StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[1], new MeanStratDist());
                strategy = filterLow(strategy, root, expander, root.getAllPlayers()[1], 1e-3);

                GeneralSumBestResponse br = new GeneralSumBestResponse(expander, 0, getActingPlayers(root), algConfig, info);
                GeneralSumWorstResponse wr = new GeneralSumWorstResponse(expander, 0, getActingPlayers(root), algConfig, info);
                BestGenSumSequenceFormMILP bNE = new BestGenSumSequenceFormMILP(seqConfig, root.getAllPlayers(), info, root.getAllPlayers()[0], strategy);
                WorstGenSumSequenceFormMILP wNE = new WorstGenSumSequenceFormMILP(seqConfig, root.getAllPlayers(), info, root.getAllPlayers()[0], strategy);

                br.calculateBR(root, strategy);
                wr.calculateBR(root, strategy);
                SolverResult bestNEResult = bNE.compute();
                SolverResult worstNEResult = wNE.compute();
                double[] bestNEValues = computeExpectedValue(bestNEResult.p1RealPlan, strategy, expander, seqConfig, root);
                double[] worstNEValues = computeExpectedValue(worstNEResult.p1RealPlan, strategy, expander, seqConfig, root);

                evaluate(neResult.p1RealPlan, strategy, root, expander, seqConfig, neWriter, worstNEValues[0], bestNEValues[0]);
                evaluate(undomResult.p1RealPlan, strategy, root, expander, seqConfig, undomWriter, worstNEValues[0], bestNEValues[0]);
                evaluate(qpResult.p1RealPlan, strategy, root, expander, seqConfig, qpWriter, worstNEValues[0], bestNEValues[0]);
                evaluate(p1MaxResult.p1RealPlan, strategy, root, expander, seqConfig, p1MaxWriter, worstNEValues[0], bestNEValues[0]);
                evaluate(welfareResult.p1RealPlan, strategy, root, expander, seqConfig, welfareWriter, worstNEValues[0], bestNEValues[0]);
                write(maximinWriter, computeExpectedValue(maximinResult.p1RealPlan, strategy, expander, seqConfig, root));
                write(stackWriter, computeExpectedValue(stackResult.get(root.getAllPlayers()[0]), strategy, expander, seqConfig, root));
                write(brWriter, computeExpectedValue(br.getBRStategy(), strategy, expander, seqConfig, root));
                write(wrWriter, computeExpectedValue(wr.getBRStategy(), strategy, expander, seqConfig, root));
                write(cfrEpsWriter, computeExpectedValue(cfrEpsilonP1RealPlan, strategy, expander, seqConfig, root));
                write(cfrWriter, computeExpectedValue(cfrP1RealPlan, strategy, expander, seqConfig, root));
                write(mctsWriter, computeExpectedValue(mctsP1RealPlan, strategy, expander, seqConfig, root));
                write(bestNEWriter, computeExpectedValue(bNE.compute().p1RealPlan, strategy, expander, seqConfig, root));
                write(worstNEWriter, computeExpectedValue(wNE.compute().p1RealPlan, strategy, expander, seqConfig, root));
            }
            cfrEpsWriter.newLine();
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
            bestNEWriter.newLine();
            worstNEWriter.newLine();
            maximinWriter.newLine();

            cfrEpsWriter.close();
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
            bestNEWriter.close();
            worstNEWriter.close();
            maximinWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void evaluateP1StrategiesAgainstMCTS(Map<Sequence, Double> cfrEpsilonP1RealPlan, SolverResult maximinResult, SolverResult neResult, SolverResult undomResult, SolverResult qpResult, SolverResult p1MaxResult, SolverResult welfareResult, Map<Player, Map<Sequence, Double>> stackResult,
                                                       Map<Sequence, Double> cfrP1RealPlan, Map<Sequence, Double> mctsP1RealPlan, GameState root, Expander<MCTSInformationSet> expander, GameInfo info, MCTSConfig algConfig, Player player, GenSumSequenceFormConfig seqConfig) {
        try {
            BufferedWriter cfrEpsWriter = new BufferedWriter(new FileWriter("P1CFREpsvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter maximinWriter = new BufferedWriter(new FileWriter("P1MAXIMINvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter neWriter = new BufferedWriter(new FileWriter("P1NEvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter undomWriter = new BufferedWriter(new FileWriter("P1UndomvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter qpWriter = new BufferedWriter(new FileWriter("P1QPvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter p1MaxWriter = new BufferedWriter(new FileWriter("P1MNEvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter welfareWriter = new BufferedWriter(new FileWriter("P1WNEvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter stackWriter = new BufferedWriter(new FileWriter("P1StackvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter brWriter = new BufferedWriter(new FileWriter("P1BRvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter wrWriter = new BufferedWriter(new FileWriter("P1WRvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter cfrWriter = new BufferedWriter(new FileWriter("P1CFRvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter mctsWriter = new BufferedWriter(new FileWriter("P1MCTSvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter bestNEWriter = new BufferedWriter(new FileWriter("P1BestNEvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter worstNEWriter = new BufferedWriter(new FileWriter("P1WorstNEvsMCTSExpVal" + getDomainDependentString() + ".csv", true));

            ISMCTSExploitability.rootState = root;
            ISMCTSExploitability.expander = expander;
            ISMCTSExploitability.gameInfo = info;
            BackPropFactory factory = new UCTBackPropFactory(Math.sqrt(2) * info.getMaxUtility());
            GenSumISMCTSNestingRunner.alg = new GenSumISMCTSAlgorithm(root.getAllPlayers()[1], new DefaultSimulator(expander), factory, root, expander);
            buildMCTSCompleteTree(GenSumISMCTSNestingRunner.alg.getRootNode(), factory);
//            GenSumISMCTSAlgorithm mcts = new GenSumISMCTSAlgorithm(root.getAllPlayers()[1], new DefaultSimulator(expander), new UCTBackPropFactory(Math.sqrt(2) * info.getMaxUtility()), root, expander);
            for (int i = 0; i < 100; i++) {
                GenSumISMCTSNestingRunner.clear();
                GenSumISMCTSNestingRunner.buildStichedStrategy(root.getAllPlayers()[1], GenSumISMCTSNestingRunner.alg.getRootNode().getInformationSet(),
                        GenSumISMCTSNestingRunner.alg.getRootNode(), 40);
                GenSumISMCTSNestingRunner.alg.resetRootNode();
                Map<Sequence, Double> strategy = StrategyCollector.getStrategyFor(GenSumISMCTSNestingRunner.alg.getRootNode(), root.getAllPlayers()[1], new MeanStratDist());
                strategy = filterLow(strategy, root, expander, root.getAllPlayers()[1], 1e-3);

                GeneralSumBestResponse br = new GeneralSumBestResponse(expander, 0, getActingPlayers(root), algConfig, info);
                GeneralSumWorstResponse wr = new GeneralSumWorstResponse(expander, 0, getActingPlayers(root), algConfig, info);
                BestGenSumSequenceFormMILP bNE = new BestGenSumSequenceFormMILP(seqConfig, root.getAllPlayers(), info, root.getAllPlayers()[0], strategy);
                WorstGenSumSequenceFormMILP wNE = new WorstGenSumSequenceFormMILP(seqConfig, root.getAllPlayers(), info, root.getAllPlayers()[0], strategy);
                br.calculateBR(root, strategy);
                wr.calculateBR(root, strategy);
                SolverResult worstNEResult = wNE.compute();
                SolverResult bestNEResult = bNE.compute();

                double bneObj = bNE.getObjectiveValue();
                double wneObj = wNE.getObjectiveValue();

//                System.out.println("*****");
//                checkIfNE(root, info, seqConfig, expander, welfareResult);
//                checkIfNE(root, info, seqConfig, expander, worstNEResult);
//                checkIfNE(root, info, seqConfig, expander, bestNEResult);
                double[] bestNEValues = computeExpectedValue(bestNEResult.p1RealPlan, strategy, expander, seqConfig, root);
                double[] worstNEValues = computeExpectedValue(worstNEResult.p1RealPlan, strategy, expander, seqConfig, root);

                evaluate(neResult.p1RealPlan, strategy, root, expander, seqConfig, neWriter, worstNEValues[0], bestNEValues[0]);
                evaluate(undomResult.p1RealPlan, strategy, root, expander, seqConfig, undomWriter, worstNEValues[0], bestNEValues[0]);
                evaluate(qpResult.p1RealPlan, strategy, root, expander, seqConfig, qpWriter, worstNEValues[0], bestNEValues[0]);
                evaluate(p1MaxResult.p1RealPlan, strategy, root, expander, seqConfig, p1MaxWriter, worstNEValues[0], bestNEValues[0]);
                evaluate(welfareResult.p1RealPlan, strategy, root, expander, seqConfig, welfareWriter, worstNEValues[0], bestNEValues[0]);
                write(maximinWriter, computeExpectedValue(maximinResult.p1RealPlan, strategy, expander, seqConfig, root));
                write(stackWriter, computeExpectedValue(stackResult.get(root.getAllPlayers()[0]), strategy, expander, seqConfig, root));
                write(brWriter, computeExpectedValue(br.getBRStategy(), strategy, expander, seqConfig, root));
                write(wrWriter, computeExpectedValue(wr.getBRStategy(), strategy, expander, seqConfig, root));
                write(cfrWriter, computeExpectedValue(cfrP1RealPlan, strategy, expander, seqConfig, root));
                write(cfrEpsWriter, computeExpectedValue(cfrEpsilonP1RealPlan, strategy, expander, seqConfig, root));
                write(mctsWriter, computeExpectedValue(mctsP1RealPlan, strategy, expander, seqConfig, root));
                write(bestNEWriter, bestNEValues);
                write(worstNEWriter, worstNEValues);
                System.out.println();
            }

            cfrEpsWriter.newLine();
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
            bestNEWriter.newLine();
            worstNEWriter.newLine();
            maximinWriter.newLine();

            cfrEpsWriter.close();
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
            bestNEWriter.close();
            worstNEWriter.close();
            maximinWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double getBRValue(Map<Sequence, Double> strategy, GameState root, Expander<? extends InformationSet> expander, GenSumSequenceFormConfig algConfig, GameInfo info) {
        GeneralSumBestResponse br = new GeneralSumBestResponse(expander, 0, getActingPlayers(root), algConfig, info);

        return br.calculateBR(root, strategy);
    }

    public static void evaluate(Map<Sequence, Double> p1RealPlan, Map<Sequence, Double> p2RealPlan, GameState root, Expander<MCTSInformationSet> expander, GenSumSequenceFormConfig seqConfig, BufferedWriter writer, double lowerBound, double upperBound) throws IOException {
        double[] values = computeExpectedValue(p1RealPlan, p2RealPlan, expander, seqConfig, root);

//        UtilityCalculator calculator = new UtilityCalculator(root, expander);
//        Strategy p1Strtategy = new NoMissingSeqStrategy(p1RealPlan);
//        Strategy p2Strtategy = new NoMissingSeqStrategy(p2RealPlan);
//        double reward =  calculator.computeUtility(p1Strtategy, p2Strtategy);
//        values[0] = reward;
//        if(Math.abs(reward - values[0]) > 1e-3)
//            System.err.println("");
        if (values[0] > upperBound + 1e-2 || values[0] < lowerBound - 1e-2)
            System.err.println("error reward: " + values[0] + " brNE: " + upperBound + " wr: " + lowerBound);
        write(writer, values);
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

    public static void writeLambdas(BufferedWriter writer, List<Double> lambdas) throws IOException {
        writer.write(lambdas.get(0) + "");
        for (int i = 1; i < lambdas.size(); i++) {
            writer.write(", " + lambdas.get(i));
        }
        writer.newLine();
        writer.flush();
    }
//
//    public static void evaluateP1StrategiesAgainstPureRps(SolverResult neResult, SolverResult undomResult, SolverResult qpResult, SolverResult p1MaxResult, SolverResult welfareResult, Map<Player, Map<Sequence, Double>> stackResult, Iterator<Set<Sequence>> iterator, GameState root, Expander<SequenceInformationSet> expander) {
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

    public static void write(BufferedWriter writer, double[] expVals) throws IOException {
//        System.out.println(expVals[0] + ", " + expVals[1] + ", ");
        writer.write(expVals[0] + ", " + expVals[1] + ", ");
        writer.flush();
    }

    public static Map<Sequence, Double> toRp(Set<Sequence> pureRP) {
        Map<Sequence, Double> realPlan = new HashMap<>(pureRP.size());

        for (Sequence sequence : pureRP) {
            realPlan.put(sequence, 1d);
        }
        return realPlan;
    }

    public static double[] computeExpectedValue(Map<Sequence, Double> p1RealPlan, Map<Sequence, Double> p2RealPlan, Expander<? extends InformationSet> expander, GenSumSequenceFormConfig config, GameState root) {
//        double[] expVals = new double[2];
//
//        for (Map.Entry<Map<Player, Sequence>, Double[]> entry : config.getUtilityForSequenceCombinationGenSum().entrySet()) {
//            Double p1Prob = p1RealPlan.get(entry.getKey().get(root.getAllPlayers()[0]));
//            Double p2Prob = p2RealPlan.get(entry.getKey().get(root.getAllPlayers()[1]));
//
//            if(p1Prob != null && p2Prob != null) {
//                expVals[0] += p1Prob*p2Prob*entry.getValue()[0];
//                expVals[1] += p1Prob*p2Prob*entry.getValue()[1];
//            }
//        }
//        return expVals;
        if (root.isGameEnd())
            return getWeightedUtilities(p1RealPlan, p2RealPlan, root);
        if (root.isPlayerToMoveNature())
            return expectedValuesForNature(p1RealPlan, p2RealPlan, root, expander, config);
        double[] expectedUtilities = new double[root.getAllPlayers().length];

        for (Action action : expander.getActions(root)) {
            GameState nextState = root.performAction(action);
            double realizationProb = getRealProb(p1RealPlan, p2RealPlan, nextState.getSequenceFor(root.getPlayerToMove()));

            if (realizationProb > 0) {
                double[] actionUtilities = computeExpectedValue(p1RealPlan, p2RealPlan, expander, config, nextState);

                for (int i = 0; i < actionUtilities.length; i++) {
                    expectedUtilities[i] += actionUtilities[i];
                }
            }
        }
        return expectedUtilities;
    }

    public static double[] computeExpectedValueFromConfig(Map<Sequence, Double> p1RealPlan, Map<Sequence, Double> p2RealPlan, Expander<? extends InformationSet> expander, GenSumSequenceFormConfig config, GameState root) {
        double[] expVals = new double[2];

        for (Map.Entry<Map<Player, Sequence>, Double[]> entry : config.getUtilityForSequenceCombinationGenSum().entrySet()) {
            Double p1Prob = p1RealPlan.get(entry.getKey().get(root.getAllPlayers()[0]));
            Double p2Prob = p2RealPlan.get(entry.getKey().get(root.getAllPlayers()[1]));

            if (p1Prob != null && p2Prob != null) {
                expVals[0] += p1Prob * p2Prob * entry.getValue()[0];
                expVals[1] += p1Prob * p2Prob * entry.getValue()[1];
            }
        }
        return expVals;
//        if (root.isGameEnd())
//            return getWeightedUtilities(p1RealPlan, p2RealPlan, root);
//        if (root.isPlayerToMoveNature())
//            return expectedValuesForNature(p1RealPlan, p2RealPlan, root, expander, config);
//        double[] expectedUtilities = new double[root.getAllPlayers().length];
//
//        for (Action action : expander.getActions(root)) {
//            GameState nextState = root.performAction(action);
//            double realizationProb = getRealProb(p1RealPlan, p2RealPlan, nextState.getSequenceFor(root.getPlayerToMove()));
//
//            if (realizationProb > 1e-5) {
//                double[] actionUtilities = computeExpectedValue(p1RealPlan, p2RealPlan, expander, config, nextState);
//
//                for (int i = 0; i < actionUtilities.length; i++) {
//                    expectedUtilities[i] += actionUtilities[i];
//                }
//            }
//        }
//        return expectedUtilities;
    }

    public static double[] expectedValuesForNature(Map<Sequence, Double> p1RealPlan, Map<Sequence, Double> p2RealPlan, GameState root, Expander<? extends InformationSet> expander, GenSumSequenceFormConfig config) {
        double[] expectedUtilities = new double[root.getAllPlayers().length];

        for (Action action : expander.getActions(root)) {
            double[] actionUtilities = computeExpectedValue(p1RealPlan, p2RealPlan, expander, config, root.performAction(action));

            for (int i = 0; i < actionUtilities.length; i++) {
                expectedUtilities[i] += actionUtilities[i];
            }
        }
        return expectedUtilities;
    }

    public static double getRealProb(Map<Sequence, Double> p1RealPlan, Map<Sequence, Double> p2RealPlan, Sequence sequence) {
        Double prob;

        if (sequence.getPlayer().getId() == 0)
            prob = p1RealPlan.get(sequence);
        else
            prob = p2RealPlan.get(sequence);
        return prob == null ? 0 : prob;
    }

    public static double[] getWeightedUtilities(Map<Sequence, Double> p1RealPlan, Map<Sequence, Double> p2RealPlan, GameState root) {
        double[] utilities = root.getUtilities();
        double probability = root.getNatureProbability() *
                p1RealPlan.get(root.getSequenceFor(root.getAllPlayers()[0])) * p2RealPlan.get(root.getSequenceFor(root.getAllPlayers()[1]));

        if (probability < 1e-4)
            return new double[utilities.length];
        for (int i = 0; i < utilities.length; i++) {
            utilities[i] *= probability;
        }
        return utilities;
    }


    public static String getDomainDependentString() {
        return "RndBF" + RandomGameInfo.MAX_BF + "D" + RandomGameInfo.MAX_DEPTH + "COR" + RandomGameInfo.CORRELATION;
    }
}
