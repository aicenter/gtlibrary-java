package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.Key;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class InitialQBuilder {

    protected DoubleOracleConfig<DoubleOracleInformationSet> config;
    protected Player[] players;
    protected RecyclingNFPTable lpTable;
    protected String lpFileName;
    protected double initialValue;

    public InitialQBuilder(Player[] players, DoubleOracleConfig<DoubleOracleInformationSet> config, double initialValue) {
        this.players = players;
        this.config = config;
        this.initialValue = initialValue;
        lpFileName = "P1DO_Q.lp";
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

    protected void updateForP1(Sequence p1Sequence) {
        lpTable.watchPrimalVariable(p1Sequence, p1Sequence);
        if (p1Sequence.size() == 0)
            return;
        Object varKey = getSubsequence(p1Sequence);
        Object eqKey = getLastISKey(p1Sequence);

        lpTable.setConstraint(eqKey, varKey, -1);//E
        lpTable.setConstraintType(eqKey, 1);
        lpTable.setLowerBound(varKey, 0);
        lpTable.setConstraint(eqKey, p1Sequence, 1);//E
        lpTable.setLowerBound(p1Sequence, 0);
    }

    protected void updateForP2(Sequence p2Sequence) {
        addU(p2Sequence);
        if (p2Sequence.size() == 0)
            return;
        Object eqKey = getSubsequence(p2Sequence);
        Object varKey = getLastISKey(p2Sequence);

        lpTable.setConstraint(eqKey, varKey, -1);//F
        lpTable.setConstraintType(eqKey, 0);
        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
        lpTable.setConstraint(p2Sequence, varKey, 1);//F
        lpTable.setConstraintType(p2Sequence, 0);
    }

//    protected void updateForP1(Sequence p1Sequence) {
//        if (p1Sequence.size() == 0)
//            return;
//        Object varKey = getSubsequence(p1Sequence);
//        Object eqKey = getLastISKey(p1Sequence);
//
//        lpTable.setConstraint(eqKey, varKey, -1);//E
//        lpTable.setConstraintType(eqKey, 1);
//        lpTable.setLowerBound(varKey, 0);
//        lpTable.watchPrimalVariable(p1Sequence, p1Sequence);
//        addLinksToPrevISForP1(p1Sequence, eqKey);
//    }
//
//    public void addLinksToPrevISForP1(Sequence sequence, Object eqKey) {
//        SequenceInformationSet set = (SequenceInformationSet) sequence.getLastInformationSet();
//
//        for (Sequence outgoingSequence : set.getOutgoingSequences()) {
//            lpTable.setConstraint(eqKey, outgoingSequence, 1);//E child
//            lpTable.setLowerBound(outgoingSequence, 0);
//            lpTable.watchPrimalVariable(outgoingSequence, outgoingSequence);
//        }
//    }
//
//
//    protected void updateForP2(Sequence p2Sequence) {
//        addU(p2Sequence);
//        if (p2Sequence.size() == 0)
//            return;
//        Object eqKey = getSubsequence(p2Sequence);
//        Object varKey = getLastISKey(p2Sequence);
//
//        lpTable.setConstraint(eqKey, varKey, -1);//F
//        lpTable.setConstraintType(eqKey, 0);
//        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
//        addLinksToPrevISForP2(p2Sequence, varKey);
//    }
//
//    protected void addLinksToPrevISForP2(Sequence sequence, Object varKey) {
//        SequenceInformationSet set = (SequenceInformationSet) sequence.getLastInformationSet();
//
//        for (Sequence outgoingSequence : set.getOutgoingSequences()) {
//            lpTable.setConstraint(outgoingSequence, varKey, 1);
//            lpTable.setConstraintType(outgoingSequence, 0);
//            lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
//        }
//    }


    protected void addUtilities(Iterable<Sequence> p1Sequences, Iterable<Sequence> p2Sequences) {
        for (Sequence p1Sequence : p1Sequences) {
            for (Sequence p2Sequence : p2Sequences) {
                Double utility = config.getUtilityFor(p1Sequence, p2Sequence);

                if (utility != null) {
                    lpTable.substractFromConstraint(p2Sequence, p1Sequence, utility);
                }
            }
        }
    }

    protected Sequence getSubsequence(Sequence sequence) {
        return sequence.getSubSequence(sequence.size() - 1);
    }

    public QResult solve() {
        try {
            LPData lpData = lpTable.toCplex();
            boolean solved = false;

//            lpData.getSolver().exportModel(lpFileName);
            for (int algorithm : lpData.getAlgorithms()) {
                lpData.getSolver().setParam(IloCplex.IntParam.RootAlg, algorithm);
                if (solved = trySolve(lpData))
                    break;
            }
            if (!solved)
                solveUnfeasibleLP(lpData);
            System.out.println(lpData.getSolver().getStatus());
            System.out.println(lpData.getSolver().getObjValue());

            //			System.out.println(Arrays.toString(lpData.getSolver().getValues(lpData.getVariables())));
//            for (int i = 0; i < lpData.getVariables().length; i++) {
//                System.out.println(lpData.getVariables()[i] + ": " + lpData.getSolver().getValue(lpData.getVariables()[i]));
//            }
            return createResult(lpData);
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected QResult createResult(LPData lpData) throws IloException {
        Map<Sequence, Double> watchedSequenceValues = getWatchedUSequenceValues(lpData);
        Set<Sequence> exploitableSequences = getExploitableSequences(watchedSequenceValues);
        Map<Sequence, Double> updatedSum = getSum(exploitableSequences, null, initialValue);

        return new QResult(lpData.getSolver().getObjValue(), updatedSum, exploitableSequences, getRealizationPlan(lpData));
    }


    protected boolean trySolve(LPData lpData) {
        boolean solved = false;

        try {
            solved = lpData.getSolver().solve();
            System.out.println("Q: " + solved);
            System.out.println("Status: " + lpData.getSolver().getStatus());

        } catch (IloException e) {
            e.printStackTrace();
            return false;
        }

        if (!solved)
            try {
                printUnfeasibleConstraints(lpData);
            } catch (UnknownObjectException e) {
                e.printStackTrace();
            } catch (IloException e) {
                e.printStackTrace();
            }
        return solved;
    }

    protected void printUnfeasibleConstraints(LPData lpData) throws UnknownObjectException, IloException {
        double[] infeasibilities = lpData.getSolver().getInfeasibilities(lpData.getConstraints());

        for (int i = 0; i < lpData.getConstraints().length; i++) {
            if (infeasibilities[i] > 10) {
                System.out.println(lpData.getConstraints()[i] + ": " + infeasibilities[i]);
                System.out.println(i);
            }
        }
        //		double[] preferences = new double[lpData.getConstraints().length];
        //
        //		Arrays.fill(preferences, 1);
        //		boolean refined = lpData.getSolver().refineConflict(lpData.getConstraints(), preferences);
        //
        //		System.out.println("Refined conflict: " + refined);
        //		if (refined) {
        //			ConflictStatus[] conflicts = lpData.getSolver().getConflict(lpData.getConstraints());
        //
        //			for (int i = 0; i < lpData.getConstraints().length; i++) {
        //				if (conflicts[i] == IloCplex.ConflictStatus.Member)
        //					System.out.println("Proved member: " + lpData.getConstraints()[i]);
        //				if (conflicts[i] == IloCplex.ConflictStatus.PossibleMember)
        //					System.out.println("Possible member: " + lpData.getConstraints()[i]);
        //			}
        //		}
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

    protected Map<Sequence, Double> getUValues(Map<Sequence, Double> watchedSequenceValues) {
        Map<Sequence, Double> uValues = new HashMap<Sequence, Double>();

        for (Entry<Sequence, Double> entry : watchedSequenceValues.entrySet()) {
            if (entry.getValue() > 1e-2)
                uValues.put(entry.getKey(), entry.getValue());
        }
        return uValues;
    }

    protected Map<Sequence, Double> getSum(Set<Sequence> exploitableSequences, Map<Sequence, Double> explSeqSum, double valueOfGame) {
        Map<Sequence, Double> sum = new HashMap<Sequence, Double>();

        //		for (Sequence sequence : exploitableSequences) {
        //			sum.put(sequence, valueOfGame);
        //		}
        return sum;
    }

    protected Set<Sequence> getExploitableSequences(Map<Sequence, Double> watchedSequenceValues) {
        Set<Sequence> exploitableSequences = new HashSet<Sequence>();

        for (Entry<Sequence, Double> entry : watchedSequenceValues.entrySet()) {
            //			assert Math.abs(entry.getValue()) < 1e-5 || Math.abs(entry.getValue() - 1) < 1e-5;
            if (entry.getValue() > 0.5 && entry.getKey().size() > 0) {
                Sequence subSequence = entry.getKey().getSubSequence(entry.getKey().size() - 1);

                if (watchedSequenceValues.get(subSequence) < 0.5)
                    exploitableSequences.add(entry.getKey());
            }
        }
        return exploitableSequences;
    }

    protected Map<Sequence, Double> getWatchedUSequenceValues(LPData lpData) throws UnknownObjectException, IloException {
        Map<Sequence, Double> watchedSequenceValues = new HashMap<Sequence, Double>(lpData.getWatchedPrimalVariables().size());

        for (Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Key)
                watchedSequenceValues.put(getSequence(entry), lpData.getSolver().getValue(entry.getValue()));
        }
        return watchedSequenceValues;
    }

    protected Sequence getSequence(Entry<Object, IloNumVar> entry) {
        return (Sequence) ((Key) entry.getKey()).getObject();
    }

    protected Map<Sequence, Double> getRealizationPlan(LPData lpData) throws UnknownObjectException, IloException {
        Map<Sequence, Double> watchedSequenceValues = new HashMap<Sequence, Double>(lpData.getWatchedPrimalVariables().size());

        for (Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Sequence)
                watchedSequenceValues.put((Sequence) entry.getKey(), lpData.getSolver().getValue(entry.getValue()));
        }
        return watchedSequenceValues;
    }

    public Map<Sequence, Double> createFirstPlayerStrategy(IloCplex cplex, Map<Object, IloNumVar> watchedPrimalVars) throws IloException {
        Map<Sequence, Double> p1Strategy = new HashMap<Sequence, Double>();

        for (Entry<Object, IloNumVar> entry : watchedPrimalVars.entrySet()) {
            p1Strategy.put((Sequence) entry.getKey(), cplex.getValue(entry.getValue()));
        }
        return p1Strategy;
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

        initE(p1EmptySequence);
        initF(p2EmptySequence);
        inite();
        addPreviousItConstraints(p2EmptySequence);
    }

    protected void addPreviousItConstraints(Sequence p2EmptySequence) {
        lpTable.setConstraint("prevIt", players[1], 1);
        lpTable.setConstraint("prevIt", "s", -initialValue);
        lpTable.setConstraintType("prevIt", 1);
        lpTable.setLowerBound("s", 1);
    }

    public void inite() {
        lpTable.setConstraint(players[0], "s", -1);//e for root
    }

    public void initF(Sequence p2EmptySequence) {
        lpTable.setConstraint(p2EmptySequence, players[1], 1);//F in root (only 1)
        lpTable.setConstraintType(p2EmptySequence, 0);
        lpTable.setLowerBound(players[1], Double.NEGATIVE_INFINITY);
    }

    public void initE(Sequence p1EmptySequence) {
        lpTable.setConstraint(players[0], p1EmptySequence, 1);//E in root (only 1)
        lpTable.setConstraintType(players[0], 1);
    }

    public void initCost(Sequence p2EmptySequence) {
        lpTable.setObjective(players[1], 1);
    }

    protected void addU(Object eqKey) {
        Key uKey = new Key("u", eqKey);

        lpTable.setConstraint(eqKey, uKey, 1);
        lpTable.setLowerBound(uKey, 0);
        lpTable.setUpperBound(uKey, 1);
        lpTable.setObjective(uKey, 1);
        lpTable.watchPrimalVariable(uKey, uKey);
        lpTable.markRelaxableConstraint(eqKey);
    }

    protected Object getLastISKey(Sequence sequence) {
        InformationSet informationSet = sequence.getLastInformationSet();

        return new Pair<Integer, Sequence>(informationSet.hashCode(), informationSet.getPlayersHistory());
    }

}
