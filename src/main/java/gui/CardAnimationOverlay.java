package gui;

import javafx.animation.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import model.cards.Card;

import java.util.concurrent.Callable;

/**
 * The {@link CardAnimationOverlay} class is a pane that is used to store
 * temporary animation objects
 */
public class CardAnimationOverlay extends Pane {
  private GameGUIController controller;

  public CardAnimationOverlay(GameGUIController controller) {
    this.controller = controller;
    AnchorPane.setTopAnchor(this, 0.0);
    AnchorPane.setRightAnchor(this, 0.0);
    AnchorPane.setBottomAnchor(this, 0.0);
    AnchorPane.setLeftAnchor(this, 0.0);

    setPickOnBounds(false);
  }

  /**
   * Plays a card move animation
   *
   * @param targetX  the target x position
   * @param targetY  the target y position
   * @param rotated  marks if the card is supposedly rotated
   * @param callable a callable to be run when the animation plays
   * @param handler  a handler to be run when the animation finishes playing
   */
  void playCardMoveAnimation(CardPane cardPane, double targetX, double targetY, boolean rotated,
                             Callable<Void> callable,
                             EventHandler<ActionEvent> handler
  ) {
    getChildren().add(cardPane);
    Animation animation = generateCardMoveAnimation(cardPane, targetX, targetY, rotated);
    dispatchCardAnimation(animation, callable, handler);
  }

  /**
   * Plays a card move animation
   *
   * @param targetX  the target x position
   * @param targetY  the target y position
   * @param callable a callable to be run when the animation plays
   * @param handler  a handler to be run when the animation finishes playing
   */
  void playCardMoveAnimation(CardPane cardPane, double targetX, double targetY,
                             Callable<Void> callable,
                             EventHandler<ActionEvent> handler
  ) {
    playCardMoveAnimation(cardPane, targetX, targetY, false, callable, handler);
  }

  private Animation generateCardMoveAnimation(CardPane cardPane, double targetX, double targetY, boolean rotated) {
    ParallelTransition p = new ParallelTransition();
    TranslateTransition t = new TranslateTransition();
    t.setToX(targetX);
    t.setToY(targetY);
    t.setDuration(Duration.millis(500));
    t.setInterpolator(Interpolator.EASE_BOTH);
    t.setNode(cardPane);
    p.getChildren().add(t);

    if (rotated) {
      RotateTransition r = new RotateTransition();
      r.setByAngle(180);
      r.setDuration(Duration.millis(500));
      r.setInterpolator(Interpolator.EASE_BOTH);
      r.setAxis(new Point3D(0, 0, 1));
      r.setNode(cardPane);
      p.getChildren().add(r);
    }

    FadeTransition t2 = new FadeTransition();
    t2.setFromValue(1.0);
    t2.setToValue(0.0);
    t2.setDuration(Duration.millis(150));
    t2.setInterpolator(Interpolator.EASE_BOTH);
    t2.setNode(cardPane);

    return new SequentialTransition(p, t2);
  }

  private void dispatchCardAnimation(Animation animation,
                                     Callable<Void> callable,
                                     EventHandler<ActionEvent> handler) {
    animation.setOnFinished(e -> {
      getChildren().clear();
      if (handler != null) handler.handle(e);
    });
    if (callable != null) {
      try {
        callable.call();
      } catch (Exception ignored) {}
    }
    animation.play();
  }
}
