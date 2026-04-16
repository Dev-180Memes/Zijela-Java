public class Fibonaci {
  public static void main(String[] args) {
    int prev = 0;
    int curr = 1;

    System.out.println(prev);
    System.out.println(curr);

    for (int i = 0; i < 18; i++) {
      int next = prev + curr;
      prev = curr;
      curr = next;
      System.out.println(next);
    }
  }
}