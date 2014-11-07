package cz.agents.gtlibrary.algorithms.sequenceform.gensum.experiments;

import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.*;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
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

public class StrategyStrengthLargeExperiments {
    public static BufferedWriter neWriter;
    public static BufferedWriter undomWriter;
    public static BufferedWriter qpWriter;
    public static BufferedWriter p1MaxWriter;
    public static BufferedWriter welfareWriter;
    public static BufferedWriter stackWriter;
    public static BufferedWriter brWriter;
    public static BufferedWriter wrWriter;

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

        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;

        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
//        SolverResult undomResult = undomSolver.compute();
        SolverResult neResult = neSolver.compute();
//        SolverResult p1MaxResult = p1MaxSolver.compute();
//        SolverResult welfareResult = welfareSolver.compute();

        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
        Expander<SequenceInformationSet> stackelbergExpander = new PursuitExpander<>(stackelbergConfig);
        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver);


        if (qpResult != null && neResult != null) {
            StackelbergConfig stackConfig = new StackelbergConfig(root);
            Expander<SequenceInformationSet> stackExpander = new PursuitExpander<>(stackConfig);
            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);

            stackBuilder.generateCompleteGame();
            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> mctsExpander = new PursuitExpander<>(mctsConfig);

            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> cfrExpander = new PursuitExpander<>(cfrConfig);
//            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
            evaluateAgainstWRNE(neResult, qpResult, rps, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateAgainstBRNE(neResult, qpResult, rps, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstMCTS(neResult, qpResult, rps, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstCFR(neResult, qpResult, rps, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstQRE(neResult, qpResult, rps, root, expander, info, algConfig, root.getAllPlayers()[1]);
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

        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;

        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
//        SolverResult undomResult = undomSolver.compute();
        SolverResult neResult = neSolver.compute();
//        SolverResult p1MaxResult = p1MaxSolver.compute();
//        SolverResult welfareResult = welfareSolver.compute();

        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
        Expander<SequenceInformationSet> stackelbergExpander = new BPGExpander<>(stackelbergConfig);
        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver);


        if (qpResult != null && neResult != null) {
            StackelbergConfig stackConfig = new StackelbergConfig(root);
            Expander<SequenceInformationSet> stackExpander = new BPGExpander<>(stackConfig);
            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);

            stackBuilder.generateCompleteGame();
            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> mctsExpander = new BPGExpander<>(mctsConfig);

            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> cfrExpander = new BPGExpander<>(cfrConfig);
//            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
//            evaluateAgainstWRNE(neResult, qpResult, rps, root, expander, info, algConfig, root.getAllPlayers()[1]);
//            evaluateAgainstBRNE(neResult, qpResult, rps, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstMCTS(neResult, qpResult, rps, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstCFR(neResult, qpResult, rps, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstQRE(neResult, qpResult, rps, root, expander, info, algConfig, root.getAllPlayers()[1]);
        }
    }


    private static void runGenSumKuhnPoker(double rake) {
        GameState root = new GenSumKuhnPokerGameState();
        GameInfo info = new KPGameInfo();
        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new KuhnPokerExpander<>(algConfig);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, info, algConfig);

        builder.generateCompleteGame();

        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;

        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
//        SolverResult undomResult = undomSolver.compute();
        SolverResult neResult = neSolver.compute();
//        SolverResult p1MaxResult = p1MaxSolver.compute();
//        SolverResult welfareResult = welfareSolver.compute();

        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
        Expander<SequenceInformationSet> stackelbergExpander = new KuhnPokerExpander<>(stackelbergConfig);
        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver);


        if (qpResult != null && neResult != null) {
            StackelbergConfig stackConfig = new StackelbergConfig(root);
            Expander<SequenceInformationSet> stackExpander = new KuhnPokerExpander<>(stackConfig);
            FullSequenceEFG stackBuilder = new FullSequenceEFG(root, stackExpander, info, stackConfig);

            stackBuilder.generateCompleteGame();
            MCTSConfig mctsConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> mctsExpander = new KuhnPokerExpander<>(mctsConfig);

            MCTSConfig cfrConfig = new MCTSConfig(new Random(1));
            Expander<MCTSInformationSet> cfrExpander = new KuhnPokerExpander<>(cfrConfig);
//            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, welfareResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
            evaluateAgainstWRNE(neResult, qpResult, rps, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateAgainstBRNE(neResult, qpResult, rps, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstMCTS(neResult, qpResult, rps, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstCFR(neResult, qpResult, rps, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstQRE(neResult, qpResult, rps, root, expander, info, algConfig, root.getAllPlayers()[1]);
        }
    }

    private static void runGenSumGenericPoker(double rake) {
        GameState root = new GenSumGPGameState();
        GameInfo info = new GPGameInfo();
        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new GenericPokerExpander<>(algConfig);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, info, algConfig);

        builder.generateCompleteGame();

        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;

        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
//        SolverResult undomResult = undomSolver.compute();
        SolverResult neResult = neSolver.compute();
//        SolverResult p1MaxResult = p1MaxSolver.compute();
//        SolverResult welfareResult = welfareSolver.compute();

        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
        Expander<SequenceInformationSet> stackelbergExpander = new GenericPokerExpander<>(stackelbergConfig);
        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver);


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
            evaluateAgainstWRNE(neResult, qpResult, rps, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateAgainstBRNE(neResult, qpResult, rps, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstMCTS(neResult, qpResult, rps, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstCFR(neResult, qpResult, rps, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstQRE(neResult, qpResult, rps, root, expander, info, algConfig, root.getAllPlayers()[1]);
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
        GameState root = new GeneralSumRandomGameState();
        GameInfo info = new RandomGameInfo();
        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new RandomGameExpander<>(algConfig);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, info, algConfig);

        builder.generateCompleteGame();

        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;

        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "KuhnPokerRepr");
//        SolverResult undomResult = undomSolver.compute();
        SolverResult neResult = neSolver.compute();
//        SolverResult p1MaxResult = p1MaxSolver.compute();
//        SolverResult welfareResult = welfareSolver.compute();

        StackelbergConfig stackelbergConfig = new StackelbergConfig(root);
        Expander<SequenceInformationSet> stackelbergExpander = new RandomGameExpander<>(stackelbergConfig);
        StackelbergRunner runner = new StackelbergRunner(root, stackelbergExpander, info, stackelbergConfig);
        StackelbergSequenceFormLP solver = new StackelbergSequenceFormMILP(getActingPlayers(root), root.getAllPlayers()[0], root.getAllPlayers()[1], info, expander);
        Map<Player, Map<Sequence, Double>> rps = runner.generate(root.getAllPlayers()[0], solver);


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
            evaluateAgainstWRNE(neResult, qpResult, rps, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateAgainstBRNE(neResult, qpResult, rps, root, expander, info, algConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstMCTS(neResult, qpResult, rps, root, mctsExpander, info, mctsConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstCFR(neResult, qpResult, rps, root, cfrExpander, info, cfrConfig, root.getAllPlayers()[1]);
            evaluateP1StrategiesAgainstQRE(neResult, qpResult, rps, root, expander, info, algConfig, root.getAllPlayers()[1]);
        }
    }

    private static void evaluateAgainstBRNE(SolverResult neResult, SolverResult qpResult, Map<Player, Map<Sequence, Double>> stackResult, GameState root, Expander<SequenceInformationSet> expander, GameInfo info, GenSumSequenceFormConfig algConfig, Player player) {
        try {
            neWriter = new BufferedWriter(new FileWriter("P1NEvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            qpWriter = new BufferedWriter(new FileWriter("P1QPvsBRNEExpVal" + getDomainDependentString() + ".csv", true));
            stackWriter = new BufferedWriter(new FileWriter("P1StackvsBRNEExpVal" + getDomainDependentString() + ".csv", true));

            SolverResult vsNeSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], neResult.p1RealPlan).compute();
            SolverResult vsQPSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], qpResult.p1RealPlan).compute();
            SolverResult vsStackSolverResult = new BRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], stackResult.get(root.getAllPlayers()[0])).compute();

            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsBRNEvsNEExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsBRNEvsNEExpVal" + getDomainDependentString() + ".csv", true)),
                    neWriter, neResult.p1RealPlan, vsNeSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsBRNEvsQPExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsBRNEvsQPExpVal" + getDomainDependentString() + ".csv", true)),
                    qpWriter, qpResult.p1RealPlan, vsQPSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsBRNEvsStackExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsBRNEvsStackExpVal" + getDomainDependentString() + ".csv", true)),
                    stackWriter, stackResult.get(root.getAllPlayers()[0]), vsStackSolverResult.p2RealPlan, root, expander, info, algConfig);
            neWriter.newLine();
            qpWriter.newLine();
            stackWriter.newLine();

            neWriter.close();
            qpWriter.close();
            stackWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void evaluateAgainstWRNE(SolverResult neResult, SolverResult qpResult, Map<Player, Map<Sequence, Double>> stackResult, GameState root, Expander<SequenceInformationSet> expander, GameInfo info, GenSumSequenceFormConfig algConfig, Player player) {
        try {
            neWriter = new BufferedWriter(new FileWriter("P1NEvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            qpWriter = new BufferedWriter(new FileWriter("P1QPvsWRNEExpVal" + getDomainDependentString() + ".csv", true));
            stackWriter = new BufferedWriter(new FileWriter("P1StackvsWRNEExpVal" + getDomainDependentString() + ".csv", true));

            SolverResult vsNeSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], neResult.p1RealPlan).compute();
            SolverResult vsQPSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], qpResult.p1RealPlan).compute();
            SolverResult vsStackSolverResult = new WRGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[1], stackResult.get(root.getAllPlayers()[0])).compute();
//
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsWRNEvsNEExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsWRNEvsNEExpVal" + getDomainDependentString() + ".csv", true)),
                    neWriter, neResult.p1RealPlan, vsNeSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsWRNEvsQPExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsWRNEvsQPExpVal" + getDomainDependentString() + ".csv", true)),
                    qpWriter, qpResult.p1RealPlan, vsQPSolverResult.p2RealPlan, root, expander, info, algConfig);
            evaluateBRWR(new BufferedWriter(new FileWriter("P1BRvsWRNEvsStackExpVal" + getDomainDependentString() + ".csv", true)), new BufferedWriter(new FileWriter("P1WRvsWRNEvsStackExpVal" + getDomainDependentString() + ".csv", true)),
                    stackWriter, stackResult.get(root.getAllPlayers()[0]), vsStackSolverResult.p2RealPlan, root, expander, info, algConfig);
            neWriter.newLine();
            qpWriter.newLine();
            stackWriter.newLine();

            neWriter.close();
            qpWriter.close();
            stackWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void evaluateBRWR(BufferedWriter brWriter, BufferedWriter wrWriter, BufferedWriter writer, Map<Sequence, Double> p1Strategy, Map<Sequence, Double> p2Strategy, GameState root, Expander<SequenceInformationSet> expander, GameInfo info, GenSumSequenceFormConfig algConfig) throws IOException {
        GeneralSumBestResponse br = new GeneralSumBestResponse(expander, 0, getActingPlayers(root), algConfig, info);
        GeneralSumWorstResponse wr = new GeneralSumWorstResponse(expander, 0, getActingPlayers(root), algConfig, info);

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

    private static void evaluateP1StrategiesAgainstQRE(SolverResult neResult, SolverResult qpResult, Map<Player, Map<Sequence, Double>> stackResult,
                                                       GameState root, Expander<SequenceInformationSet> expander, GameInfo info, SequenceFormConfig<SequenceInformationSet> algConfig, Player player) {
        try {
            neWriter = new BufferedWriter(new FileWriter("P1NEvsQREExpVal" + getDomainDependentString() + ".csv", true));
            qpWriter = new BufferedWriter(new FileWriter("P1QPvsQREExpVal" + getDomainDependentString() + ".csv", true));
            stackWriter = new BufferedWriter(new FileWriter("P1StackvsQREExpVal" + getDomainDependentString() + ".csv", true));
            brWriter = new BufferedWriter(new FileWriter("P1BRvsQREExpVal" + getDomainDependentString() + ".csv", true));
            wrWriter = new BufferedWriter(new FileWriter("P1WRvsQREExpVal" + getDomainDependentString() + ".csv", true));

            QRESolver solver = new QRESolver(root, expander, info, algConfig);
            QREResult qreResult = solver.solve();

            writeLambdas(neWriter, qreResult.lambdas);
            writeLambdas(qpWriter, qreResult.lambdas);
            writeLambdas(stackWriter, qreResult.lambdas);
            writeLambdas(brWriter, qreResult.lambdas);
            writeLambdas(wrWriter, qreResult.lambdas);
            for (Map<Player, Map<Sequence, Double>> quantalResponse : qreResult.quantalResponses) {
                GeneralSumBestResponse br = new GeneralSumBestResponse(expander, 0, getActingPlayers(root), algConfig, info);
                GeneralSumWorstResponse wr = new GeneralSumWorstResponse(expander, 0, getActingPlayers(root), algConfig, info);
                Map<Sequence, Double> qreStrategy = filterLow(quantalResponse.get(player), 1e-6);

                br.calculateBR(root, quantalResponse.get(player));
                wr.calculateBR(root, quantalResponse.get(player));
                write(brWriter, computeExpectedValue(br.getBRStategy(), qreStrategy, root, expander));
                write(wrWriter, computeExpectedValue(wr.getBRStategy(), qreStrategy, root, expander));
                write(neWriter, computeExpectedValue(neResult.p1RealPlan, qreStrategy, root, expander));
                write(qpWriter, computeExpectedValue(qpResult.p1RealPlan, qreStrategy, root, expander));
                write(stackWriter, computeExpectedValue(stackResult.get(root.getAllPlayers()[0]), qreStrategy, root, expander));
            }
            neWriter.newLine();
            qpWriter.newLine();
            stackWriter.newLine();
            brWriter.newLine();
            wrWriter.newLine();

            neWriter.close();
            qpWriter.close();
            stackWriter.close();
            brWriter.close();
            wrWriter.close();
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

    private static void evaluateP1StrategiesAgainstCFR(SolverResult neResult, SolverResult qpResult, Map<Player, Map<Sequence, Double>> stackResult,
                                                       GameState root, Expander<MCTSInformationSet> expander, GameInfo info, MCTSConfig algConfig, Player player) {
        try {
            neWriter = new BufferedWriter(new FileWriter("P1NEvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            qpWriter = new BufferedWriter(new FileWriter("P1QPvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            stackWriter = new BufferedWriter(new FileWriter("P1StackvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            brWriter = new BufferedWriter(new FileWriter("P1BRvsCFRExpVal" + getDomainDependentString() + ".csv", true));
            wrWriter = new BufferedWriter(new FileWriter("P1WRvsCFRExpVal" + getDomainDependentString() + ".csv", true));

            CFRAlgorithm cfr = new CFRAlgorithm(root.getAllPlayers()[1], root, expander);

            buildCompleteTree(cfr.getRootNode());
            for (int i = 0; i < 200; i++) {
                cfr.runIterations(100);
                Strategy strategy = StrategyCollector.getStrategyFor(cfr.getRootNode(), root.getAllPlayers()[1], new MeanStratDist());
                GeneralSumBestResponse br = new GeneralSumBestResponse(expander, 0, getActingPlayers(root), algConfig, info);
                GeneralSumWorstResponse wr = new GeneralSumWorstResponse(expander, 0, getActingPlayers(root), algConfig, info);

                br.calculateBR(root, strategy);
                wr.calculateBR(root, strategy);
                write(neWriter, computeExpectedValue(neResult.p1RealPlan, strategy, root, expander));
                write(qpWriter, computeExpectedValue(qpResult.p1RealPlan, strategy, root, expander));
                write(stackWriter, computeExpectedValue(stackResult.get(root.getAllPlayers()[0]), strategy, root, expander));
                write(brWriter, computeExpectedValue(br.getBRStategy(), strategy, root, expander));
                write(wrWriter, computeExpectedValue(wr.getBRStategy(), strategy, root, expander));
            }
            neWriter.newLine();
            qpWriter.newLine();
            stackWriter.newLine();
            brWriter.newLine();
            wrWriter.newLine();

            neWriter.close();
            qpWriter.close();
            stackWriter.close();
            brWriter.close();
            wrWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void evaluateP1StrategiesAgainstMCTS(SolverResult neResult, SolverResult qpResult, Map<Player, Map<Sequence, Double>> stackResult,
                                                        GameState root, Expander<MCTSInformationSet> expander, GameInfo info, MCTSConfig algConfig, Player player) {
        try {
            neWriter = new BufferedWriter(new FileWriter("P1NEvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            qpWriter = new BufferedWriter(new FileWriter("P1QPvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            stackWriter = new BufferedWriter(new FileWriter("P1StackvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            brWriter = new BufferedWriter(new FileWriter("P1BRvsMCTSExpVal" + getDomainDependentString() + ".csv", true));
            wrWriter = new BufferedWriter(new FileWriter("P1WRvsMCTSExpVal" + getDomainDependentString() + ".csv", true));

            ISMCTSExploitability.rootState = root;
            ISMCTSExploitability.expander = expander;
            ISMCTSExploitability.gameInfo = info;
            GenSumISMCTSNestingRunner.alg = new GenSumISMCTSAlgorithm(root.getAllPlayers()[1], new DefaultSimulator(expander), new UCTBackPropFactory(Math.sqrt(2) * info.getMaxUtility()), root, expander);
//            GenSumISMCTSAlgorithm mcts = new GenSumISMCTSAlgorithm(root.getAllPlayers()[1], new DefaultSimulator(expander), new UCTBackPropFactory(Math.sqrt(2) * info.getMaxUtility()), root, expander);
            for (int i = 0; i < 500; i++) {
                GenSumISMCTSNestingRunner.buildStichedStrategy(root.getAllPlayers()[1], GenSumISMCTSNestingRunner.alg.getRootNode().getInformationSet(),
                        GenSumISMCTSNestingRunner.alg.getRootNode(), 200);
                Strategy strategy = StrategyCollector.getStrategyFor(GenSumISMCTSNestingRunner.alg.getRootNode(), root.getAllPlayers()[1], new MeanStratDist());
                GeneralSumBestResponse br = new GeneralSumBestResponse(expander, 0, getActingPlayers(root), algConfig, info);
                GeneralSumWorstResponse wr = new GeneralSumWorstResponse(expander, 0, getActingPlayers(root), algConfig, info);

                br.calculateBR(root, strategy);
                wr.calculateBR(root, strategy);
                write(neWriter, computeExpectedValue(neResult.p1RealPlan, strategy, root, expander));
                write(qpWriter, computeExpectedValue(qpResult.p1RealPlan, strategy, root, expander));
                write(stackWriter, computeExpectedValue(stackResult.get(root.getAllPlayers()[0]), strategy, root, expander));
                write(brWriter, computeExpectedValue(br.getBRStategy(), strategy, root, expander));
                write(wrWriter, computeExpectedValue(wr.getBRStategy(), strategy, root, expander));
            }

            neWriter.newLine();
            qpWriter.newLine();
            stackWriter.newLine();
            brWriter.newLine();
            wrWriter.newLine();

            neWriter.close();
            qpWriter.close();
            stackWriter.close();
            brWriter.close();
            wrWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void buildCompleteTree(InnerNode r) {
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

    private static void writeLambdas(BufferedWriter writer, List<Double> lambdas) throws IOException {
        writer.write(lambdas.get(0) + "");
        for (int i = 1; i < lambdas.size(); i++) {
            writer.write(", " + lambdas.get(i));
        }
        writer.newLine();
        writer.flush();
    }

    private static void evaluateP1StrategiesAgainstPureRps(SolverResult neResult, SolverResult undomResult, SolverResult qpResult, SolverResult p1MaxResult, SolverResult welfareResult, Map<Player, Map<Sequence, Double>> stackResult, Iterator<Set<Sequence>> iterator, GameState root, Expander<SequenceInformationSet> expander) {
        try {
            neWriter = new BufferedWriter(new FileWriter("P1NEvsPureRPsExpVal" + getDomainDependentString() + ".csv", true));
            undomWriter = new BufferedWriter(new FileWriter("P1UndomvsPureRPsExpVal" + getDomainDependentString() + ".csv", true));
            qpWriter = new BufferedWriter(new FileWriter("P1QPvsPureRPsExpVal" + getDomainDependentString() + ".csv", true));
            p1MaxWriter = new BufferedWriter(new FileWriter("P1MNEvsPureRPsExpVal" + getDomainDependentString() + ".csv", true));
            welfareWriter = new BufferedWriter(new FileWriter("P1WNEvsPureRPsExpVal" + getDomainDependentString() + ".csv", true));
            stackWriter = new BufferedWriter(new FileWriter("P1StackvsPureRPsExpVal" + getDomainDependentString() + ".csv", true));

            try {
                while (true) {
                    Set<Sequence> pureRP = iterator.next();

                    write(neWriter, computeExpectedValue(neResult.p1RealPlan, toRp(pureRP), root, expander));
                    write(undomWriter, computeExpectedValue(undomResult.p1RealPlan, toRp(pureRP), root, expander));
                    write(qpWriter, computeExpectedValue(qpResult.p1RealPlan, toRp(pureRP), root, expander));
                    write(p1MaxWriter, computeExpectedValue(p1MaxResult.p1RealPlan, toRp(pureRP), root, expander));
                    write(welfareWriter, computeExpectedValue(welfareResult.p1RealPlan, toRp(pureRP), root, expander));
                    write(stackWriter, computeExpectedValue(stackResult.get(root.getAllPlayers()[0]), toRp(pureRP), root, expander));
                }
            } catch (NoSuchElementException e) {
            }
            neWriter.newLine();
            undomWriter.newLine();
            qpWriter.newLine();
            p1MaxWriter.newLine();
            welfareWriter.newLine();
            stackWriter.newLine();

            neWriter.close();
            undomWriter.close();
            qpWriter.close();
            p1MaxWriter.close();
            welfareWriter.close();
            stackWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
