# Saboteur

Final project for Game Theory/AI course.

## Requirements

- JDK 14
- Maven (for dependency management)

## Setting up

1. Clone this repository by running:

    ```sh
    git clone https://github.com/nickylogan/saboteur.git
    ```

2. Run the game by executing:

   ```shell script
   mvn javafx:run
   ```

## Making your own AI class

**Create a new package in `ai.impl`**. The package **must** contain **at least one public class**, which is your custom AI. For ease of explanation, I'll refer your custom class as `FooAI`. Don't be afraid to name it something else.

**`FooAI` must extend `AI`**, and the only method you **must** override is `makeDecision()`, which will be called automatically when the game prompts you to move.

> **IMPORTANT**: The `makeDecision` method timeouts after `5` seconds. Make sure your implementation doesn't take too long to finish.

Here's an example short snippet of `FooAI`:

```java
public class FooAI extends AI {
    public FooAI() { super("Foo"); }

    @Override
    public Move makeDecision() {
        // do something here
        return move;
    }
}
```

See [`ai.impl.example`](/src/main/java/ai/impl/example) or [`ai.impl.random`](/src/main/java/ai/impl/random) for more sophisticated implementations.

## Controls

| Action                  | Control                                       |
| ----------------------- | --------------------------------------------- |
| Select a card           | **Left-click** on any card on the bottom pane |
| Place a card            | **Right-click** on the desired position       |
| Target a player         | **Left-click** on the targeted player         |
| Rotate a card           | Press `R`                                     |
| Discard a selected card | Press `D`                                     |

## Documentation

Please read the JavaDoc. If you feel lost, you can check out the [class diagram](classdiagram.png)

## Contributing

Fork this repository and create a new pull request. Please provide a clear summary of your changes.

## Bug reports and issues 🐞

Should any issue or bug occur, please [open a new issue](https://github.com/nickylogan/saboteur/issues/new), or directly PM me.

## Authors

- Nicky Logan &mdash; [**@nickylogan**](https://github.com/nickylogan)
- Nadya Felim &mdash; [**@Ao-Re**](https://github.com/Ao-Re)

## AI Authors

- [`ai.impl.logan.SaboteurAI`](/src/main/java/ai/impl/logan/SaboteurAI.java): Nicky Logan [**@nickylogan**](https://github.com/nickylogan) and Nadya Felim [**@Ao-Re**](https://github.com/Ao-Re)
- [`ai.impl.davis.FishAI`](/src/main/java/ai/impl/davis/FishAI.java): Barjuan Davis [**@barjuandavis**](https://github.com/barjuandavis/) and Regy Ezananta
- [`ai.impl.ricky.AIPakSam`](/src/main/java/ai/impl/ricky/AIPakSam.java): Ricky Gani [**@ZyphonGT**](https://github.com/ZyphonGT) and Albert Antonio [**@mailmancy**](https://github.com/mailmancy)
- [`ai.impl.tom.AndreTomAI`](/src/main/java/ai/impl/tom/AndreTomAI.java): Andreas Geraldo [**@Andreas001**](https://github.com/Andreas001) and Thompson DY [**@thompsonlie**](https://github.com/thompsonlie)
- [`ai.impl.wilbert.Perceptron`](/src/main/java/ai/impl/wilbert/Perceptron.java): Wilbert NW [**@wilbertnw**](https://github.com/wilbertnw) and Joshua Kaven [**@Nevaks**](https://github.com/Nevaks)
- [`ai.impl.ray.HeuristicsAI`](/src/main/java/ai/impl/ray/HeuristicsAI.java): Ray Antonius [**@rocksus**](https://github.com/Rocksus) and Joshua Budijanto
- [`ai.impl.yj.YJ_AI`](/src/main/java/ai/impl/yj/YJ_AI.java): Christopher Yefta [**@ChrisYef**](https://github.com/ChrisYef) and James Adhitthana [**@jamesadhitthana**](https://github.com/jamesadhitthana)
- [`ai.impl.jasson.Jasson`](/src/main/java/ai/impl/jasson/Jasson.java): Jasson and Peter
- [`ai.impl.angjoshel.Core`](/src/main/java/ai/impl/angjoshel/Core.java): Angela Ivany [**@angelaivany**](https://github.com/angelaivany), Josephine [**@josessca**](https://github.com/josessca), and Shella L [**@shellal**](https://github.com/shellal)
- [`ai.impl.cen.botGDCN`](/src/main/java/ai/impl/cen/botGDCN.java): Bong Cen Choi [**@Bongcen**](https://github.com/Bongcen) and Gabriel Dejan [**@gabrieldejan17**](https://github.com/gabrieldejan17)
- [`ai.impl.jerry.AIJR`](/src/main/java/ai/impl/jerry/AIJR.java): Ryan Hiroshi [**@RyanHiroshi**](https://github.com/RyanHiroshi) and Jerry
