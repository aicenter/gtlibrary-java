package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp.reusing;

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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class InitialP2PBuilderReuse {

    protected String lpFileName;
    protected RecyclingNFPTable lpTable;
    protected DoubleOracleConfig<DoubleOracleInformationSet> config;
    protected Player[] players;

    public InitialP2PBuilderReuse(Player[] players) {
        this.players = players;
        lpFileName = "P2DO_P.lp";
        initTable();
    }

    public void buildLP(DoubleOracleConfig<DoubleOracleInformationSet> config) {
        this.config = config;
        for (Sequence sequence : config.getNewSequences()) {
            if (sequence.getPlayer().equals(players[0]))
                updateForP1(sequence);
            else
                updateForP2(sequence);
        }
        updateUtilities(config);
//        addUtilities(config.getSequencesFor(players[0]), config.getSequencesFor(players[1]));
    }

    private void updateUtilities(DoubleOracleConfig<DoubleOracleInformationSet> config) {
        for (Sequence p1Sequence : config.getSequencesFor(players[0])) {
            for (Sequence p2Sequence : config.getSequencesFor(players[1])) {
                Double utility = config.getUtilityFor(p1Sequence, p2Sequence);

                if (utility == null)
                    lpTable.removeFromConstraint(p1Sequence, p2Sequence);
                else
                    lpTable.setConstraint(p1Sequence, p2Sequence, utility);
            }
        }
    }
//    private void clearUtilities(DoubleOracleConfig<DoubleOracleInformationSet> config) {
//        Set<Sequence> newSequences = config.getNewSequences();
//
//        for (Sequence p1Sequence : config.getSequencesFor(players[0])) {
//            if (!newSequences.contains(p1Sequence))
//                for (Sequence p2Sequence : config.getSequencesFor(players[1])) {
//                    if (!newSequences.contains(p2Sequence))
//                        lpTable.removeFromConstraint(p1Sequence, p2Sequence);
//                }
//        }
//    }
//
//    protected void addUtilities(Iterable<Sequence> p1Sequences, Iterable<Sequence> p2Sequences) {
//        for (Sequence p1Sequence : p1Sequences) {
//            for (Sequence p2Sequence : p2Sequences) {
//                Double utility = config.getUtilityFor(p1Sequence, p2Sequence);
//
//                if (utility != null) {
//                    lpTable.substractFromConstraint(p1Sequence, p2Sequence, -utility);
//                }
//            }
//        }
//    }

//    protected void addUtilities(Iterable<Sequence> newSequences) {
//        Set<Pair<Sequence, Sequence>> blackList = new HashSet<Pair<Sequence, Sequence>>();
//
//        for (Sequence newSequence : newSequences) {
//            if (newSequence.getPlayer().equals(players[0]))
//                for (Sequence compatibleSequence : config.getCompatibleSequencesFor(newSequence)) {
//                    Double utility = config.getUtilityFor(newSequence, compatibleSequence);
//                    Pair<Sequence, Sequence> sequencePair = new Pair<Sequence, Sequence>(newSequence, compatibleSequence);
//
//                    if (utility != null)
//                        if (!blackList.contains(sequencePair)) {
//                            lpTable.substractFromConstraint(newSequence, compatibleSequence, -utility);
//                            blackList.add(sequencePair);
//                        }
//                    clearForAllP1Prefixes(newSequence);
//                }
//            else
//                for (Sequence compatibleSequence : config.getCompatibleSequencesFor(newSequence)) {
//                    Double utility = config.getUtilityFor(newSequence, compatibleSequence);
//                    Pair<Sequence, Sequence> sequencePair = new Pair<Sequence, Sequence>(compatibleSequence, newSequence);
//
//                    if (utility != null)
//                        if (!blackList.contains(sequencePair)) {
//                            lpTable.substractFromConstraint(compatibleSequence, newSequence, -utility);
//                            blackList.add(sequencePair);
//                        }
//                    clearForAllP2Prefixes(newSequence);
//                }
//        }
//    }
//
//    private void clearForAllP1Prefixes(Sequence newSequence, Set<Pair<Sequence, Sequence>> blackList) {
//        for (Sequence prefix : newSequence.getAllPrefixes()) {
//            for (Sequence compatibleSequence : config.getCompatibleSequencesFor(prefix)) {
//                Double utility = config.getUtilityFor(prefix, compatibleSequence);
//                if (utility == null)
//                    lpTable.removeFromConstraint(prefix, compatibleSequence);
//                else
//
//            }
//        }
//
//    }
//
//    private void clearForAllP2Prefixes(Sequence newSequence) {
//        for (Sequence prefix : newSequence.getAllPrefixes()) {
//            for (Sequence compatibleSequence : config.getCompatibleSequencesFor(prefix)) {
//                if (config.getUtilityFor(prefix, compatibleSequence) == null)
//                    lpTable.removeFromConstraint(compatibleSequence, prefix);
//            }
//        }
//
//    }

    protected void updateForP1(Sequence p1Sequence) {
        if (p1Sequence.size() == 0)
            return;
        Object eqKey = getSubsequence(p1Sequence);
        Object varKey = getLastISKey(p1Sequence);

        lpTable.setConstraint(eqKey, varKey, -1);//F
        lpTable.setConstraintType(eqKey, 0);
        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
        addLinksToPrevISForP1(p1Sequence, varKey);
//        lpTable.setConstraint(p1Sequence, varKey, 1);//F
//        lpTable.setConstraintType(p1Sequence, 0);
    }

    public void addLinksToPrevISForP1(Sequence sequence, Object varKey) {
        SequenceInformationSet set = (SequenceInformationSet) sequence.getLastInformationSet();

        for (Sequence outgoingSequence : set.getOutgoingSequences()) {
            lpTable.setConstraint(outgoingSequence, varKey, 1);//F child
            lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
            lpTable.setConstraintType(outgoingSequence, 0);

//            lpTable.setConstraint(outgoingSequence, tmpKey, -1);//u (eye)
//			lpTable.setObjective(tmpKey, 0);//k(\epsilon)
        }
    }

    protected void updateForP2(Sequence p2Sequence) {
        lpTable.watchPrimalVariable(p2Sequence, p2Sequence);
        if (p2Sequence.size() == 0)
            return;
        Object varKey = getSubsequence(p2Sequence);
        Object eqKey = getLastISKey(p2Sequence);

        lpTable.setConstraint(eqKey, varKey, -1);//E
        lpTable.setConstraintType(eqKey, 1);
        lpTable.setLowerBound(varKey, 0);
//        lpTable.setConstraint(eqKey, p2Sequence, 1);//E
//        lpTable.setLowerBound(p2Sequence, 0);
        addLinksToPrevISForP2(p2Sequence, eqKey);
    }

    protected void addLinksToPrevISForP2(Sequence sequence, Object eqKey) {
        SequenceInformationSet set = (SequenceInformationSet) sequence.getLastInformationSet();

        for (Sequence outgoingSequence : set.getOutgoingSequences()) {
//            Key tmpKey = new Key("V", outgoingSequence);

            lpTable.setConstraint(eqKey, outgoingSequence, 1);//E child
            lpTable.setConstraintType(eqKey, 1);
            lpTable.setLowerBound(outgoingSequence, 0);
//			lpTable.setConstant(tmpKey, 0);//l(\epsilon)
        }

    }


    protected Sequence getSubsequence(Sequence sequence) {
        return sequence.getSubSequence(sequence.size() - 1);
    }

    public PResultReuse solve() {
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

            return new PResultReuse(createSecondPlayerStrategy(lpData.getSolver(), lpData.getWatchedPrimalVariables()), lpData.getSolver().getObjValue());
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
