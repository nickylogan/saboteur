/*
 * Authors:
 * Nicky (https://github.com/nickylogan)
 * Nadya (https://github.com/Ao-Re)
 */

package customAI.nn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;

import static customAI.nn.Utils.factorial;

public class KripkeModel {
  private boolean[][] states;
  private double[][][] relations;
  private int selfIndex;
  private int numPlayers;
  private int worldSize;

  private static int calculateWorldSize(int numPlayers, int numSaboteurs) {
    long num = factorial(numPlayers);
    long den = factorial(numSaboteurs) * factorial(numPlayers - numSaboteurs);
    return (int) (num / den);
  }

  KripkeModel(int numPlayers, int numSaboteurs) {
    this.numPlayers = numPlayers;
    // Initialize world size
    this.worldSize = calculateWorldSize(numPlayers, numSaboteurs);

    // Initialize all possible states
    this.states = new boolean[worldSize][numPlayers];
    boolean[] currState = new boolean[numPlayers];
    for (int i = numPlayers - 1; (numPlayers - i) <= numSaboteurs; --i)
      currState[i] = true;
    for (int i = 0; i < worldSize; ++i) {
      states[i] = currState.clone();
      nextPermutation(currState);
    }
  }

  void initialize(int playerIndex, boolean saboteur) {
    this.selfIndex = playerIndex;

    // Initialize relationship graph
    this.relations = new double[numPlayers][worldSize][worldSize];
    for (int i = 0; i < numPlayers; ++i) {
      for (int j = 0; j < worldSize; ++j) {
        for (int k = 0; k < worldSize; ++k) {
          if (i == selfIndex && states[j][selfIndex] == saboteur && states[k][selfIndex] != saboteur) {
            this.relations[i][j][k] = 0;
          } else {
            this.relations[i][j][k] = .5;
          }
        }
      }
    }
  }

  void weakenPlayerRole(int player, boolean saboteur) {

  }

  void strengthenPlayerRole(int player, boolean saboteur) {

  }

  void strenghtenOppositeRoles(int player1, int player2) {

  }

  void strengthenSameRoles(int player1, int player2) {
    for (int i = 0; i < numPlayers; ++i) {

    }
  }

  void weakenRelation(int from, int to, int exception, double beta) {
    for (int i = 0; i < numPlayers; ++i) {
      if (i == exception) continue;
      relations[i][from][to] -= relations[i][from][to] / beta;
    }
  }

  void strengthenRelation(int from, int to, int exception, double alpha) {
    for (int i = 0; i < numPlayers; ++i) {
      if (i == exception) continue;
      relations[i][from][to] += (1 - relations[i][from][to]) / alpha;
    }
  }

  private static void reverse(boolean[] arr, int left, int right) {
    while (left < right) {
      boolean temp = arr[left];
      arr[left] = arr[right];
      arr[right] = temp;
      left++;
      right--;
    }
  }

  private static void nextPermutation(boolean[] arr) {
    if (arr == null || arr.length < 2) return;

    int p = 0;
    for (int i = arr.length - 2; i >= 0; --i) {
      int a = arr[i] ? 1 : 0;
      int b = arr[i + 1] ? 1 : 0;
      if (a < b) { p = i; break;}
    }

    int q = 0;
    for (int i = arr.length - 1; i > p; --i) {
      int a = arr[i] ? 1 : 0;
      int b = arr[p] ? 1 : 0;
      if (a > b) { q = i; break;}
    }

    if (p == 0 && q == 0) {
      reverse(arr, 0, arr.length - 1);
      return;
    }

    boolean temp = arr[p];
    arr[p] = arr[q];
    arr[q] = temp;


    if (p < arr.length - 1) {
      reverse(arr, p + 1, arr.length - 1);
    }
  }
}
