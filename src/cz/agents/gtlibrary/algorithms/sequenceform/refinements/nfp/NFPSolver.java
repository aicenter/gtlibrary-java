package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp;

import java.util.Map;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.upordown.UDExpander;
import cz.agents.gtlibrary.domain.upordown.UDGameState;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

public class NFPSolver {
	
	private GameState root;
	private Expander<SequenceInformationSet> expander;
	
	public static void main(String[] args) {
//		runUpOrDown();
		runAceOfSpades();
	}

	private static void runUpOrDown() {
		NFPSolver solver = new NFPSolver(new UDGameState(), new UDExpander<SequenceInformationSet>(new SequenceFormConfig<SequenceInformationSet>()));
		
		System.out.println(solver.solveForP1());
		System.out.println(solver.solveForP2());
	}
	
	private static void runAceOfSpades() {
		NFPSolver solver = new NFPSolver(new AoSGameState(), new AoSExpander<SequenceInformationSet>(new SequenceFormConfig<SequenceInformationSet>()));
		
		System.out.println(solver.solveForP1());
		System.out.println(solver.solveForP2());
	}
	
	public NFPSolver(GameState root, Expander<SequenceInformationSet> expander) {
		this.root = root;
		this.expander = expander;
	}
	
	public Map<Sequence, Double> solveForP1() {
		InitialPBuilder initPbuilder = new InitialPBuilder(expander, root);
		
		initPbuilder.buildLP();
		double initialValue = initPbuilder.solve();
		
		InitialQBuilder initQBuilder = new InitialQBuilder(expander, root, initialValue);
		
		initQBuilder.buildLP();
		IterationData data = initQBuilder.solve();
		
		if(data.getLastItSeq().isEmpty())
			return data.getRealizationPlan();
		while(Math.abs(data.getGameValue()) > 1e-8) {
			PBuilder pBuilder = new PBuilder(expander, root, data, initialValue);
			
			pBuilder.buildLP();
			double currentValue = pBuilder.solve();
			
			QBuilder qBuilder = new QBuilder(expander, root, initialValue, currentValue, data);
			
			qBuilder.buildLP();
			data = qBuilder.solve();
			if(data.getLastItSeq().isEmpty())
				return data.getRealizationPlan();
		}
		return data.getRealizationPlan();
	}
	
	public Map<Sequence, Double> solveForP2() {
		InitialP2PBuilder initPbuilder = new InitialP2PBuilder(expander, root);
		
		initPbuilder.buildLP();
		double initialValue = initPbuilder.solve();
		
		InitialP2QBuilder initQBuilder = new InitialP2QBuilder(expander, root, initialValue);
		
		initQBuilder.buildLP();
		IterationData data = initQBuilder.solve();
		
		if(data.getLastItSeq().isEmpty())
			return data.getRealizationPlan();
		while(Math.abs(data.getGameValue()) > 1e-8) {
			P2PBuilder pBuilder = new P2PBuilder(expander, root, data, initialValue);
			
			pBuilder.buildLP();
			double currentValue = pBuilder.solve();
			
			P2QBuilder qBuilder = new P2QBuilder(expander, root, initialValue, currentValue, data);
			
			qBuilder.buildLP();
			data = qBuilder.solve();
			
			if(data.getLastItSeq().isEmpty())
				return data.getRealizationPlan();
		}
		return data.getRealizationPlan();
	}

}
