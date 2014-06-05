package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import ilog.concert.IloException;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class P2QUpdater extends InitialP2QBuilder {

    private Map<Sequence, Double> explSeqSum;

    public P2QUpdater(Player[] players, DoubleOracleConfig<DoubleOracleInformationSet> config, double initialValue, RecyclingNFPTable table) {
        super(players, config, initialValue);
        this.lpTable = table;
    }

    public void buildLP(double gameValue, QResult data) {
        this.explSeqSum = getSum(data.getLastItSeq(), data.getExplSeqSum(), gameValue);
        super.buildLP();
    }

    @Override
    public void initTable() {
    }

    @Override
    protected void updateForP1(Sequence p1Sequence) {
        Double value = explSeqSum.get(p1Sequence);

        if (value != null)
            lpTable.setConstraint(p1Sequence, "s", explSeqSum.get(p1Sequence));
    }

    @Override
    protected void updateForP2(Sequence p2Sequence) {
    }

    @Override
    protected void addUtilities(Iterable<Sequence> p1Sequences, Iterable<Sequence> p2Sequences) {
    }

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
    protected QResult createResult(LPData lpData) throws IloException {
        Map<Sequence, Double> watchedSequenceValues = getWatchedUSequenceValues(lpData);
        Set<Sequence> exploitableSequences = getExploitableSequences(watchedSequenceValues);

        return new QResult(lpData.getSolver().getObjValue(), explSeqSum, exploitableSequences, getRealizationPlan(lpData));
    }
}
