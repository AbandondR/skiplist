package algorithm;

import java.util.Comparator;
import java.util.Random;

/**
 * Skip List :使用数组保存链表层与层之间的指向
 * 1.插入
 * 2.查询
 * 3.删除
 * 4.随机层数
 *
 * @author Yu
 * @since 2020/4/1
 */
// head(假定4层位最大层数，初始时均指向tail)
// +-++-+                        +-++-+
// | ||_|                        | ||_|
// | ||_|(head.levels)--->       | ||_|  (tail)
// | ||_|                        | ||_|
// +-++-+                        +-++-+

public class SkipList implements Comparator<SkipList.Node<Integer, String>> {

    public static final int MAX_LEVEL = 64;

    public double probability = 0.25;


    //跳表最大层数
    private volatile int level;

    //跳表头结点
    private final Node head;

    //跳表尾节点
    private final Node tail;

    //节点数量
    private int size;


    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    /**
     * initial head and tail. Both of them are {@link SkipList#MAX_LEVEL} for default level num, to avoid dynamic allocate levels
     */
    public SkipList() {

        head = new Node(Integer.MIN_VALUE, null, initialLevels(MAX_LEVEL));
        tail = new Node(Integer.MAX_VALUE, null);
        for (int i = 0; i < MAX_LEVEL; i++) {
            head.levels[i].next = tail;
        }
    }

    /**
     * customerize the propbability
     *
     * @param probability
     */
    public SkipList(double probability) {
        this();
        this.probability = probability;
    }


    private int insert(Node node) {
        //链表为空时
        if (this.level == 0) {
            node.levels = initialLevels(randomLevel());
            level = node.levels.length;
            for (int i = this.level - 1; i >= 0; i--) {
                head.levels[i].next = node;
                node.levels[i].next = tail;
            }
            size++;
        } else {
            Node cursorNode = head;
            Node[] update = new Node[MAX_LEVEL];
            for (int i = this.level - 1; i >= 0; i--) {
                while (compare(cursorNode.levels[i].next, node) < 0) {
                    if (cursorNode.levels[i].next == tail) {
                        break;
                    }
                    cursorNode = cursorNode.levels[i].next;
                }
                update[i] = cursorNode;
            }
            //更新
            if (compare(cursorNode.levels[0].next, node) == 0) {
                cursorNode.levels[0].next.value = node.value;
            }
            //新增
            else {
                int nodeLevel = randomLevel();
                node.levels = initialLevels(nodeLevel);
                if (nodeLevel > this.level) {
                    for (int i = this.level; i < nodeLevel; i++) {
                        update[i] = head;
                    }
                    this.level = nodeLevel;
                }
                for (int i = 0; i < nodeLevel; i++) {
                    node.levels[i].next = update[i].levels[i].next;
                    update[i].levels[i].next = node;
                }
                this.size++;
            }
        }
        return 0;
    }

    /**
     * @param key
     * @return null, if that key is not exist or return actual {@link Node}
     */
    public Node search(Object key) {
        return searchKey(new Node(key, null));
    }

    private Node searchKey(Node node) {
        Node cursorNode = this.head;
        for (int i = level - 1; i >= 0; i--) {
            while (compare(cursorNode.levels[i].next, node) < 0) {
                cursorNode = cursorNode.levels[i].next;
            }
        }
        if (compare(cursorNode.levels[0].next, node) == 0) {
            return cursorNode.levels[0].next;
        }
        return null;
    }

    /**
     * 删除key
     *
     * @param key
     */
    public void delete(Object key) {
        Node node = new Node(key, null);
        Node cursorNode = this.head;
        Node[] update = new Node[MAX_LEVEL];
        for (int i = level - 1; i >= 0; i--) {
            while (compare(cursorNode.levels[i].next, node) < 0) {
                cursorNode = cursorNode.levels[i].next;
            }
            update[i] = cursorNode;
        }
        if (compare(cursorNode.levels[0].next, node) == 0) {
            Node targetNode = cursorNode.levels[0].next;
            for (int i = 0; i < this.level; i++) {
                if (update[i].levels[i].next != targetNode) {
                    break;
                }
                update[i].levels[i].next = targetNode.levels[i].next;
            }
            System.out.println(targetNode);
            targetNode = null;
            while (this.level > 0 && this.head.levels[this.level - 1].next == tail) {
                this.level = this.level - 1;
            }
            size--;
        }
    }

    /**
     * 新添加节点,key不能为null
     *
     * @param key
     * @param value
     * @return
     */
    public <K, V> int insert(K key, V value) {
        assert key != null;
        Node node = new Node(key, value);
        return insert(node);
    }

    private SkipListLevel[] initialLevels(int level) {
        if (level > MAX_LEVEL) {
            throw new RuntimeException("层数超过最大值");
        }
        SkipListLevel[] levels = new SkipListLevel[level];
        //所有初始化的层下一个节点均指向null(尾节点)
        for (int i = 0; i < levels.length; i++) {
            levels[i] = new SkipListLevel();
            levels[i].next = tail;
        }
        return levels;
    }

    /**
     * 借用的是redis的实现方式，参考 t_zset.c
     *
     * @return
     */
    public int randomLevel() {
        int level = 1;
        Random random = new Random();
        while ((random.nextInt() & 0xFFFF) < (this.probability * 0xFFFF)) {
            level += 1;
        }
        return (level < MAX_LEVEL) ? level : MAX_LEVEL;
    }

    @Override
    public int compare(Node<Integer, String> o1, Node<Integer, String> o2) {
        if (o1.key.intValue() < o2.key.intValue()) {
            return -1;
        }
        if (o1.key.intValue() > o2.key.intValue()) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        Node node = head.levels[0].next;
        for (int i = 0; i < size; i++) {
            builder.append(node).append(", ");
            node = node.levels[0].next;
        }
        builder.append("]");
        return builder.toString();
    }


    /**
     * 链表数据节点
     *
     * @param <K>
     * @param <V>
     */

    class Node<K, V> {
        //sorted by
        private K key;
        //save data
        private V value;

        protected SkipListLevel[] levels;

        public Node(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public Node(K key, V value, SkipListLevel[] levels) {
            this.key = key;
            this.value = value;
            this.levels = levels;
        }

        @Override
        public String toString() {
            return "Node{" +
                    "key=" + key +
                    ", value=" + value +
                    ", levelLenth=" + levels.length +
                    '}';
        }
    }

    class SkipListLevel<K, V> {
        Node<K, V> next;
    }


}
