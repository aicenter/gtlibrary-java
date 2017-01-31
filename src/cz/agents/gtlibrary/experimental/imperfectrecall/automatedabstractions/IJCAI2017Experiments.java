package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions;

import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.randomabstraction.RandomAbstractionGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;

public class IJCAI2017Experiments {
    public static void main(String[] args) {
        if (args[0].equals("RG")) {
            RandomGameInfo.MAX_DEPTH = Integer.parseInt(args[1]);
            RandomGameInfo.MAX_BF = Integer.parseInt(args[2]);
            RandomGameInfo.seed = Long.parseLong(args[3]);
            RandomAbstractionGameInfo.JOIN_PROB = Double.parseDouble(args[5]);
            if (args[4].equals("IR"))
                IRFicticiousPlay.runBothIRRandomAbstractionGame();
            else
                IRFicticiousPlay.runCPRRBothIRRandomAbstractionGame();
        } else if (args[0].equals("GP")) {
            GPGameInfo.MAX_RAISES_IN_ROW = Integer.parseInt(args[1]);
            if (args[1].equals("IR"))
                IRFicticiousPlay.runIRGenericPoker();
            else
                IRFicticiousPlay.runGenericPoker();
        }
    }
}
