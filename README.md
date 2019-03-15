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

Create a new package for your AI class. The package **must** contain at least one class, which is your custom AI. 

Your AI class must extend `AI`, and you only need to implement the `makeDecision` method, which will be called automatically when the game prompts you to move. 

> The `makeDecision` method is set to timeout after `5` seconds.

See [src/example/ExampleAI.java](https://github.com/nickylogan/saboteur/blob/master/src/example/ExampleAI.java) for an example implementation.

## Documentation

Please read the javadoc

## Contributing

Fork this repository and create a new pull request. Please provide a clear summary of your changes.

## Issue

Should any issue or bug occur, please [open a new issue](https://github.com/nickylogan/saboteur/issues/new), or directly PM me.