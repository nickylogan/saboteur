package model;

public interface GameObserver {
  void onMovementPrompt();
  void onGameFinished();
  void onGameStart();
}
