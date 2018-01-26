package cz.agents.gtlibrary.algorithms.stackelberg.oracle;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.LeaderGenerationConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.GadgetAction;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.GadgetSefceLPWithoutMiddleState;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.*;

/**
 * Created by Jakub Cerny on 14/12/2017.
 */
public class GadgetOracle2pSumForbiddingLP extends GadgetSefceLPWithoutMiddleState {

    protected int bnbBranchingCount = 0;
    protected long brokenStrategyIdentificationTime = 0;
    protected long solvingForSefceTime = 0;

    public GadgetOracle2pSumForbiddingLP(Player leader, GameInfo info) {
        super(leader, info);
        eps = 1e-6;
    }

    @Override
    public double calculateLeaderStrategies(AlgorithmConfig algConfig, Expander expander) {
        this.algConfig = (LeaderGenerationConfig)algConfig;
        this.expander = expander;

        findInitialRestrictedGame();

        Pair<Map<Sequence, Double>, Double> solution = solve(-info.getMaxUtility(), info.getMaxUtility());
        if (MAKE_GADGET_STATS) writeGadgetStats();
        gameValue = solution.getRight();
        System.out.println("final number of gadgets created: " + (gadgetsDismissed+gadgetRootsSequences.size()));
        return gameValue;
    }

    public Map<Object, Integer> getVariableIndices(){return  lpTable.getVariableIndices();}
    public Map<Object, Map<Object, Double>> getConstraints(){return lpTable.getConstraints();}

    protected Pair<Map<Sequence, Double>, Double> solve(double lowerBound, double upperBound) {
        try {
            long startTime = threadBean.getCurrentThreadCpuTime();
            System.out.println("-----------------------");
            System.out.println("Invocation: "+ bnbBranchingCount);

            System.out.println("LP size: " + lpTable.getLPSize());

//            updateLowerBound(lowerBound);
            System.out.printf("Watching...");
            lpTable.watchAllPrimalVariables();
            System.out.println("done.");
            LPData lpData = lpTable.toCplex();

            if (TUNE_LP) tuneSolver(lpData);

            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
//            lpData.getSolver().exportModel("SSEIter.lp");
            startTime = threadBean.getCurrentThreadCpuTime();
            System.out.printf("Solving...");
            lpData.getSolver().solve();
            System.out.println("done.");
            bnbBranchingCount++;
            overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
//            printBinaryVariableValues(lpData);
            if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                double value = lpData.getSolver().getObjValue();

                System.out.println("LP reward: " + value + " lower bound: " + lowerBound);

                startTime = threadBean.getCurrentThreadCpuTime();
                System.out.printf("Finding current sefce....");
                lpData = solveForSefce(lpData);
                value = lpData.getSolver().getObjValue();
                System.out.println("done.");
                System.out.println("LP reward after fixing sefce: " + value + " lower bound: " + lowerBound);
                solvingForSefceTime += threadBean.getCurrentThreadCpuTime() - startTime;


                startTime = threadBean.getCurrentThreadCpuTime();
                System.out.printf("Finding broken strategy...");
                Map<InformationSet, Map<Sequence, Double>> followerBehavStrat = getBehavioralStrategy(lpData, follower);
                Iterable<Sequence> brokenStrategyCauses = getBrokenStrategyCauses(followerBehavStrat, lpData);
                System.out.println("done.");
//                System.out.println("BSC: " + brokenStrategyCauses);
                brokenStrategyIdentificationTime += threadBean.getCurrentThreadCpuTime() - startTime;
                final boolean OUTPUT_STRATEGY = false;
                if (OUTPUT_STRATEGY) {
                    Map<InformationSet, Map<Sequence, Double>> behavStrat = getBehavioralStrategy(lpData, leader);
                    for (InformationSet set : behavStrat.keySet()) {
                        System.out.println(set == null || set.getISKey() == null ? "Null set" : set.getISKey());
                        for (Sequence seq : behavStrat.get(set).keySet()) {
                            System.out.println("\t" + seq + " : \t" + behavStrat.get(set).get(seq));
                        }
                    }
                }


                if (brokenStrategyCauses == null) {
                    lpData = null;
                    return new Pair<Map<Sequence, Double>, Double>(new HashMap<>(), value);

                } else {
                    if (value <= lowerBound + eps) {
                        System.out.println("***********lower bound " + lowerBound + " not exceeded, cutting***********");
                        return new Pair<Map<Sequence, Double>, Double>(new HashMap<>(), Double.NEGATIVE_INFINITY);
                    }
                    return handleBrokenStrategyCause(lowerBound, upperBound, lpData, value, brokenStrategyCauses);
                }
            } else {
                System.err.println(lpData.getSolver().getStatus());
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return new Pair<Map<Sequence, Double>, Double>(new HashMap<>(), Double.NEGATIVE_INFINITY);
    }

    private LPData solveForSefce(LPData lpData) {
        boolean reachableGadget = findReachableGadgetRoots(getThreats(lpData));
        Pair<LPData,Map<Sequence, Double>> solution = null;

        while (reachableGadget){
            iteration++;
            solution = getCurrentSolution();
            reachableGadget = findReachableGadgetRoots(solution.getRight());
        }
        return solution == null ? lpData : solution.getLeft();
    }

    protected Pair<LPData,Map<Sequence, Double>> getCurrentSolution(){
        try {
            long startTime = threadBean.getCurrentThreadCpuTime();

            lpTable.watchAllPrimalVariables();
            LPData lpData = lpTable.toCplex();

//            System.out.println("LP Solving method: " + LPTable.CPLEXALG);

            if (TUNE_LP) tuneSolver(lpData);

            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
            if (EXPORT_LP) lpData.getSolver().exportModel("Gadget2pSEFCE.lp");
            startTime = threadBean.getCurrentThreadCpuTime();
            if (PRINT_PROGRESS || PRINT_SOLVING) System.out.printf("Solving...");
            lpData.getSolver().solve();
            if (PRINT_PROGRESS || PRINT_SOLVING) System.out.println("done");
            overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
            if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                gameValue = lpData.getSolver().getObjValue();
//                System.out.println("-----------------------");
                System.out.println("SEFCE reward: " + gameValue);

                // compute RPs
                Map<Sequence, Double> leaderRealPlan = null;
                leaderRealPlan = getThreats(lpData);
//                leaderRealPlan = behavioralToRealizationPlan(getBehavioralStrategy(lpData, leader));

                if (leaderRealPlan != null)
                    return new Pair<>(lpData,leaderRealPlan);
                return new Pair<>(null, null);
            } else {
                System.err.println(lpData.getSolver().getStatus());
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return new Pair<>(null, null);
    }

    protected Map<InformationSet, Map<Sequence, Double>> getBehavioralStrategy(LPData lpData, Player player) {
        Map<InformationSet, Map<Sequence, Double>> strategy = new HashMap<>();

        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Pair) {
                Pair varKey = (Pair) entry.getKey();

                if (varKey.getLeft() instanceof Sequence && varKey.getRight() instanceof Sequence) {
                    Sequence playerSequence = player.equals(leader) ? (Sequence) varKey.getLeft() : (Sequence) varKey.getRight();
                    Map<Sequence, Double> isStrategy = strategy.get(playerSequence.getLastInformationSet());
                    Double currentValue = getValueFromCplex(lpData, entry);

                    if (currentValue > eps)
                        if (isSequenceFrom(player.equals(leader) ? (Sequence) varKey.getRight() : (Sequence) varKey.getLeft(), playerSequence.getLastInformationSet()))
                            if (isStrategy == null) {
                                if (currentValue > eps) {
                                    isStrategy = new HashMap<>();
                                    double behavioralStrat = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                    isStrategy.put(playerSequence, behavioralStrat);
                                    strategy.put(playerSequence.getLastInformationSet(), isStrategy);
                                }
                            } else {
                                double behavioralStrategy = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                if (behavioralStrategy > eps) {
                                    isStrategy.put(playerSequence, behavioralStrategy);
                                }
                            }
                }
            }
        }
        return strategy;
    }

    protected double getBehavioralStrategy(LPData lpData, Pair varKey, Sequence playerSequence, Double currentValue) {
        double behavioralStrat = currentValue;

        if (!playerSequence.isEmpty()) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(playerSequence);

            sequenceCopy.removeLast();
            double previousValue = getValueFromCplex(lpData, playerSequence.getPlayer().equals(leader) ? new Pair(sequenceCopy, varKey.getRight()) : new Pair<>(varKey.getLeft(), sequenceCopy));

            if (previousValue == 0) {
                Sequence opponentSequence = new ArrayListSequenceImpl((Sequence) (playerSequence.getPlayer().equals(leader) ? varKey.getRight() : varKey.getLeft()));

                if (!opponentSequence.isEmpty()) {
                    opponentSequence.removeLast();
                    previousValue = getValueFromCplex(lpData, playerSequence.getPlayer().equals(leader) ? new Pair(sequenceCopy, opponentSequence) : new Pair<>(opponentSequence, sequenceCopy));
                }
            }
            behavioralStrat /= previousValue;
        }
        return behavioralStrat;
    }

    protected boolean isSequenceFrom(Sequence sequence, InformationSet informationSet) {
        if (informationSet == null)
            return sequence.isEmpty();
        assert !sequence.getPlayer().equals(informationSet.getPlayer());
        for (GameState gameState : informationSet.getAllStates()) {
            if (gameState.getSequenceFor(sequence.getPlayer()).equals(sequence))
                return true;
        }
        return false;
    }

    protected Double getValueFromCplex(LPData lpData, Map.Entry<Object, IloNumVar> entry) {
        Double currentValue = null;

        try {
            currentValue = lpData.getSolver().getValue(entry.getValue());
        } catch (IloException e) {
            System.out.println(((Pair)entry.getKey()).getLeft());
            e.printStackTrace();
        }
        return currentValue;
    }

    protected Double getValueFromCplex(LPData lpData, Object varKey) {
        Double currentValue = null;

        try {
//            System.out.println(varKey + " #:" + varKey.hashCode());
//            System.out.println(lpData.getWatchedPrimalVariables().containsKey(varKey));
            currentValue = lpData.getSolver().getValue(lpData.getWatchedPrimalVariables().get(varKey));
        } catch (IloException e) {
            System.out.println(varKey);
            e.printStackTrace();
        }
        return currentValue;
    }

    protected Iterable<Sequence> getBrokenStrategyCauses(Map<InformationSet, Map<Sequence, Double>> strategy, LPData lpData) {
        Map<Sequence, Double> shallowestBrokenStrategyCause = null;

        for (Map.Entry<InformationSet, Map<Sequence, Double>> isStrategy : strategy.entrySet()) {
            if (isStrategy.getValue().size() > 1) {
                if (shallowestBrokenStrategyCause == null) {
                    shallowestBrokenStrategyCause = new HashMap<>(isStrategy.getValue());
                } else {
                    Sequence candidate = isStrategy.getValue().keySet().iterator().next();
                    Sequence bestSoFar = shallowestBrokenStrategyCause.keySet().iterator().next();

                    if (candidate.size() < bestSoFar.size()) {
                        shallowestBrokenStrategyCause = new HashMap<>(isStrategy.getValue());
                    }
                }
            }
        }
        if (shallowestBrokenStrategyCause == null)
            return null;
        return sort(shallowestBrokenStrategyCause, shallowestBrokenStrategyCause.keySet());
    }

    protected Iterable<Sequence> sort(final Map<Sequence, Double> shallowestBrokenStrategyCause, final Collection<Sequence> allSeq) {
        List<Sequence> list = new ArrayList<>(allSeq);

        Collections.sort(list, new Comparator<Sequence>() {
            @Override
            public int compare(Sequence o1, Sequence o2) {
                return Double.compare(shallowestBrokenStrategyCause.get(o1), shallowestBrokenStrategyCause.get(o2));
            }
        });
        return list;
    }

    protected Pair<Map<Sequence, Double>, Double> handleBrokenStrategyCause(double lowerBound, double upperBound, LPData lpData, double value, Iterable<Sequence> brokenStrategyCauses) {
        Pair<Map<Sequence, Double>, Double> currentBest = new Pair<>(null, Double.NEGATIVE_INFINITY);

        for (Sequence brokenStrategyCause : brokenStrategyCauses) {
            restrictFollowerPlay(brokenStrategyCause, brokenStrategyCauses, lpData);
            Pair<Map<Sequence, Double>, Double> result = solve(getLowerBound(lowerBound, currentBest), upperBound);

            if (result.getRight() > currentBest.getRight()) {
                currentBest = result;
                if (currentBest.getRight() >= value - eps) {
                    System.out.println("----------------currentBest " + currentBest.getRight() + " reached parent reward " + value + "----------------");
                    return currentBest;
                }
            }
            removeRestriction(brokenStrategyCause, brokenStrategyCauses, lpData);
        }
        return currentBest;
    }

    protected double getLowerBound(double lowerBound, Pair<Map<Sequence, Double>, Double> currentBest) {
        return Math.max(lowerBound, currentBest.getRight());
    }


    protected void restrictFollowerPlay(Sequence brokenStrategyCause, Iterable<Sequence> brokenStrategyCauses, LPData lpData) {
        System.out.println(brokenStrategyCause + " fixed to zero");
        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Pair) {
                if (((Pair) varKey).getLeft() instanceof Sequence && ((Pair) varKey).getRight() instanceof Sequence) {
                    Pair<Sequence, Sequence> p = (Pair<Sequence, Sequence>) varKey;

                    if (p.getRight().equals(brokenStrategyCause)) {
                        Pair<String, Pair<Sequence, Sequence>> eqKey = new Pair<>("restr", p);

                        lpTable.setConstraint(eqKey, p, 1);
                        lpTable.setConstraintType(eqKey, 1);

                        if (!p.getLeft().isEmpty() && p.getLeft().getLast() instanceof GadgetAction)
                            eqsToDelete.get(((GadgetAction)p.getLeft().getLast()).getState()).add(eqKey);
                    }
                }
            }
        }
    }

    protected void removeRestriction(Sequence brokenStrategyCause, Iterable<Sequence> brokenStrategyCauses, LPData lpData) {
        System.out.println(brokenStrategyCause + " released");
        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Pair) {
                if (((Pair) varKey).getLeft() instanceof Sequence && ((Pair) varKey).getRight() instanceof Sequence) {
                    Pair<Sequence, Sequence> p = (Pair<Sequence, Sequence>) varKey;

                    if (p.getRight().equals(brokenStrategyCause)) {
                        Pair<String, Pair<Sequence, Sequence>> eqKey = new Pair<>("restr", p);
                        lpTable.deleteConstraintWithoutVars(eqKey);
                    }
                }
            }
        }
    }

}
