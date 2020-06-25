package ai.utils;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.time.LocalDateTime;

import static org.fusesource.jansi.Ansi.ansi;

@SuppressWarnings("unused")
public class Logger {
  public enum Level {
    ERROR,
    WARN,
    INFO,
    DEBUG,
  }

  static {
    AnsiConsole.systemInstall();
  }

  private static Level verbosity = Level.INFO;

  public static void init(Level verbosity) {
    setVerbosity(verbosity);
  }

  public static void errorln(Object x) {
    String s = String.valueOf(x);
    System.out.println(format(s, Level.ERROR));
  }

  public static void errorf(String format, Object... args) {
    String s = String.format(format, args);
    System.out.println(format(s, Level.ERROR));
  }

  public static void warnln(Object x) {
    if (verbosity.ordinal() < Level.WARN.ordinal()) return;

    String s = String.valueOf(x);
    System.out.println(format(s, Level.WARN));
  }

  public static void warnf(String format, Object... args) {
    if (verbosity.ordinal() < Level.WARN.ordinal()) return;

    String s = String.format(format, args);
    System.out.println(format(s, Level.WARN));
  }

  public static void infoln(Object x) {
    if (verbosity.ordinal() < Level.INFO.ordinal()) return;

    String s = String.valueOf(x);
    System.out.println(format(s, Level.INFO));
  }

  public static void infof(String format, Object... args) {
    if (verbosity.ordinal() < Level.INFO.ordinal()) return;

    String s = String.format(format, args);
    System.out.println(format(s, Level.INFO));
  }

  public static void debugln(Object x) {
    if (verbosity.ordinal() < Level.DEBUG.ordinal()) return;

    String s = String.valueOf(x);
    System.out.println(format(s, Level.DEBUG));
  }

  public static void debugf(String format, Object... args) {
    if (verbosity.ordinal() < Level.DEBUG.ordinal()) return;

    String s = String.format(format, args);
    System.out.println(format(s, Level.DEBUG));
  }

  private static String format(String s, Level level) {
    String ts = getCurrentTimestamp();
    String tag = getTag(level);

    return String.format("%s %s %s", ts, tag, s);
  }

  private static String getCurrentTimestamp() {
    String ts = "[" + LocalDateTime.now() + "]";
    return ansi().fgBrightBlue().a(ts).reset().toString();
  }

  private static String getTag(Level level) {
    Ansi ansi = ansi();
    switch (level) {
      case ERROR:
        ansi = ansi.fgRed();
        break;
      case WARN:
        ansi = ansi.fgYellow();
        break;
      case INFO:
        ansi = ansi.fgCyan();
        break;
      case DEBUG:
        ansi = ansi.fgBrightBlack();
        break;
      default:
    }

    // TODO: not gud, but will do for now.
    StringBuilder tag = new StringBuilder("[" + level.name() + "]");
    if (tag.length() == 6) {
      tag.append(" ");
    }

    return ansi.a(tag.toString()).reset().toString();
  }

  private static void setVerbosity(Level verbosity) {
    Logger.verbosity = verbosity;
  }
}
