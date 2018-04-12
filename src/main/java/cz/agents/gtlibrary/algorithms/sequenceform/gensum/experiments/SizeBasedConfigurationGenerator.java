package cz.agents.gtlibrary.algorithms.sequenceform.gensum.experiments;

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.domain.randomgame.GeneralSumRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;

import java.io.*;
import java.util.Arrays;

public class SizeBasedConfigurationGenerator {
    public static final int bucketCount = 20;
    public static final int bucketSize = 100;
    public static final int countPerBucket = 50;
    public static final int start = 500;
    public static BufferedWriter writer;

    public static void main(String[] args) throws IOException {
        int[] counts = new int[bucketCount];
        int count = 0;

        RandomGameInfo.FIXED_SIZE_BF = false;
        for (int branchingFactor = 3; branchingFactor < 4; branchingFactor++) {
            for (int depth = 4; depth < 7; depth++) {
                for (int obs = 3; obs < 4; obs++) {
                    for (int seed = 0; seed < 30000; seed++) {
                        RandomGameInfo.seed = seed;
                        RandomGameInfo.MAX_OBSERVATION = obs;
                        RandomGameInfo.MAX_DEPTH = depth;
                        RandomGameInfo.MAX_BF = branchingFactor;
                        GameState root = new GeneralSumRandomGameState();
                        SequenceFormConfig<SequenceInformationSet> config = new SequenceFormConfig<>();
                        Expander<SequenceInformationSet> expander = new RandomGameExpander<>(config);
                        FullSequenceEFG builder = new FullSequenceEFG(root, expander, new RandomGameInfo(), config);

                        builder.generateCompleteGame();
                        int size = getSize(config);

                        System.out.println(size);
                        if (size > start) {
                            int index = (int) ((double) size - start) / bucketSize;

                            if (index < counts.length && index >= 0) {
                                if (counts[index] < countPerBucket) {
                                    count++;
                                    counts[index]++;
                                    System.out.println(Arrays.toString(counts));
                                    storeConfiguration(size);
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

    private static boolean isFull(int[] counts) {
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] < countPerBucket)
                return false;
        }
        return true;
    }

    private static void storeConfiguration(int rpCount) {
        try {
            if (writer == null)
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("configIncrDepthSeq"), true)));
            writer.write(RandomGameInfo.seed + " " + RandomGameInfo.MAX_OBSERVATION + " " + RandomGameInfo.MAX_DEPTH + " " + RandomGameInfo.MAX_BF + " " + rpCount);
            writer.newLine();
            writer.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static int getSize(SequenceFormConfig<SequenceInformationSet> config) {
        return config.getAllSequences().size();
    }
}
