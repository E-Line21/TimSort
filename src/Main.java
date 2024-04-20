import ru.khafizov.timsort.TimSort;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            //прогоняю много раз, чтобы JVM раздуплилась и jit-компилятор сделал свою работу
            generateFiles();
            testFiles(i);
        }
    }
    
    public static void generateFiles() {
        Random random = new Random();
        for (int i = 100; i <= 10000; i+=100) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("res" + i + ".txt"))) {
                for (int j = 0; j < i; j++) {
                    writer.write(random.nextInt(0, 10000) + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void testFiles(int i) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data" + i + ".csv"))) {
            for (int size = 100; size <= 10000; size+=100) {
                try (BufferedReader reader = new BufferedReader(new FileReader("res" + size + ".txt"))) {
                    Integer[] arr = new Integer[size];
                    for (int j = 0; j < size; j++) {
                        arr[j] = Integer.parseInt(reader.readLine());
                    }

                    long startTime = System.nanoTime();
                    int iterations = TimSort.sort(arr);
                    long endTime = System.nanoTime();

                    writer.write(size + "; " + (endTime - startTime) + "; " + iterations + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void test() {
        Integer[] arr = {20, 90, 84, 13, 48, 70, 65, 34};
        TimSort.sort(arr);
        if (!check(arr)) {
            throw new RuntimeException("Ошибка сортировки");
        }


        for (int i = 0; i < 10000; i++) {
            arr = generateIntegerArray();
            TimSort.sort(arr);
            if (!check(arr)) {
                throw new RuntimeException("Ошибка сортировки");
            }
        }
    }

    public static Integer[] generateIntegerArray() {
        Random random = new Random();
        int size = random.nextInt(0, 1000);
        Integer[] arr = new Integer[size];
        for (int i = 0; i < size; i++) {
            arr[i] = random.nextInt(1, 1000);
        }

        return arr;
    }

    public static boolean check(Integer[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i-1] > arr[i]) {

                System.out.println(Arrays.toString(arr));
                return false;
            }
        }

        return true;
    }
}