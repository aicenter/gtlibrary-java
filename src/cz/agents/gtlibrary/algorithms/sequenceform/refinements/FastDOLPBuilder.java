package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

import java.util.Set;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class FastDOLPBuilder extends DOLPBuilder {

	public FastDOLPBuilder(Player[] players) {
		super(players);
		lpFileName = "DO_LP_mod_fast.lp";
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
		generationTime += System.currentTimeMillis() - startTime;
	}

	private void updateUtilities(DoubleOracleConfig<DoubleOracleInformationSet> config) {
		for (Sequence sequence : config.getNewSequences()) {
			if (sequence.getPlayer().equals(players[0]))
				updateForAllPrefixesOfP1(config, sequence);
			else
				updateForAllPrefixesOfP2(config, sequence);
		}
	}

	public void updateForAllPrefixesOfP2(DoubleOracleConfig<DoubleOracleInformationSet> config, Sequence sequence) {
		for (Sequence p1Sequence : config.getCompatibleSequencesFor(sequence)) {
			Set<Sequence> prefixes = sequence.getAllPrefixes();

			prefixes.add(sequence);
			for (Sequence prefix : prefixes) {
				updateUtility(config, p1Sequence, prefix);
			}
		}
	}

	public void updateForAllPrefixesOfP1(DoubleOracleConfig<DoubleOracleInformationSet> config, Sequence sequence) {
		for (Sequence p2Sequence : config.getCompatibleSequencesFor(sequence)) {
			Set<Sequence> prefixes = sequence.getAllPrefixes();

			prefixes.add(sequence);
			for (Sequence prefix : prefixes) {
				updateUtility(config, prefix, p2Sequence);
			}
		}
	}

	public void updateUtility(DoubleOracleConfig<DoubleOracleInformationSet> config, Sequence p1Sequence, Sequence p2Sequence) {
		Double utility = config.getUtilityForSequences(p1Sequence, p2Sequence);
		Key eqKey = new Key(p1Sequence);
		Key varKey = new Key(p2Sequence);

		lpTable.clearConstraint(eqKey, varKey);
		if (utility != null)
			lpTable.substractFromConstraint(eqKey, varKey, utility);
	}
}
