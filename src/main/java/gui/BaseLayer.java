package gui;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class BaseLayer extends Pane implements Initializable {
  public BaseLayer() {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/layouts/base-layer.fxml"));
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
    AnchorPane.setTopAnchor(this, 0.0);
    AnchorPane.setRightAnchor(this, 0.0);
    AnchorPane.setBottomAnchor(this, 0.0);
    AnchorPane.setLeftAnchor(this, 0.0);
    setPickOnBounds(false);
  }
}
