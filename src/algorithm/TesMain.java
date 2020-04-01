package algorithm;

import java.util.Random;

/**
 * {@link SkipList} function test
 *
 * @author Yu
 * @since 2020/4/1
 */
public class TesMain {

    public static void main(String[] args) {
        SkipList skipList = new SkipList();
        Random random = new Random();
        for (int i = 0; i < 100000; i++) {
            int num = random.nextInt();
            skipList.insert(num, String.valueOf(num));
        }
        skipList.insert(2, "2");
        skipList.insert(5, "5");
        skipList.insert(3, "3");
        skipList.insert(6, "6");
        skipList.insert(11, "11");
        skipList.insert(10, "10");
        skipList.insert(8, "8");

        skipList.insert(3, "4");
        skipList.delete(3);
        skipList.insert(7, "7");
        long start = System.currentTimeMillis();
        SkipList.Node node = skipList.search(Integer.MIN_VALUE + 100020);
        System.out.println(node);
        long end = System.currentTimeMillis();
        System.out.println("spent time:" + (end - start) + "(ms)");
    }

}
