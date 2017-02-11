package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions;

import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.randomabstraction.RandomAbstractionGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;

public class IJCAI2017Experiments {
    public static void main(String[] args) {
        if (args[0].equals("RG")) {
            RandomGameInfo.MAX_DEPTH = Integer.parseInt(args[1]);
            RandomGameInfo.MIN_BF = Integer.parseInt(args[2]);
            RandomGameInfo.MAX_BF = Integer.parseInt(args[2]);
            RandomGameInfo.seed = Long.parseLong(args[3]);
            RandomAbstractionGameInfo.JOIN_PROB = 1;
            IRFictitiousPlay.EPS = Double.parseDouble(args[6]);
            if(args.length > 7)
                IRFictitiousPlay.CONVERGENCE_POWER = Double.parseDouble(args[7]);
            if (args[4].equals("IR"))
                IRFictitiousPlay.runCPRRConstantBothIRRandomAbstractionGame();
            else
                IRFictitiousPlay.runSimpleCPRRBothIRRandomAbstractionGame();
        } else if (args[0].equals("GP")) {
            GPGameInfo.MAX_RAISES_IN_ROW = Integer.parseInt(args[1]);
            GPGameInfo.MAX_DIFFERENT_BETS = Integer.parseInt(args[2]);
            GPGameInfo.MAX_DIFFERENT_RAISES = Integer.parseInt(args[3]);
            GPGameInfo.MAX_CARD_TYPES = Integer.parseInt(args[4]);
            GPGameInfo.MAX_CARD_OF_EACH_TYPE = Integer.parseInt(args[5]);
            IRFictitiousPlay.EPS = Double.parseDouble(args[7]);
            if(args.length > 8)
                IRFictitiousPlay.CONVERGENCE_POWER = Double.parseDouble(args[8]);
            if (args[6].equals("IR"))
                IRFictitiousPlay.runIRGenericPoker();
            else
                IRFictitiousPlay.runGenericPoker();
        }
    }
}
