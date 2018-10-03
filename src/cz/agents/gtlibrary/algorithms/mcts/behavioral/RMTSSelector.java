package cz.agents.gtlibrary.algorithms.mcts.behavioral;

import cz.agents.gtlibrary.algorithms.cfr.br.responses.ImmediateActionOutcomeProvider;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.RMBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.RMSelector;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;
import org.apache.commons.math3.random.MersenneTwister;

import java.util.List;

/**
 * Created by Jakub Cerny on 04/09/2018.
 */
public class RMTSSelector extends RMSelector {

    protected MersenneTwister rand;

    public RMTSSelector(List<Action> actions, RMBackPropFactory fact) {
        super(actions, fact);
        rand = new MersenneTwister();
    }

    public RMTSSelector(int N, RMBackPropFactory fact) {
        super(N, fact);
        rand = new MersenneTwister();
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
    public int select(){

        int player = actions.get(0).getInformationSet().getPlayer().getId();

        if(player == 0) {

            updateProb();
            //d_t=fact.gamma/Math.sqrt(t);//does not work
            double rand = fact.random.nextDouble();
            for (int i = 0; i < p.length; i++) {
                double pa = (1 - d_t) * p[i] + d_t / p.length;

                if (rand > pa) {
                    rand -= pa;
                } else {
                    return i;
                }
            }
            assert false;
            return -1;
        }
        else{

            double bestW = Double.NEGATIVE_INFINITY;
            int bestA = -1;

            double w = 0;
            double theta = 0;

            GameState state = actions.get(0).getInformationSet().getAllStates().iterator().next();

            // get current sequences of actions for both players
            Sequence playerSequence = state.getSequenceForPlayerToMove();
            Sequence opponentSequence = state.getSequenceFor(((RMTSBackPropFactory)fact).opponent);

            // for each node
            int idx = 0;
            for(Action a : actions){
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
                /* too slow */
//                beta = new BetaDistribution(si + 1, fi + 1);
//                theta = beta.inverseCumulativeProbability(Math.random());
                theta = sampleBeta(si + 1, fi + 1);

                // calculate W
                w = ((ImmediateActionOutcomeProvider)a).getImmediateReward() * theta
                        - ((ImmediateActionOutcomeProvider)a).getImmediateCost();

                if (w > bestW){
                    bestW = w;
                    bestA = idx;
                }
                idx++;
            }
            return bestA;
        }
    }


}
