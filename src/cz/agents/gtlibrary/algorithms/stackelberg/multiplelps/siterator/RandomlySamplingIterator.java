package cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.siterator;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.FeasibilitySequenceFormLP;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.rpiterator.DepthPureRealPlanIterator;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

/**
 * Created by Jakub Cerny on 23/07/2018.
 */
public class RandomlySamplingIterator extends DepthPureRealPlanIterator {

    protected final int MAX_RPS;
    protected final double RP_SAMPLE_PROB;
    protected HighQualityRandom rnd;
    protected int sampledRPsCount;
    protected int sampledRPIndex;

    public static boolean USE_LOG = false;
    public static int SEED = 0;

    public RandomlySamplingIterator(Player player, StackelbergConfig config, Expander<SequenceInformationSet> expander, FeasibilitySequenceFormLP solver, int maxRPs, double samplingProb) {
        super(player, config, expander, solver);
        this.MAX_RPS = maxRPs;
        this.RP_SAMPLE_PROB = samplingProb;
        this.rnd = new HighQualityRandom(SEED);
        this.sampledRPsCount = 0;
        this.sampledRPIndex = 0;
        System.out.println("Using log # of pure RPS: " + USE_LOG);
        System.out.println("Max RPs: " + MAX_RPS);
        System.out.println("Sample prob: " + RP_SAMPLE_PROB);
//        System.out.println("Random Sampling Iterator");
    }

    public RandomlySamplingIterator(Player player, StackelbergConfig config, Expander<SequenceInformationSet> expander, FeasibilitySequenceFormLP solver, int maxRPs, double samplingProb, boolean useLog) {
        super(player, config, expander, solver);
        this.MAX_RPS = maxRPs;
        this.RP_SAMPLE_PROB = samplingProb;
        this.USE_LOG = useLog;
        this.rnd = new HighQualityRandom(SEED);
        this.sampledRPsCount = 0;
        this.sampledRPIndex = 0;
        System.out.println("Using log # of pure RPS: " + USE_LOG);
        System.out.println("Max RPs: " + MAX_RPS);
        System.out.println("Sample prob: " + RP_SAMPLE_PROB);
//        System.out.println("Random Sampling Iterator");
    }

    @Override
    public Set<Sequence> next() {
        if(sampledRPsCount > MAX_RPS) {
            System.out.println("Maximum number of sampled RPs exceeded. Exiting.");
            throw new NoSuchElementException();
        }
        double rand = rnd.nextDouble();
        getNext();
        sampledRPIndex++;
        while(rand >= RP_SAMPLE_PROB){
            sampledRPIndex++;
            getNext();
            rand = rnd.nextDouble();
        }

        System.out.println("Sampled! RP with index: " + sampledRPIndex);
        System.out.println("Size: "+ currentSet.size());

        if(USE_LOG) sampledRPsCount += currentSet.size();
        else sampledRPsCount++;

        return new HashSet<>(currentSet);
    }

    public Set<Sequence> getNext() {
        if (first) {
            first = false;
            rnd = new HighQualityRandom(SEED);
            System.out.println("Using seed: " + SEED);
            return new HashSet<>(currentSet);
        }
        int index = getIndexOfReachableISWithActionsLeftFrom(stack.size() - 1);

        updateRealizationPlan(index);

        return currentSet;
    }

}
