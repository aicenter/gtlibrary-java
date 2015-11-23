package cz.agents.gtlibrary.algorithms.sequenceform.gensum.experiments;

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormMILP;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.UndomGenSumSequenceFormMILP;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.librarycom.DataBuilder;
import cz.agents.gtlibrary.domain.randomgame.GeneralSumRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.stacktest.StackTestGameInfo;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;

import java.io.*;
import java.util.StringTokenizer;

public class RandomGameFromConfigRunner {

    /**
     * @param args [0] alg
     *             [1] config file
     *             [2] correlation
     *             [3] line
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[1]))));
        String line = null;

        RandomGameInfo.FIXED_SIZE_BF = false;
        RandomGameInfo.BINARY_UTILITY = true;
        RandomGameInfo.UTILITY_CORRELATION = false;
        RandomGameInfo.CORRELATION = Double.parseDouble(args[2]);
        BufferedWriter timeWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[0] + " " + RandomGameInfo.CORRELATION + " finalTime.csv", true)));
        int lineIndex = Integer.parseInt(args[3]);
        int count = 0;

        while (count++ <= lineIndex) {
            line = reader.readLine();
        }
        if (line == null)
            return;
//        while ((line = reader.readLine()) != null) {
        StringTokenizer tokenizer = new StringTokenizer(line);
        RandomGameInfo.seed = Integer.parseInt(tokenizer.nextToken());
        RandomGameInfo.MAX_OBSERVATION = Integer.parseInt(tokenizer.nextToken());
        RandomGameInfo.MAX_DEPTH = Integer.parseInt(tokenizer.nextToken());
        RandomGameInfo.MAX_BF = Integer.parseInt(tokenizer.nextToken());
        runRandomGame(timeWriter, args[0]);

        timeWriter.close();
        reader.close();
    }

    public static void runRandomGame(BufferedWriter timeWriter, String algType) {
        try {
            if (algType.startsWith("MILP")) {
                GameState rootState = new GeneralSumRandomGameState();
                GameInfo gameInfo = new RandomGameInfo();
                GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
                Expander<SequenceInformationSet> expander = new RandomGameExpander<>(algConfig);
                FullSequenceEFG builder = new FullSequenceEFG(rootState, expander, new StackTestGameInfo(), algConfig);

                builder.generateCompleteGame();
                GenSumSequenceFormMILP solver = getMILPSolver(rootState, gameInfo, algConfig, algType, expander);

                solver.compute();
                timeWriter.write(String.valueOf(solver.getLpTime()));
                timeWriter.newLine();
                timeWriter.flush();
            } else {
                if (algType.equals("lemkeNash"))
                    DataBuilder.alg = DataBuilder.Alg.lemkeNash;
                else if (algType.equals("lemkeNash1"))
                    DataBuilder.alg = DataBuilder.Alg.lemkeNash2;
                else if (algType.equals("lemkeQP"))
                    DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect;
                else if (algType.equals("lemkeQP1"))
                    DataBuilder.alg = DataBuilder.Alg.lemkeQuasiPerfect2;
                else if (algType.equals("simplexNash"))
                    DataBuilder.alg = DataBuilder.Alg.simplexNash;
                else if (algType.equals("simplexQP"))
                    DataBuilder.alg = DataBuilder.Alg.simplexQuasiPerfect;
                else
                    throw new UnsupportedOperationException("Unsupported algorithm.");
                GameState rootState = new GeneralSumRandomGameState();
                GameInfo gameInfo = new RandomGameInfo();
                GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
                Expander<SequenceInformationSet> expander = new RandomGameExpander<>(algConfig);

                timeWriter.write(String.valueOf(DataBuilder.runDataBuilder(rootState, expander, algConfig, gameInfo, "GenSumRndGameRepr")));
                timeWriter.newLine();
                timeWriter.flush();
            }
//            System.out.println("current: " + ConfigurationGenerator.getRPCount(rootState.getAllPlayers()[leaderIndex], rootState.getAllPlayers()[1 - leaderIndex], algConfig, expander));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static GenSumSequenceFormMILP getMILPSolver(GameState rootState, GameInfo gameInfo, GenSumSequenceFormConfig algConfig, String algType, Expander<SequenceInformationSet> expander) {
        if (algType.equals("MILP"))
            return new GenSumSequenceFormMILP(algConfig, rootState.getAllPlayers(), gameInfo);
        return new UndomGenSumSequenceFormMILP(algConfig, rootState.getAllPlayers(), gameInfo, rootState, expander, rootState.getAllPlayers()[0]);
    }
}
