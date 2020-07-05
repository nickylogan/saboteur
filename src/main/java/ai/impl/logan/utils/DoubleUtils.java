package ai.impl.logan.utils;

public class DoubleUtils {
  private static double EPS;

  public static void setEpsilon(double eps) {
    EPS = eps;
  }

  public static int compare(double a, double b) {
    if (a - b > EPS)
      return 1;
    else if (b - a > EPS)
      return -1;
    else
      return 0;
  }
}
