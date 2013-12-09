package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp.reusing;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import ilog.concert.IloException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class QBuilderReuse extends InitialQBuilderReuse {

	private Map<Sequence, Double> explSeqSum;

	public QBuilderReuse(Player[] players) {
		super(players);
        explSeqSum = new HashMap<Sequence, Double>();
	}

    public void updateSum(double gameValue, QResultReuse data) {
        this.explSeqSum = getSum(data.getLastItSeq(), data.getExplSeqSum(), gameValue);
    }

    @Override
    public void buildLP(DoubleOracleConfig<DoubleOracleInformationSet> config, double initialValue) {
        clearSlacks(config.getSequencesFor(players[1]));
        super.buildLP(config, initialValue);
    }

    private void clearSlacks(Iterable<Sequence> sequences) {
        for (Sequence sequence : sequences) {
            lpTable.removeFromConstraint(sequence, "s");
        }
    }

    @Override
    protected void updateForP2(Sequence p2Sequence) {
        super.updateForP2(p2Sequence);
        Double value = explSeqSum.get(p2Sequence);

        if (value != null)
            lpTable.setConstraint(p2Sequence, "s", value);
    }

	@Override
	protected Map<Sequence, Double> getSum(Set<Sequence> exploitableSequences, Map<Sequence, Double> explSeqSum, double valueOfGame) {
		Map<Sequence, Double> updatedSum = new HashMap<Sequence, Double>(explSeqSum);

		for (Sequence sequence : exploitableSequences) {
			Double value = updatedSum.get(sequence);
			updatedSum.put(sequence, (value == null ? 0 : value) + valueOfGame);
		}
		return updatedSum;
	}

    @Override
    protected QResultReuse createResult(LPData lpData) throws IloException {
        Map<Sequence, Double> watchedSequenceValues = getWatchedUSequenceValues(lpData);
        Set<Sequence> exploitableSequences = getExploitableSequences(watchedSequenceValues);

        return new QResultReuse(lpData.getSolver().getObjValue(), explSeqSum, exploitableSequences, getRealizationPlan(lpData));
    }

    public void updateSolver() {
        try {
            lpTable.toCplex();
        } catch (IloException e) {
            e.printStackTrace();
        }
    }
}
