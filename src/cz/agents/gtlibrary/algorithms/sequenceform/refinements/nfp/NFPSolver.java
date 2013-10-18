package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp;

import java.util.Map;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.upordown.UDExpander;
import cz.agents.gtlibrary.domain.upordown.UDGameState;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

public class NFPSolver {
	
	private GameState root;
	private Expander<SequenceInformationSet> expander;
	
	public static void main(String[] args) {
		NFPSolver solver = new NFPSolver(new UDGameState(), new UDExpander<SequenceInformationSet>(new SequenceFormConfig<SequenceInformationSet>()));
		
		System.out.println(solver.solve());
	}
	
	public NFPSolver(GameState root, Expander<SequenceInformationSet> expander) {
		this.root = root;
		this.expander = expander;
	}
	
	public Map<Sequence, Double> solve() {
		InitialPBuilder initPbuilder = new InitialPBuilder(expander, root);
		
		initPbuilder.buildLP();
		double initialValue = initPbuilder.solve();
		
		InitialQBuilder initQBuilder = new InitialQBuilder(expander, root, initialValue);
		
		initQBuilder.buildLP();
		IterationData data = initQBuilder.solve();
		
		while(Math.abs(data.getGameValue()) > 1e-8) {
			PBuilder pBuilder = new PBuilder(expander, root, data, initialValue);
			
			pBuilder.buildLP();
			double currentValue = pBuilder.solve();
			
			QBuilder qBuilder = new QBuilder(expander, root, initialValue, currentValue, data);
			
			qBuilder.buildLP();
			data = qBuilder.solve();
		}
		return data.getRealizationPlan();
	}

}
