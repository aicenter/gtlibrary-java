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
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenSumGPGameState;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.randomgame.GeneralSumRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class StrategyStrengthLargeExperiments {

    /**
     * bf, depth, corr, observations, seed
     * card types, card count, bet count, raise count, rake
     *
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("****Large experiments****");
//        runGenSumKuhnPoker(0.1);
//        runGenSumBPG(Integer.parseInt(args[0]));
//        runGenSumGenericPoker(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Double.parseDouble(args[4]));
//        runGenSumPursuit(1);
        runGenSumRandomGames(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Double.parseDouble(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
//        runGoofspiel(Integer.parseInt(args[0]));
    }

//    private static void runGenSumPursuit(int depth) {
//        PursuitGameInfo.depth = depth;
//        GameState root = new GenSumPursuitGameState();
//        GameInfo info = new PursuitGameInfo();
//        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
//        Expander<SequenceInformationSet> expander = new PursuitExpander<>(algConfig);
//        FullSequenceEFG builder = new FullSequenceEFG(root, expander, info, algConfig);
//
//        builder.generateCompleteGame();
//
//        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
//        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;
//
//        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
//        SolverResult neResult = neSolver.compute();
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
//        if (qpResult != null && neResult != null) {
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
////            evaluateAgainstWRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
////            evaluateAgainstBRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//            evaluateP1StrategiesAgainstMCTS(neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1]);
//            evaluateP1StrategiesAgainstCFR(neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1]);
//            evaluateP1StrategiesAgainstQRE(neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//        }
//    }

//    private static void runGenSumBPG(int depth) {
//        BPGGameInfo.DEPTH = depth;
//        GameState root = new GenSumBPGGameState();
//        GameInfo info = new BPGGameInfo();
//        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
//        Expander<SequenceInformationSet> expander = new BPGExpander<>(algConfig);
//        FullSequenceEFG builder = new FullSequenceEFG(root, expander, info, algConfig);
//
//        builder.generateCompleteGame();
//
//        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;
//
//        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
//        DataBuilder.alg = DataBuilder.Alg.lemkeNash2;
//
//        SolverResult neResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
//
//        StrategyStrengthExperiments.checkIfNE(root, info, algConfig, expander, qpResult);
//        StrategyStrengthExperiments.checkIfNE(root, info, algConfig, expander, neResult);
//        MCTSConfig mctsP1Config = new MCTSConfig(new Random(1));
//        Expander<MCTSInformationSet> mctsP1Expander = new BPGExpander<>(mctsP1Config);
//        MCTSConfig cfrP1Config = new MCTSConfig(new Random(1));
//        Expander<MCTSInformationSet> cfrP1Expander = new BPGExpander<>(cfrP1Config);
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
//        if (qpResult != null && neResult != null) {
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
////            evaluateAgainstWRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
////            evaluateAgainstBRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//            evaluateP1StrategiesAgainstMCTS(neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1]);
//            evaluateP1StrategiesAgainstCFR(neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1]);
//            evaluateP1StrategiesAgainstQRE(neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//        }
//    }


//    private static void runGenSumKuhnPoker(double rake) {
//        GameState root = new GenSumKuhnPokerGameState(rake);
//        GameInfo info = new KPGameInfo();
//        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
//        Expander<SequenceInformationSet> expander = new KuhnPokerExpander<>(algConfig);
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
//        SolverResult neResult = neSolver.compute();
//        MCTSConfig mctsP1Config = new MCTSConfig(new Random(1));
//        Expander<MCTSInformationSet> mctsP1Expander = new RandomGameExpander<>(mctsP1Config);
//        MCTSConfig cfrP1Config = new MCTSConfig(new Random(1));
//        Expander<MCTSInformationSet> cfrP1Expander = new RandomGameExpander<>(cfrP1Config);
//
//        Map<Sequence, Double> cfrP1RealPlan = getCFRStrategy(root, cfrP1Expander);
//        Map<Sequence, Double> mctsP1RealPlan = getMCTSStrategy(root, mctsP1Expander, info);
//
//        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
//        Expander<SequenceInformationSet> stackelbergExpander = new KuhnPokerExpander<>(stackelbergConfig);
//        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
//        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
//        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver);
//
//
//        if (qpResult != null && neResult != null) {
//            StackelbergConfig stackConfig = new StackelbergConfig(root);
//            Expander<SequenceInformationSet> stackExpander = new KuhnPokerExpander<>(stackConfig);
//            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);
//
//            stackBuilder.generateCompleteGame();
//            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
//            Expander<MCTSInformationSet> mctsExpander = new KuhnPokerExpander<>(mctsConfig);
//
//            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
//            Expander<MCTSInformationSet> cfrExpander = new KuhnPokerExpander<>(cfrConfig);
////            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
////            evaluateAgainstWRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
////            evaluateAgainstBRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//            evaluateP1StrategiesAgainstMCTS(neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1]);
//            evaluateP1StrategiesAgainstCFR(neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1]);
//            evaluateP1StrategiesAgainstQRE(neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//        }
//    }

    private static void runGenSumGenericPoker(int cardTypes, int cardCount, int betCount, int raiseCount, double rake) {
        GPGameInfo.MAX_CARD_TYPES = cardTypes;
        GPGameInfo.MAX_CARD_OF_EACH_TYPE = cardCount;
        GPGameInfo.MAX_DIFFERENT_BETS = betCount;
        GPGameInfo.MAX_DIFFERENT_RAISES = raiseCount;

        GPGameInfo.RAISES_FIRST_ROUND = new int[GPGameInfo.MAX_DIFFERENT_RAISES];
        for (int i = 0; i < GPGameInfo.MAX_DIFFERENT_RAISES; i++)
            GPGameInfo.RAISES_FIRST_ROUND[i] = (i + 1) * 2;
        GPGameInfo.CARD_TYPES = new int[GPGameInfo.MAX_CARD_TYPES];
        for (int i = 0; i < GPGameInfo.MAX_CARD_TYPES; i++)
            GPGameInfo.CARD_TYPES[i] = i;
        GPGameInfo.DECK = new int[GPGameInfo.MAX_CARD_OF_EACH_TYPE * GPGameInfo.MAX_CARD_TYPES];
        for (int i = 0; i < GPGameInfo.MAX_CARD_TYPES; i++)
            for (int j = 0; j < GPGameInfo.MAX_CARD_OF_EACH_TYPE; j++) {
                GPGameInfo.DECK[i * GPGameInfo.MAX_CARD_OF_EACH_TYPE + j] = i;
            }
        GPGameInfo.BETS_FIRST_ROUND = new int[GPGameInfo.MAX_DIFFERENT_BETS];
        for (int i = 0; i < GPGameInfo.MAX_DIFFERENT_BETS; i++)
            GPGameInfo.BETS_FIRST_ROUND[i] = (i + 1) * 2;

        GPGameInfo.RAISES_FIRST_ROUND = new int[GPGameInfo.MAX_DIFFERENT_RAISES];
        for (int i = 0; i < GPGameInfo.MAX_DIFFERENT_RAISES; i++)
            GPGameInfo.RAISES_FIRST_ROUND[i] = (i + 1) * 2;

        GPGameInfo.BETS_SECOND_ROUND = new int[GPGameInfo.BETS_FIRST_ROUND.length];
        for (int i = 0; i < GPGameInfo.BETS_FIRST_ROUND.length; i++) {
            GPGameInfo.BETS_SECOND_ROUND[i] = 2 * GPGameInfo.BETS_FIRST_ROUND[i];
        }

        GPGameInfo.RAISES_SECOND_ROUND = new int[GPGameInfo.RAISES_FIRST_ROUND.length];
        for (int i = 0; i < GPGameInfo.RAISES_FIRST_ROUND.length; i++) {
            GPGameInfo.RAISES_SECOND_ROUND[i] = 2 * GPGameInfo.RAISES_FIRST_ROUND[i];
        }
        GameState root = new GenSumGPGameState();
        GameInfo info = new GPGameInfo();
        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new GenericPokerExpander<>(algConfig);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, info, algConfig);
        FullSequenceFormLP lp = new SequenceFormLP(getActingPlayers(root));

        builder.generateCompleteGame();

        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;

        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
        DataBuilder.alg = DataBuilder.Alg.lemkeNash2;
        Map<Player, Map<Sequence, Double>> maximinRps = builder.generate();

        SolverResult maximinResult = new SolverResult(maximinRps.get(root.getAllPlayers()[0]), maximinRps.get(root.getAllPlayers()[1]), 0);
        SolverResult neResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
        StrategyStrengthExperiments.checkIfNE(root, info, algConfig, expander, qpResult);
        StrategyStrengthExperiments.checkIfNE(root, info, algConfig, expander, neResult);
        MCTSConfig mctsP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> mctsP1Expander = new GenericPokerExpander<>(mctsP1Config);
        OOSAlgorithmData.useEpsilonRM = false;
        MCTSConfig cfrP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> cfrP1Expander = new GenericPokerExpander<>(cfrP1Config);

        Map<Sequence, Double> cfrP1RealPlan = getCFRStrategy(root, cfrP1Expander);
        Map<Sequence, Double> mctsP1RealPlan = getMCTSStrategy(root, mctsP1Expander, info);
        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
        Expander<SequenceInformationSet> stackelbergExpander = new GenericPokerExpander<>(stackelbergConfig);
        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
        Map<Player, Map<Sequence, Double>> rps = new HashMap<>(2);//runner.generate(root.getAllPlayers()[0], solver);

        rps.put(root.getAllPlayers()[0], new HashMap<Sequence, Double>());
        rps.put(root.getAllPlayers()[1], new HashMap<Sequence, Double>());

        if (qpResult != null && neResult != null) {
            StackelbergConfig stackConfig = new StackelbergConfig(root);
            Expander<SequenceInformationSet> stackExpander = new GenericPokerExpander<>(stackConfig);
            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);

            stackBuilder.generateCompleteGame();
            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> mctsExpander = new GenericPokerExpander<>(mctsConfig);

            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> cfrExpander = new GenericPokerExpander<>(cfrConfig);
//            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
//            evaluateAgainstWRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//            evaluateAgainstBRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstMCTS(maximinResult, neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstCFR(maximinResult, neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstQRE(maximinResult, neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
        }
    }

//    private static void runGoofspiel(int cardCount) {
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
//        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;
//
//        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
//        DataBuilder.alg = DataBuilder.Alg.lemkeNash2;
//
//        SolverResult neResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
//        StrategyStrengthExperiments.checkIfNE(root, info, algConfig, expander, qpResult);
//        StrategyStrengthExperiments.checkIfNE(root, info, algConfig, expander, neResult);
//        MCTSConfig mctsP1Config = new MCTSConfig(new Random(1));
//        Expander<MCTSInformationSet> mctsP1Expander = new GoofSpielExpander<>(mctsP1Config);
//        MCTSConfig cfrP1Config = new MCTSConfig(new Random(1));
//        Expander<MCTSInformationSet> cfrP1Expander = new GoofSpielExpander<>(cfrP1Config);
//
//        Map<Sequence, Double> cfrP1RealPlan = getCFRStrategy(root, cfrP1Expander);
//        Map<Sequence, Double> mctsP1RealPlan = getMCTSStrategy(root, mctsP1Expander, info);
//        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
//        Expander<SequenceInformationSet> stackelbergExpander = new GoofSpielExpander<>(stackelbergConfig);
//        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
//        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
//        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver);
//
//
//        if (qpResult != null && neResult != null) {
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
////            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
////            evaluateAgainstWRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
////            evaluateAgainstBRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//            evaluateP1StrategiesAgainstMCTS(neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1]);
//            evaluateP1StrategiesAgainstCFR(neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1]);
//            evaluateP1StrategiesAgainstQRE(neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//        }
//    }

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

        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;

        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "GenSumRndGameRepr");
        DataBuilder.alg = DataBuilder.Alg.lemkeNash2;

        FullSequenceFormLP lp = new SequenceFormLP(getActingPlayers(root));
        SolverResult neResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "GenSumRndGameRepr");
        MCTSConfig mctsP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> mctsP1Expander = new RandomGameExpander<>(mctsP1Config);
        MCTSConfig cfrP1Config = new MCTSConfig(new Random(1));
        Expander<MCTSInformationSet> cfrP1Expander = new RandomGameExpander<>(cfrP1Config);
        lp.calculateBothPlStrategy(root, algConfig);

        SolverResult maximinResult = new SolverResult(lp.getResultStrategiesForPlayer(root.getAllPlayers()[0]), lp.getResultStrategiesForPlayer(root.getAllPlayers()[1]), 0);
        OOSAlgorithmData.useEpsilonRM = false;
        Map<Sequence, Double> cfrP1RealPlan = getCFRStrategy(root, cfrP1Expander);
        Map<Sequence, Double> mctsP1RealPlan = getMCTSStrategy(root, mctsP1Expander, info);

        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
        Expander<SequenceInformationSet> stackelbergExpander = new RandomGameExpander<>(stackelbergConfig);
        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(root.getAllPlayers(), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver).getRight();


        if (qpResult != null && neResult != null) {
            StackelbergConfig stackConfig = new StackelbergConfig(root);
            Expander<SequenceInformationSet> stackExpander = new RandomGameExpander<>(stackConfig);
            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);

            stackBuilder.generateCompleteGame();
            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> mctsExpander = new RandomGameExpander<>(mctsConfig);

            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> cfrExpander = new RandomGameExpander<>(cfrConfig);
//            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
//            evaluateAgainstWRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
//            evaluateAgainstBRNE(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstMCTS(maximinResult, neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstCFR(maximinResult, neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstQRE(maximinResult, neResult, qpResult, rps, cfrP1RealPlan, mctsP1RealPlan, root, expander, info, algConfig, root.getAllPlayers()[1]);
        }
    }

    private static Map<Sequence, Double> getMCTSStrategy(GameState root, Expander<MCTSInformationSet> expander, GameInfo info) {
        ISMCTSExploitability.rootState = root;
        ISMCTSExploitability.expander = expander;
        BackPropFactory factory = new UCTBackPropFactory(Math.sqrt(2) * info.getMaxUtility());
        GenSumISMCTSNestingRunner.alg = new GenSumISMCTSAlgorithm(root.getAllPlayers()[1], new DefaultSimulator(expander), factory, root, expander);
        buildMCTSCompleteTree(GenSumISMCTSNestingRunner.alg.getRootNode(), factory);
        GenSumISMCTSNestingRunner.buildStichedStrategy(root.getAllPlayers()[0], GenSumISMCTSNestingRunner.alg.getRootNode().getInformationSet(),
                GenSumISMCTSNestingRunner.alg.getRootNode(), 100000);
        GenSumISMCTSNestingRunner.alg.resetRootNode();
        return StrategyCollector.getStrategyFor(GenSumISMCTSNestingRunner.alg.getRootNode(), root.getAllPlayers()[0], new MeanStratDist());
    }

    private static Map<Sequence, Double> getCFRStrategy(GameState root, Expander<MCTSInformationSet> expander) {
        CFRAlgorithm cfr = new CFRAlgorithm(root.getAllPlayers()[0], root, expander);

        buildCFRCompleteTree(cfr.getRootNode());
        cfr.runIterations(100000);
        return StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[0], new MeanStratDist());
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

    private static void evaluateP1StrategiesAgainstQRE(SolverResult maximinResult, SolverResult neResult, SolverResult qpResult, Map<Player, Map<Sequence, Double>> stackResult,
                                                       Map<Sequence, Double> cfrP1RealPlan, Map<Sequence, Double> mctsP1RealPlan, GameState root, Expander<SequenceInformationSet> expander, GameInfo info, SequenceFormConfig<SequenceInformationSet> algConfig, Player player) {
        try {
            BufferedWriter maximinWriter = new BufferedWriter(new FileWriter("P1MAXIMINvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter neWriter = new BufferedWriter(new FileWriter("P1NEvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter qpWriter = new BufferedWriter(new FileWriter("P1QPvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter stackWriter = new BufferedWriter(new FileWriter("P1StackvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter brWriter = new BufferedWriter(new FileWriter("P1BRvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter wrWriter = new BufferedWriter(new FileWriter("P1WRvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter cfrWriter = new BufferedWriter(new FileWriter("P1CFRvsQREExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter mctsWriter = new BufferedWriter(new FileWriter("P1MCTSvsQREExpVal" + getDomainDependentString() + ".csv", true));

            QRESolver solver = new QRESolver(root, expander, info, algConfig);
            QREResult qreResult = solver.solve();

            writeLambdas(maximinWriter, qreResult.lambdas);
            writeLambdas(neWriter, qreResult.lambdas);
            writeLambdas(qpWriter, qreResult.lambdas);
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
                write(maximinWriter, computeExpectedValue(maximinResult.p1RealPlan, qreStrategy, root, expander));
                write(brWriter, computeExpectedValue(br.getBRStategy(), qreStrategy, root, expander));
                write(wrWriter, computeExpectedValue(wr.getBRStategy(), qreStrategy, root, expander));
                write(neWriter, computeExpectedValue(neResult.p1RealPlan, qreStrategy, root, expander));
                write(qpWriter, computeExpectedValue(qpResult.p1RealPlan, qreStrategy, root, expander));
                write(stackWriter, computeExpectedValue(stackResult.get(root.getAllPlayers()[0]), qreStrategy, root, expander));
                write(cfrWriter, computeExpectedValue(cfrP1RealPlan, qreStrategy, root, expander));
                write(mctsWriter, computeExpectedValue(mctsP1RealPlan, qreStrategy, root, expander));
            }
            neWriter.newLine();
            qpWriter.newLine();
            stackWriter.newLine();
            brWriter.newLine();
            wrWriter.newLine();
            cfrWriter.newLine();
            mctsWriter.newLine();
            maximinWriter.newLine();

            neWriter.close();
            qpWriter.close();
            stackWriter.close();
            brWriter.close();
            wrWriter.close();
            cfrWriter.close();
            mctsWriter.close();
            maximinWriter.close();
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

    private static void evaluateP1StrategiesAgainstCFR(SolverResult maximinResult, SolverResult neResult, SolverResult qpResult, Map<Player, Map<Sequence, Double>> stackResult,
                                                       Map<Sequence, Double> cfrP1RealPlan, Map<Sequence, Double> mctsP1RealPlan, GameState root, Expander<MCTSInformationSet> expander, GameInfo info, MCTSConfig algConfig, Player player) {
        try {
            BufferedWriter maximinWriter = new BufferedWriter(new FileWriter("P1MAXIMINvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter neWriter = new BufferedWriter(new FileWriter("P1NEvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter qpWriter = new BufferedWriter(new FileWriter("P1QPvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter stackWriter = new BufferedWriter(new FileWriter("P1StackvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter brWriter = new BufferedWriter(new FileWriter("P1BRvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter wrWriter = new BufferedWriter(new FileWriter("P1WRvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter cfrWriter = new BufferedWriter(new FileWriter("P1CFRvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter mctsWriter = new BufferedWriter(new FileWriter("P1MCTSvsCFRExpVal" + getDomainDependentString() + ".csv", true));

            CFRAlgorithm cfr = new CFRAlgorithm(root.getAllPlayers()[1], root, expander);

            buildCFRCompleteTree(cfr.getRootNode());
            for (int i = 0; i < 500; i++) {
                cfr.runIterations(20);
                Strategy strategy = StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[1], new MeanStratDist());
                GeneralSumBestResponse br = new GeneralSumBestResponse(expander, 0, getActingPlayers(root), algConfig, info);
                GeneralSumWorstResponse wr = new GeneralSumWorstResponse(expander, 0, getActingPlayers(root), algConfig, info);

                br.calculateBR(root, strategy);
                wr.calculateBR(root, strategy);
                write(maximinWriter, computeExpectedValue(maximinResult.p1RealPlan, strategy, root, expander));
                write(neWriter, computeExpectedValue(neResult.p1RealPlan, strategy, root, expander));
                write(qpWriter, computeExpectedValue(qpResult.p1RealPlan, strategy, root, expander));
                write(stackWriter, computeExpectedValue(stackResult.get(root.getAllPlayers()[0]), strategy, root, expander));
                write(brWriter, computeExpectedValue(br.getBRStategy(), strategy, root, expander));
                write(wrWriter, computeExpectedValue(wr.getBRStategy(), strategy, root, expander));
                write(cfrWriter, computeExpectedValue(cfrP1RealPlan, strategy, root, expander));
                write(mctsWriter, computeExpectedValue(mctsP1RealPlan, strategy, root, expander));
            }
            neWriter.newLine();
            qpWriter.newLine();
            stackWriter.newLine();
            brWriter.newLine();
            wrWriter.newLine();
            cfrWriter.newLine();
            mctsWriter.newLine();
            maximinWriter.newLine();

            neWriter.close();
            qpWriter.close();
            stackWriter.close();
            brWriter.close();
            wrWriter.close();
            cfrWriter.close();
            mctsWriter.close();
            maximinWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void evaluateP1StrategiesAgainstMCTS(SolverResult maximinResult, SolverResult neResult, SolverResult qpResult, Map<Player, Map<Sequence, Double>> stackResult,
                                                        Map<Sequence, Double> cfrP1RealPlan, Map<Sequence, Double> mctsP1RealPlan, GameState root, Expander<MCTSInformationSet> expander, GameInfo info, MCTSConfig algConfig, Player player) {
        try {
            BufferedWriter maximinWriter = new BufferedWriter(new FileWriter("P1MAXIMINvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter neWriter = new BufferedWriter(new FileWriter("P1NEvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter qpWriter = new BufferedWriter(new FileWriter("P1QPvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter stackWriter = new BufferedWriter(new FileWriter("P1StackvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter brWriter = new BufferedWriter(new FileWriter("P1BRvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter wrWriter = new BufferedWriter(new FileWriter("P1WRvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter cfrWriter = new BufferedWriter(new FileWriter("P1CFRvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            BufferedWriter mctsWriter = new BufferedWriter(new FileWriter("P1MCTSvsMCTSExpVal" + getDomainDependentString() + ".csv", true));

            ISMCTSExploitability.rootState = root;
            ISMCTSExploitability.expander = expander;
            ISMCTSExploitability.gameInfo = info;
            BackPropFactory factory = new UCTBackPropFactory(Math.sqrt(2) * info.getMaxUtility());
            GenSumISMCTSNestingRunner.alg = new GenSumISMCTSAlgorithm(root.getAllPlayers()[1], new DefaultSimulator(expander), factory, root, expander);
            buildMCTSCompleteTree(GenSumISMCTSNestingRunner.alg.getRootNode(), factory);
            for (int i = 0; i < 500; i++) {
                GenSumISMCTSNestingRunner.clear();
                GenSumISMCTSNestingRunner.buildStichedStrategy(root.getAllPlayers()[1], GenSumISMCTSNestingRunner.alg.getRootNode().getInformationSet(),
                        GenSumISMCTSNestingRunner.alg.getRootNode(), 50);
                GenSumISMCTSNestingRunner.alg.resetRootNode();
                Strategy strategy = StrategyCollector.getStrategyFor(GenSumISMCTSNestingRunner.alg.getRootNode(), root.getAllPlayers()[1], new MeanStratDist());

                GeneralSumBestResponse br = new GeneralSumBestResponse(expander, 0, getActingPlayers(root), algConfig, info);
                GeneralSumWorstResponse wr = new GeneralSumWorstResponse(expander, 0, getActingPlayers(root), algConfig, info);

                br.calculateBR(root, strategy);
                wr.calculateBR(root, strategy);
                write(maximinWriter, computeExpectedValue(maximinResult.p1RealPlan, strategy, root, expander));
                write(neWriter, computeExpectedValue(neResult.p1RealPlan, strategy, root, expander));
                write(qpWriter, computeExpectedValue(qpResult.p1RealPlan, strategy, root, expander));
                write(stackWriter, computeExpectedValue(stackResult.get(root.getAllPlayers()[0]), strategy, root, expander));
                write(brWriter, computeExpectedValue(br.getBRStategy(), strategy, root, expander));
                write(wrWriter, computeExpectedValue(wr.getBRStategy(), strategy, root, expander));
                write(cfrWriter, computeExpectedValue(cfrP1RealPlan, strategy, root, expander));
                write(mctsWriter, computeExpectedValue(mctsP1RealPlan, strategy, root, expander));
            }

            neWriter.newLine();
            qpWriter.newLine();
            stackWriter.newLine();
            brWriter.newLine();
            wrWriter.newLine();
            cfrWriter.newLine();
            mctsWriter.newLine();
            maximinWriter.newLine();

            neWriter.close();
            qpWriter.close();
            stackWriter.close();
            brWriter.close();
            wrWriter.close();
            cfrWriter.close();
            mctsWriter.close();
            maximinWriter.close();
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
