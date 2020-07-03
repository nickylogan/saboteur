package ai.impl.proposed;

import model.*;
import model.cards.*;

class MoveValidator {
  private final GameLogicController game;
  private final Player player;

  MoveValidator(GameLogicController game, Player player) {
    this.game = game;
    this.player = player;
  }

  boolean isLegal(Move move, Card card) {
    return switch (move.type()) {
      case PLAY_PATH ->
          isLegalPathMove(move, (PathCard) card);
      case PLAY_PLAYER ->
          isLegalPlayerActionMove(move, (PlayerActionCard) card);
      case PLAY_ROCKFALL ->
          isLegalRockfallMove(move);
      default ->
          true;
    };
  }

  private boolean isLegalPathMove(Move move, PathCard card) {
    if (player.isSabotaged()) {
      return false;
    }
    card.setRotated(move.args()[2] == 1);
    return game.board().isCardPlaceableAt(card, move.args()[0], move.args()[1]);
  }

  private boolean isLegalPlayerActionMove(Move move, PlayerActionCard card) {
    int target = move.args()[0];
    if (target < 0 || target >= game.numPlayers()) {
      return false;
    }

    if (card.type() == Card.Type.BLOCK) {
      return target != player.index() && game.playerAt(target).isSabotageable(card.effects()[0]);
    }

    if (card.type() == Card.Type.REPAIR) {
      return game.playerAt(target).isRepairable(card.effects());
    }

    return false;
  }

  private boolean isLegalRockfallMove(Move move) {
    return game.board().isDestroyable(move.args()[0], move.args()[1]);
  }
}
