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


/**
 *
 */
package cz.agents.gtlibrary.nfg.lplibrary.lpWrapper;

import java.io.OutputStream;
import java.util.List;

/**
 * TODO: clear up unnecessary methods.
 * Common interface for MIP solvers. Currently, CPLEX in {@link lplibrary.cplex.MIProblemCplex} is used.
 *
 * @author Ondrej Vanek
 */
public abstract class AMIProblem {
    protected boolean isLoaded = false;

    protected PROBLEM_TYPE probType;
    protected OBJECTIVE_TYPE objectiveType;

    protected long genTime, loadTime, runTime;

    protected int numRows, numCols;

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    protected abstract void setProblemType();

    protected abstract void setVariableBounds();

    protected abstract void setConstraintBounds();

    protected abstract void generateData();

    protected abstract void saveBasisStatus();

    protected abstract void loadBasisStatus();

    protected abstract void resetVariableBound(int variableNumber,
                                               BOUNDS_TYPE boundType, double lowerBound, double upperBound);

    protected abstract void resetConstraintBound(int constraintNumber, BOUNDS_TYPE boundType,
                                                 double lowerBound, double upperBound);

    public AMIProblem() {
        this.genTime = 0;
        this.loadTime = 0;
        this.runTime = 0;
        this.numRows = 0;
        this.numCols = 0;
    }

    protected abstract void initialize();

    protected abstract void setProblemName(String name);

    protected abstract void setObjectiveCoef(int index, double value);

    protected abstract void setProblemType(PROBLEM_TYPE problemType,
                                           OBJECTIVE_TYPE objectiveType);

    public abstract int addVariable(String name, BOUNDS_TYPE boundType,
                                    double lowerBound, double upperBound, VARIABLE_TYPE varType,
                                    double objCoeff);

    public abstract int addConstraint(String name, BOUNDS_TYPE boundType, double bound);

    public abstract int addAndSetConstraint(String name, BOUNDS_TYPE boundType, double bound, List<Integer> indices, List<Double> values);

    public abstract int addAndSetVariable(String name, VARIABLE_TYPE varType,
                                          double objCoeff, double lowerBound, double upperBound,
                                          List<Integer> indices, List<Double> values) throws RuntimeException;


    public abstract void updateObjective() throws Exception;

    /**
     * Calls setVariableBounds(), setConstraintBounds and generateData() Loads the problem
     * specified from indices 1 to finalIndex (both included) in ia,ja and ar
     * into the lp object.
     */
    public void loadProblem() {
        long start = System.currentTimeMillis();
        try {
            this.setProblemType();
            this.setVariableBounds();
            this.setConstraintBounds();
            this.generateData();
            this.updateObjective();
            this.isLoaded = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.genTime = System.currentTimeMillis() - start;
    }

    /**
     * cleans the data structures
     */
    public abstract void end();

    public void resetLP() {
        this.numRows = 0;
        this.numCols = 0;
        this.end();
        this.initialize();
        this.loadProblem();
    }

    public abstract STATUS_TYPE getSolveStatus();

    public abstract boolean disableConstraint(int constraintNumber) throws LPSolverException;

    public abstract boolean enableConstraint(int constraintNumber) throws LPSolverException;

    public abstract void solve() throws LPSolverException;

    public abstract double getConstraintDual(int constraintNumber);

    public abstract double getConstraintSlack(int constraintNumber);

    public abstract double getVariablePrimal(int variableNumber) throws LPSolverException;

    public abstract List<Double> getConstraintDualVector();

    public abstract List<Double> getVariablePrimalVector();

    public abstract double getLPObjective();

    public abstract void writeProb(String fileName);

    public abstract void writeSol(String fileName);

    public long getGenTime() {
        return this.genTime;
    }

    public long getLoadTime() {
        return this.loadTime;
    }

    public long getRunTime() {
        return this.runTime;
    }

    public void redirectOutput(OutputStream stream) {
    }

    public abstract int getNumberIterations();

    public abstract int getMIPStarts();

    public abstract int getNodesExplored();

    public abstract int getNodesLeft();

    public abstract int getSimplexIterations();

    //public abstract void importFile(String fileName);

    public abstract double getConstraintPrimal(int constraintNumber);

    public enum STATUS_TYPE {
        OPTIMAL, INFEASIBLE, UNBOUNDED, UNKNOWN
    }

    public enum BOUNDS_TYPE {
        LOWER, UPPER, DOUBLE, FIXED, FREE
    }

    public enum VARIABLE_TYPE {
        CONTINUOUS, INTEGER
    }

    public enum PROBLEM_TYPE {
        LP, MIP
    }

    public enum OBJECTIVE_TYPE {
        MIN, MAX
    }

    public enum ROW_STATUS {
        ENABLED, DISABLED
    }

}
