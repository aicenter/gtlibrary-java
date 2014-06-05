package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.milp;

import ilog.concert.IloException;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.InitialQBuilder;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.IterationData;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;

public class MILPInitialQ extends InitialQBuilder {

	public MILPInitialQ(Expander<SequenceInformationSet> expander, GameState rootState, double initialValue) {
		super(expander, rootState, initialValue);
	}

	@Override
	public void initTable() {
		Sequence p1EmptySequence = new ArrayListSequenceImpl(players[0]);
		Sequence p2EmptySequence = new ArrayListSequenceImpl(players[1]);

		lpTable = new MILPNFPTable();

		initE(p1EmptySequence);
		initF(p2EmptySequence);
		inite();
		addPreviousItConstraints(p2EmptySequence);
	}
//	
//	@Override
//	public IterationData solve() {
//		try {
//			LPData lpData = lpTable.toCplex();
//
//			lpData.getSolver().exportModel(lpFileName);
//			boolean solved = lpData.getSolver().solve();
//			
//			if(!solved) {
//				System.out.println("Infeasible constraints: ");
//				System.out.println(lpData.getSolver().getInfeasibilities(lpData.getConstraints()));
//			}
//			System.out.println("Q: " + lpData.getSolver().solve());
//			System.out.println(lpData.getSolver().getStatus());
//			System.out.println(lpData.getSolver().getObjValue());
//			
////			System.out.println(Arrays.toString(lpData.getSolver().getValues(lpData.getVariables())));
////			for (int i = 0; i < lpData.getVariables().length; i++) {
////				System.out.println(lpData.getVariables()[i] + ": " + lpData.getSolver().getValue(lpData.getVariables()[i]));
////			}
//			return createIterationData(lpData);
//		} catch (IloException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
}
