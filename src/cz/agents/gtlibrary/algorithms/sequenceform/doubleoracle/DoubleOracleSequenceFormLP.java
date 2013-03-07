package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle;

import ilog.concert.IloException;
import cz.agents.gtlibrary.algorithms.sequenceform.GeneralSequenceFormLP;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class DoubleOracleSequenceFormLP extends GeneralSequenceFormLP {

	public DoubleOracleSequenceFormLP(Player[] players) {
		super(players);
	}

	public Double calculateStrategyForPlayer(int secondPlayerIndex, GameState root, DoubleOracleConfig algConfig) {
		try {
			int firstPlayerIndex = (1 + secondPlayerIndex) % 2;
			createVariables(algConfig, root.getAllPlayers());
			return calculateOnePlStrategy(algConfig, root, root.getAllPlayers()[firstPlayerIndex], root.getAllPlayers()[secondPlayerIndex]); // TODO replace with iterative building the LP  		
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
	}

    protected Double calculateOnePlStrategy(SequenceFormConfig<SequenceInformationSet> algConfig, GameState root, Player firstPlayer, Player secondPlayer) {
        try {
            IloCplex cplex = modelsForPlayers.get(firstPlayer);
            IloNumVar v0 = objectiveForPlayers.get(firstPlayer);

            createConstraintsForSequences(algConfig, cplex, sequences.get(firstPlayer));
            System.out.println("phase 1 done");
            createConstraintsForSets(secondPlayer, cplex, informationSets.get(secondPlayer));
            System.out.println("phase 2 done");
            cplex.exportModel("gt-lib-sqf-" + firstPlayer + ".lp"); // uncomment for model export
            System.out.println("Solving");
            cplex.solve();
            System.out.println("Status: " + cplex.getStatus());

            if (cplex.getCplexStatus() != IloCplex.CplexStatus.Optimal) {
                return null;
            }

            resultStrategies.put(secondPlayer, createSolution(algConfig, secondPlayer, cplex));
            resultValues.put(firstPlayer, cplex.getValue(v0));

            return cplex.getValue(v0);
        } catch (IloException e) {
            e.printStackTrace();
            return null;
        }
    }

}
