package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class NFPSolver {

    private Double p1Value;
    private Double p2Value;
    private Player[] players;
    private Map<Sequence, Double> p1RealizationPlan;
    private Map<Sequence, Double> p2RealizationPlan;

    public NFPSolver(Player[] players) {
        this.players = players;
        p1Value = null;
        p2Value = null;
        p1RealizationPlan = new HashMap<Sequence, Double>();
        p2RealizationPlan = new HashMap<Sequence, Double>();
    }


    public Double getResultForPlayer(Player player) {
//        assert !Double.isNaN(p1Value);
        return player.equals(players[0]) ? p1Value : p2Value;
    }

    public Map<Sequence, Double> getResultStrategiesForPlayer(Player player) {
        return player.equals(players[0]) ? p1RealizationPlan : p2RealizationPlan;
    }

    public void calculateStrategyForPlayer(int playerIndex, GameState root, DoubleOracleConfig<DoubleOracleInformationSet> config, double currentBoundSize) {
        long startTime = System.currentTimeMillis();

        if (playerIndex == 0)
            p1RealizationPlan = solveForP1(config);
        else
            p2RealizationPlan = solveForP2(config);
    }

    public Map<Sequence, Double> solveForP1(DoubleOracleConfig<DoubleOracleInformationSet> config) {
        InitialPBuilder initPbuilder = new InitialPBuilder(players, config);

        initPbuilder.buildLP();
        PResult pResult = initPbuilder.solve();

        p1Value = pResult.getGameValue();
        InitialQBuilder initQBuilder = new InitialQBuilder(players, config, p1Value);

        initQBuilder.buildLP();
        QResult qResult = initQBuilder.solve();

        System.out.println("Exploitable sequences: ");
        for (Sequence exploitableSequence : qResult.getLastItSeq()) {
            System.out.println(exploitableSequence);
        }
        if (qResult.getGameValue() > 1e-6) {
            PBuilder pBuilder = new PBuilder(players, config, qResult, p1Value);

            pBuilder.buildLP();
            pResult = pBuilder.solve();

            QBuilder qBuilder = new QBuilder(players, config, p1Value, pResult.getGameValue(), qResult);

            qBuilder.buildLP();
            qResult = qBuilder.solve();

            System.out.println("Exploitable sequences: ");
            for (Sequence exploitableSequence : qResult.getLastItSeq()) {
                System.out.println(exploitableSequence);
            }

            PUpdater pUpdater = new PUpdater(players, config, pBuilder.lpTable);
            QUpdater qUpdater = new QUpdater(players, config, p1Value, qBuilder.lpTable);

            while (Math.abs(qResult.getGameValue()) > 1e-6) {
                assert !qResult.getLastItSeq().isEmpty();
                System.out.println("Exploitable seq. count " + qResult.getLastItSeq().size());

                pUpdater.buildLP(qResult);
                pResult = pUpdater.solve();

                qUpdater.buildLP(qResult, pResult.getGameValue());
                qResult = qUpdater.solve();
                System.out.println("Exploitable sequences: ");
                for (Sequence exploitableSequence : qResult.getLastItSeq()) {
                    System.out.println(exploitableSequence);
                }
            }
        }
        return pResult.getRealizationPlan();
//        return qResult.getRealizationPlan();
    }

    public void setDebugOutput(PrintStream debugOutput) {
        //To change body of created methods use File | Settings | File Templates.
    }

    public long getOverallGenerationTime() {
        return 0;
    }

    public long getOverallConstraintGenerationTime() {
        return 0;
    }

    public long getOverallConstraintLPSolvingTime() {
        return 0;
    }

    public Map<Sequence, Double> solveForP2(DoubleOracleConfig<DoubleOracleInformationSet> config) {
        InitialP2PBuilder initPbuilder = new InitialP2PBuilder(players, config);

        initPbuilder.buildLP();
        PResult pResult = initPbuilder.solve();

        p2Value = pResult.getGameValue();
        InitialP2QBuilder initQBuilder = new InitialP2QBuilder(players, config, p2Value);

        initQBuilder.buildLP();
        QResult qResult = initQBuilder.solve();

        System.out.println("Exploitable sequences: ");
        for (Sequence exploitableSequence : qResult.getLastItSeq()) {
            System.out.println(exploitableSequence);
        }

        if (qResult.getGameValue() > 1e-6) {
            P2PBuilder pBuilder = new P2PBuilder(players, config, qResult, p2Value);

            pBuilder.buildLP();
            pResult = pBuilder.solve();

            P2QBuilder qBuilder = new P2QBuilder(players, config, p2Value, pResult.getGameValue(), qResult);

            qBuilder.buildLP();
            qResult = qBuilder.solve();

            System.out.println("Exploitable sequences: ");
            for (Sequence exploitableSequence : qResult.getLastItSeq()) {
                System.out.println(exploitableSequence);
            }
            P2PUpdater pUpdater = new P2PUpdater(players, config, pBuilder.lpTable);
            P2QUpdater qUpdater = new P2QUpdater(players, config, p2Value, qBuilder.lpTable);

            while (Math.abs(qResult.getGameValue()) > 1e-6) {
                assert !qResult.getLastItSeq().isEmpty();
                System.out.println("Exploitable seq. count " + qResult.getLastItSeq().size());

                pUpdater.buildLP(qResult);
                pResult = pUpdater.solve();

                qUpdater.buildLP(pResult.getGameValue(), qResult);
                qResult = qUpdater.solve();
                System.out.println("Exploitable sequences: ");
                for (Sequence exploitableSequence : qResult.getLastItSeq()) {
                    System.out.println(exploitableSequence);
                }
            }
        }
        return pResult.getRealizationPlan();
//        return  qResult.getRealizationPlan();
    }

}
