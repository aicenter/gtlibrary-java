package cz.agents.gtlibrary.cfr;

import java.util.List;

import cz.agents.gtlibrary.iinodes.IIConfig;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;


public class OutcomeSamplingCFR extends CFR<OSInformationSet> {

	public OutcomeSamplingCFR(CFRConfig<OSInformationSet> config) {
		super(config);
		// TODO Auto-generated constructor stub
	}

	@Override
	public OSInformationSet createInformationSet(GameState state, List<Action> actions) {
		return new OSInformationSet(state, actions);
	}

	@Override
	public void updateTree() {
		// TODO Auto-generated method stub
		
	}

}
