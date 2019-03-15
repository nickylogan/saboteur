package model;

public class GameException extends Exception {
  public GameException(String message) {
    super(message);
  }

  public GameException(String format, Object... args) { this(String.format(format, args)); }
}
