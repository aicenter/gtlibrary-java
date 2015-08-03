package cz.agents.gtlibrary.algorithms.stackelberg.experiments;

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.EmptyFeasibilitySequenceFormLP;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.rpiterator.NoCutDepthPureRealPlanIterator;
import cz.agents.gtlibrary.domain.randomgame.GeneralSumRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class SeqConfigurationGenerator {
    public static final int bucketCount = 1;
    public static int[] upperBounds;
    public static int[] lowerBounds;
    public static int countPerBucket = 10;

    /**
     * @param args file, init bf, init depth, starting seed, ending seed, rp count, bucket size, epsilon
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        int[] counts = new int[bucketCount];
        int count = 0;

        countPerBucket = Integer.parseInt(args[6]);
        lowerBounds = new int[]{Integer.parseInt(args[5]) - Integer.parseInt(args[7])};
        upperBounds = new int[]{Integer.parseInt(args[5]) + Integer.parseInt(args[7])};
        RandomGameInfo.FIXED_SIZE_BF = false;
        for (int branchingFactor = Integer.parseInt(args[1]); branchingFactor <= Integer.parseInt(args[1]); branchingFactor++) {
            for (int depth = Integer.parseInt(args[2]); depth <= Integer.parseInt(args[2]); depth++) {
                for (int obs = 2; obs < 4; obs++) {
                    for (int seed = Integer.parseInt(args[3]); seed < Integer.parseInt(args[4]); seed++) {
                        RandomGameInfo.seed = seed;
                        RandomGameInfo.MAX_OBSERVATION = obs;
                        RandomGameInfo.MAX_DEPTH = depth;
                        RandomGameInfo.MAX_BF = branchingFactor;
                        GameState root = new GeneralSumRandomGameState();
                        StackelbergConfig config = new StackelbergConfig(root);
                        Expander<SequenceInformationSet> expander = new RandomGameExpander<>(config);
                        FullSequenceEFG builder = new FullSequenceEFG(root, expander, new RandomGameInfo(), config);

                        builder.generateCompleteGame();
                        System.err.println("seed: " + seed);
                        GambitEFG gambit = new GambitEFG();

                        gambit.write("smallRandom", root, expander);
                        int seqCount = getRPCount(root.getAllPlayers()[0], root.getAllPlayers()[1], config, expander, counts);//config.getSequencesFor(root.getAllPlayers()[1]).size();
                        long rpC = RPCounter.count(config, expander, config.getInformationSetFor(root), root.getAllPlayers()[1]);

                        System.out.println(seqCount + " vs " + rpC);
                        assert seqCount == rpC;
                        int index = -1;


                        System.err.println("rp count: " + seqCount);
                        for (int i = 0; i < bucketCount; i++) {
                            if (seqCount >= lowerBounds[i] && seqCount <= upperBounds[i]) {
                                index = i;
                                break;
                            }
                        }
                        if (index < counts.length && index >= 0) {
                            if (counts[index] < countPerBucket) {
                                System.err.println(Arrays.toString(counts));
                                count++;
                                counts[index]++;
//                                int rpCount = getRPCount(root.getAllPlayers()[0], root.getAllPlayers()[1], config, expander);
                                storeConfiguration(seqCount, args[0], seqCount);
                                if (isFull(counts)) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isFull(int[] counts) {
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] < countPerBucket)
                return false;
        }
        return true;
    }

    private static void storeConfiguration(int seqCount, String fileName, int rpCount) {
        System.out.println(RandomGameInfo.seed + " " + RandomGameInfo.MAX_OBSERVATION + " " + RandomGameInfo.MAX_DEPTH + " " + RandomGameInfo.MAX_BF + " " + seqCount + " " + rpCount);
    }

    public static int getRPCount(Player leader, Player follower, StackelbergConfig config, Expander<SequenceInformationSet> expander, int[] counts) {
        Iterator<Set<Sequence>> iterator = new NoCutDepthPureRealPlanIterator(follower, config, expander, new EmptyFeasibilitySequenceFormLP());
        int count = 0;

        try {
            while (true) {
                iterator.next();
                count++;
                if (count % 1000000 == 0 && count > 0)
                    System.err.println("generated " + count + " real. plans");
                if (count > upperBoundOfHighestUnfilleBucket(counts)) {
                    System.err.println(Arrays.toString(counts) + " cutting for " + count);
                    return count;
                }
            }
        } catch (NoSuchElementException e) {
        }
        return count;
    }

    private static int upperBoundOfHighestUnfilleBucket(int[] counts) {
        for (int i = upperBounds.length - 1; i >= 0; i--) {
            if (counts[i] < countPerBucket)
                return upperBounds[i];
        }
        return 0;
    }
}
