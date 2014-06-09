package cz.agents.gtlibrary.interfaces;

import java.io.Serializable;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.utils.Pair;

public interface GameState extends Serializable {
	public Player[] getAllPlayers();

	public Player getPlayerToMove();

	public GameState performAction(Action action);

	public History getHistory();

	public Sequence getSequenceFor(Player player);

	public Sequence getSequenceForPlayerToMove();

	public GameState copy();

	public double[] getUtilities();

    public Rational[] getExactUtilities();

	public double getProbabilityOfNatureFor(Action action);

    public Rational getExactProbabilityOfNatureFor(Action action);

	public boolean isGameEnd();

	public boolean isPlayerToMoveNature();

	public double getNatureProbability();

    public Rational getExactNatureProbability();

	public void performActionModifyingThisState(Action action);

	public void reverseAction();

	public Pair<Integer, Sequence> getISKeyForPlayerToMove();

	public boolean checkConsistency(Action action);

    public double[] evaluate();
}
