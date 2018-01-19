package cz.agents.gtlibrary.domain.oshizumo;

/**
 * Created by Jakub Cerny on 02/11/2017.
 */
public class GenSumIIOshiZumoGameState extends IIOshiZumoGameState {

    @Override
    protected double[] getEndGameUtilities() {
        double p1CoinsUti = (double)p1Coins / OZGameInfo.startingCoins;
        double p2CoinsUti = (double)p2Coins / OZGameInfo.startingCoins;
        System.out.println(p1CoinsUti + " / " + p2CoinsUti);
        // almost binary
        if (OZGameInfo.BINARY_UTILITIES) {
            if (wrestlerLoc < OZGameInfo.locK)
                return new double[]{-1 + p1CoinsUti, 1 + p2CoinsUti, 0};
            else if (wrestlerLoc > OZGameInfo.locK)
                return new double[]{1 + p1CoinsUti, -1 + p2CoinsUti, 0};
            return new double[]{0 + p1CoinsUti, 0 + p2CoinsUti, 0};
        } else {
            return new double[] {wrestlerLoc - OZGameInfo.locK + p1CoinsUti, OZGameInfo.locK - wrestlerLoc + p2CoinsUti, 0};
        }
    }
}
