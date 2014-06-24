package cz.agents.gtlibrary.algorithms.cfr.os;

import cz.agents.gtlibrary.algorithms.cfr.CFR;
import cz.agents.gtlibrary.algorithms.cfr.CFRConfig;
import cz.agents.gtlibrary.interfaces.GameState;


/**
 * This class will be removed, the implementation of CFR and OOS is obsolete.
 * Use cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm/CFRISAlgorithm instead.
 */
@Deprecated
public class OutcomeSamplingCFR extends CFR<OSInformationSet> {

	public OutcomeSamplingCFR(CFRConfig<OSInformationSet> config) {
		super(config);
		// TODO Auto-generated constructor stub
	}

	@Override
	public OSInformationSet createInformationSet(GameState state) {
		return new OSInformationSet(state);
	}

	@Override
	public void updateTree() {
		// TODO Auto-generated method stub
		
	}

}
