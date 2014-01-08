package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;
import java.util.Set;

public class P2PBuilder extends InitialP2PBuilder {

    private Set<Sequence> lastItSeq;
    private Map<Sequence, Double> explSeqSum;
    private double initialValueOfGame;

    public P2PBuilder(Player[] players, SequenceFormConfig<SequenceInformationSet> config, QResult data, double initialValueOfGame) {
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
        lpTable.setConstraint("prevIt", players[0], 1);
        lpTable.setConstant("prevIt", initialValueOfGame);
        lpTable.setConstraintType("prevIt", 1);
    }

    @Override
    public void initObjective(Sequence p2EmptySequence) {
        lpTable.setObjective("t", 1);
    }

    @Override
    protected void updateForP1(Sequence p1Sequence) {
        super.updateForP1(p1Sequence);

        if (lastItSeq.contains(p1Sequence))
            lpTable.setConstraint(p1Sequence, "t", 1);
        Double value = explSeqSum.get(p1Sequence);

        if (value != null)
            lpTable.setConstant(p1Sequence, -value);
    }
}
