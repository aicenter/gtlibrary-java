package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.reusing;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NFPSolverReuse {

    protected Double p1Value;
    protected Double p2Value;
    protected Player[] players;
    protected InitialPBuilderReuse initPBuilderP1;
    protected InitialQBuilderReuse initQBuilderP1;
    protected PBuilderReuse pBuilderP1;
    protected QBuilderReuse qBuilderP1;
    protected InitialP2PBuilderReuse initPBuilderP2;
    protected InitialP2QBuilderReuse initQBuilderP2;
    protected P2PBuilderReuse pBuilderP2;
    protected P2QBuilderReuse qBuilderP2;
    protected Map<Sequence, Double> p1RealizationPlan;
    protected Map<Sequence, Double> p2RealizationPlan;
    protected Set<Sequence> p1SequencesToAdd;
    protected Set<Sequence> p2SequencesToAdd;
    protected GameInfo info;

    public NFPSolverReuse(Player[] players, GameInfo info) {
        this.players = players;
        this.info = info;
        p1Value = null;
        p2Value = null;
        p1RealizationPlan = new HashMap<Sequence, Double>();
        p2RealizationPlan = new HashMap<Sequence, Double>();
        p1SequencesToAdd = new HashSet<Sequence>();
        p2SequencesToAdd = new HashSet<Sequence>();
    }


    public Double getResultForPlayer(Player player) {
//        assert !Double.isNaN(p1Value);
        Double value = player.equals(players[0]) ? p1Value : p2Value;

        return value == null ? null : value / info.getUtilityStabilizer();
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
        updateP1Sequences(config);
        updateP2Sequences(config);
        if (initPBuilderP1 == null)
            initPBuilderP1 = new InitialPBuilderReuse(players, info);

        initPBuilderP1.buildLP(config, p1SequencesToAdd);
        PResultReuse pResult = initPBuilderP1.solve();

        p2Value = -pResult.getGameValue();


        if (initQBuilderP1 == null)
            initQBuilderP1 = new InitialQBuilderReuse(players, info);

        initQBuilderP1.buildLP(config, -p2Value, p1SequencesToAdd);
        QResultReuse qResult = initQBuilderP1.solve();

//        System.out.println("Exploitable sequences: ");
//        for (Sequence exploitableSequence : qResult.getLastItSeq()) {
//            System.out.println(exploitableSequence);
//        }
        if (pBuilderP1 == null)
            pBuilderP1 = new PBuilderReuse(players, info);
//        pBuilderP1.updateFromLastIteration(qResult, p1Value);
        pBuilderP1.buildLP(config, p1SequencesToAdd);
//        pBuilderP1.updateSolver();
        pBuilderP1.update(qResult, -p2Value, config);
        if (qBuilderP1 == null)
            qBuilderP1 = new QBuilderReuse(players, info);

        qBuilderP1.buildLP(config, -p2Value, p1SequencesToAdd);
//        qBuilderP1.updateSolver();
        if (qResult.getGameValue() > 1e-6) {
            pResult = pBuilderP1.solve();

            qBuilderP1.update(pResult.getGameValue(), qResult, config);
//            qBuilderP1.buildLP(config, p1Value);
            qResult = qBuilderP1.solve();

//            System.out.println("Exploitable sequences: ");
//            for (Sequence exploitableSequence : qResult.getLastItSeq()) {
//                System.out.println(exploitableSequence);
//            }

            PUpdaterReuse pUpdater = new PUpdaterReuse(players, pBuilderP1.lpTable, info);
            QUpdaterReuse qUpdater = new QUpdaterReuse(players, qBuilderP1.lpTable, info);

            while (Math.abs(qResult.getGameValue()) > 1e-6) {
                assert !qResult.getLastItSeq().isEmpty();
//                System.out.println("Exploitable seq. count " + qResult.getLastItSeq().size());

                pUpdater.update(qResult, config);
                pResult = pUpdater.solve();

                qUpdater.update(pResult.getGameValue(), qResult, config);
                qResult = qUpdater.solve();
//                System.out.println("Exploitable sequences: ");
//                for (Sequence exploitableSequence : qResult.getLastItSeq()) {
//                    System.out.println(exploitableSequence);
//                }
            }
        }


        p1SequencesToAdd.clear();
        return pResult.getRealizationPlan();
//        return qResult.getRealizationPlan();
    }

    protected void updateP2Sequences(DoubleOracleConfig<DoubleOracleInformationSet> config) {
        p2SequencesToAdd.addAll(config.getNewSequences());
        for (Sequence sequence : config.getNewSequences()) {
            p2SequencesToAdd.addAll(config.getCompatibleSequencesFor(sequence));
            p2SequencesToAdd.add(sequence.getSubSequence(sequence.size() - 1));
        }
    }

    protected void updateP1Sequences(DoubleOracleConfig<DoubleOracleInformationSet> config) {
        p1SequencesToAdd.addAll(config.getNewSequences());
        for (Sequence sequence : config.getNewSequences()) {
            p1SequencesToAdd.addAll(config.getCompatibleSequencesFor(sequence));
            p1SequencesToAdd.add(sequence.getSubSequence(sequence.size() - 1));
        }
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
        updateP1Sequences(config);
        updateP2Sequences(config);
        if (initPBuilderP2 == null)
            initPBuilderP2 = new InitialP2PBuilderReuse(players, info);

        initPBuilderP2.buildLP(config, p2SequencesToAdd);
        PResultReuse pResult = initPBuilderP2.solve();

        p1Value = -pResult.getGameValue();

        if (initQBuilderP2 == null)
            initQBuilderP2 = new InitialP2QBuilderReuse(players, info);

        initQBuilderP2.buildLP(config, -p1Value, p2SequencesToAdd);
        QResultReuse qResult = initQBuilderP2.solve();

//        System.out.println("Exploitable sequences: ");
//        for (Sequence exploitableSequence : qResult.getLastItSeq()) {
//            System.out.println(exploitableSequence);
//        }

        if (pBuilderP2 == null)
            pBuilderP2 = new P2PBuilderReuse(players, info);
//        pBuilderP2.updateFromLastIteration(qResult, p2Value);
        pBuilderP2.buildLP(config, p2SequencesToAdd);
        pBuilderP2.update(qResult, -p1Value, config);
//        pBuilderP2.updateSolver();
        if (qBuilderP2 == null)
            qBuilderP2 = new P2QBuilderReuse(players, info);

        qBuilderP2.buildLP(config, -p1Value, p2SequencesToAdd);
//        qBuilderP2.updateSolver();
        if (qResult.getGameValue() > 1e-6) {
            pResult = pBuilderP2.solve();

//            qBuilderP2.updateSum(pResult.getGameValue(), qResult);
            qBuilderP2.update(pResult.getGameValue(), qResult, config);
            qResult = qBuilderP2.solve();

//            System.out.println("Exploitable sequences: ");
//            for (Sequence exploitableSequence : qResult.getLastItSeq()) {
//                System.out.println(exploitableSequence);
//            }
            P2PUpdaterReuse pUpdater = new P2PUpdaterReuse(players, pBuilderP2.lpTable, info);
            P2QUpdaterReuse qUpdater = new P2QUpdaterReuse(players, qBuilderP2.lpTable, info);

            while (Math.abs(qResult.getGameValue()) > 1e-6) {
                assert !qResult.getLastItSeq().isEmpty();
//                System.out.println("Exploitable seq. count " + qResult.getLastItSeq().size());

                pUpdater.update(qResult, config);
                pResult = pUpdater.solve();

                qUpdater.update(pResult.getGameValue(), qResult, config);
                qResult = qUpdater.solve();
//                System.out.println("Exploitable sequences: ");
//                for (Sequence exploitableSequence : qResult.getLastItSeq()) {
//                    System.out.println(exploitableSequence);
//                }
            }
        }


        p2SequencesToAdd.clear();
        return pResult.getRealizationPlan();
//        return  qResult.getRealizationPlan();
    }

}
