package cz.agents.gtlibrary.algorithms.sequenceform.gensum.experiments;

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.*;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.quantalresponse.QREResult;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.quantalresponse.QRESolver;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom.DataBuilder;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergRunner;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergSequenceFormLP;
import cz.agents.gtlibrary.algorithms.stackelberg.milp.StackelbergSequenceFormMILP;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.EmptyFeasibilitySequenceFormLP;
import cz.agents.gtlibrary.domain.randomgame.GeneralSumRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.interfaces.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class StrategyStrengthExperiments {

    public static BufferedWriter neWriter;
    public static BufferedWriter undomWriter;
    public static BufferedWriter qpWriter;
    public static BufferedWriter p1MaxWriter;
    public static BufferedWriter stackWriter;

    /**
     * bf, depth, corr, seed
     *
     * @param args
     */
    public static void main(String[] args) {
        runGenSumRandomGames(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Double.parseDouble(args[2]), Integer.parseInt(args[3]));
    }

    private static void runGenSumRandomGames(int bf, int depth, double correlation, int seed) {
        RandomGameInfo.seed = seed;
        RandomGameInfo.MAX_BF = bf;
        RandomGameInfo.MAX_DEPTH = depth;
        RandomGameInfo.CORRELATION = correlation;
        GeneralSumRandomGameState root = new GeneralSumRandomGameState();
        GameInfo info = new RandomGameInfo();
        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
        Expander<SequenceInformationSet> expander = new RandomGameExpander<>(algConfig);
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, info, algConfig);

        builder.generateCompleteGame();

        GenSumSequenceFormMILP undomSolver = new UndomGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root, expander, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP p1MaxSolver = new PlayerExpValMaxGenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info, root.getAllPlayers()[0]);
        GenSumSequenceFormMILP neSolver = new GenSumSequenceFormMILP(algConfig, root.getAllPlayers(), info);
        DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;

        SolverResult qpResult = DataBuilder.runDataBuilder(root, expander, algConfig, info, "GenSumRndGameRepr");
        SolverResult undomResult = undomSolver.compute();
        SolverResult neResult = neSolver.compute();
        SolverResult p1MaxResult = p1MaxSolver.compute();

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
            evaluateP1StrategiesAgainstPureRps(neResult, undomResult, qpResult, p1MaxResult, rps, stackConfig.getIterator(root.getAllPlayers()[1], stackExpander, new EmptyFeasibilitySequenceFormLP()), root, expander);
            evaluateP1StrategiesAgainstQRE(neResult, undomResult, qpResult, p1MaxResult, rps, root, expander, info, algConfig, root.getAllPlayers()[1]);
        }
    }

    private static void evaluateP1StrategiesAgainstQRE(SolverResult neResult, SolverResult undomResult, SolverResult qpResult, SolverResult p1MaxResult, Map<Player, Map<Sequence, Double>> stackResult,
                                                       GameState root, Expander<SequenceInformationSet> expander, GameInfo info, SequenceFormConfig<SequenceInformationSet> algConfig, Player player) {
        try {
            neWriter = new BufferedWriter(new FileWriter("P1NEvsQREExpVal" + getDomainDependentString() + ".csv", true));
            undomWriter = new BufferedWriter(new FileWriter("P1UndomvsQREExpVal" + getDomainDependentString() + ".csv", true));
            qpWriter = new BufferedWriter(new FileWriter("P1QPvsQREExpVal" + getDomainDependentString() + ".csv", true));
            p1MaxWriter = new BufferedWriter(new FileWriter("P1MNEvsQREExpVal" + getDomainDependentString() + ".csv", true));
            stackWriter = new BufferedWriter(new FileWriter("P1StackvsQREExpVal" + getDomainDependentString() + ".csv", true));

            QRESolver solver = new QRESolver(root, expander, info, algConfig);
            QREResult qreResult = solver.solve();

            writeLambdas(neWriter, qreResult.lambdas);
            writeLambdas(undomWriter, qreResult.lambdas);
            writeLambdas(qpWriter, qreResult.lambdas);
            writeLambdas(p1MaxWriter, qreResult.lambdas);
            writeLambdas(stackWriter, qreResult.lambdas);
            for (Map<Player, Map<Sequence, Double>> quantalResponse : qreResult.quantalResponses) {
                write(neWriter, computeExpectedValue(neResult.p1RealPlan, quantalResponse.get(player), root, expander));
                write(undomWriter, computeExpectedValue(undomResult.p1RealPlan, quantalResponse.get(player), root, expander));
                write(qpWriter, computeExpectedValue(qpResult.p1RealPlan, quantalResponse.get(player), root, expander));
                write(p1MaxWriter, computeExpectedValue(p1MaxResult.p1RealPlan, quantalResponse.get(player), root, expander));
                write(stackWriter, computeExpectedValue(stackResult.get(root.getAllPlayers()[0]), quantalResponse.get(player), root, expander));
            }
            neWriter.newLine();
            undomWriter.newLine();
            qpWriter.newLine();
            p1MaxWriter.newLine();
            stackWriter.newLine();

            neWriter.close();
            undomWriter.close();
            qpWriter.close();
            p1MaxWriter.close();
            stackWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeLambdas(BufferedWriter writer, List<Double> lambdas) throws IOException {
        writer.write(lambdas.get(0) + "");
        for (int i = 1; i < lambdas.size(); i++) {
            writer.write(", " + lambdas.get(i));
        }
        writer.newLine();
        writer.flush();
    }

    private static void evaluateP1StrategiesAgainstPureRps(SolverResult neResult, SolverResult undomResult, SolverResult qpResult, SolverResult p1MaxResult, Map<Player, Map<Sequence, Double>> stackResult, Iterator<Set<Sequence>> iterator, GameState root, Expander<SequenceInformationSet> expander) {
        try {
            neWriter = new BufferedWriter(new FileWriter("P1NEvsPureRPsExpVal" + getDomainDependentString() + ".csv", true));
            undomWriter = new BufferedWriter(new FileWriter("P1UndomvsPureRPsExpVal" + getDomainDependentString() + ".csv", true));
            qpWriter = new BufferedWriter(new FileWriter("P1QPvsPureRPsExpVal" + getDomainDependentString() + ".csv", true));
            p1MaxWriter = new BufferedWriter(new FileWriter("P1MNEvsPureRPsExpVal" + getDomainDependentString() + ".csv", true));
            stackWriter = new BufferedWriter(new FileWriter("P1StackvsPureRPsExpVal" + getDomainDependentString() + ".csv", true));

            try {
                while (true) {
                    Set<Sequence> pureRP = iterator.next();

                    write(neWriter, computeExpectedValue(neResult.p1RealPlan, toRp(pureRP), root, expander));
                    write(undomWriter, computeExpectedValue(undomResult.p1RealPlan, toRp(pureRP), root, expander));
                    write(qpWriter, computeExpectedValue(qpResult.p1RealPlan, toRp(pureRP), root, expander));
                    write(p1MaxWriter, computeExpectedValue(p1MaxResult.p1RealPlan, toRp(pureRP), root, expander));
                    write(stackWriter, computeExpectedValue(stackResult.get(root.getAllPlayers()[0]), toRp(pureRP), root, expander));
                }
            } catch (NoSuchElementException e) {
            }
            neWriter.newLine();
            undomWriter.newLine();
            qpWriter.newLine();
            p1MaxWriter.newLine();
            stackWriter.newLine();

            neWriter.close();
            undomWriter.close();
            qpWriter.close();
            p1MaxWriter.close();
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

    private static double[] computeExpectedValue(Map<Sequence, Double> p1RealPlan, Map<Sequence, Double> p2RealPlan, GameState root, Expander<SequenceInformationSet> expander) {
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

    private static double[] expectedValuesForNature(Map<Sequence, Double> p1RealPlan, Map<Sequence, Double> p2RealPlan, GameState root, Expander<SequenceInformationSet> expander) {
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
