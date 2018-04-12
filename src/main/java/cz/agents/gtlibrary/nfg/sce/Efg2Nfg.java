package cz.agents.gtlibrary.nfg.sce;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.strategy.PureStrategyImpl;
import cz.agents.gtlibrary.interfaces.*;

import java.util.*;

/**
 * Created by Jakub Cerny on 22/08/2017.
 */
public class Efg2Nfg {

    protected GameInfo info;
    protected GenSumSequenceFormConfig algConfig;
    protected Expander<SequenceInformationSet> expander;

    protected ArrayList<HashSet<PureStrategyImpl>> strategiesOfAllPlayers;

    protected HashMap<ArrayList<PureStrategyImpl>, double[]> utility;
    HashMap<Sequence, HashSet<PureStrategyImpl>> agrees;// = new HashMap<>();

    public Efg2Nfg(GameState rootState, Expander<SequenceInformationSet> expander, GameInfo gameInfo, GenSumSequenceFormConfig algConfig) {
        this.expander = expander;
        this.info = gameInfo;
        this.algConfig = algConfig;
        agrees = new HashMap<>();
        utility = new HashMap<>();
        strategiesOfAllPlayers = new ArrayList<>();
        for (int i = 0; i < info.getAllPlayers().length; i++)
            strategiesOfAllPlayers.add(new HashSet<>());

        generateCompleteGame(rootState);
        System.out.println("Game tree generated.");

        generatePureStrategies();
        System.out.println("Strategies generated.");
        generateUtility();
    }

    public ArrayList<HashSet<PureStrategyImpl>> getStrategiesOfAllPlayers(){
        return  strategiesOfAllPlayers;
    }

    public HashMap<ArrayList<PureStrategyImpl>, double[]> getUtility(){
        return utility;
    }

    protected void generateCompleteGame(GameState rootState) {
        LinkedList<GameState> queue = new LinkedList<>();

        queue.add(rootState);

        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();

            algConfig.addStateToSequenceForm(currentState);
            if (currentState.isGameEnd()) {
                final double[] utilities = currentState.getUtilities();
                Double[] u = new Double[utilities.length];

                for (Player p : currentState.getAllPlayers()){
                    if(utilities.length > p.getId())
                        u[p.getId()] = utilities[p.getId()] * currentState.getNatureProbability()*info.getUtilityStabilizer();
                }
                algConfig.setUtility(currentState, u);
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
    }

    protected ArrayList<HashSet<SequenceInformationSet>> getISsForEachPlayer(){
        ArrayList<HashSet<SequenceInformationSet>> ISs = new ArrayList<HashSet<SequenceInformationSet>>();
        for(int i = 0; i < info.getAllPlayers().length; i++){
            ISs.add(new HashSet<SequenceInformationSet>());
        }
        for(SequenceInformationSet IS : algConfig.getAllInformationSets().values()){
            if(IS.getOutgoingSequences().isEmpty())
                continue;
            ISs.get(IS.getPlayer().getId()).add(IS);
        }
        return ISs;
    }

    protected void generatePureStrategy(Stack<SequenceInformationSet> stack, PureStrategyImpl strategy, int idx, int ISsize){
        SequenceInformationSet set;
        if(!stack.isEmpty()){
            set = stack.pop();
        }
        else{
            return;
        }
        if(!set.getOutgoingSequences().isEmpty()){
            int playerIndex = set.getPlayer().getId();
            int id = ++idx;
            List<Action> actions = expander.getActions(set);
            for(Action a : actions){
                strategy.set(id,a);
                if(id == ISsize-1){
                    this.strategiesOfAllPlayers.get(playerIndex).add(strategy.copy());
                }
                generatePureStrategy(stack, strategy, idx, ISsize);
            }
            stack.push(set);
            strategy.set(id,null);
            idx--;
        }
        else{
            generatePureStrategy(stack, strategy, idx, ISsize);
        }
    }

    protected void generateReducedStrategy(Stack<SequenceInformationSet> stack, PureStrategyImpl strategy, int idx, int ISsize){
        SequenceInformationSet set;
        if(!stack.isEmpty()){
            set = stack.pop();
        }
        else{
            return;
        }
        if(!set.getOutgoingSequences().isEmpty()){
            int playerIndex = set.getPlayer().getId();
            int id = ++idx;
            if(strategy.isCompatibleWithPartial(set.getPlayersHistory())){
                List<Action> actions = expander.getActions(set);
                for(Action a : actions){
                    strategy.set(id,a);
                    if(id == ISsize-1){
                        this.strategiesOfAllPlayers.get(playerIndex).add(strategy.copy());
                    }
                    generateReducedStrategy(stack, strategy, idx, ISsize);
                }
            }
            else{
                if(id == ISsize-1){
                    this.strategiesOfAllPlayers.get(playerIndex).add(strategy.copy());
                }
                generateReducedStrategy(stack, strategy, idx, ISsize);
            }
            stack.push(set);
            strategy.set(id,null);
            idx--;
        }
        else{
            generateReducedStrategy(stack, strategy, idx, ISsize);
        }
    }


    protected void generatePureStrategies(){
        ArrayList<HashSet<SequenceInformationSet>> ISs = getISsForEachPlayer();
        for(int i = 0; i < info.getAllPlayers().length; i++){
            if(ISs.get(i).isEmpty()){
//                randomPlayerIndex = i;
                continue;
            }
            ArrayList<SequenceInformationSet> list = new ArrayList<SequenceInformationSet>(ISs.get(i));
            Collections.sort(list, new Comparator<SequenceInformationSet>() {
                @Override
                public int compare(SequenceInformationSet s1, SequenceInformationSet s2) {
//                    System.out.println(s1.getPlayersHistory());
//                    System.out.println(s2.getPlayersHistory());
//                    System.out.println();
                    if (s1.getPlayersHistory().size() == s2.getPlayersHistory().size()) return 0;
                    if (s1.getPlayersHistory().isPrefixOf(s2.getPlayersHistory()) && s1.getPlayersHistory().size() < s2.getPlayersHistory().size()) {
//                        System.out.println("<");
                        return 1;
                    }
                    if (s2.getPlayersHistory().isPrefixOf(s1.getPlayersHistory()) && s2.getPlayersHistory().size() < s1.getPlayersHistory().size()) {
//                        System.out.println(">");
                        return -1;
                    }
//                    System.out.println("==");
                    return 0;
                }
            });
            Stack<SequenceInformationSet> stack = new Stack<>();
            stack.addAll(list);
            generateReducedStrategy(stack,new PureStrategyImpl(new Action[stack.size()],info.getAllPlayers()[i]),-1,stack.size());
//        	generatePureStrategy(stack,new PureStrategyImpl(new Action[stack.size()],info.getAllPlayers()[i]),-1,stack.size());

        }
        System.out.println("final size [strategies] : ");
        for (Player p : info.getAllPlayers()){
            if (p.getName() == "Nature") continue;
            System.out.printf("\t%d.player strategies: %d \n",p.getId()+1,strategiesOfAllPlayers.get(p.getId()).size());
        }
    }

    protected void generateUtility(){
//        HashMap<Sequence, HashSet<PureStrategyImpl>> agrees = new HashMap<>();
        for (GameState leaf : algConfig.getActualNonZeroUtilityValuesInLeafsGenSum().keySet()){
            for (int player = 0; player < info.getAllPlayers().length; player++) {
                Sequence sequence = leaf.getSequenceFor(info.getAllPlayers()[player]);
                for (PureStrategyImpl strategy : strategiesOfAllPlayers.get(player)) {
                    if (strategy.isCompatibleWith(sequence)) {
                        // add to agrees
                        if (!agrees.containsKey(sequence))
                            agrees.put(sequence, new HashSet<PureStrategyImpl>());
                        agrees.get(sequence).add(strategy);
                    }
                }
            }
        }

        System.out.println("Agrees generated.");
        for (GameState leaf : algConfig.getActualNonZeroUtilityValuesInLeafsGenSum().keySet())
            generateProfilesLeadingToLeaf(0, initStrategyProfile(), leaf);
    }

    public ArrayList<PureStrategyImpl> initStrategyProfile(){
        ArrayList<PureStrategyImpl> profile = new ArrayList<PureStrategyImpl>(info.getAllPlayers().length);
        for(int i = 0; i < info.getAllPlayers().length; i++)
            profile.add(null);
        return profile;
    }

    protected void generateProfilesLeadingToLeaf(int playerIndex, ArrayList<PureStrategyImpl> profile, GameState leaf){
        if(playerIndex == info.getAllPlayers().length){
            double[] utilities = leaf.getUtilities();
//            System.out.println(utilities[0] + " , " + utilities[1]);
            for (int i = 0 ; i < utilities.length; i++) utilities[i] *= leaf.getNatureProbability();
            if (utility.containsKey(profile)) {
                System.out.println("Profile leading to multiple leafs.");
                double[] otherLeafUtility = utility.get(profile);
                for (int i = 0 ; i < utilities.length; i++) utilities[i] += otherLeafUtility[i];
            }
            ArrayList<PureStrategyImpl> newProfile = initStrategyProfile();
            for (int i = 0 ; i < newProfile.size(); i++) newProfile.set(i, profile.get(i).copy());
            utility.put(newProfile, utilities);
            return;
        }
        if (!agrees.containsKey(leaf.getSequenceFor(info.getAllPlayers()[playerIndex]))){
            System.err.println("No strategy leading to this leaf.");
            generateProfilesLeadingToLeaf(playerIndex+1, profile, leaf);
            return;
        }
        for(PureStrategyImpl strategy : agrees.get(leaf.getSequenceFor(info.getAllPlayers()[playerIndex]))){
            profile.set(playerIndex, strategy);
            generateProfilesLeadingToLeaf(playerIndex+1, profile, leaf);
        }
    }

}
