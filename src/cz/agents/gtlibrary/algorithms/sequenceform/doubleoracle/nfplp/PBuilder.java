package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;
import java.util.Set;

public class PBuilder extends InitialPBuilder {

	private Set<Sequence> lastItSeq;
	private Map<Sequence, Double> explSeqSum;
	private double initialValueOfGame;

	public PBuilder(Player[] players, DoubleOracleConfig<DoubleOracleInformationSet> config, QResult data, double initialValueOfGame) {
		super(players, config);
		this.lastItSeq = data.getLastItSeq();
		this.explSeqSum = data.getExplSeqSum();
		this.initialValueOfGame = initialValueOfGame;
	}

	@Override
	public void initTable() {
		super.initTable();
		addPreviousItConstraints();
	}

	private void addPreviousItConstraints() {
		lpTable.setConstraint("prevIt", players[1], 1);
		lpTable.setConstant("prevIt", initialValueOfGame);
		lpTable.setConstraintType("prevIt", 1);
	}

	@Override
	public void initObjective(Sequence p2EmptySequence) {
		lpTable.setObjective("t", 1);
	}

    @Override
    protected void updateForP2(Sequence p2Sequence) {
        super.updateForP2(p2Sequence);
        if (lastItSeq.contains(p2Sequence))
            lpTable.setConstraint(p2Sequence, "t", 1);
        Double value = explSeqSum.get(p2Sequence);

        if (value != null)
            lpTable.setConstant(p2Sequence, -value);
    }

}
