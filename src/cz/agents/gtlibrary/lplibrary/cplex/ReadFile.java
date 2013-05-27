package cz.agents.gtlibrary.lplibrary.cplex;

import ilog.concert.*;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.ArrayList;
import java.util.List;

public class ReadFile {

	/**
	 * @throws IloException
	 * @throws UnknownObjectException 
	 */
	public static void printSolution(IloCplex cplex, List<IloNumVar> xVar, List<IloRange> rows ) throws UnknownObjectException, IloException {
		int numVars = xVar.size();
		int numRows = rows.size();
		double[] xVals = cplex.getValues( xVar.toArray(new IloNumVar[] {}));
		System.out.print("Variable Values: ");
		for ( int i=0; i<numVars; i++){
			System.out.print(xVals[i] + "\t");
		}
		System.out.println();
		double[] duals = cplex.getDuals(rows.toArray(new IloRange[] {}));
		System.out.print("Duals: ");
		for ( int j=0; j<numRows; j++){
			System.out.print(duals[j]+"\t");
		}
		System.out.println();
		cplex.output().println("Solution value  = " + cplex.getObjValue());
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			IloCplex cplex = new IloCplex();
			cplex.importModel("/home/manish/EclipseWorkspaces/UrbanSecurity/RANGER-LP.lp");
			cplex.solve();

			System.out.println("Status: " + cplex.getStatus());
			System.out.println("Objective: " + cplex.getObjValue());
			cplex.writeSolution("CPLEXOutFile.xml");

			IloLPMatrix lp = (IloLPMatrix)cplex.LPMatrixIterator().next();

			double[] incx = cplex.getValues(lp);
			for (int j = 0; j < incx.length; j++) {
				System.out.println("Variable " + j + ": Value = " + incx[j]);
			}

			//cz.agents.lplibrary.cplex.
			cplex.end();

			/**
			 * Trying problem:
			 * max x1+x2+x3
			 * 3x1 + 4x2 + 5x3 <= 10
			 * 5x1 + 3x2 + 4x3 <= 15
			 * 
			 * Add row:
			 * 4x1 + 5x2 + 3x3 <= 5
			 * 
			 * Add column: 
			 * max x1+x2+x3+x4
			 * 3x4, 3x4, 0x4
			 * 
			 */

			cplex = new IloCplex();
			// 6 variables for gamma
			// 2 variables for z

			int numVars = 3;
			int numRows = 2;

			List<IloNumVar> xVar = new ArrayList<IloNumVar>(numVars);
			for ( int i=0; i<numVars; i++) {
				xVar.add(cplex.numVarArray(1, new double[]{0} , new double[]{Double.MAX_VALUE}, new String[]{"x"+(i+1)})[0]);
			}

			List<IloRange> rows = new ArrayList<IloRange>(numRows);
			for ( int i=0; i<numRows; i++) {
				rows.add((new IloRange[1])[0]);
			}

			rows.set(0, cplex.addLe(cplex.sum(cplex.prod(3, xVar.get(0)), 
					cplex.prod(4, xVar.get(1)),
					cplex.prod(5, xVar.get(2))), 10, "c1" ));
			rows.set(1, cplex.addLe(cplex.sum(cplex.prod(5, xVar.get(0)), 
					cplex.prod(3, xVar.get(1)),
					cplex.prod(4, xVar.get(2))), 15, "c2" ));

			double[] objvals = {1.0, 1.0, 1.0};
			IloObjective obj = cplex.addMaximize(cplex.scalProd(xVar.toArray(new IloNumVar[] {}), objvals));

			cplex.exportModel("Model1.lp");

			boolean retVal = cplex.solve();
			if ( retVal == false ) {
				throw new RuntimeException();
			}
			printSolution(cplex, xVar, rows);

			System.err.println("Row Generation");
			
			// Adding Row
			IloRange newRow = (new IloRange[1])[0];
			rows.add(newRow);
			rows.set(2, cplex.addLe(cplex.sum(cplex.prod(4, xVar.get(0)), 
					cplex.prod(5, xVar.get(1)),
					cplex.prod(3, xVar.get(2))), 5, "c3" ));
			numRows ++;
			cplex.exportModel("Model2.lp");
			retVal = cplex.solve();
			if ( retVal == false ) {
				throw new RuntimeException();
			}
			printSolution(cplex, xVar, rows);

			System.err.println("Col Generation");
			
			// Adding Column
			IloNumVar newVar = (cplex.numVarArray(1, new double[]{0} , new double[]{Double.MAX_VALUE}, new String[]{"x4"}))[0];
			xVar.add(newVar);
			xVar.set(numVars, cplex.numVar(cplex.column(obj, 1.0).and(
					cplex.column(rows.get(0), 3.0).and(
							cplex.column(rows.get(1), 3.0))),
							0.0, Double.MAX_VALUE, "x4"));
			numVars ++;
			cplex.exportModel("Model3.lp");
			retVal = cplex.solve();
			if ( retVal == false ) {
				throw new RuntimeException();
			}
			printSolution(cplex, xVar, rows);
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
