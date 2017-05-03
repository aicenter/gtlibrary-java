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

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class RPConfigurationGenerator {
    public static final int bucketCount = 4;
    public static final int bucketSize = 100000;
    public static final int countPerBucket = 200;
    public static final int start = 100000;
    public static BufferedWriter writer;

    public static void main(String[] args) throws IOException {
        int[] counts = new int[bucketCount];
        int count = 0;

        RandomGameInfo.FIXED_SIZE_BF = false;
        for (int branchingFactor = 3; branchingFactor < 5; branchingFactor++) {
            for (int depth = 3; depth < 4; depth++) {
//                if(depth == 3 && branchingFactor == 3)
//                    continue;
//                for (int obs = 2; obs < 4; obs++) {
                    for (int seed = 0; seed < 1000000; seed++) {
                        RandomGameInfo.seed = seed;
                        RandomGameInfo.MAX_OBSERVATION = 2;
                        RandomGameInfo.MAX_DEPTH = depth;
                        RandomGameInfo.MAX_BF = branchingFactor;
                        GameState root = new GeneralSumRandomGameState();
                        StackelbergConfig config = new StackelbergConfig(root);
                        Expander<SequenceInformationSet> expander = new RandomGameExpander<>(config);
                        FullSequenceEFG builder = new FullSequenceEFG(root, expander, new RandomGameInfo(), config);

                        builder.generateCompleteGame();
                        long rpCount = RPCounter.count(config, expander, config.getInformationSetFor(root), root.getAllPlayers()[1]);//getRPCount(root.getAllPlayers()[0], root.getAllPlayers()[1], config, expander);

                        System.out.println(rpCount);
                        if (rpCount > start) {
                            int index = (int) ((double) rpCount - start) / bucketSize;

                            if (index < counts.length && index >= 0) {
                                if (counts[index] < countPerBucket) {
                                    System.out.println(Arrays.toString(counts));
                                    count++;
                                    counts[index]++;
                                    storeConfiguration(rpCount);
                                    if (isFull(counts)) {
                                        writer.close();
                                        return;
                                    }
                                }
                            }
                        }
//                    }
                }
            }
        }
//        for (int branchingFactor = 5; branchingFactor < 7; branchingFactor++) {
//            for (int depth = 5; depth < 7; depth++) {
//                for (int obs = 2; obs < 3; obs++) {
//                    for (int seed = 0; seed < 1500; seed++) {
//                        RandomGameInfo.seed = seed;
//                        RandomGameInfo.MAX_OBSERVATION = obs;
//                        RandomGameInfo.MAX_DEPTH = depth;
//                        RandomGameInfo.MAX_BF = branchingFactor;
//                        GameState root = new GeneralSumRandomGameState();
//                        StackelbergConfig config = new StackelbergConfig(root);
//                        Expander<SequenceInformationSet> expander = new RandomGameExpander<>(config);
//                        FullSequenceEFG builder = new FullSequenceEFG(root, expander, new RandomGameInfo(), config);
//
//                        builder.generateCompleteGame();
//                        int rpCount = getRPCount(root.getAllPlayers()[0], root.getAllPlayers()[1], config, expander);
//                        if(rpCount > start) {
//                            int index = (int) ((double) rpCount - start) / bucketSize;
//
//                            if (index < counts.length && index >= 0) {
//                                if (counts[index] < countPerBucket) {
//                                    System.out.println(Arrays.toString(counts));
//                                    count++;
//                                    counts[index]++;
//                                    storeConfiguration(rpCount);
//                                    if (isFull(counts)) {
//                                        writer.close();
//                                        return;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
        writer.close();
    }

    private static boolean isFull(int[] counts) {
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] < countPerBucket)
                return false;
        }
        return true;
    }

    private static void storeConfiguration(long rpCount) {
        try {
            if (writer == null)
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("config100kto400ksmall"), true)));
            writer.write(RandomGameInfo.seed + " " + RandomGameInfo.MAX_OBSERVATION + " " + RandomGameInfo.MAX_DEPTH + " " + RandomGameInfo.MAX_BF + " " + rpCount);
            writer.newLine();
            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static int getRPCount(Player leader, Player follower, StackelbergConfig config, Expander<SequenceInformationSet> expander) {
        Iterator<Set<Sequence>> iterator = new NoCutDepthPureRealPlanIterator(follower, config, expander, new EmptyFeasibilitySequenceFormLP());
        int count = 0;

        try {
            while (true) {
                iterator.next();
                count++;
                if(count > start + bucketCount*bucketSize) {
                    System.err.println("break");
                    return Integer.MAX_VALUE;
                }
            }
        } catch (NoSuchElementException e) {
        }
        return count;
    }
}
