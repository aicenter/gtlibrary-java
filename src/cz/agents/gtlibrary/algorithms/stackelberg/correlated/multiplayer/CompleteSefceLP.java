package cz.agents.gtlibrary.algorithms.stackelberg.correlated.multiplayer;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import ilog.cplex.IloCplex;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.strategy.*;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * Created by Jakub Cerny on 24/07/2017.
 */
public class CompleteSefceLP implements Solver {

    protected long overallConstraintGenerationTime = 0;
    protected double eps;

    protected long overallConstraintLPSolvingTime = 0;
    protected LPTable lpTable;
    protected Player leader;
    protected GameInfo info;
    protected ThreadMXBean threadBean;
    protected StackelbergConfig algConfig;
    protected Expander<SequenceInformationSet> expander;
    protected HashMap<Sequence,HashSet<PureStrategyImpl>> agrees;

    protected ArrayList<HashSet<PureStrategyImpl>> strategiesOfAllPlayers;

    protected HashSet<Integer> hashes;
    protected Random random;
    protected double gameValue;

    protected final boolean CHECK_HASHES = false;
    protected final boolean DEBUG = false;
    protected final boolean OUTPUT_LP = false;



    // vars : z(s), u(c), v(I,c)

    // cons :   pro vsechny c mame u(c) a u(c)/v(I(c),c)
    //          pro vsechny kombinace <c,d>

    public CompleteSefceLP(Player leader, GameInfo info) {
        this.hashes = new HashSet<Integer>();
        this.lpTable = new LPTable();
        this.leader = leader;
//        this.randomPlayerIndex = -1;
        this.info = info;
        this.threadBean = ManagementFactory.getThreadMXBean();
        this.eps = 1e-8;
        this.strategiesOfAllPlayers = new ArrayList<HashSet<PureStrategyImpl>>();
        for (int i = 0; i < info.getAllPlayers().length; i++) {
            HashSet<PureStrategyImpl> s = new HashSet<PureStrategyImpl>();
            strategiesOfAllPlayers.add(s);
        }
        random = new Random();
    }

    public String getInfo(){
        return "Complete multiplayer SEFCE LP";
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
                public int compare(SequenceInformationSet s1, SequenceInformationSet s2){
                    if(s1.getPlayersHistory().isPrefixOf(s2.getPlayersHistory()))
                        return 1;
                    if(s2.getPlayersHistory().isPrefixOf(s1.getPlayersHistory()))
                        return -1;
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

    protected void generateProfilesLeadingToLeafRecursive(int playerIndex, ArrayList<PureStrategyImpl> profile, GameState leaf, Object eqKey, double value, int skipPlayerIndex){
        if(playerIndex == skipPlayerIndex){
            generateProfilesLeadingToLeafRecursive(playerIndex+1, profile, leaf, eqKey, value, skipPlayerIndex);
            return;
        }

        if(playerIndex == info.getAllPlayers().length){
            // add to eq
            lpTable.setConstraint(eqKey, profile.hashCode(), value);
            return;
        }
        if (!agrees.containsKey(leaf.getSequenceFor(info.getAllPlayers()[playerIndex]))){
            generateProfilesLeadingToLeafRecursive(playerIndex+1, profile, leaf, eqKey, value, skipPlayerIndex);
            return;
        }
        // muze se stat ze hrac nema zadny strategie (proste vubec nehraje)
        for(PureStrategyImpl strategy : agrees.get(leaf.getSequenceFor(info.getAllPlayers()[playerIndex]))){
            profile.set(playerIndex, strategy);
            generateProfilesLeadingToLeafRecursive(playerIndex+1, profile, leaf, eqKey, value, skipPlayerIndex);
        }
    }

    protected void generateProfilesLeadingToLeafRecursive(int playerIndex, ArrayList<PureStrategyImpl> profile, HashSet<GameState> dLeafs, GameState leaf, Object eqKey, int actionPlayerIndex){
//        if(playerIndex == skipPlayerIndex){
//            generateProfilesLeadingToLeafRecursive(playerIndex+1, profile, dLeafs, leaf, eqKey, actionPlayerIndex);
//            return;
//        }
//        if(playerIndex == randomPlayerIndex){
//            generateProfilesLeadingToLeafRecursive(playerIndex+1, profile, leaf, eqKey, value, skipPlayerIndex);
//            return;
//        }
        if(playerIndex == info.getAllPlayers().length){
            // add to eq
            for (GameState dLeaf : dLeafs) {
                boolean isCompatible = true;
                for (int player = 0; player < profile.size(); player++){
                    if (info.getAllPlayers()[player].getName() == "Nature") continue;
                    if (player == actionPlayerIndex) continue;
                    if (!profile.get(player).isCompatibleWith(dLeaf.getSequenceFor(info.getAllPlayers()[player]))){
                        isCompatible = false;
                        break;
                    }
                }
                if (isCompatible) {
//                  System.out.println("is Compatible");
                    lpTable.addToConstraint(eqKey, profile.hashCode(), -dLeaf.getUtilities()[actionPlayerIndex]*dLeaf.getNatureProbability());
                }
            }
            return;
        }
        if (!agrees.containsKey(leaf.getSequenceFor(info.getAllPlayers()[playerIndex]))){
            generateProfilesLeadingToLeafRecursive(playerIndex+1, profile, dLeafs, leaf, eqKey, actionPlayerIndex);
            return;
        }
        // muze se stat ze hrac nema zadny strategie (proste vubec nehraje) ... pak by agrees melo obsahovat vsechny strategie
        for(PureStrategyImpl strategy : agrees.get(leaf.getSequenceFor(info.getAllPlayers()[playerIndex]))){
            profile.set(playerIndex, strategy);
            generateProfilesLeadingToLeafRecursive(playerIndex+1, profile, dLeafs, leaf, eqKey, actionPlayerIndex);
        }
    }

    protected void generateProfilesForProbDistrConstraint(int playerIndex, ArrayList<PureStrategyImpl> profile, Object eqKey){
//        if(playerIndex == randomPlayerIndex){
//            generateProfilesRecursive(playerIndex+1, profile, eqKey);
//            return;
//        }
        if(playerIndex == info.getAllPlayers().length){
            // add to eq
            int varKey = profile.hashCode();
            if (CHECK_HASHES) {
                if (hashes.contains(varKey))
                    System.err.println("Regenerated hashcode");
                hashes.add(varKey);
                System.out.println(profile.hashCode() + "; " + profile);
            }
            lpTable.setConstraint(eqKey, varKey, 1.0);
            lpTable.setLowerBound(varKey, 0.0);
            lpTable.setUpperBound(varKey, 1.0);
            return;
        }
        if (strategiesOfAllPlayers.get(playerIndex).isEmpty())
            generateProfilesForProbDistrConstraint(playerIndex+1, profile, eqKey);

        for(PureStrategyImpl strategy : strategiesOfAllPlayers.get(playerIndex)){
            profile.set(playerIndex, strategy);
            generateProfilesForProbDistrConstraint(playerIndex+1, profile, eqKey);
        }
    }

    protected void generateProfilesLeadingToLeafForObjective(int playerIndex, ArrayList<PureStrategyImpl> profile, GameState leaf, double value){
//        if(playerIndex == randomPlayerIndex){
//            generateProfilesLeadingToLeafRecursive(playerIndex+1, profile, leaf, value);
//            return;
//        }
        if(playerIndex == info.getAllPlayers().length){
            // add to eq
            lpTable.setObjective(profile.hashCode(), value);
            return;
        }
        if (!agrees.containsKey(leaf.getSequenceFor(info.getAllPlayers()[playerIndex]))){
            generateProfilesLeadingToLeafForObjective(playerIndex+1, profile, leaf, value);
            return;
        }
        for(PureStrategyImpl strategy : agrees.get(leaf.getSequenceFor(info.getAllPlayers()[playerIndex]))){
            profile.set(playerIndex, strategy);
            generateProfilesLeadingToLeafForObjective(playerIndex+1, profile, leaf, value);
        }
    }

    protected ArrayList<PureStrategyImpl> initStrategyProfile(){
        ArrayList<PureStrategyImpl> profile = new ArrayList<PureStrategyImpl>(info.getAllPlayers().length);
        for(int i = 0; i < info.getAllPlayers().length; i++)
            profile.add(null);
        return profile;
    }

    protected void generateExpectedUtilityConstraint(){
        ArrayList<HashSet<SequenceInformationSet>> ISs = getISsForEachPlayer();
        // for each player
        // for each IS
        // for each action
        // 1/  set of strategies which agree with it -> combine them to get profiles
        // 2/  u(c)=v(I(c),c)
        for (int p = 0; p < ISs.size(); p++){
            // no constraint for leader
            if (p == leader.getId()) continue;
            for (SequenceInformationSet h : ISs.get(p)){
                for (Action c : expander.getActions(h)){
                    // add equality u(c) = v(h,c)
                    Object eqKey = new Pair<>("expIsDev",c);
                    Object varKeyU = new Pair<>("u",c);
                    Object varKeyV = new Triplet<>("v",h,c);
                    lpTable.setConstraint(eqKey, varKeyU, 1.0);
                    lpTable.setLowerBound(varKeyU, Double.NEGATIVE_INFINITY);
                    lpTable.addToConstraint(eqKey, varKeyV, -1.0);
                    lpTable.setLowerBound(varKeyV, Double.NEGATIVE_INFINITY);
                    lpTable.setConstraintType(eqKey, 1);
                    lpTable.setConstant(eqKey, 0.0);

                    // add equality u(c) = \sum u(s)z(s)
//                    boolean isLastAction = true;
//                    for (SequenceInformationSet h2 : ISs.get(p))
//                        if (!h.equals(h2) && precedes(h,h2) && h2.getPlayersHistory().getAsList().contains(c)){
//                        isLastAction = false;
//                            break;
//                        }
//                    if (!isLastAction) continue;
                    Object eqKeyU = new Pair<>("expUt",c);
                    lpTable.setConstraint(eqKeyU, varKeyU, 1.0);
                    lpTable.setLowerBound(varKeyU, Double.NEGATIVE_INFINITY);
                    // get all leaves T accesible from c, generate all profiles leading to T

//                    Sequence s = h.getPlayersHistory();
                    Sequence s = new ArrayListSequenceImpl(h.getAllStates().iterator().next().getSequenceFor(info.getAllPlayers()[p]));
                    s.addLast(c);
                    if (DEBUG) System.out.println("Eval :" + c.toString() + " seq : " + s);
                    for (GameState leaf : algConfig.getAllLeafs()){
                        if (s.isPrefixOf(leaf.getSequenceFor(info.getAllPlayers()[p])) || s.equals(leaf.getSequenceFor(info.getAllPlayers()[p]))){
                            if (DEBUG) System.out.printf("\t %s : %f \n",leaf.toString(), leaf.getUtilities()[p]);
                            ArrayList<PureStrategyImpl> profile = initStrategyProfile();
                            generateProfilesLeadingToLeafRecursive(0,profile,leaf,eqKeyU,-leaf.getUtilities()[p]*leaf.getNatureProbability(),-1);
                        }
                    }
                    lpTable.setConstraintType(eqKeyU, 1);
                    lpTable.setConstant(eqKeyU, 0.0);

                }
            }
        }
    }

    protected void generateProfilesAgreeingWith(SequenceInformationSet h, Action c){
        ArrayList<HashSet<PureStrategyImpl>> agreeing = new ArrayList<>();
        for (Player p : info.getAllPlayers()) {
            HashSet<PureStrategyImpl> agreeingStrategies = new HashSet<>();
            for (PureStrategyImpl strategy : strategiesOfAllPlayers.get(p.getId()))
                for (GameState state : h.getAllStates())
                    if (strategy.isCompatibleWith(state.getSequenceFor(p))) {
                        agreeingStrategies.add(strategy);
                        break;
                    }
            agreeing.add(agreeingStrategies);
        }
    }

    protected boolean precedes(SequenceInformationSet h, SequenceInformationSet k){
        for (Action a : k.getPlayersHistory())
            if (expander.getActions(h).contains(a))
                return true;
        return false;
    }

    protected void generateDeviationIncentiveConstraint(){
        ArrayList<HashSet<SequenceInformationSet>> ISs = getISsForEachPlayer();
        for (int p = 0; p < ISs.size(); p++){
            // no constraint for leader
            if (p == leader.getId()) continue;
            for (SequenceInformationSet h : ISs.get(p)){
                for (Action c : expander.getActions(h)){
                    for (SequenceInformationSet k : ISs.get(p)) {
//                        if (k.getOutgoingSequences().isEmpty()) continue;
                        // check k succeeds h
//                        if (!h.equals(k) && !h.getPlayersHistory().isPrefixOf(k.getPlayersHistory())) continue;
//                        if (!h.equals(k) && !hcseq.isPrefixOf(k.getPlayersHistory())) continue;
                        if (!h.equals(k) && !precedes(h,k)) continue; // continue only if either h==k or h precedes k
                        for (Action d : expander.getActions(k)) {
                            Object eqKey = new Triplet<>("dev",c, d);
                            Object varKeyV = new Triplet<>("v",k,c);
                            lpTable.setConstraint(eqKey, varKeyV, 1.0);
                            lpTable.setLowerBound(varKeyV, Double.NEGATIVE_INFINITY);

                            // generate first part ..
                            // get all leaves T accessible from c, get profiles, change players part s.t. d is last action, generate all profiles leading to T
                            // get all leafs where d is last action
                            HashSet<GameState> dLastLeafs = new HashSet<GameState>();
                            for (GameState leaf : algConfig.getAllLeafs()){
                                if (leaf.getSequenceFor(info.getAllPlayers()[p]).isEmpty()) continue;
                                if (d.equals(leaf.getSequenceFor(info.getAllPlayers()[p]).getLast())){
                                    dLastLeafs.add(leaf);
                                }
                            }
                            // get all leafs T which agrees with c
                            Sequence s = new ArrayListSequenceImpl(h.getPlayersHistory());
                            s.addLast(c);
                            for (GameState leaf : algConfig.getAllLeafs()) {
                                if (s.isPrefixOf(leaf.getSequenceFor(info.getAllPlayers()[p]))) {
                                    // check whether same sequences of opponents leads to it
//                                    for (GameState dLeaf : dLastLeafs){
//                                        if(sameSequencesOfOpponentsLeadsTo(leaf,dLeaf,p)){
//                                            ArrayList<PureStrategyImpl> profile = initStrategyProfile();
//                                            generateProfilesLeadingToLeafRecursive(0,profile,leaf,eqKey,-dLeaf.getUtilities()[p]*dLeaf.getNatureProbability(),-1);
//
//                                        }
//                                    }
                                    ArrayList<PureStrategyImpl> profile = initStrategyProfile();
                                    generateProfilesLeadingToLeafRecursive(0,profile,dLastLeafs,leaf,eqKey,p);
                                }
                            }



                            // generate second part .. get all l under k via d
                            for (SequenceInformationSet l : ISs.get(p)) {
                                if (l.getOutgoingSequences().isEmpty()) continue;
                                Sequence seq = new ArrayListSequenceImpl(k.getPlayersHistory());
                                seq.addLast(d);
                                if(l.getPlayersHistory().equals(seq)){// && precedes(k,l)){
                                    Object varKeyVl = new Triplet<>("v",l,c);
                                    lpTable.addToConstraint(eqKey, varKeyVl, -1.0);
                                    lpTable.setLowerBound(varKeyVl, Double.NEGATIVE_INFINITY);
                                }
                            }

                            lpTable.setConstraintType(eqKey, 2);
                            lpTable.setConstant(eqKey, 0.0);

                        }
                    }
                }
            }
        }
    }

//    private boolean sameSequencesOfOpponentsLeadsTo(GameState leaf, GameState dLeaf, int p) {
//        for (int player = 0 ; player < info.getAllPlayers().length; player++){
//            if (player == p ) continue;
//            if (!leaf.getSequenceFor(info.getAllPlayers()[player]).equals(dLeaf.getSequenceFor(info.getAllPlayers()[player]))){
//                return false;
//            }
//        }
//        return true;
//    }

    protected void generateProbabilityDistributionConstraint(){
        ArrayList<PureStrategyImpl> profile = initStrategyProfile();
        String eqKey = "probConsistency";
        generateProfilesForProbDistrConstraint(0, profile, eqKey);
        lpTable.setConstraintType(eqKey, 1);
        lpTable.setConstant(eqKey, 1.0);
    }

    protected void generateObjective(){
        ArrayList<PureStrategyImpl> profile = initStrategyProfile();
        for(GameState leaf : algConfig.getAllLeafs()){
            generateProfilesLeadingToLeafForObjective(0, profile, leaf, leaf.getUtilities()[leader.getId()]*leaf.getNatureProbability());
        }
    }

    /*
     * For sequence, get strategy profiles of players leading to this leaf.
     * Map: Sequence -> Strategies
     * for terminal nodes : all players
     * for non-terminal nodes : only for acting player
     */
    protected void generateAgrees(){
        agrees = new HashMap<Sequence,HashSet<PureStrategyImpl>>();
        for(SequenceInformationSet set : algConfig.getAllInformationSets().values()) {
            for (int player = 0; player < info.getAllPlayers().length; player++) {
                Sequence sequence = set.getAllStates().iterator().next().getSequenceFor(info.getAllPlayers()[player]);
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
        for (GameState leaf : algConfig.getAllLeafs()){
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
    }



    public double calculateLeaderStrategies(AlgorithmConfig algConfig,
                                          Expander expander) {

        this.algConfig = (StackelbergConfig)algConfig;
        this.expander = (Expander<SequenceInformationSet>)expander;
        long startTime = threadBean.getCurrentThreadCpuTime();

        // create structures
        generatePureStrategies();
        generateAgrees();

        if (DEBUG) {
            for (int player = 0; player < info.getAllPlayers().length; player++) {
                System.out.println(player);
                for (PureStrategyImpl strategy : strategiesOfAllPlayers.get(player)) {
                    System.out.println(strategy);
                }
                System.out.println("////");
            }
        }

        System.out.println();
        System.out.println("final structure-generation time : "+ (threadBean.getCurrentThreadCpuTime()-startTime) / 1000000l);


        long startConstraintTime = threadBean.getCurrentThreadCpuTime();

        // create constraints & criterion
        generateExpectedUtilityConstraint();
        generateDeviationIncentiveConstraint();
        generateProbabilityDistributionConstraint();
        generateObjective();

        System.out.println("final lp-generation time : "+ (threadBean.getCurrentThreadCpuTime()-startConstraintTime) / 1000000l);
        System.out.println();

//        System.out.println("LP build...");
        lpTable.watchAllPrimalVariables();
        overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
        solve();
        return gameValue;
    }

    protected double solve() {
        try {
            long startTime = threadBean.getCurrentThreadCpuTime();

            LPTable.CPLEXALG = IloCplex.Algorithm.Primal;
            LPData lpData = lpTable.toCplex();

            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
            if (OUTPUT_LP) lpData.getSolver().exportModel("completeSEFCE_mp.lp");
            startTime = threadBean.getCurrentThreadCpuTime();
            System.out.println("Solving method : " + lpData.getSolver().getParam(IloCplex.IntParam.RootAlg));
            lpData.getSolver().solve();
            overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
            System.out.println("LP status: " + lpData.getSolver().getStatus());
            if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                gameValue = lpData.getSolver().getObjValue();

                System.out.println("-----------------------");
                System.out.println("LP value: " + gameValue);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return  gameValue;
    }



    public Double getResultForPlayer(Player leader) {
        return gameValue;
    }

    @Override
    public Map<Sequence, Double> getResultStrategiesForPlayer(Player player) {
        return null;
    }


    public long getOverallConstraintGenerationTime() {
        return overallConstraintGenerationTime;
    }



    public long getOverallConstraintLPSolvingTime() {
        return overallConstraintLPSolvingTime;
    }


}

