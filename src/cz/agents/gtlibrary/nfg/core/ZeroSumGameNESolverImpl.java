/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.nfg.core;

import cz.agents.gtlibrary.nfg.simalphabeta.Killer;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.List;

import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.PlayerStrategySet;
import cz.agents.gtlibrary.nfg.PureStrategy;
import cz.agents.gtlibrary.nfg.Utility;
import cz.agents.gtlibrary.nfg.lplibrary.cplex.MIProblemCplex;
import cz.agents.gtlibrary.nfg.lplibrary.lpWrapper.AMIProblem;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;

/**
 * Basic implementation of the zero-sum game solver searching for a Nash
 * Equilibrium. Using the LP formulation and utilizing CPLEX to find the
 * solution.
 * 
 * @param <T>
 * @param <U>
 */
public class ZeroSumGameNESolverImpl<T extends PureStrategy, U extends PureStrategy> extends MIProblemCplex implements ZeroSumGameNESolver<T, U> {

	private static final int MM = Integer.MAX_VALUE;

	private PlayerStrategySet<T> playerOneStrategySet = null;
	private PlayerStrategySet<U> playerTwoStrategySet = null;

	private MixedStrategy<T> playerOneMixedStrategy = null;
	private MixedStrategy<U> playerTwoMixedStrategy = null;

	private Utility<T, U> utilityComputer;
	
	private double finalValue = -Double.MAX_VALUE;

	static private IloCplex singeltonModel = null;
	
	public ZeroSumGameNESolverImpl(Utility<T, U> utilityComputer) {
		this.utilityComputer = utilityComputer;

		try {
			if (singeltonModel == null) {
				singeltonModel = new IloCplex();
				singeltonModel.setParam(IloCplex.DoubleParam.EpMrk, 0.999);
				singeltonModel.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Dual);
				singeltonModel.setOut(null);
//				System.setErr(new PrintStream(new OutputStream() {
//			        public void close() {}
//			        public void flush() {}
//			        public void write(byte[] b) {}
//			        public void write(byte[] b, int off, int len) {}
//			        public void write(int b) {}
//
//			    } ));
			}
			singeltonModel.clearModel();
			cplex = singeltonModel;
//			cplex = new IloCplex();
			initProblem();
		} catch (IloException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	private void initProblem() throws IloException{
		cplex.setName("MIProblem");
		//objectiveFunction = cplex.getObjective();
		cplex.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Dual);
		setProblemType(AMIProblem.PROBLEM_TYPE.LP, AMIProblem.OBJECTIVE_TYPE.MAX);
		cplex.setOut(null);

//		variablesBackup.add(new StoreVariable("d", VARIABLE_TYPE.CONTINUOUS, 1, -MM, MM, null, null));
		addAndSetVariable("d", VARIABLE_TYPE.CONTINUOUS, 1, -MM, MM, null, null);
		//addConstraint("SumX", AMIProblem.BOUNDS_TYPE.FIXED, 1.0);
//		constraintsBackup.add(new StoreConstraint("SumX", BOUNDS_TYPE.FIXED, 1, null, null));
		addAndSetConstraint("SumX", BOUNDS_TYPE.FIXED, 1, null, null);
	}

	@Override
	protected void setProblemType() {
		setProblemType(PROBLEM_TYPE.LP, OBJECTIVE_TYPE.MAX);
	}

	@Override
	protected void setVariableBounds() {
	}

	@Override
	protected void setConstraintBounds() {
	}

	@Override
	protected void generateData() {

	}

	protected void setProblemName(String name) {
		cplex.setName(name);
	}

	@Override
	public void computeNashEquilibrium() {
//		//TODO
//		if (history.getSequenceFor(GSConfig.FIRST_PLAYER).getSize() >= 1 && history.getSequenceFor(GSConfig.FIRST_PLAYER).getSize() <= 3) {
//			writeProb(history.toString());
//		}
		try {
			restoreModel();
			long time = System.currentTimeMillis();
//            cplex.exportModel("LP.lp");
			solve();
			Stats.getInstance().addToLPSolveTime(System.currentTimeMillis() - time);
			finalValue = cplex.getObjValue();
		} catch (Exception e) {
			e.printStackTrace();
		}

//		if (history.getSequenceFor(GSConfig.FIRST_PLAYER).getSize() >= 1 && history.getSequenceFor(GSConfig.FIRST_PLAYER).getSize() <= 3) {
//			File file = new File(history.toString() + ".lp");
//			try {
//				BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
//				bw.write(String.valueOf(getGameValue()));
//				bw.newLine();
//				bw.write(getPlayerOneStrategy()==null?"null":getPlayerOneStrategy().toString());
//				bw.newLine();
//				bw.write(getPlayerTwoStrategy()==null?"null":getPlayerTwoStrategy().toString());
//				bw.flush();
//				bw.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}		
		extractMixedStrategies();
	}

	private void extractMixedStrategies() {
		extractPlayerOneStrategy();
		extractPlayerTwoStrategy();
	}

	private void extractPlayerTwoStrategy() {
        if(Killer.kill)
            return;
		MixedStrategy<U> mixedStrategy = new MixedStrategy<U>();

		int i = 1 + 1; //indexed from 1, first reward is sumX
		for (U strategy : playerTwoStrategySet) {
			double prob = -getConstraintDual(i); //+1
			mixedStrategy.put(strategy, prob);
			i++;
		}
		mixedStrategy.sanityCheck();

		playerTwoMixedStrategy = mixedStrategy;
	}

	private void extractPlayerOneStrategy() {
		MixedStrategy<T> mixedStrategy = new MixedStrategy<T>();

		int i = 1 + 1; //indexed from 1, first reward is d - game reward
		for (T strategy : playerOneStrategySet) {
			double prob = getVariablePrimal(i);
			mixedStrategy.put(strategy, prob);
			i++;
		}
		mixedStrategy.sanityCheck();

		playerOneMixedStrategy = mixedStrategy;
	}

	@Override
	public MixedStrategy<T> getPlayerOneStrategy() {
		return playerOneMixedStrategy;
	}

	@Override
	public MixedStrategy<U> getPlayerTwoStrategy() {
		return playerTwoMixedStrategy;
	}

	@Override
	public double getGameValue() {		
//		try {
//			return cplex.getObjValue();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return -Double.MAX_VALUE;
		return finalValue;
	}

	@Override
	public void addPlayerOneStrategies(Iterable<T> playerStrategySet) {

		List<T> strategiesToAdd = new ArrayList<T>();
		if (playerOneStrategySet == null) {
			playerOneStrategySet = new PlayerStrategySet<T>();
			for (T s : playerStrategySet) {
				strategiesToAdd.add(s);
			}

		} else {
			for (T s : playerStrategySet) {
				if (!playerOneStrategySet.containsStrategy(s)) {
					strategiesToAdd.add(s);
				}
			}
		}

		final int colsToAdd = strategiesToAdd.size();
		if (colsToAdd == 0) {
			return;
		}
		// Add elems in the LP now
		for (int j = 1; j <= colsToAdd; j++) {
			List<Integer> iaS = new ArrayList<Integer>();
			List<Double> arS = new ArrayList<Double>();
			// Add Elems for SumX = 1. Row=1, Columns=1+curDefAlloc+j, Elem=1.0
			iaS.add(1);
			arS.add(1.0);
			// Add Elems for |A| constraints
			int i = 1;
			if (playerTwoStrategySet != null) {
				for (U strategy : playerTwoStrategySet) {
					double utility = utilityComputer.getUtility(strategiesToAdd.get(j - 1), strategy);
                    if(Killer.kill)
                        return;
					assert utility == utility;
					//double utility = patrollerBRs.get(j - 1).getUtility(problem.getEvaderStrategies().get(i - 1));
					// adding x_i
					iaS.add(i + 1);
					arS.add(utility);
					i++;
				}
		    	// store
		    	matrixBackup.add(new StoreVariable("x" + (playerOneStrategySet.size() + j), AMIProblem.VARIABLE_TYPE.CONTINUOUS, 0, 0, 1, iaS, arS));
				this.addAndSetVariable("x" + (playerOneStrategySet.size() + j), AMIProblem.VARIABLE_TYPE.CONTINUOUS, 0, 0, 1, iaS, arS);
			} else {
				matrixBackup.add(new StoreVariable("x" + j, AMIProblem.VARIABLE_TYPE.CONTINUOUS, 0, 0, 1, iaS, arS));
				this.addAndSetVariable("x" + j, AMIProblem.VARIABLE_TYPE.CONTINUOUS, 0, 0, 1, iaS, arS);
			}
		}

		playerOneStrategySet.addAll(strategiesToAdd);
	}

	@Override
	public void addPlayerTwoStrategies(Iterable<U> playerStrategySet) {
		List<U> strategiesToAdd = new ArrayList<U>();
		if (playerTwoStrategySet == null) {
			playerTwoStrategySet = new PlayerStrategySet<U>();
			for (U s : playerStrategySet) {
				strategiesToAdd.add(s);
			}

		} else {
			for (U s : playerStrategySet) {
				if (!playerTwoStrategySet.containsStrategy(s)) {
					strategiesToAdd.add(s);
				}
			}
		}

		int rowsToAdd = strategiesToAdd.size();
		if (rowsToAdd == 0)
			return;

		// Add elems in the LP now
		// Add Elems for |A| constraints
		for (int i = 1; i <= rowsToAdd; i++) {
			List<Integer> jaS = new ArrayList<Integer>();
			List<Double> arS = new ArrayList<Double>();
			
			jaS.add(1);
			arS.add(-1.0); //TODO: test properly
			int j = 1;
			if (playerOneStrategySet != null) {
				for (T strategy : playerOneStrategySet) {
					double utility = utilityComputer.getUtility(strategy, strategiesToAdd.get(i - 1));
                    if(Killer.kill)
                        return;
					assert utility == utility;
					jaS.add(j + 1);
					arS.add(utility);
					j++;
				}
		    	matrixBackup.add(new StoreConstraint("AP" + (i + playerTwoStrategySet.size()), AMIProblem.BOUNDS_TYPE.LOWER, 0, jaS, arS));
				this.addAndSetConstraint("AP" + (i + playerTwoStrategySet.size()), AMIProblem.BOUNDS_TYPE.LOWER, 0, jaS, arS);
			} else {
				matrixBackup.add(new StoreConstraint("AP" + i, AMIProblem.BOUNDS_TYPE.LOWER, 0, jaS, arS));
				this.addAndSetConstraint("AP" + i, AMIProblem.BOUNDS_TYPE.LOWER, 0, jaS, arS);
			}
		}

		playerTwoStrategySet.addAll(strategiesToAdd);
	}

//	public void releaseModel() {
//		try {
//			finalValue = cplex.getObjValue();						
//		} catch (IloException e) {
//			e.printStackTrace();
//		}		
//		end();
//	}

    public void clearModel() {
        try {
            constraints.clear();
            variableStatuses = null;
            constraintStatuses = null;
            variables.clear();
            playerOneMixedStrategy.clear();
            playerOneStrategySet.clear();
            playerTwoMixedStrategy.clear();
            playerTwoStrategySet.clear();
            cplex.clearModel();
            numCols = 0;
            numRows = 0;
            matrixBackup.clear();
            initProblem();
        } catch (IloException e) {
            e.printStackTrace();
    //    		assert false;
        }
    }
	
    public void restoreModel() {
        try {
            constraints.clear();
            variableStatuses = null;
            constraintStatuses = null;
            variables.clear();
            cplex.clearModel();
            numCols = 0;
            numRows = 0;
            initProblem();
        } catch (IloException e) {
            e.printStackTrace();
            //    		assert false;
        }

        for (StoreObject o : matrixBackup) {
            if (o instanceof StoreVariable) {
                StoreVariable v = (StoreVariable)o;
                addAndSetVariable(v.name, v.varType, v.objCoeff, v.lowerBound, v.upperBound, v.indices, v.values);
            } else if (o instanceof StoreConstraint) {
                StoreConstraint c = (StoreConstraint)o;
                addAndSetConstraint(c.name, c.boundType, c.bound, c.indices, c.values);
            }
        }
    }

}
