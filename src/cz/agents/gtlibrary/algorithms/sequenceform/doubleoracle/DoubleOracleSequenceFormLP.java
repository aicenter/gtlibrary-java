package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle;

import ilog.concert.IloException;
import cz.agents.gtlibrary.algorithms.sequenceform.GeneralSequenceFormLP;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

public class DoubleOracleSequenceFormLP extends GeneralSequenceFormLP {

	public DoubleOracleSequenceFormLP(Player[] players) {
		super(players);
	}

	public Double calculateStrategyForPlayer(int secondPlayerIndex, GameState root, SequenceFormConfig<SequenceInformationSet> algConfig) {
		try {
			int firstPlayerIndex = (1 + secondPlayerIndex) % 2;
			createVariables(algConfig, root.getAllPlayers()); // TODO replace with iterative creating only necessary variables			
			return calculateOnePlStrategy(algConfig, root, root.getAllPlayers()[firstPlayerIndex], root.getAllPlayers()[secondPlayerIndex]); // TODO replace with iterative building the LP  		
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
	} 
}
