package cz.agents.gtlibrary.algorithms.crswfabstraction;

import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class UnscaledErrors {

    private double rewardError;
    private double leafProbabilityError;
    private double distributionError;
    private double scaledUtility;

    public UnscaledErrors(LeafNode leaf, LeafNode otherLeaf, Expander<InformationSet> expander, double utilityCorrection) {

        GameState state = leaf.getState();
        GameState parentState = leaf.getParentState();
        GameState otherState = otherLeaf.getState();
        GameState otherParentState = otherLeaf.getParentState();
        double utility = state.getUtilities()[parentState.getPlayerToMove().getId()] + utilityCorrection;
        double otherUtility = otherState.getUtilities()[otherParentState.getPlayerToMove().getId()] + utilityCorrection;
        rewardError = Math.abs(utility - otherUtility);

        computeLeafProbabilityError(state, parentState, otherState, otherParentState);
        computeDistributionError(parentState, otherParentState, expander);
        scaledUtility = utility + rewardError;
    }

    public UnscaledErrors() {
        rewardError = 0;
        leafProbabilityError = 0;
        distributionError = 0;
        scaledUtility = 0;
    }

    private void computeLeafProbabilityError(GameState state, GameState parentState, GameState otherState, GameState otherParentState) {
        double leafProbability = state.getNatureProbability() / parentState.getNatureProbability();
        double otherLeafProbability = otherState.getNatureProbability() / otherParentState.getNatureProbability();
        leafProbabilityError = Math.abs(leafProbability - otherLeafProbability);
    }

    private void computeDistributionError(GameState parentState, GameState otherParentState, Expander<InformationSet> expander) {
        CrswfInformationSet parentSet = (CrswfInformationSet) expander.getAlgorithmConfig().getInformationSetFor(parentState);
        CrswfInformationSet otherParentSet = (CrswfInformationSet) expander.getAlgorithmConfig().getInformationSetFor(otherParentState);
        double distributionProbability = parentState.getNatureProbability() / parentSet.getNatureProbability();
        double otherDistributionProbability = otherParentState.getNatureProbability() / otherParentSet.getNatureProbability();
        distributionError =  Math.abs(distributionProbability - otherDistributionProbability);
    }

    @Override
    public String toString() {
        return "{" +
                "rewardError=" + rewardError +
                ", leafProbabilityError=" + leafProbabilityError +
                ", distributionError=" + distributionError +
                ", scaledUtility=" + scaledUtility +
                '}';
    }

    public void addPlayerChildErrors(UnscaledErrors childErrors) {
        leafProbabilityError = Math.max(leafProbabilityError, childErrors.leafProbabilityError);
        rewardError = Math.max(rewardError, childErrors.rewardError);
        distributionError = childErrors.distributionError;
        scaledUtility = Math.max(scaledUtility, childErrors.scaledUtility);
    }

    public void addNatureChildErrors(UnscaledErrors childErrors, double natureProbability) {
        leafProbabilityError += childErrors.leafProbabilityError;
        rewardError += natureProbability * childErrors.rewardError;
        distributionError = childErrors.distributionError;
        scaledUtility = Math.max(scaledUtility, childErrors.scaledUtility);
    }

    public double getRewardError() {
        return rewardError;
    }

    public double getLeafProbabilityError() {
        return leafProbabilityError;
    }

    public double getDistributionError() {
        return distributionError;
    }

    public double getScaledUtility() {
        return scaledUtility;
    }
}
