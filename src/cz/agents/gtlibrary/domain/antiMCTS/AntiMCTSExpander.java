package cz.agents.gtlibrary.domain.antiMCTS;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import java.util.ArrayList;
import java.util.List;

public class AntiMCTSExpander<I extends cz.agents.gtlibrary.interfaces.InformationSet>  extends ExpanderImpl<I> {
    
	public AntiMCTSExpander(AlgorithmConfig<I> algConfig) {
        super(algConfig);
    }
    
     @Override
     public List<Action> getActions(GameState gameState) {
		AntiMCTSState s = (AntiMCTSState) gameState;
                InformationSet curIS = getAlgorithmConfig().getInformationSetFor(gameState);
		ArrayList<Action> actions = new ArrayList<Action>();
                actions.add(new AntiMCTSAction(false, curIS));
                actions.add(new AntiMCTSAction(true, curIS));
		return actions;
	}
}
