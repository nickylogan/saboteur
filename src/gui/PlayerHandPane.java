package gui;

import com.sun.org.apache.bcel.internal.generic.RETURN;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import model.cards.Card;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HandPane extends Pane implements Initializable {
  private final static double PADDING = 8;
  private final static double HEIGHT = 80;
  GameWindow window;

  public HandPane(GameWindow window) {
    this.window = window;
    setMaxWidth(Double.MAX_VALUE);
    setPrefHeight(HEIGHT);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    StackPane.setAlignment(this, Pos.BOTTOM_LEFT);
    layoutXProperty().bind(window.getLogScroll().prefWidthProperty());
  }

  public void animateCards(ArrayList<Card> newCards, EventHandler<ActionEvent> handler) {
    Animation collapse = generateCollapseAnimation();
    collapse.setOnFinished(e -> {
      renderCards(newCards);
      Animation expand = generateExpandAnimation();
      expand.setOnFinished(handler);
      expand.play();
    });
    collapse.play();
  }

  private Animation generateCollapseAnimation() {
    ParallelTransition cardCollapse = new ParallelTransition();
    getChildren().forEach(n -> {
      TranslateTransition t = new TranslateTransition();
      t.setToX(PADDING);
      t.setDuration(Duration.millis(500));
      t.setInterpolator(Interpolator.EASE_OUT);
      t.setNode(n);
      cardCollapse.getChildren().add(t);
    });

    TranslateTransition down = new TranslateTransition();
    down.setByY(HEIGHT);
    down.setDuration(Duration.millis(500));
    down.setInterpolator(Interpolator.EASE_OUT);
    down.setNode(this);

    return new SequentialTransition(cardCollapse, down);
  }

  private Animation generateExpandAnimation() {
    ParallelTransition cardExpand = new ParallelTransition();
    int len = getChildren().size();
    for (int i = 0; i < len; ++i) {
      CardPane cardPane = (CardPane) getChildren().get(i);
      TranslateTransition t = new TranslateTransition();
      t.setToX(PADDING + i * (cardPane.getFitWidth() + PADDING));
      t.setDuration(Duration.millis(500));
      t.setInterpolator(Interpolator.EASE_OUT);
      t.setNode(cardPane);
      cardExpand.getChildren().add(t);
    }

    TranslateTransition up = new TranslateTransition();
    up.setByY(-HEIGHT);
    up.setDuration(Duration.millis(500));
    up.setInterpolator(Interpolator.EASE_BOTH);
    up.setNode(this);

    return new SequentialTransition(up, cardExpand);
  }

  private void renderCards(ArrayList<Card> cards) {
    getChildren().clear();
    if (cards.isEmpty()) return;
    List<CardPane> panes = cards.stream().map(CardPane::new).collect(Collectors.toList());
    int n = cards.size();
    double c = panes.get(0).getFitWidth();
    for (int i = 0; i < n; ++i) {
      panes.get(i).setLayoutX(PADDING + i * (c + PADDING));
    }
    getChildren().addAll(panes);
  }

  public void replaceCards(int prev, Card newCard) {

  }
}
