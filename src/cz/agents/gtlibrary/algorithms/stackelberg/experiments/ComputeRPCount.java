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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

public class ComputeRPCount {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("rpCounting")));
        RandomGameInfo.FIXED_SIZE_BF = false;
        RandomGameInfo.BINARY_UTILITY = true;
        RandomGameInfo.UTILITY_CORRELATION = false;
        String line = null;

        while((line = reader.readLine())!= null) {
            StringTokenizer tokenizer = new StringTokenizer(line);
            RandomGameInfo.seed = Integer.parseInt(tokenizer.nextToken());
            RandomGameInfo.MAX_OBSERVATION = Integer.parseInt(tokenizer.nextToken());
            RandomGameInfo.MAX_DEPTH = Integer.parseInt(tokenizer.nextToken());
            RandomGameInfo.MAX_BF = Integer.parseInt(tokenizer.nextToken());
            GameState root = new GeneralSumRandomGameState();
            StackelbergConfig config = new StackelbergConfig(root);
            Expander<SequenceInformationSet> expander = new RandomGameExpander<>(config);
            FullSequenceEFG builder = new FullSequenceEFG(root, expander, new RandomGameInfo(), config);

            builder.generateCompleteGame();
            System.out.println(line);
            System.out.println(getRPCount(root.getAllPlayers()[0], root.getAllPlayers()[1], config, expander));
//            System.out.println("!!!!stored: " + tokenizer.nextToken());
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
}
