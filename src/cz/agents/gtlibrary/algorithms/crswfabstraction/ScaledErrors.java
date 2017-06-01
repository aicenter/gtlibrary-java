package cz.agents.gtlibrary.algorithms.crswfabstraction;

import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

public class ScaledErrors {

    private IloNumVar leafProbabilityError;
    private double distributionError;
    private IloNumVar rewardError;
    private IloNumVar scaledUtility;

    private IloNumExpr rewardErrorTerm;
    private IloNumExpr leafProbabilityErrorTerm;

    public ScaledErrors(LeafNode leaf, LeafNode otherLeaf, Expander<InformationSet> expander, double utilityCorrection, IloCplex cplex, IloNumVar scalingVariable) throws IloException {
        GameState state = leaf.getState();
        GameState parentState = leaf.getParentState();
        GameState otherState = otherLeaf.getState();
        GameState otherParentState = otherLeaf.getParentState();
        double utility = state.getUtilities()[parentState.getPlayerToMove().getId()] + utilityCorrection;
        double otherUtility = otherState.getUtilities()[otherParentState.getPlayerToMove().getId()] + utilityCorrection;

        rewardError = cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "Er(" + leaf + ")");
        IloNumExpr rewardErrorValue = cplex.sum(cplex.constant(utility), cplex.negative(cplex.prod(scalingVariable, cplex.constant(otherUtility))));
        cplex.addGe(rewardError, rewardErrorValue);
        cplex.addGe(rewardError, cplex.negative(rewardErrorValue));

        distributionError = computeDistributionError(parentState, otherParentState, expander);

        scaledUtility = cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "Us(" + leaf + ")");
        cplex.addGe(scaledUtility, cplex.sum(cplex.constant(utility), rewardError));

        IloNumExpr leafProbabilityErrorValue = cplex.prod(cplex.constant(computeLeafProbabilityError(state, parentState, otherState, otherParentState)),
                                                          scaledUtility);
        leafProbabilityError = cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "E0(" + leaf + ")");
        cplex.addGe(leafProbabilityError, leafProbabilityErrorValue);
    }

    public ScaledErrors(IloCplex cplex, GameState state) throws IloException {
        leafProbabilityError = cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "E0(" + state + ")");
        distributionError = 0;
        rewardError = cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "Er(" + state + ")");
        scaledUtility = cplex.numVar(0, Double.POSITIVE_INFINITY, IloNumVarType.Float, "Us(" + state + ")");
        rewardErrorTerm = cplex.constant(0);
        leafProbabilityErrorTerm = cplex.constant(0);
    }

    private double computeLeafProbabilityError(GameState state, GameState parentState, GameState otherState, GameState otherParentState) {
        double leafProbability = state.getNatureProbability() / parentState.getNatureProbability();
        double otherLeafProbability = otherState.getNatureProbability() / otherParentState.getNatureProbability();
        return Math.abs(leafProbability - otherLeafProbability);
    }

    private double computeDistributionError(GameState parentState, GameState otherParentState, Expander<InformationSet> expander) {
        CrswfInformationSet parentSet = (CrswfInformationSet) expander.getAlgorithmConfig().getInformationSetFor(parentState);
        CrswfInformationSet otherParentSet = (CrswfInformationSet) expander.getAlgorithmConfig().getInformationSetFor(otherParentState);
        double distributionProbability = parentState.getNatureProbability() / parentSet.getNatureProbability();
        double otherDistributionProbability = otherParentState.getNatureProbability() / otherParentSet.getNatureProbability();
        return Math.abs(distributionProbability - otherDistributionProbability);
    }

    public void addPlayerChildErrors(ScaledErrors childErrors, IloCplex cplex) throws IloException {
        cplex.addGe(leafProbabilityError, childErrors.leafProbabilityError);
        cplex.addGe(rewardError, childErrors.rewardError);
        distributionError = childErrors.distributionError;
        cplex.addGe(scaledUtility, childErrors.scaledUtility);
    }

    public void addNatureChildErrors(ScaledErrors childErrors, double natureProbability, IloCplex cplex) throws IloException {
        leafProbabilityErrorTerm = cplex.sum(leafProbabilityErrorTerm, childErrors.leafProbabilityError);
        rewardErrorTerm = cplex.sum(rewardError, cplex.prod(cplex.constant(natureProbability), childErrors.rewardError));
        distributionError = childErrors.distributionError;
        cplex.addGe(scaledUtility, childErrors.scaledUtility);
    }

    public void finalizeNatureErrors(IloCplex cplex) throws IloException {
        cplex.addGe(rewardError, rewardErrorTerm);
        cplex.addGe(leafProbabilityError, leafProbabilityErrorTerm);
    }

    public IloNumExpr getLeafProbabilityError() {
        return leafProbabilityError;
    }

    public double getDistributionError() {
        return distributionError;
    }

    public IloNumExpr getRewardError() {
        return rewardError;
    }

    public IloNumExpr getScaledUtility() {
        return scaledUtility;
    }
}
