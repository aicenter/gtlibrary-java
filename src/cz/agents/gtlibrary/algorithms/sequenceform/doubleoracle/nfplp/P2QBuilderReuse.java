package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import ilog.concert.IloException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class P2QBuilderReuse extends InitialP2QBuilderReuse {

    private Map<Sequence, Double> explSeqSum;

    public P2QBuilderReuse(Player[] players, GameInfo info) {
        super(players, info);
        explSeqSum = new HashMap<Sequence, Double>();
    }


    public void update(double gameValue, QResultReuse data, SequenceFormConfig<? extends SequenceInformationSet> config) {
        this.explSeqSum = getSum(data.getLastItSeq(), data.getExplSeqSum(), gameValue);
        clearSlacks(config.getSequencesFor(players[0]));
        for (Map.Entry<Sequence, Double> entry : explSeqSum.entrySet()) {
            updateSlackVariable(entry);
        }

    }

    private void updateSlackVariable(Map.Entry<Sequence, Double> entry) {
        lpTable.setConstraint(entry.getKey(), "s", entry.getValue());
    }

//    @Override
//    public void buildLP(DoubleOracleConfig<DoubleOracleInformationSet> config, double initialValue) {
//        clearSlacks(config.getSequencesFor(players[0]));
//        super.buildLP(config, initialValue);
//    }

    private void clearSlacks(Iterable<Sequence> sequences) {
        for (Sequence sequence : sequences) {
            lpTable.removeFromConstraint(sequence, "s");
        }
    }

//    @Override
//    protected void updateForP1(Sequence p1Sequence) {
//        super.updateForP1(p1Sequence);
//        Double value = explSeqSum.get(p1Sequence);
//
//        if (value != null)
//            lpTable.setConstraint(p1Sequence, "s", explSeqSum.get(p1Sequence));
//    }

    @Override
    protected Map<Sequence, Double> getSum(Set<Sequence> exploitableSequences, Map<Sequence, Double> explSeqSum, double valueOfGame) {
        Map<Sequence, Double> updatedSum = new HashMap<Sequence, Double>(explSeqSum);

        for (Sequence sequence : exploitableSequences) {
            Double oldValue = updatedSum.get(sequence);

            updatedSum.put(sequence, (oldValue == null ? 0 : oldValue) + valueOfGame);
        }
        return updatedSum;
    }

    @Override
    protected QResultReuse createResult(LPData lpData) throws IloException {
        Map<Sequence, Double> watchedSequenceValues = getWatchedUSequenceValues(lpData);
        Set<Sequence> exploitableSequences = getExploitableSequences(watchedSequenceValues);

        return new QResultReuse(lpData.getSolver().getObjValue(), explSeqSum, exploitableSequences, getRealizationPlan(lpData));
    }

//    public void updateSolver() {
//        try {
//            lpTable.toCplex();
//        } catch (IloException e) {
//            e.printStackTrace();
//        }
//    }
}
