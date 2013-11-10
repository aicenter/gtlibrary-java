package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.unprunning;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.GeneralDoubleOracle;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.interfaces.*;

import java.util.HashSet;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 4/5/13
 * Time: 3:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class UnprunningDoubleOracle extends GeneralDoubleOracle {

    public static void main(String[] args) {
        // currently only for Poker betting/raising values
        long start = System.currentTimeMillis();
        int maxParam = GPGameInfo.MAX_RAISES_IN_ROW;
//        int maxParam = RandomGameInfo.MAX_DEPTH;
        Map<Player, Map<Sequence, Double>> init = null;
//        GameState rootState = new RandomGameState();

        for (int curParam = 1; curParam <= maxParam;) {
            GPGameInfo.MAX_RAISES_IN_ROW = curParam;
            GameState rootState = new GenericPokerGameState();
            GameInfo gameInfo = new GPGameInfo();
            DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
            Expander<DoubleOracleInformationSet> expander = new GenericPokerExpander<DoubleOracleInformationSet>(algConfig);

//            RandomGameInfo.MAX_DEPTH = curParam;
//            GameInfo gameInfo = new RandomGameInfo();
//            DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
//            Expander<DoubleOracleInformationSet> expander = new RandomGameExpander<DoubleOracleInformationSet>(algConfig);

            UnprunningDoubleOracle doefg = new UnprunningDoubleOracle(rootState, expander, gameInfo, algConfig);
            init = doefg.generate(init);

            for (Player p : init.keySet()) {
                for (Sequence s : new HashSet<Sequence>(init.get(p).keySet())) {
                    Double v = init.get(p).get(s);
                    if (v == 0) {
                        init.get(p).remove(s);
                    }
                }
            }

            if (curParam == maxParam) break;
            else curParam=maxParam;
        }
        System.out.println("Unprunning overall time: " + (System.currentTimeMillis() - start));
    }

    public UnprunningDoubleOracle(GameState rootState, Expander<DoubleOracleInformationSet> expander, GameInfo config, DoubleOracleConfig<DoubleOracleInformationSet> algConfig) {
        super(rootState, expander, config, algConfig);
    }

}
