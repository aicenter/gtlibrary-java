package cz.agents.gtlibrary.algorithms.crswfabstraction.testdomain;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.Arrays;

public enum GameEnum {    //left
    L4S1(true, new int[]{1, 2}),
    L4S2(true, new int[]{2, 3}),
    L4S3(true, new int[]{3 ,4}),
    L4S4(true, new int[]{4, 5}),
    L4S5(true, new int[]{1, 3}),
    L4S6(true, new int[]{2, 4}),
    L4S7(true, new int[]{3, 5}),
    L4S8(true, new int[]{7, 3}),
    //right
    L4S9(true, new int[]{2, 1}),
    L4S10(true, new int[]{3, 2}),
    L4S11(true, new int[]{4, 3}),
    L4S12(true, new int[]{5, 4}),
    L4S13(true, new int[]{3, 1}),
    L4S14(true, new int[]{4, 2}),
    L4S15(true, new int[]{5, 3}),
    L4S16(true, new int[]{3, 7}),

    //left
    L3S1(TestInfo.PL2, 2, new GameEnum[]{GameEnum.L4S1, GameEnum.L4S5}, 1),
    L3S2(TestInfo.PL2, 2, new GameEnum[]{GameEnum.L4S2, GameEnum.L4S6}, 2),
    L3S3(TestInfo.PL2, 2, new GameEnum[]{GameEnum.L4S3, GameEnum.L4S7}, 3),
    L3S4(TestInfo.PL2, 2, new GameEnum[]{GameEnum.L4S4, GameEnum.L4S8}, 4),
    //right
    L3S5(TestInfo.PL2, 2, new GameEnum[]{GameEnum.L4S9, GameEnum.L4S13}, 5),
    L3S6(TestInfo.PL2, 2, new GameEnum[]{GameEnum.L4S10, GameEnum.L4S14}, 6),
    L3S7(TestInfo.PL2, 2, new GameEnum[]{GameEnum.L4S11, GameEnum.L4S15}, 7),
    L3S8(TestInfo.PL2, 2, new GameEnum[]{GameEnum.L4S12, GameEnum.L4S16}, 8),

    //left
    L2S1(TestInfo.PL2, 2, new GameEnum[]{GameEnum.L3S1, GameEnum.L3S2}, 9),
    L2S2(TestInfo.PL2, 2, new GameEnum[]{GameEnum.L3S3, GameEnum.L3S4}, 9),
    //right
    L2S3(TestInfo.PL2, 2, new GameEnum[]{GameEnum.L3S5, GameEnum.L3S6}, 10),
    L2S4(TestInfo.PL2, 2, new GameEnum[]{GameEnum.L3S7, GameEnum.L3S8}, 10),

    L1S1(TestInfo.NATURE, 4, new GameEnum[]{GameEnum.L2S1, GameEnum.L2S2, GameEnum.L2S3, GameEnum.L2S4}, 11, new int[]{3, 1, 2, 3}),


    ROOT(TestInfo.PL1, 1, new GameEnum[]{GameEnum.L1S1}, 12);

    protected static final Player[] players = {TestInfo.PL1, TestInfo.PL2, TestInfo.NATURE};

    protected Player player;
    protected int actionNo;
    protected GameEnum[] successors;
    protected int informationSet;
    protected Rational[] probabilities;
    protected boolean isLeaf;
    protected int[] utilities;

    GameEnum(Player player, int actionNo, GameEnum[] successors, int informationSet, int[] probabilities, boolean isLeaf, int[] utilities) {
        this.player = player;
        this.actionNo = actionNo;
        this.successors = successors;
        this.informationSet = informationSet;

        if (probabilities == null) this.probabilities = null;
        else {
            int denominanor = Arrays.stream(probabilities).reduce(0, (a, b) -> a + b);
            this.probabilities = new Rational[probabilities.length];
            for (int i = 0; i < probabilities.length; i++)
                this.probabilities[i] = new Rational(probabilities[i], denominanor);
        }

        this.isLeaf = isLeaf;
        this.utilities = utilities;
    }

    GameEnum(boolean isLeaf, int[] utilities) {
        this(TestInfo.PL1, 0, null, -1, null, isLeaf, utilities);
    }

    GameEnum(Player player, int actionNo, GameEnum[] successors, int informationSet, int[] probabilities) {
        this(player, actionNo, successors, informationSet, probabilities, false, null);
    }

    GameEnum(Player player, int actionNo, GameEnum[] successors, int informationSet) {
        this(player, actionNo, successors, informationSet, null);
    }
}
