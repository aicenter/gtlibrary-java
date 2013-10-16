package cz.agents.gtlibrary.domain.upordown;

import java.util.ArrayList;
import java.util.List;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class UDExpander<I extends InformationSet> extends ExpanderImpl<I>{

	private static final long serialVersionUID = 5404146015131595801L;

	public UDExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

	@Override
	public List<Action> getActions(GameState gameState) {
		if(gameState.getPlayerToMove().equals(UDGameInfo.FIRST))
			return getP1Actions(gameState);
		return getP2Actions(gameState);
	}

	private List<Action> getP2Actions(GameState gameState) {
		UDGameState udState = (UDGameState) gameState;
		List<Action> actions = new ArrayList<Action>(2);
		
		if(udState.getP1Action().getType().equals("U")) {
			actions.add(new P2UDAction(getAlgorithmConfig().getInformationSetFor(gameState), "l"));
			actions.add(new P2UDAction(getAlgorithmConfig().getInformationSetFor(gameState), "r"));
		} else {
			actions.add(new P2UDAction(getAlgorithmConfig().getInformationSetFor(gameState), "l'"));
			actions.add(new P2UDAction(getAlgorithmConfig().getInformationSetFor(gameState), "r'"));
		}
		return actions;
	}

	private List<Action> getP1Actions(GameState gameState) {
		List<Action> actions = new ArrayList<Action>(2);
		
		actions.add(new P1UDAction(getAlgorithmConfig().getInformationSetFor(gameState), "U"));
		actions.add(new P1UDAction(getAlgorithmConfig().getInformationSetFor(gameState), "D"));
		return actions;
	}

}
