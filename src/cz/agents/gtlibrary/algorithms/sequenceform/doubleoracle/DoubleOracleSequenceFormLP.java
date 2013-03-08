package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle;

import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import ilog.concert.IloException;
import cz.agents.gtlibrary.algorithms.sequenceform.GeneralSequenceFormLP;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DoubleOracleSequenceFormLP extends GeneralSequenceFormLP {

    private Map<Player, Set<Sequence>> newSequencesSinceLastLPCalculation = new FixedSizeMap<Player, Set<Sequence>>(2);
    private Player[] players;

	public DoubleOracleSequenceFormLP(Player[] players) {
		super(players);
        this.players = players;
        for (Player p : players) {
            newSequencesSinceLastLPCalculation.put(p, new HashSet<Sequence>());
        }
	}

	public Double calculateStrategyForPlayer(int secondPlayerIndex, GameState root, DoubleOracleConfig algConfig) {
		try {
			int firstPlayerIndex = (1 + secondPlayerIndex) % 2;
			createVariables(algConfig, root.getAllPlayers());

            for (Player p : players) {
                    newSequencesSinceLastLPCalculation.get(p).addAll(algConfig.getNewSequences());
            }

            setSequencesAndISForGeneratingConstraints(algConfig,players[firstPlayerIndex], players[secondPlayerIndex]);
			double v = calculateOnePlStrategy(algConfig, root, players[firstPlayerIndex], players[secondPlayerIndex]);
            newSequencesSinceLastLPCalculation.get(players[secondPlayerIndex]).clear();
            return v;
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
	}

    private void setSequencesAndISForGeneratingConstraints(DoubleOracleConfig algConfig, Player firstPlayer, Player secondPlayer) {
        sequences.get(firstPlayer).clear();
        informationSets.get(secondPlayer).clear();

        for (Sequence s : newSequencesSinceLastLPCalculation.get(secondPlayer)) {
            if (s.getPlayer().equals(firstPlayer)) {
                sequences.get(firstPlayer).add(s);
                if (s.size() > 0)
                    sequences.get(firstPlayer).add(s.getLast().getInformationSet().getPlayersHistory());
            } else if (s.getPlayer().equals(secondPlayer)) {
                sequences.get(firstPlayer).addAll(algConfig.getCompatibleSequencesFor(s));
                if (s.size() > 0)
                    informationSets.get(secondPlayer).add((SequenceInformationSet) s.getLast().getInformationSet());
                if (algConfig.getReachableSets(s) != null) {
                    for (SequenceInformationSet i : (Set<SequenceInformationSet>)algConfig.getReachableSets(s)) {
                        if (i.getPlayer().equals(secondPlayer))
                            informationSets.get(secondPlayer).add(i);
                    }
                }
            } else assert false;
        }
    }
}
