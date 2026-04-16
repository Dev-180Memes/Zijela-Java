public class LinearSearch {
  public static int linearSearch(int[] array, int target) {
    for (int i = 0; i < array.length; i++) {
      if (array[i] == target) {
        return i;
      }
    }

    return -1;
  }

  public static void main(String[] args) {
    int[] numbers = {0, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181};

    int target = 9;
    int result = linearSearch(numbers, target);

    if (result != -1) {
      System.out.println("Found " + target + " at index " + result);
    } else {
      System.out.println(target + " was not found in the array");
    }
  }
}
