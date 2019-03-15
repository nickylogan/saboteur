# Saboteur

Final project for Game Theory/AI course.

## Progress

- [x] Model refactor
- [x] Extensible AI model
- [ ] Runnable program
- [ ] JavaFX layout
- [ ] JavaFX controller

## Usage

### Setting up

1. Clone this repository by running:
    ```sh
    git clone https://github.com/nickylogan/saboteur.git
    ```
2. For IntelliJ users, build and run the project

### Making your own AI class

Create a new AI class by extending the `AI` class in the `src/ai` directory.

Here is an example code
```java
package ai;

import model.GameException;
import model.Move;
import model.Player;

public class ExampleAI extends AI {
  public ExampleAI(String name) {
    super(name);
  }

  private Move makeDecision() {
    //
  }

  // Open javadoc for this method for more information
  @Override
  public void onMovementPrompt() { 
    Move move = makeDecision();
    try {
      // Play the decided move to the game
      state().playMove(move);
    } catch (GameException e) {
      System.out.println(e.getMessage());
    }
  }
}
```

## Documentation

Please read the javadoc

## Contributing

Fork this repository and create a new pull request. Please provide a clear summary of your changes.

## Issue

Should any issue or bug occur, please [open a new issue](https://github.com/nickylogan/saboteur/issues/new), or directly PM me.