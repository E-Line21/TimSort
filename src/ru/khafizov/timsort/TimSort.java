package ru.khafizov.timsort;

import java.util.*;

public class TimSort<T> {

    private static int iter = 0;

    private class Run {
        public final int start;
        public final int len;

        public Run(int start, int len) {
            this.start = start;
            this.len = len;
        }

        private T get(int idx) {
            return data[start + idx];
        }
    }

    private final static int MIN_MERGE = 64;
    private final Comparator<? super T> c;
    private int stackSize;
    private final List<Run> runStack;
    private final T[] data;
    private TimSort(T[] data, Comparator<? super T> c) {
        this.c = c;
        this.runStack = new LinkedList<>();
        this.data = data;
        this.stackSize = 0;

    }

    public static <T extends Comparable<? super T>> int  sort(T[] data) {
        return sort(data, T::compareTo);
    }

    public static <T> int sort(T[] data, Comparator<? super T> c) {
        iter = 0;

        if (data.length < 2) {
            return iter; //Массив длины 0 и 1 всегда отсортирован
        }

        if (data.length < MIN_MERGE) {
            // если длина массива меньше определенного значения, то лучше использовать просто сортировку вставкой
            insertionsSort(data, 0, data.length, c);
            return iter;
        }

        int minRun = getMinrun(data.length); // вычисляем оптимальное значение для run'a
        int unprocessedLength = data.length; // длина необработанной последовательности
        int currentIdx = 0; // индекс первого необработанного элемента
        TimSort<T> ts = new TimSort<>(data, c);
        do {
            int runLen = calculateRunLenAndMakeAscending(data, currentIdx, c); // начиная с первого необработанного
                                                                               // элемента, ищем упорядоченную
                                                                               // последовательность и делаем ее
                                                                               // возрастающей

            if (runLen < minRun) { // если длина последовательности меньше оптимального,
                int force = Math.min(unprocessedLength, minRun); // то добавляем в нее элементы
                insertionsSort(data, currentIdx, currentIdx + force, c); // и сортируем
                runLen = force;
            }

            ts.pushRun(currentIdx, runLen); // добавляем найденный run в стэк
            iter++;
            ts.merge(); // пробуем мержить

            currentIdx += runLen; // обновляем индекс последнего необработанного элемента
            unprocessedLength -= runLen; // обновляем длину необработанной последовательности
        } while (unprocessedLength != 0); // повторяем пока не обработаем всю последовательность

        ts.mergeForce(); // принудительно мержим оставшиеся run'ы
        return iter;
    }

    private static <T> int calculateRunLenAndMakeAscending(T[] data, int from, Comparator<? super T> c) {
        int runIdx = from + 1;
        if (runIdx == data.length) {
            return 1;
        }

        if (c.compare(data[runIdx++], data[from]) < 0) { // Убывающая последовательность
            while (runIdx < data.length && c.compare(data[runIdx], data[runIdx - 1]) < 0) {
                runIdx++;
                iter++;
            }
            reverseRange(data, from, runIdx);
        } else {                              // Возрастающая последовательность
            while (runIdx < data.length && c.compare(data[runIdx], data[runIdx - 1]) >= 0) {
                runIdx++;
                iter++;
            }
        }

        return runIdx - from;
    }

    private static void reverseRange(Object[] data, int from, int to) {
        to--;

        while (from < to) {
            Object t = data[from];
            data[from++] = data[to];
            data[to--] = t;
            iter++;
        }

    }

    public static <T> void insertionsSort(T[] data, int from, int to, Comparator<? super T> c) {
        for (int i = from + 1; i < to; ++i) {
            T key = data[i];
            int j = i - 1;


            while (j >= from && c.compare(data[j], key) > 0) {
                data[j + 1] = data[j];
                j = j - 1;
                iter++;
            }
            data[j + 1] = key;
        }
    }

    private static int getMinrun(int n) {
        int r = 0;
        while (n >= 64) {
            r |= n & 1;
            n >>= 1;
            iter++;
        }
        return n + r;
    }

    private void pushRun(int start, int length) {
        this.runStack.add(0, new Run(start, length));
        this.stackSize++;
    }

    private void merge() {

        while (stackSize > 1) {
            if ( (stackSize > 2 && run(3).len <= run(2).len + run(1).len) || (stackSize > 3 && run(4).len <= run(3).len + run(2).len) ) {
                if ( run(3).len < run(1).len ) {
                    mergeAt(2);
                } else {
                    mergeAt(1);
                }
            } else if (run(2).len <= run(1).len) {
                mergeAt(1);
            } else {
                break;
            }
        }
    }

    private void mergeForce() {
        while (stackSize > 1) {
            int n = 1;
            if (stackSize > 2 && run(3).len < run(1).len)
                n++;
            mergeAt(n);
        }
    }

    private Run run(int n) {
        return runStack.get(n - 1);
    }

    private void mergeAt(int run) {
        Run first = run(run);
        Run second = run(run + 1);
        if (first.start > second.start) {
            Run temp = first;
            first = second;
            second = temp;
        }


        List<T> temp = new ArrayList<>();
        int i = 0;
        int j = 0;
        while (i < first.len && j < second.len) {
            iter++;
            if (this.c.compare(first.get(i), second.get(j)) < 0) {
                temp.add(first.get(i));
                i++;
            } else {
                temp.add(second.get(j));
                j++;
            }
        }

        while (i < first.len) {
            temp.add(first.get(i));
            i++;
            iter++;
        }

        while (j < second.len) {
            temp.add(second.get(j));
            j++;
            iter++;
        }

        for (int k = 0; k < temp.size(); k++) {
            iter++;
            data[first.start + k] = temp.get(k);
        }

        this.setRun(run, new Run(first.start, first.len + second.len));
        iter++;
        this.removeRun(run + 1);
        iter++;
    }

    private void setRun(int idx, Run run) {
        runStack.set(idx - 1, run);
    }

    private void removeRun(int idx) {
        runStack.remove(idx - 1);
        stackSize--;
    }

}
