package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp.reusing;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import ilog.concert.IloException;

import java.util.Map;
import java.util.Set;

public class PBuilderReuse extends InitialPBuilderReuse {

    private Set<Sequence> lastItSeq;
    private Map<Sequence, Double> explSeqSum;

    public PBuilderReuse(Player[] players) {
        super(players);
    }

    public void updateFromLastIteration(QResultReuse data, Double initialValueOfGame) {
        this.lastItSeq = data.getLastItSeq();
        this.explSeqSum = data.getExplSeqSum();
        addPreviousItConstraints(initialValueOfGame);
    }

    @Override
    public void buildLP(DoubleOracleConfig<DoubleOracleInformationSet> config) {
        clearSlacks(config.getSequencesFor(players[1]));
        super.buildLP(config);
    }

    private void clearSlacks(Iterable<Sequence> sequences) {
        for (Sequence sequence : sequences) {
            lpTable.removeFromConstraint(sequence, "t");
            lpTable.removeConstant(sequence);
        }
    }

    private void addPreviousItConstraints(double initialValueOfGame) {
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

    public void updateSolver() {
        try {
            lpTable.toCplex();
        } catch (IloException e) {
            e.printStackTrace();
        }
    }
}
