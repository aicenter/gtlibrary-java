package cz.agents.gtlibrary.domain.aceofspades;

import java.util.LinkedList;
import java.util.List;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class AoSExpander<I extends InformationSet> extends ExpanderImpl<I>{

	private static final long serialVersionUID = 8770670308276388429L;

	public AoSExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

	@Override
	public List<Action> getActions(GameState gameState) {
		List<Action> actions = new LinkedList<Action>();
		
		if(gameState.isPlayerToMoveNature()) {
			actions.add(new NatureAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), true));
			actions.add(new NatureAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), false));
			return actions;
		}
		if(gameState.getPlayerToMove().equals(AoSGameInfo.FIRST_PLAYER)) {
			actions.add(new FirstPlayerAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), false));
			actions.add(new FirstPlayerAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), true));
			return actions;
		}
		actions.add(new SecondPlayerAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), true));
		actions.add(new SecondPlayerAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), false));
		return actions;
	}

}
