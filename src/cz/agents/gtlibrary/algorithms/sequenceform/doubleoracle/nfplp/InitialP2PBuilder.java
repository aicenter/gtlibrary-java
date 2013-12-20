package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class InitialP2PBuilder {

    protected String lpFileName;
    protected RecyclingNFPTable lpTable;
    protected SequenceFormConfig<? extends SequenceInformationSet>config;
    protected Player[] players;

    public InitialP2PBuilder(Player[] players, SequenceFormConfig<? extends SequenceInformationSet> config) {
        this.players = players;
        this.config = config;
        lpFileName = "P2DO_P.lp";
    }

    public void buildLP() {
        initTable();
        for (Sequence p1Sequence : config.getSequencesFor(players[0])) {
            updateForP1(p1Sequence);
        }
        for (Sequence p2Sequence : config.getSequencesFor(players[1])) {
            updateForP2(p2Sequence);
        }
        addUtilities(config.getSequencesFor(players[0]), config.getSequencesFor(players[1]));
    }

    protected void addUtilities(Iterable<Sequence> p1Sequences, Iterable<Sequence> p2Sequences) {
        for (Sequence p1Sequence : p1Sequences) {
            for (Sequence p2Sequence : p2Sequences) {
                Double utility = config.getUtilityFor(p1Sequence, p2Sequence);

                if (utility != null) {
                    lpTable.substractFromConstraint(p1Sequence, p2Sequence, -utility);
                }
            }
        }
    }

    protected void updateForP1(Sequence p1Sequence) {
        if (config.getReachableSets(p1Sequence) == null)
            return;
        for (SequenceInformationSet informationSet : config.getReachableSets(p1Sequence)) {
            for (Sequence outgoingSequence : informationSet.getOutgoingSequences()) {
                Object varKey = getKey(informationSet);

                lpTable.setConstraint(p1Sequence, varKey, -1);//F
                lpTable.setConstraintType(p1Sequence, 0);
                lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
                lpTable.setConstraint(outgoingSequence, varKey, 1);//F
                lpTable.setConstraintType(outgoingSequence, 0);
            }
        }
    }

    protected void updateForP2(Sequence p2Sequence) {
        lpTable.watchPrimalVariable(p2Sequence, p2Sequence);
        if (config.getReachableSets(p2Sequence) == null)
            return;
        for (SequenceInformationSet informationSet : config.getReachableSets(p2Sequence)) {
            for (Sequence outgoingSequence : informationSet.getOutgoingSequences()) {
                Object eqKey = getKey(informationSet);

                lpTable.watchPrimalVariable(outgoingSequence, outgoingSequence);
                lpTable.setConstraint(eqKey, p2Sequence, -1);//E
                lpTable.setConstraintType(eqKey, 1);
                lpTable.setLowerBound(p2Sequence, 0);
                lpTable.setConstraint(eqKey, outgoingSequence, 1);//E
                lpTable.setLowerBound(outgoingSequence, 0);
            }
        }
    }

    private Object getKey(SequenceInformationSet informationSet) {
        return new Pair<Integer, Sequence>(informationSet.hashCode(), informationSet.getPlayersHistory());
    }

//    protected void updateForP1(Sequence p1Sequence) {
//        if (p1Sequence.size() == 0)
//            return;
//        Object eqKey = getSubsequence(p1Sequence);
//        Object varKey = getLastISKey(p1Sequence);
//
//        lpTable.setConstraint(eqKey, varKey, -1);//F
//        lpTable.setConstraintType(eqKey, 0);
//        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
//        lpTable.setConstraint(p1Sequence, varKey, 1);//F
//        lpTable.setConstraintType(p1Sequence, 0);
//    }
//
//    protected void updateForP2(Sequence p2Sequence) {
//        lpTable.watchPrimalVariable(p2Sequence, p2Sequence);
//        if (p2Sequence.size() == 0)
//            return;
//        Object varKey = getSubsequence(p2Sequence);
//        Object eqKey = getLastISKey(p2Sequence);
//
//        lpTable.setConstraint(eqKey, varKey, -1);//E
//        lpTable.setConstraintType(eqKey, 1);
//        lpTable.setLowerBound(varKey, 0);
//        lpTable.setConstraint(eqKey, p2Sequence, 1);//E
//        lpTable.setLowerBound(p2Sequence, 0);
//    }

    protected Sequence getSubsequence(Sequence sequence) {
        return sequence.getSubSequence(sequence.size() - 1);
    }

    public PResult solve() {
        try {
            LPData lpData = lpTable.toCplex();
            boolean solved = false;

            lpData.getSolver().exportModel(lpFileName);
            for (int algorithm : lpData.getAlgorithms()) {
                lpData.getSolver().setParam(IloCplex.IntParam.RootAlg, algorithm);
                if (solved = trySolve(lpData))
                    break;
            }
            if (!solved)
                solveUnfeasibleLP(lpData);
            System.out.println(lpData.getSolver().getStatus());
            System.out.println(lpData.getSolver().getObjValue());

//            System.out.println("values:");
//            for (int i = 0; i < lpData.getVariables().length; i++) {
//                System.out.println(lpData.getVariables()[i] + ": " + lpData.getSolver().getValue(lpData.getVariables()[i]));
//            }

            //			Map<Sequence, Double> p1RealizationPlan = createFirstPlayerStrategy(lpData.getSolver(), lpData.getWatchedPrimalVariables());

//            			for (int i = 0; i < lpData.getVariables().length; i++) {
//            				System.out.println(lpData.getVariables()[i] + ": " + lpData.getSolver().getValue(lpData.getVariables()[i]));
//            			}

            //			for (Entry<Sequence, Double> entry : p1RealizationPlan.entrySet()) {
            //				if(entry.getValue() > 0)
            //					System.out.println(entry);
            //			}
//            double objValue = lpData.getSolver().getObjValue();
//            BigDecimal preciseValue = new BigDecimal(objValue);
//
//            preciseValue = preciseValue.movePointRight(16);
//            preciseValue.subtract(new BigDecimal(1));
//            objValue = new BigDecimal(preciseValue.toBigInteger()).movePointLeft(16).doubleValue();

            return new PResult(createSecondPlayerStrategy(lpData.getSolver(), lpData.getWatchedPrimalVariables()), lpData.getSolver().getObjValue());
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected boolean trySolve(LPData lpData) throws IloException {
        boolean solved;

        try {
            solved = lpData.getSolver().solve();
        } catch (IloException e) {
            e.printStackTrace();
            return false;
        }

        System.out.println("P: " + solved);
        return solved;
    }

    protected void solveUnfeasibleLP(LPData lpData) throws IloException {
        lpData.getSolver().setParam(IloCplex.IntParam.FeasOptMode, IloCplex.Relaxation.OptQuad);

        boolean solved = lpData.getSolver().feasOpt(lpData.getConstraints(), getPreferences(lpData));

        assert solved;
        System.out.println("Q feas: " + solved);
    }

    protected double[] getPreferences(LPData lpData) {
        double[] preferences = new double[lpData.getConstraints().length];

        for (int i = 0; i < preferences.length; i++) {
            if (lpData.getRelaxableConstraints().values().contains(lpData.getConstraints()[i]))
                preferences[i] = 1;
            else
                preferences[i] = 0.5;
        }
        return preferences;
    }

    public Map<Sequence, Double> createSecondPlayerStrategy(IloCplex cplex, Map<Object, IloNumVar> watchedPrimalVars) throws IloException {
        Map<Sequence, Double> p1Strategy = new HashMap<Sequence, Double>();

        for (Entry<Object, IloNumVar> entry : watchedPrimalVars.entrySet()) {
            p1Strategy.put((Sequence) entry.getKey(), cplex.getValue(entry.getValue()));
        }
        return p1Strategy;
    }

    protected Map<Sequence, Double> getWatchedSequenceValues(LPData lpData) throws UnknownObjectException, IloException {
        Map<Sequence, Double> watchedSequenceValues = new HashMap<Sequence, Double>(lpData.getWatchedPrimalVariables().size());

        for (Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            watchedSequenceValues.put((Sequence) entry.getKey(), lpData.getSolver().getValue(entry.getValue()));
        }
        return watchedSequenceValues;
    }

    //	public Map<Sequence, Double> createSecondPlayerStrategy(IloCplex cplex, Map<Object, IloNumVar> watchedPrimalVars) throws IloException {
    //		Map<Sequence, Double> p2Strategy = new HashMap<Sequence, Double>();
    //
    //		for (Entry<Object, IloNumVar> entry : watchedPrimalVars.entrySet()) {
    //			p2Strategy.put((Sequence) entry.getKey(), cplex.getValue(entry.getValue()));
    //		}
    //		return p2Strategy;
    //	}

    public void initTable() {
        Sequence p1EmptySequence = new ArrayListSequenceImpl(players[0]);
        Sequence p2EmptySequence = new ArrayListSequenceImpl(players[1]);

        lpTable = new RecyclingNFPTable();

        initObjective(p1EmptySequence);
        initE(p1EmptySequence);
        initF(p2EmptySequence);
        initf();
    }

    public void initf() {
        lpTable.setConstant(players[1], 1);//e for root
    }

    public void initE(Sequence p1EmptySequence) {
        lpTable.setConstraint(p1EmptySequence, players[0], 1);//F in root (only 1)
        lpTable.setConstraintType(p1EmptySequence, 0);
        lpTable.setLowerBound(players[0], Double.NEGATIVE_INFINITY);
    }

    public void initF(Sequence p2EmptySequence) {
        lpTable.setConstraint(players[1], p2EmptySequence, 1);//E in root (only 1)
        lpTable.setConstraintType(players[1], 1);
    }

    public void initObjective(Sequence p1EmptySequence) {
        lpTable.setObjective(players[0], 1);
    }

    protected Object getLastISKey(Sequence sequence) {
        InformationSet informationSet = sequence.getLastInformationSet();

        return new Pair<Integer, Sequence>(informationSet.hashCode(), informationSet.getPlayersHistory());
    }
}
