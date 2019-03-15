package model;

class GameException extends Exception {
  GameException(String message) {
    super(message);
  }

  GameException(String format, Object... args) { this(String.format(format, args)); }
}
