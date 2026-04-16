public class BinarySearch {
  public static int binarySearch(int[] array, int target) {
    int left = 0;
    int right = array.length - 1;

    while (left <= right) {
      int mid = (left + right)/2;

      if (array[mid] == target) {
        return mid;
      } else if (array[mid] < target) {
        left = mid + 1;
      } else {
        right = mid - 1;
      }
    }

    return -1;
  }

  public static void main(String[] args) {
    int[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    int target = 16;
    int result = binarySearch(numbers, target);

    if (result != -1) {
      System.out.println("Position of 7 in the array is " + result);
    } else {
      System.out.println("Target not found");
    }
  }
}
