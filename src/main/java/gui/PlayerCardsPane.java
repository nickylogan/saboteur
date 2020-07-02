package gui;

import javafx.animation.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import model.cards.Card;
import model.cards.PathCard;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

public class PlayerCardsPane extends GridPane implements Initializable {
  @FXML
  private Pane cardArea;
  @FXML
  private Label nameLabel;

  private final static double PADDING = 16;

  private GameGUIController controller;

  private int selectedIndex = -1;

  boolean interactive = false;

  private List<CardPane> panes;

  PlayerCardsPane(GameGUIController controller) {
    this.controller = controller;

    FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/player-cards-pane.fxml"));
    loader.setRoot(this);
    loader.setController(this);
    try {
      loader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AnchorPane.setBottomAnchor(this, 16.0);
    AnchorPane.setLeftAnchor(this, 332.0);
    nameLabel.setVisible(false);

    DropShadow shadow = new DropShadow();
    shadow.setBlurType(BlurType.GAUSSIAN);
    shadow.setColor(new Color(0, 0, 0, 0.5));
    shadow.setHeight(100);
    shadow.setWidth(100);
    shadow.setRadius(100);
    shadow.setSpread(0);

    setEffect(shadow);
    setPickOnBounds(false);
  }

  /**
   * Plays change hand animation
   *
   * @param name     the new player's name
   * @param newCards the new cards
   * @param callable a callable to be run when the animation plays
   * @param handler  a handler to be run when the animation finishes playing
   */
  void playChangeHandAnimation(String name, ArrayList<Card> newCards,
                               Callable<Void> callable,
                               EventHandler<ActionEvent> handler) {
    playHideAnimation(callable, e -> playShowAnimation(name, newCards, null, handler));
  }

  void playHideAnimation(Callable<Void> callable, EventHandler<ActionEvent> handler) {
    if (callable != null) try { callable.call(); } catch (Exception ignored) {}
    toggle(handler, false);
  }

  void playShowAnimation(String name, ArrayList<Card> newCards,
                         Callable<Void> callable,
                         EventHandler<ActionEvent> handler) {
    if (callable != null) try { callable.call(); } catch (Exception ignored) {}
    nameLabel.setVisible(true);
    nameLabel.setText(name);
    renderCards(newCards);
    toggle(handler, true);
  }

  /**
   * Plays change hand animation
   *
   * @param handIndex the hand index to be removed
   * @param newCard   the new card
   * @param callable  a callable to be run when the animation plays
   * @param handler   a handler to be run when the animation finishes playing
   */
  void playReplaceAnimation(int handIndex, Card newCard,
                            Callable<Void> callable,
                            EventHandler<ActionEvent> handler) {
    Animation replace = generateReplaceAnimation(handIndex, newCard, true);
    if (handler != null) {
      replace.setOnFinished(e -> {
        cardArea.getChildren().remove(handIndex);
        handler.handle(e);
      });
    }
    if (callable != null) try { callable.call(); } catch (Exception ignored) {}
    replace.play();
  }

  /**
   * Plays a card discard animation
   *
   * @param handIndex the card to be animated
   * @param newCard   the replacement card
   * @param callable  a callable to be run when the animation plays
   * @param handler   a handler to be run when the animation finishes playing
   */
  void playDiscardAnimation(
    int handIndex, Card newCard,
    Callable<Void> callable,
    EventHandler<ActionEvent> handler
  ) {
    SequentialTransition animation = new SequentialTransition();
    Animation discard = generateDiscardAnimation(handIndex);
    Animation replace = generateReplaceAnimation(handIndex, newCard, false);

    animation.getChildren().addAll(discard, replace);
    animation.setOnFinished(e -> {
      cardArea.getChildren().remove(handIndex);
      if (handler != null) handler.handle(e);
    });
    if (callable != null) try { callable.call(); } catch (Exception ignored) {}
    animation.play();
  }

  private Animation generateReplaceAnimation(int handIndex, Card newCard, boolean disappear) {
    ParallelTransition replace = new ParallelTransition();
    int len = cardArea.getChildren().size();
    if (disappear) cardArea.getChildren().get(handIndex).setOpacity(0);

    for (int i = handIndex + 1; i < len; ++i) {
      CardPane cardPane = (CardPane) cardArea.getChildren().get(i);
      TranslateTransition t = new TranslateTransition();
      t.setByX(-(PADDING + cardPane.getFitWidth()));
      t.setDuration(Duration.millis(250));
      t.setInterpolator(Interpolator.EASE_BOTH);
      t.setNode(cardArea.getChildren().get(i));
      replace.getChildren().add(t);
    }

    if (newCard != null) {
      CardPane cardPane = new CardPane(newCard);
      cardPane.setLayoutY(PADDING);
      double c = cardPane.getFitWidth();
      cardArea.getChildren().add(cardPane);
      cardPane.setOpacity(0.0);

      FadeTransition ft = new FadeTransition();
      ft.setDuration(Duration.millis(250));
      ft.setFromValue(0.0);
      ft.setToValue(1.0);
      ft.setNode(cardPane);

      TranslateTransition tt = new TranslateTransition();
      tt.setDuration(Duration.millis(250));
      tt.setFromX(len * (c + PADDING));
      tt.setByX(-c);
      tt.setNode(cardPane);

      replace.getChildren().addAll(ft, tt);
    }

    return replace;
  }

  private Animation generateDiscardAnimation(int handIndex) {
    CardPane cardPane = (CardPane) cardArea.getChildren().get(handIndex);

    TranslateTransition t1 = new TranslateTransition();
    t1.setByY(-30);
    t1.setDuration(Duration.millis(500));
    t1.setInterpolator(Interpolator.EASE_IN);

    FadeTransition t2 = new FadeTransition();
    t2.setFromValue(1.0);
    t2.setToValue(0.0);
    t2.setDuration(Duration.millis(500));

    ParallelTransition transition = new ParallelTransition(t1, t2);
    transition.setNode(cardPane);

    return transition;
  }

  /**
   * Replaces the current cards with the specified card list
   *
   * @param cards new card list
   */
  private void renderCards(ArrayList<Card> cards) {
    selectedIndex = -1;
    cardArea.getChildren().clear();
    if (cards.isEmpty()) return;

    panes = new ArrayList<>();
    for (Card card : cards) {
      panes.add(new CardPane(card));
    }
    int n = cards.size();
    double c = panes.get(0).getFitWidth();
    for (int i = 0; i < n; ++i) {
      int curr = i;
      panes.get(i).setLayoutY(PADDING);
      panes.get(i).setLayoutX(PADDING + i * (c + PADDING));
      panes.get(i).setCursor(Cursor.HAND);
      panes.get(i).setOnMouseClicked(e -> {
        if (e.getButton() == MouseButton.PRIMARY) {
          setSelectedIndex(curr);
        }
      });
    }
    cardArea.getChildren().addAll(panes);
    setPrefWidth((PADDING + c) * n + PADDING);
  }

  /**
   * Toggles the pane with the specified <code>show</code> variable
   *
   * @param handler a handler to be run when the animation finishes playing
   * @param show    <code>true</code> to show pane, <code>false</code> to hide pane
   */
  private void toggle(EventHandler<ActionEvent> handler, boolean show) {
    TranslateTransition t = new TranslateTransition();
    t.setByY(show ? -196 : 196);
    t.setInterpolator(Interpolator.EASE_BOTH);
    t.setDuration(Duration.millis(250));
    if (handler != null) t.setOnFinished(handler);
    t.setNode(this);
    t.play();
  }

  void setInteractive(boolean interactive) {
    this.interactive = interactive;
    TranslateTransition t1 = new TranslateTransition();
    t1.setByY(-10);
    t1.setDuration(Duration.millis(125));
    t1.setInterpolator(Interpolator.EASE_OUT);
    t1.setNode(this);

    TranslateTransition t2 = new TranslateTransition();
    t2.setByY(10);
    t2.setDuration(Duration.millis(125));
    t2.setInterpolator(Interpolator.EASE_BOTH);
    t2.setNode(this);

    SequentialTransition s = new SequentialTransition(t1, t2);
    s.play();
  }

  /**
   * Sets the selected card
   *
   * @param index the selected card index
   */
  private void setSelectedIndex(int index) {
    if (!interactive) return;
    if (selectedIndex != -1) {
      panes.get(selectedIndex).setSelected(false);
    }
    if (selectedIndex == index) return;

    // highlight correct pane
    panes.get(index).setSelected(true);
    GameGUIController.playSound("select_card");
    selectedIndex = index;
    // send event to controller
    controller.handleCardSelected(index);
  }

  /**
   * Rotates the specified card
   *
   * @param index the rotated card index
   */
  void rotateCardAt(int index) {
    panes.get(index).rotate();
  }

  CardPane getCard(int index) {
    return (CardPane) cardArea.getChildren().get(index);
  }
}
