package cz.agents.gtlibrary.domain.testGame.gameDefs;

import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Jakub Cerny on 13/11/2017.
 */
public class GambitGame implements GameDefinition {

    protected int nodeCount = 1;
    protected Stack<Integer> stack = new Stack<>();

    protected static String gameFile = "tlGame.gbt";

    // State -> States
    public static HashMap<Integer, ArrayList<Integer>> successors = new HashMap<Integer, ArrayList<Integer>>();

    // State -> IS
    public static HashMap<Integer, Integer> iss = new HashMap<Integer,Integer>();

    // IS -> Player
    public static HashMap<Integer, Integer> players = new HashMap<Integer,Integer>();

    // State -> utility
    public static HashMap<Integer, Double[]> utilities = new HashMap<Integer,Double[]>();

    public GambitGame(String gameFile){
        this.gameFile = gameFile;
        initGame();
    }

    public GambitGame(){
        initGame();
    }

    protected void initGame() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(gameFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
//            StringBuilder sb = new StringBuilder();
            StringTokenizer tokenizer;
            String line = br.readLine();
            line = br.readLine();

            while (line != null) {
                tokenizer = new StringTokenizer(line);
                String nodeType = tokenizer.nextToken();
                if (nodeType.equals("p")) processInnerState(tokenizer);
                if (nodeType.equals("t")) processTerminalState(tokenizer);
//                sb.append(line);
//                sb.append(System.lineSeparator());
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processTerminalState(StringTokenizer tokenizer) {
        int nodeIdx = nodeCount++;
        int parent = stack.pop();
        if (!successors.containsKey(parent)) successors.put(parent, new ArrayList<>());
        successors.get(parent).add(nodeIdx);

        String token = tokenizer.nextToken();
        while (!token.endsWith("\"")){
            token = tokenizer.nextToken();
        }
        token = tokenizer.nextToken();
        while (!token.endsWith("\"")){
            token = tokenizer.nextToken();
        }
        tokenizer.nextToken();
        Double[] outcome = new Double[2];
        outcome[0] = Double.parseDouble(tokenizer.nextToken().replace(',',' '));
        outcome[1] = Double.parseDouble(tokenizer.nextToken().replace('}',' '));
        utilities.put(nodeIdx, outcome);
//        System.out.println(Arrays.toString(outcome));
    }

    private void processInnerState(StringTokenizer tokenizer) {
        int nodeIdx = nodeCount++;
        if (!stack.isEmpty()) {
            int parent = stack.pop();
            if (!successors.containsKey(parent)) successors.put(parent, new ArrayList<>());
            successors.get(parent).add(nodeIdx);
        }

        String token = tokenizer.nextToken();
        while (!token.endsWith("\"")){
            token = tokenizer.nextToken();
        }
        int player = Integer.parseInt(tokenizer.nextToken()) - 1;
        int IS = Integer.parseInt(tokenizer.nextToken());

        iss.put(nodeIdx, IS);
        players.put(IS, player);

        token = tokenizer.nextToken();
        while (!token.endsWith("\"")){
            token = tokenizer.nextToken();
        }
        // token = ..."

        int numberOfActions = 0;
        token = tokenizer.nextToken(); // {
        token = tokenizer.nextToken();
        while (!token.endsWith("}")){
            numberOfActions++;
            while (!token.endsWith("\"")){
                token = tokenizer.nextToken();
            }
            token = tokenizer.nextToken();
        }

        for (int i = 0; i < numberOfActions; i++)
            stack.push(nodeIdx);
    }

    public static void main(String[] args) {
        GambitGame game = new GambitGame();
//        StringTokenizer tok = new StringTokenizer("\"FlipIt : No Info GS of Defender: Defender: [] / Attacker: []\" 1 1 \"\" { \"(N0:10916369)\" \"(PASS:10916369)\" \"(PASS:10916369)\" } 0\n");
//        game.processInnerState(tok);
//        tok = new StringTokenizer("\"FlipIt : No Info GS of Defender: Defender: [(N0:10916369), (N0:428158923), TLA{0}] / Attacker: [(N0:10916370), (N0:428158955)]\" 1 \"\" { 5.120000000000001, -10.44}");
//        game.processTerminalState(tok);
        System.out.println(game.stack);
        System.out.println(game.getISs());
        System.out.println(game.getPlayersForISs());
        System.out.println(game.getUtilities().toString());
    }

    @Override
    public HashMap<Integer, ArrayList<Integer>> getSuccessors() {
        return successors;
    }

    @Override
    public HashMap<Integer, Integer> getISs() {
        return iss;
    }

    @Override
    public HashMap<Integer, Integer> getPlayersForISs() {
        return players;
    }

    @Override
    public HashMap<Integer, Double[]> getUtilities() {
        return utilities;
    }
}
