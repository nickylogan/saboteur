package ai.impl.proposed;

import model.Board;
import model.GoalType;

import java.util.Map;
import java.util.Optional;

class GoalKnowledge {
  private final Map<Board.GoalPosition, GoalType> ref;

  enum Scenario {
    UNKNOWN,
    TOP_ROCK,
    TOP_GOLD,
    MID_GOLD,
    BOT_GOLD,
  }

  GoalKnowledge(Map<Board.GoalPosition, GoalType> ref) {
    this.ref = ref;
  }

  Optional<GoalType> top() {
    GoalType goalType = ref.get(Board.GoalPosition.TOP);
    return Optional.ofNullable(goalType);
  }

  Optional<GoalType> mid() {
    GoalType goalType = ref.get(Board.GoalPosition.MIDDLE);
    return Optional.ofNullable(goalType);
  }

  Optional<GoalType> bottom() {
    GoalType goalType = ref.get(Board.GoalPosition.BOTTOM);
    return Optional.ofNullable(goalType);
  }

  Scenario getScenario() {
    if (ref.isEmpty()) {
      return Scenario.UNKNOWN;
    } else if (top().orElse(null) == GoalType.ROCK) {
      return Scenario.TOP_ROCK;
    } else if (top().orElse(null) == GoalType.GOLD) {
      return Scenario.TOP_GOLD;
    } else if (mid().orElse(null) == GoalType.GOLD) {
      return Scenario.MID_GOLD;
    } else {
      return Scenario.BOT_GOLD;
    }
  }

  Optional<Board.GoalPosition> getGoldPosition() {
    if (containsGold()) {
      return Optional.empty();
    }

    if (top().orElse(null) == GoalType.GOLD) {
      return Optional.of(Board.GoalPosition.TOP);
    } else if (mid().orElse(null) == GoalType.GOLD) {
      return Optional.of(Board.GoalPosition.MIDDLE);
    } else {
      return Optional.of(Board.GoalPosition.BOTTOM);
    }
  }

  boolean containsGold() {
    return ref.containsValue(GoalType.GOLD);
  }

  boolean isComplete() {
    // all goals are known
    return ref.size() == 3;
  }

  void infer() {
    // There's nothing left to infer
    if (isComplete()) return;

    if (containsGold()) {
      // If one is known to be gold, the others can be marked as rock.
      Optional<Board.GoalPosition> pos = getGoldPosition();
      assert pos.isPresent();
      for (Board.GoalPosition p : Board.GoalPosition.values()) {
        if (p == pos.get()) continue;
        ref.put(p, GoalType.ROCK);
      }
    } else if (ref.size() == 2) {
      // Non contains gold, but it is known that both are rock cards.
      // Infer the only one left as a gold card.
      for (Board.GoalPosition p : Board.GoalPosition.values()) {
        if (ref.containsKey(p)) continue;
        ref.put(p, GoalType.GOLD);
      }
    }
  }
}
