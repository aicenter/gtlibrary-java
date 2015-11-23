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

public class ConfigUpdater {
    public static final int bucketCount = 35;
    public static final int bucketSize = 2000;
    public static final int countPerBucket = 20;
    public static final int start = 20000;
    public static BufferedWriter writer;

    public static void main(String[] args) throws IOException {
        int[] counts = new int[bucketCount];
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
        String line = null;

        while ((line = reader.readLine()) != null) {
            String[] split = line.split(" ");
            int rpCount = Integer.parseInt(split[split.length - 1]);

            if (rpCount > start) {
                int index = (int) Math.floor(((double) rpCount - start) / bucketSize);

                if (index < counts.length && index >= 0) {
                    counts[index]++;
                    System.out.println(Arrays.toString(counts));
                    if (isFull(counts)) {
                        reader.close();
                        return;
                    }
                }
            }
        }
        reader.close();
        System.out.println("config contains: " + Arrays.toString(counts));

        RandomGameInfo.FIXED_SIZE_BF = false;
        for (int branchingFactor = 3; branchingFactor < 4; branchingFactor++) {
            for (int depth = 3; depth < 4; depth++) {
                for (int obs = 2; obs < 3; obs++) {
                    for (int seed = 10000; seed < 15000; seed++) {
                        RandomGameInfo.seed = seed;
                        RandomGameInfo.MAX_OBSERVATION = obs;
                        RandomGameInfo.MAX_DEPTH = depth;
                        RandomGameInfo.MAX_BF = branchingFactor;
                        GameState root = new GeneralSumRandomGameState();
                        StackelbergConfig config = new StackelbergConfig(root);
                        Expander<SequenceInformationSet> expander = new RandomGameExpander<>(config);
                        FullSequenceEFG builder = new FullSequenceEFG(root, expander, new RandomGameInfo(), config);

                        builder.generateCompleteGame();
                        int rpCount = getRPCount(root.getAllPlayers()[0], root.getAllPlayers()[1], config, expander);

                        System.out.println(rpCount);
                        if (rpCount > start) {
                            int index = (int) Math.floor(((double) rpCount - start) / bucketSize);

                            if (index < counts.length && index >= 0) {
                                if (counts[index] < countPerBucket) {
                                    counts[index]++;
                                    System.out.println(Arrays.toString(counts));
                                    storeConfiguration(rpCount, args[0]);
                                    if (isFull(counts)) {
                                        writer.close();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        writer.close();
    }

    private static void storeConfiguration(int rpCount, String fileName) {
        try {
            if (writer == null)
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName), true)));
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
            }
        } catch (NoSuchElementException e) {
        }
        return count;
    }

    private static boolean isFull(int[] counts) {
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] < countPerBucket)
                return false;
        }
        return true;
    }
}
