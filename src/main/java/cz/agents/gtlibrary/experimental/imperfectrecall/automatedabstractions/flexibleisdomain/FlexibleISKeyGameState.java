package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.flexibleisdomain;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;

import java.util.Map;

public class FlexibleISKeyGameState extends GameStateImpl {
    private GameStateImpl wrappedState;
    private Map<GameState, ISKey> isKeys;

    public FlexibleISKeyGameState(GameStateImpl gameState, Map<GameState, ISKey> isKeys) {
        super(gameState.getAllPlayers());
        wrappedState = gameState;
        this.isKeys = isKeys;
    }

    public FlexibleISKeyGameState(FlexibleISKeyGameState gameState) {
        super(gameState);
        wrappedState = (GameStateImpl) gameState.getWrappedState().copy();
        this.isKeys = gameState.isKeys;
    }

    @Override
    public void performActionModifyingThisState(Action action) {
        super.performActionModifyingThisState(action);
        wrappedState.performActionModifyingThisState(((FlexibleISAction)action).getWrappedAction());
    }

//    @Override
//    public void performActionModifyingThisState(Action action) {
//        try {
//            if (wrappedState instanceof RandomAbstractionGameState) {
//                GameState origState = ((RandomAbstractionGameState) wrappedState).getWrappedGameState();
//                Action origAction = ((RandomAbstractionAction) action).wrappedActions.get(origState.getISKeyForPlayerToMove());
//                Method m = GameStateImpl.class.getDeclaredMethod("updateNatureProbabilityFor", Action.class);
//
//                m.setAccessible(true);
//                m.invoke(origState, origAction);
//                m = GameStateImpl.class.getDeclaredMethod("addActionToHistory", Action.class, Player.class);
//                m.setAccessible(true);
//                m.invoke(origState, origAction, getPlayerToMove());
//                origAction.perform(origState);
//            } else {
//                Method m = GameStateImpl.class.getDeclaredMethod("updateNatureProbabilityFor", Action.class);
//                m.setAccessible(true);
//                m.invoke(wrappedState, action);
//                m = GameStateImpl.class.getDeclaredMethod("addActionToHistory", Action.class, Player.class);
//                m.setAccessible(true);
//                m.invoke(wrappedState, action, getPlayerToMove());
//                action.perform(wrappedState);
//            }
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return wrappedState.getProbabilityOfNatureFor(((FlexibleISAction)action).getWrappedAction());
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        return wrappedState.getExactProbabilityOfNatureFor(((FlexibleISAction)action).getWrappedAction());
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        return isKeys.computeIfAbsent(this, s -> wrappedState.getISKeyForPlayerToMove());
    }

    @Override
    public Player getPlayerToMove() {
        return wrappedState.getPlayerToMove();
    }

    @Override
    public GameState copy() {
        return new FlexibleISKeyGameState(this);
    }

    @Override
    public double[] getUtilities() {
        return wrappedState.getUtilities();
    }

    @Override
    public boolean isGameEnd() {
        return wrappedState.isGameEnd();
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return wrappedState.isPlayerToMoveNature();
    }

    @Override
    public int hashCode() {
        return wrappedState.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof FlexibleISKeyGameState))
            return false;
        return wrappedState.equals(((FlexibleISKeyGameState) object).getWrappedState());
    }

    public GameState getWrappedState() {
        return wrappedState;
    }

    @Override
    public String toString() {
        return wrappedState.toString();
    }
}
