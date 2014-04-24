package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.bothplayerslp;

import java.util.HashSet;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public class RecyclingDOLPBuilder extends DOLPBuilder {

	public RecyclingDOLPBuilder(Player[] players) {
		super(players);
		lpFileName = "DO_LP_mod_faster.lp";
	}

	@Override
	public void initTable() {
		Sequence p1EmptySequence = new LinkedListSequenceImpl(players[0]);
		Sequence p2EmptySequence = new LinkedListSequenceImpl(players[1]);

		if (lpTable == null)
			lpTable = new RecyclingLPTable();
		lpTable.clearTable();

		initCost(p1EmptySequence);
		initE(p1EmptySequence);
		initF(p2EmptySequence);
		initf(p2EmptySequence);
	}

	@Override
	public void calculateStrategyForPlayer(int playerIndex, GameState root, DoubleOracleConfig<DoubleOracleInformationSet> config, double currentBoundSize) {
		long startTime = System.currentTimeMillis();

		p1Value = Double.NaN;
		for (Sequence sequence : config.getNewSequences()) {
			if (sequence.getPlayer().equals(players[0]))
				updateForP1(sequence);
			else
				updateForP2(sequence);
		}
		updateUtilities(config);
		constraintGenerationTime += System.currentTimeMillis() - startTime;
	}

	private void updateUtilities(DoubleOracleConfig<DoubleOracleInformationSet> config) {
		Set<Pair<Sequence, Sequence>> closedSequenceCombinations = new HashSet<Pair<Sequence, Sequence>>();

		for (Sequence sequence : config.getNewSequences()) {
			if (sequence.getPlayer().equals(players[0]))
				updateForAllPrefixesOfP1(config, sequence, closedSequenceCombinations);
			else
				updateForAllPrefixesOfP2(config, sequence, closedSequenceCombinations);
		}
	}

	public void updateForAllPrefixesOfP2(DoubleOracleConfig<DoubleOracleInformationSet> config, Sequence sequence, Set<Pair<Sequence, Sequence>> closedSeqComb) {
		for (Sequence p1Sequence : config.getCompatibleSequencesFor(sequence)) {
			Set<Sequence> prefixes = sequence.getAllPrefixes();

			prefixes.add(sequence);
			for (Sequence prefix : prefixes) {
				Pair<Sequence, Sequence> seqPair = new Pair<Sequence, Sequence>(p1Sequence, prefix);

				if (!closedSeqComb.contains(seqPair)) {
					updateUtility(config, p1Sequence, prefix);
					closedSeqComb.add(seqPair);
				}
			}
		}
	}

	public void updateForAllPrefixesOfP1(DoubleOracleConfig<DoubleOracleInformationSet> config, Sequence sequence, Set<Pair<Sequence, Sequence>> closedSeqComb) {
		for (Sequence p2Sequence : config.getCompatibleSequencesFor(sequence)) {
			Set<Sequence> prefixes = sequence.getAllPrefixes();

			prefixes.add(sequence);
			for (Sequence prefix : prefixes) {
				Pair<Sequence, Sequence> seqPair = new Pair<Sequence, Sequence>(prefix, p2Sequence);

				if (!closedSeqComb.contains(seqPair)) {
					updateUtility(config, prefix, p2Sequence);
					closedSeqComb.add(seqPair);
				}
			}
		}
	}

	public void updateUtility(DoubleOracleConfig<DoubleOracleInformationSet> config, Sequence p1Sequence, Sequence p2Sequence) {
		Double utility = config.getUtilityFor(p1Sequence, p2Sequence);

		lpTable.removeFromConstraint(p1Sequence, p2Sequence);
		if (utility != null)
			lpTable.substractFromConstraint(p1Sequence, p2Sequence, utility);
	}
}
