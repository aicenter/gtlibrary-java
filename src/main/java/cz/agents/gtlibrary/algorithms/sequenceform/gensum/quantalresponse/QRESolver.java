package cz.agents.gtlibrary.algorithms.sequenceform.gensum.quantalresponse;

import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.randomgame.GeneralSumRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.CSVStrategyImport;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.io.*;
import java.util.*;

public class QRESolver {

    private Expander<SequenceInformationSet> expander;
    private SequenceFormConfig<SequenceInformationSet> algConfig;
    private GameState root;
    private GameInfo info;

    public static void main(String[] args) throws IOException, InterruptedException {
        runGenSumRndGame();
    }

    private static void runGenSumRndGame() throws InterruptedException, IOException {
        GeneralSumRandomGameState root = new GeneralSumRandomGameState();
        GameInfo info = new RandomGameInfo();
        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> expander = new RandomGameExpander<>(algConfig);
        QRESolver solver = new QRESolver(root, expander, info, algConfig);

        System.out.println(solver.solve());
    }


    public QRESolver(GameState root, Expander<SequenceInformationSet> expander, GameInfo info, SequenceFormConfig<SequenceInformationSet> algConfig) {
        this.root = root;
        this.expander = expander;
        this.info = info;
        this.algConfig = algConfig;
    }

    public QREResult solve() {
        FullSequenceEFG builder = new FullSequenceEFG(root, expander, info, algConfig);

        builder.generateCompleteGame();
        GambitEFG writer = new GambitEFG();

        writer.write("GenSumRndGambit", root, expander);
        try {
            new ProcessBuilder().command("./gambit-logit").redirectInput(new File("GenSumRndGambit")).redirectOutput(new File("GenSumRndGambitQRE.csv")).start().waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buildQREResult("GenSumRndGambitQRE.csv");
    }

    private QREResult buildQREResult(String fileName) {
        return new QREResult(getQunatalRealPlans(fileName), getLambdas(fileName));
    }

    private List<Double> getLambdas(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            List<Double> lambdas = new ArrayList<>();
            String line;

            while((line = reader.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, ",");

                lambdas.add(Double.parseDouble(tokenizer.nextToken()));
            }
            return lambdas;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Map<Player, Map<Sequence, Double>>> getQunatalRealPlans(String fileName) {
        ArrayList<Map<Sequence, Double>> quantalResponses = CSVStrategyImport.readStrategyFromCSVForEFG(fileName, root, expander);
        List<Map<Player, Map<Sequence, Double>>> quantalRealPlans = new ArrayList<>(quantalResponses.size());

        for (Map<Sequence, Double> quantalResponse : quantalResponses) {
            Map<Player, Map<Sequence, Double>> realPlans = new HashMap<>(2);

            realPlans.put(root.getAllPlayers()[0], getRealPlan(quantalResponse, root.getAllPlayers()[0]));
            realPlans.put(root.getAllPlayers()[1], getRealPlan(quantalResponse, root.getAllPlayers()[1]));
            quantalRealPlans.add(realPlans);
        }
        return quantalRealPlans;
    }

    private Map<Sequence, Double> getRealPlan(Map<Sequence, Double> quantalResponse, Player player) {
        Map<Sequence, Double> realPlan = new HashMap<>();

        realPlan.put(new ArrayListSequenceImpl(player), 1d);
        for (Map.Entry<Sequence, Double> entry : quantalResponse.entrySet()) {
            if (entry.getKey().getPlayer().equals(player))
                realPlan.put(entry.getKey(), entry.getValue());
        }
        return realPlan;
    }
}
