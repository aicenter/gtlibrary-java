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


package cz.agents.gtlibrary.nfg.lplibrary.cplex;

import ilog.concert.*;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.BasisStatus;
import ilog.cplex.IloCplex.UnknownObjectException;
import cz.agents.gtlibrary.nfg.lplibrary.lpWrapper.AMIProblem;
import cz.agents.gtlibrary.nfg.lplibrary.lpWrapper.LPSolverException;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CLPEX implementation of the {@link AMIProblem}.
 */
public abstract class MIProblemCplex extends AMIProblem {
    private static final double EPSILON = 0.0000001;
    protected static final int MM = Integer.MAX_VALUE;

    protected IloCplex cplex;

    protected Map<Integer, CPLEXVariable> variables;

    protected List<IloRange> constraints;
    
    protected List<AMIProblem.ROW_STATUS> constraintsInModel;
    //protected Map<String,Double> objectiveCoeff;
    //protected Map<String,IloNumVarType> columnType;

    protected IloCplex.BasisStatus[] variableStatuses;
    protected IloCplex.BasisStatus[] constraintStatuses;
    
    protected ArrayList<StoreObject> matrixBackup = new ArrayList<StoreObject>();

    public void initialize() {
   /*     try {
            cplex = new IloCplex();
            cplex.setName("MIProblem");
            //objectiveFunction = cplex.getObjective();
            //cplex.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Auto);
            //cplex.setParam(IloCplex.DoubleParam.EpMrk, 0.999);
            this.redirectOutput(null);
        } catch (IloException e) {
            e.printStackTrace();

            throw new RuntimeException(e.getMessage());
        }//*/

        variables = new HashMap<Integer, CPLEXVariable>();
        constraints = new ArrayList<IloRange>(this.numRows);
        constraintsInModel = new ArrayList<ROW_STATUS>(this.numRows);
        variableStatuses = null;
        constraintStatuses = null;
    }

    public MIProblemCplex() {
        super();
        this.initialize();
    }

    protected void setProblemName(String name) {
        cplex.setName(name);
    }


    protected void setProblemType(PROBLEM_TYPE problemType, OBJECTIVE_TYPE objectiveType) {
        this.probType = problemType;
        this.objectiveType = objectiveType;
        try {
            this.updateObjective();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Update Objective Failed");
        }
    }

    public boolean removeRow(int rowNumber) throws RuntimeException {
        try {
            if (this.constraintsInModel.get(rowNumber - 1) == ROW_STATUS.ENABLED) {
                this.cplex.remove(this.constraints.get(rowNumber - 1));
                this.constraintsInModel.set(rowNumber - 1, ROW_STATUS.DISABLED);
                return true;
            }
        } catch (IloException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return false;
    }

    public boolean addRow(int rowNumber) throws RuntimeException {
        try {
            if (this.constraintsInModel.get(rowNumber - 1) == ROW_STATUS.DISABLED) {
                this.cplex.add(this.constraints.get(rowNumber - 1));
                this.constraintsInModel.set(rowNumber - 1, ROW_STATUS.ENABLED);
                return true;
            }
        } catch (IloException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return false;
    }

    private IloNumExpr getObjectiveFunction() throws IloException {
        //Double[] objCoefDblObj = this.objectiveCoeff.toArray(new Double[] {});
        double[] objCoeffDblBasicType = new double[variables.size()];
        IloNumVar[] vars = new IloNumVar[variables.size()];

        int index = 0;
        for (CPLEXVariable var : variables.values()) {
            double val = var.objectiveCoeffcient;
            if (Math.abs(val) < EPSILON) {
                val = 0.0;
            }
            objCoeffDblBasicType[index] = val;
            vars[index] = var.cplexVar;
            index++;
        }
        return (cplex.scalProd(vars, objCoeffDblBasicType));
    }

    public void updateObjective() throws Exception {
        IloNumExpr objectiveFunctionExpr = this.getObjectiveFunction();

        cplex.delete(cplex.getObjective());
        switch (this.objectiveType) {
            case MAX:
                cplex.addMaximize(objectiveFunctionExpr);
                break;
            case MIN:
                cplex.addMinimize(objectiveFunctionExpr);
                break;
            default:
                throw new IllegalArgumentException("I don't know this type, kid!");
        }
    }

    public int addVariable(String name, BOUNDS_TYPE boundType, double lowerBound, double upperBound,
                           VARIABLE_TYPE varType, double objCoeff) {
        try {
            //IloColumn newCol = cplex.column(this.objectiveFunction, objCoeff);
            IloNumVar col;
            CPLEXVariable var = new CPLEXVariable();
            switch (varType) {
                case CONTINUOUS:
                    col = cplex.numVar(lowerBound, upperBound, IloNumVarType.Float, name);
                    var.type = IloNumVarType.Float;
                    break;
                case INTEGER:
                    col = cplex.numVar(lowerBound, upperBound, IloNumVarType.Int, name);
                    var.type = IloNumVarType.Int;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Variable Type");
            }
            var.cplexVar = col;
            var.objectiveCoeffcient = objCoeff;
            var.name = name;
            var.index = numCols;
            variables.put(var.index, var);
            this.numCols++;
            this.updateObjective();
            return numCols;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public int addConstraint(String name, BOUNDS_TYPE boundType, double bound) {
        try {
            IloRange newRow;
            IloNumExpr rowExpr = cplex.constant(0.0);
            switch (boundType) {
                case UPPER:
                    newRow = cplex.addGe(bound, rowExpr, name);
                    break;
                case LOWER:
                    newRow = cplex.addLe(bound, rowExpr, name);
                    break;
                case FIXED:
                    newRow = cplex.addEq(bound, rowExpr, name);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Type");
            }
            this.numRows++;
            constraints.add(newRow);
            constraintsInModel.add(ROW_STATUS.ENABLED);
        } catch (IloException e) {
            e.printStackTrace();
            throw new RuntimeException("Error in Set Row: " + (this.numRows + 1));
        }
        return constraints.size() - 1;
    }


    /**
     * adds a new variable and initializes it
     *
     * @param name
     * @param varType
     * @param objCoeff
     * @param lowerBound
     * @param upperBound
     * @param indices    if null, the constraint values are not set.
     * @param values
     * @throws RuntimeException
     */
    public int addAndSetVariable(String name, VARIABLE_TYPE varType, double objCoeff, double lowerBound, double upperBound, List<Integer> indices, List<Double> values) throws RuntimeException {
        // do addColumn
        // setColumn
        // setVariable
        IloColumn newColumn;
        CPLEXVariable var = new CPLEXVariable();
        try {
            newColumn = cplex.column(cplex.getObjective(), objCoeff);
            if (indices != null) {
                for (int i = 0; i < indices.size(); i++) {
                    newColumn = newColumn.and(cplex.column(this.constraints.get(indices.get(i) - 1), values.get(i)));
                }
            }
            IloNumVar col;
            switch (varType) {
                case CONTINUOUS:
                    col = cplex.numVar(newColumn, lowerBound, upperBound, IloNumVarType.Float, name);
                    var.type = IloNumVarType.Float;
                    break;
                case INTEGER:
                    col = cplex.numVar(newColumn, lowerBound, upperBound, IloNumVarType.Int, name);
                    var.type = IloNumVarType.Int;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Variable Type");
            }
            var.objectiveCoeffcient = objCoeff;
            //objectiveCoeff.add(objCoeff);
            var.cplexVar = col;
            //columns.add(col);
            var.name = name;
            var.index = numCols + 1;
            variables.put(var.index, var);
            this.numCols++;
            return numCols;
        } catch (IloException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * Doesn't add a new row
     */
    private void setConstraint(int rowNo, List<Integer> indices, List<Double> values) throws RuntimeException {
        if (indices != null && indices.size() != values.size()) {
            throw new RuntimeException();
        }
        IloNumExpr constraintExpr;
        try {
            constraintExpr = cplex.constant(0.0);
            if (indices != null) {
                for (int i = 0; i < indices.size(); i++) {
                    constraintExpr = cplex.sum(constraintExpr, cplex.prod(values.get(i).doubleValue(), variables.get(indices.get(i)).cplexVar));
                }
            }
            constraints.get(rowNo).setExpr(constraintExpr);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public int addAndSetConstraint(String name, BOUNDS_TYPE boundType, double bound, List<Integer> indices, List<Double> values) {
        int rowNumber = addConstraint(name, boundType, bound);
        setConstraint(rowNumber, indices, values);
        return rowNumber;
    }

    @Override
    protected void saveBasisStatus() {
        if (this.variables.size() == 0 || this.constraints.size() == 0 || this.probType == PROBLEM_TYPE.MIP)
            return;
        try {
            for (CPLEXVariable var : variables.values()) {
                var.basisStatus = cplex.getBasisStatus(var.cplexVar);
            }
            //variableStatuses = cplex.getBasisStatuses(this.columns.toArray(new IloNumVar[] {}));
            constraintStatuses = cplex.getBasisStatuses(this.constraints.toArray(new IloRange[]{}));
        } catch (IloException e) {
            e.printStackTrace();
            System.err.println("Save Basis Failed: " + e.getMessage());
            variableStatuses = null;
            constraintStatuses = null;
            throw new RuntimeException(e.getMessage());
        }
    }

    private IloNumVar[] varsAsArray() {
        IloNumVar[] array = new IloNumVar[variables.size()];
        for (int i = 0; i < variables.size(); i++) {
            array[i] = variables.get(i).cplexVar;
        }
        return array;
    }

    private BasisStatus[] basisStatusesAsArray() {
        BasisStatus[] array = new BasisStatus[variables.size()];
        for (int i = 0; i < variables.size(); i++) {
            array[i] = variables.get(i).basisStatus;
        }
        return array;
    }

    @Override
    protected void loadBasisStatus() {
        if (variableStatuses == null || constraintStatuses == null || this.probType == PROBLEM_TYPE.MIP)
            return;
        try {

            cplex.setBasisStatuses(varsAsArray(),
                    basisStatusesAsArray(),
                    0,
                    variableStatuses.length,
                    this.constraints.toArray(new IloRange[]{}),
                    constraintStatuses,
                    0,
                    constraintStatuses.length);
        } catch (IloException e) {
            e.printStackTrace();
            System.err.println("Load Basis Failed: " + e.getMessage());
            variableStatuses = null;
            constraintStatuses = null;
            throw new RuntimeException(e.getMessage());
        }
    }

    public void solve() throws LPSolverException {
        long start = System.currentTimeMillis();
        try {
            this.updateObjective();
            this.loadBasisStatus();
//            cplex.exportModel("NFGDO.lp");
            boolean retval = cplex.solve();
            // MANISH DEBUG PRINT
            //cplex.getCplexStatus().equals(CplexStatus.Optimal)
            if (retval == false /*|| !cplex.getCplexStatus().equals(CplexStatus.Optimal) */) {
                writeProb("CPLEXfail");
                System.err.println("CPLEX error: " + cplex.getObjValue() + ";" + cplex.getCplexStatus());
                throw new Exception("CPLEX error: " + cplex.getObjValue() + ";" + cplex.getCplexStatus());
            }
            this.saveBasisStatus();
        } catch (Exception e) {
            e.printStackTrace();
            throw new LPSolverException(e.getMessage());
        }
        this.runTime = System.currentTimeMillis() - start;
    }

    public double getConstraintDual(int constraintNumber) {
        try {
            double dual = cplex.getDual(constraints.get(constraintNumber - 1));
            return dual;
        } catch (UnknownObjectException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        } catch (IloException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public double getVariablePrimal(int variableNumber) {
        double xVal;
        try {
            xVal = cplex.getValue(variables.get(variableNumber).cplexVar);
            return xVal;
        } catch (Exception e) {
            throw new LPSolverException(e.getMessage());
        }
    }

    public List<Double> getConstraintDualVector() {
        double[] duals;
        try {
            duals = cplex.getDuals(constraints.toArray(new IloRange[]{}));
            List<Double> dualVect = new ArrayList<Double>(duals.length);
            for (int i = 0; i < duals.length; i++) {
                dualVect.add(duals[i]);
            }
            return dualVect;
        } catch (Exception e) {
            e.printStackTrace();
            throw new LPSolverException(e.getMessage());
        }
    }

    public List<Double> getVariablePrimalVector() {
        try {
            double[] xVals = cplex.getValues(varsAsArray());
            List<Double> primalVect = new ArrayList<Double>(xVals.length);
            for (int i = 0; i < xVals.length; i++) {
                primalVect.add(xVals[i]);
            }
            return primalVect;
        } catch (Exception e) {
            e.printStackTrace();

            throw new LPSolverException(e.getMessage());
        }
    }

    public double getLPObjective() {
        try {
            return cplex.getObjValue();
        } catch (IloException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public void writeProb(String fileName) {
        try {
            cplex.exportModel(fileName + ".lp");
        } catch (IloException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public void writeSol(String fileName) {
        try {
            cplex.writeSolution(fileName);
        } catch (IloException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * clean the data structures.
     */
    public void end() {
        cplex.end();
        constraints.clear();
        variableStatuses = null;
        constraintStatuses = null;
        variables.clear();
    }

    /**
     * @param stream can be <code>null</code> if no output should be provided.
     */
    public void redirectOutput(OutputStream stream) {
        cplex.setOut(stream);
        cplex.setWarning(stream);
    }

    @Override
    public int getNumberIterations() {
        return cplex.getNiterations();
    }

    @Override
    public int getMIPStarts() {
        try {
            return cplex.getNMIPStarts();
        } catch (IloException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int getNodesExplored() {
        return cplex.getNnodes();
    }

    @Override
    public int getNodesLeft() {
        return cplex.getNnodesLeft();
    }

    @Override
    public int getSimplexIterations() {
        return cplex.getNiterations();
    }

    private class CPLEXVariable {
        IloNumVar cplexVar;
        IloNumVarType type;
        double objectiveCoeffcient;
        int index = 1;
        String name;
        IloCplex.BasisStatus basisStatus;
    }

    @Override
    protected void setObjectiveCoef(int index, double value) {
        variables.get(index).objectiveCoeffcient = value;
        try {
            cplex.setLinearCoef(cplex.getObjective(), variables.get(index).cplexVar, value);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    public double getRowSlack(int rowNumber) {
        try {
            return cplex.getSlack(constraints.get(rowNumber - 1));
        } catch (UnknownObjectException e) {
            e.printStackTrace();
            throw new LPSolverException(e.getMessage());
        } catch (IloException e) {
            e.printStackTrace();
            throw new LPSolverException(e.getMessage());
        }
    }

    public boolean enableConstraint(int constraintNumber) throws LPSolverException {
        try {
            if (this.constraintsInModel.get(constraintNumber - 1) == ROW_STATUS.DISABLED) {
                this.cplex.add(this.constraints.get(constraintNumber - 1));
                this.constraintsInModel.set(constraintNumber - 1, ROW_STATUS.ENABLED);
                return true;
            }
        } catch (IloException e) {
            e.printStackTrace();
            throw new LPSolverException(e.getMessage());
        }
        return false;
    }

    public boolean disableConstraint(int constraintNumber) throws LPSolverException {
        try {
            if (this.constraintsInModel.get(constraintNumber - 1) == ROW_STATUS.ENABLED) {
                this.cplex.remove(this.constraints.get(constraintNumber - 1));
                this.constraintsInModel.set(constraintNumber - 1, ROW_STATUS.DISABLED);
                return true;
            }
        } catch (IloException e) {
            e.printStackTrace();
            throw new LPSolverException(e.getMessage());
        }
        return false;
    }

    public double getConstraintSlack(int constraintNumber) {
        try {
            return cplex.getSlack(constraints.get(constraintNumber - 1));
        } catch (UnknownObjectException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        } catch (IloException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * TODO: Test
     *
     * @param columnNumber
     * @param boundType
     * @param lowerBound
     * @param upperBound
     */
    public void resetVariableBound(int columnNumber, BOUNDS_TYPE boundType,
                                   double lowerBound, double upperBound) {
        try {
            switch (boundType) {
                case FREE:
                    this.variables.get(columnNumber - 1).cplexVar.setLB(-MM);
                    this.variables.get(columnNumber - 1).cplexVar.setUB(MM);
                    break;
                case LOWER:
                    this.variables.get(columnNumber - 1).cplexVar.setLB(lowerBound);
                    this.variables.get(columnNumber - 1).cplexVar.setUB(MM);
                    break;
                case UPPER:
                    this.variables.get(columnNumber - 1).cplexVar.setLB(-MM);
                    this.variables.get(columnNumber - 1).cplexVar.setUB(upperBound);
                    break;
                case DOUBLE:
                    this.variables.get(columnNumber - 1).cplexVar.setLB(lowerBound);
                    this.variables.get(columnNumber - 1).cplexVar.setUB(upperBound);
                    break;
                case FIXED:
                    this.variables.get(columnNumber - 1).cplexVar.setLB(lowerBound);
                    this.variables.get(columnNumber - 1).cplexVar.setUB(lowerBound);
                    break;
                default:
                    throw new LPSolverException("No such bound type.");
            }
        } catch (IloException e) {
            e.printStackTrace();
            throw new LPSolverException("Column Number is wrong.");
        }
    }

    public void resetConstraintBound(int rowNumber, BOUNDS_TYPE boundType,
                                     double lowerBound, double upperBound) {
        try {

            switch (boundType) {
                case LOWER:
                    this.constraints.get(rowNumber - 1).setLB(lowerBound);
                    this.constraints.get(rowNumber - 1).setUB(Double.MAX_VALUE);
                    break;
                case UPPER:
                    this.constraints.get(rowNumber - 1).setLB(-Double.MAX_VALUE);
                    this.constraints.get(rowNumber - 1).setUB(upperBound);
                    break;
                case DOUBLE:
                    this.constraints.get(rowNumber - 1).setLB(lowerBound);
                    this.constraints.get(rowNumber - 1).setUB(upperBound);
                    break;
                case FIXED:
                    this.constraints.get(rowNumber - 1).setLB(lowerBound);
                    this.constraints.get(rowNumber - 1).setUB(lowerBound);
                    break;
                default:
                    throw new LPSolverException("No such bound type.");
            }
        } catch (IloException e) {
            e.printStackTrace();
            throw new LPSolverException("Row Number is wrong. (row number = "
                    + rowNumber + ")");
        }

    }

    @Override
    public double getConstraintPrimal(int constraintNumber) {
        try {
            return this.cplex.getValue(this.constraints.get(constraintNumber - 1).getExpr());
        } catch (IloException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public STATUS_TYPE getSolveStatus() {
        try {
            return STATUS_TYPE.valueOf(cplex.getStatus().toString().toUpperCase());
        } catch (IloException e) {
            e.printStackTrace();
        }
        return STATUS_TYPE.UNKNOWN;
    }

    public abstract class StoreObject {
    	
    }
    
    public class StoreVariable extends StoreObject {
    	public String name;
    	public VARIABLE_TYPE varType;
    	public double objCoeff;
    	public double lowerBound;
    	public double upperBound;
    	public List<Integer> indices;
    	public List<Double> values;
		
    	public StoreVariable(String name, VARIABLE_TYPE varType,
				double objCoeff, double lowerBound, double upperBound,
				List<Integer> indices, List<Double> values) {
			this.name = name;
			this.varType = varType;
			this.objCoeff = objCoeff;
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
			this.indices = indices;
			this.values = values;
		}
    }

    public class StoreConstraint extends StoreObject{
    	public String name;
    	public BOUNDS_TYPE boundType;
    	public double bound;
    	public List<Integer> indices;
    	public List<Double> values;
		
    	public StoreConstraint(String name, BOUNDS_TYPE boundType,
				double bound, List<Integer> indices, List<Double> values) {
			this.name = name;
			this.boundType = boundType;
			this.bound = bound;
			this.indices = indices;
			this.values = values;
		}
    }
}


