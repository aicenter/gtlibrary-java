package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp.reusing;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import ilog.concert.IloException;

import java.util.Map;
import java.util.Set;

public class P2PBuilderReuse extends InitialP2PBuilderReuse {

    private Set<Sequence> lastItSeq;
    private Map<Sequence, Double> explSeqSum;

    public P2PBuilderReuse(Player[] players) {
        super(players);

    }

    public void updateFromLastIteration(QResultReuse data, double initialValueOfGame) {
        this.lastItSeq = data.getLastItSeq();
        this.explSeqSum = data.getExplSeqSum();
        addPreviousItConstraints(initialValueOfGame);
    }

    private void addPreviousItConstraints(double initialValueOfGame) {
        lpTable.setConstraint("prevIt", players[0], 1);
        lpTable.setConstant("prevIt", initialValueOfGame);
        lpTable.setConstraintType("prevIt", 1);
    }

    @Override
    public void buildLP(DoubleOracleConfig<DoubleOracleInformationSet> config) {
        clearSlacks(config.getSequencesFor(players[0]));
        super.buildLP(config);
    }

    private void clearSlacks(Iterable<Sequence> sequences) {
        for (Sequence sequence : sequences) {
            lpTable.removeFromConstraint(sequence, "t");
            lpTable.removeConstant(sequence);
        }
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

    public void updateSolver() {
        try {
            lpTable.toCplex();
        } catch (IloException e) {
            e.printStackTrace();
        }
    }
}
