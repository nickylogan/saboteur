package model;

public class PlayerDelta extends StateDelta {
  private final int affectedPlayerIndex;
  private final Tool[] sabotagedBefore;
  private final Tool[] sabotagedAfter;

  PlayerDelta(int affectedPlayerIndex, Tool[] sabotagedBefore, Tool[] sabotagedAfter) {
    this.affectedPlayerIndex = affectedPlayerIndex;
    this.sabotagedBefore = sabotagedBefore;
    this.sabotagedAfter = sabotagedAfter;
  }

  /**
   * Returns the affected player index
   *
   * @return the affected player index
   */
  public final int affectedPlayerIndex() {
    return affectedPlayerIndex;
  }

  /**
   * Returns the previously sabotaged tools of the affected player
   *
   * @return a {@link Tool} array representing the previously sabotaged tools
   */
  public final Tool[] sabotagedBefore() {
    return sabotagedBefore;
  }

  /**
   * Returns the new sabotaged tools of the affected player
   *
   * @return a {@link Tool} array representing the newly sabotaged tools
   */
  public final Tool[] sabotagedAfter() {
    return sabotagedAfter;
  }
}
