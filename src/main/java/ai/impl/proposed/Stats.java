package ai.impl.proposed;

import model.Player;

class Stats {
  private int playAsMiner = 0;
  private int playAsSaboteur = 0;
  private int winAsMiner = 0;
  private int winAsSaboteur = 0;

  Stats() {}

  void incrementPlay(Player.Role role) {
    switch (role) {
      case GOLD_MINER -> ++playAsMiner;
      case SABOTEUR -> ++playAsSaboteur;
    }
  }

  void incrementWin(Player.Role role) {
    switch (role) {
      case GOLD_MINER -> ++winAsMiner;
      case SABOTEUR -> ++winAsSaboteur;
    }
  }

  public int getPlayAsMiner() {
    return this.playAsMiner;
  }

  public int getPlayAsSaboteur() {
    return this.playAsSaboteur;
  }

  public int getWinAsMiner() {
    return this.winAsMiner;
  }

  public int getWinAsSaboteur() {
    return this.winAsSaboteur;
  }
}
