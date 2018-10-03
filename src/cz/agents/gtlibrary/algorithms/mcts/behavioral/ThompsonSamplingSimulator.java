package cz.agents.gtlibrary.algorithms.mcts.behavioral;

import cz.agents.gtlibrary.algorithms.cfr.br.responses.ImmediateActionOutcomeProvider;
import cz.agents.gtlibrary.algorithms.mcts.DefaultSimulator;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.random.MersenneTwister;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Jakub Cerny on 03/09/2018.
 */
public class ThompsonSamplingSimulator extends DefaultSimulator {

    protected int tsPlayer;
    protected MersenneTwister rand;
    protected Player opponent;

    public ThompsonSamplingSimulator(Expander expander, int tsPlayer, Player opponent) {
        super(expander);
        this.tsPlayer = tsPlayer;
        this.opponent = opponent;
        this.rand = new MersenneTwister();
    }

    protected double sampleBeta(int a, int b){
        if(a == 1 && b == 1) return sampleJohnk();
        double Ga = sampleGamma(a);     //rk_standard_gamma(state, a);
        double Gb = sampleGamma(b);     //rk_standard_gamma(state, b);
        return Ga/(Ga + Gb);
    }

    protected double sampleJohnk(){
        double U, V, X, Y;
        /* Use Johnk's algorithm */

        while (true)
        {
            X = rand.nextDouble(); //rk_double(state);
            Y = rand.nextDouble();  //rk_double(state);

            if ((X + Y) <= 1.0)
            {
                if (X +Y > 0)
                {
                    return X / (X + Y);
                }
                else
                {
                    double logX = Math.log(X) ;
                    double logY = Math.log(X) ;
                    double logM = logX > logY ? logX : logY;
                    logX -= logM;
                    logY -= logM;

                    return Math.exp(logX - Math.log(Math.exp(logX) + Math.exp(logY)));
                }
            }
        }
    }

    protected double sampleGamma(double shape){
        double b = shape - 1.0/3.0;
        double c = 1.0/Math.sqrt(9*b);
        for (;;)
        {
            double V = 0.0;
            double X = 0.0;
            do
            {
                X = rand.nextGaussian(); //rk_gauss(state);
                V = 1.0 + c*X;
            } while (V <= 0.0);

            V = V*V*V;
            double U = rand.nextDouble();   //rk_double(state);
            if (U < 1.0 - 0.0331*(X*X)*(X*X)) return (b*V);
            if (Math.log(U) < 0.5*X*X + b*(1. - V + Math.log(V))) return (b*V);
        }
    }

    @Override
    protected Action getActionForRegularPlayer(GameStateImpl state) {

//        List<Action> possibleActions = expander.getActions(new MCTSInformationSet(state));
        List<Action> possibleActions = expander.getActions(state);

        if (state.getPlayerToMove().getId() != tsPlayer) {

            return possibleActions.get(rnd.nextInt(possibleActions.size()));
        }
        else {
            double bestW = Double.NEGATIVE_INFINITY;
            Action bestA = null;

            double w = 0;
            double theta = 0;

            // get current sequences of actions for both players
            Sequence playerSequence = state.getSequenceForPlayerToMove();
            Sequence opponentSequence = state.getSequenceFor(opponent);

            // for each node
            for(Action a : possibleActions){
                // calculate Si and Fi for each node
                int si = 0;
                int fi = 0;
                for(int depth = 0; depth < playerSequence.size(); depth++){
                    if(((ImmediateActionOutcomeProvider)playerSequence.get(depth))
                            .getImmediateRewardForAction(opponentSequence.get(depth)) > 0)
                        si += 1;
                    else
                        fi += 1;
                }
                // sample theta
//                beta = new BetaDistribution(si + 1, fi + 1);
//                theta = beta.inverseCumulativeProbability(Math.random());
                theta = sampleBeta(si + 1, fi + 1);

//                if(si > 10 && fi > 30) {
//                    BetaDistribution beta = new BetaDistribution(si + 1, fi + 1);
//                    int[] b1 = new int[10];
//                    int[] b2 = new int[10];
//
//                    double d1, d2;
//
//                    for (int i = 0; i < 1000000; i++) {
//                        d1 = beta.inverseCumulativeProbability(Math.random());
//                        d2 = sampleBeta(si + 1, fi + 1);
//                        if (d1 < 1.0 && d2 < 1.0) {
//                            b1[(int) Math.floor(10 * d1)]++;
//                            b2[(int) Math.floor(10 * d2)]++;
//                        }
//                    }
//
//                    System.out.println((si + 1) + " " + (fi + 1));
//                    System.out.println(Arrays.toString(b1));
//                    System.out.println(Arrays.toString(b2));
//                    System.out.println("****************");
//                }

                // calculate W
                w = ((ImmediateActionOutcomeProvider)a).getImmediateReward() * theta
                        - ((ImmediateActionOutcomeProvider)a).getImmediateCost();

                if (w > bestW){
                    bestW = w;
                    bestA = a;
                }
            }
            return bestA;
        }

    }
}
