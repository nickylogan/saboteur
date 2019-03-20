package gui;

import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class NextButton extends Button {
  NextButton() {
    getStylesheets().add(getClass().getResource("../css/next-button.css").toExternalForm());
    getStyleClass().add("next-button");
    setPrefWidth(300);
    setPrefHeight(100);
    setText("START GAME");
    setTextAlignment(TextAlignment.CENTER);
    setCursor(Cursor.HAND);

    DropShadow shadow = new DropShadow();
    shadow.setBlurType(BlurType.GAUSSIAN);
    shadow.setColor(new Color(0,0,0,0.5));
    shadow.setHeight(72);
    shadow.setWidth(72);
    shadow.setRadius(72);
    shadow.setSpread(0);

    setEffect(shadow);

    AnchorPane.setBottomAnchor(this, 16.0);
    AnchorPane.setRightAnchor(this, 16.0);
  }

  void setStarted() {
    setText("NEXT TURN");
  }
}
