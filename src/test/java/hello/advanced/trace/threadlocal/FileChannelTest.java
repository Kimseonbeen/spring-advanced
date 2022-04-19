package hello.advanced.trace.threadlocal;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
public class FileChannelTest {

    @Test
    void fileChannel() throws IOException {
        String[] data = {"안녕하세요, 여러분", "data1", "모든방법에 대해 공부해봅시다."};

        Path path = Paths.get("c:\\workspace\\test.txt");
        Files.createDirectories(path.getParent());
        FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        Charset charset = Charset.defaultCharset();
        ByteBuffer buffer;
        int byteCount = 0;
        for (int i = 0; i < data.length; i++) {
            buffer = charset.encode(data[i]);
            log.info("buffer={}", buffer);
            byteCount = fileChannel.write(buffer);
            log.info("byteCount={}", byteCount);
        }

        fileChannel.close();

    }

    @Test
    void watchServiceTest() throws IOException, InterruptedException {
        try {
            WatchService ws;
            ws = FileSystems.getDefault().newWatchService();
            Path path = Paths.get("c:\\workspace");
            path.register(ws, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                WatchKey key = ws.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> k = event.kind();
                    Path p = (Path) event.context();
                    if (k == StandardWatchEventKinds.ENTRY_CREATE) {
                        System.out.println("File " + p.getFileName() + " is created");
                    }
                    boolean valid = key.reset();
                    if (!valid) break;
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Test
    void arrayLislTest() {
        ArrayList<String> list = new ArrayList<>();

        list.add("one");
        list.add("two");
        list.add("three");
        list.add(1, "one");

        Consumer<Object> con1 = new Consumer<>() {
            @Override
            public void accept(Object t) {
                System.out.println("t = " + t);
            }
        };
        // 인자 객체가 Consumer로 된 객체를 넣어야함
        list.forEach(con1);

        Consumer<String> con2 = t -> System.out.println("t = " + t);
        list.forEach(con2);

        list.forEach(t -> System.out.println("t = " + t));
    }

    @Test
    void queueTest() {
        Queue<String> quene = new LinkedList<>();
        quene.offer("one");
        quene.offer("two");
        quene.offer("three");
        quene.offer("four");

        System.out.println("quene = " + quene);

        String s = quene.poll();
        while (s != null) {
            System.out.println("s = " + s);
            s= quene.poll();
        }
    }

    @Test
    void hashMapTest() {
        ArrayList<String> names = new ArrayList<>();
        Map<String, Integer> lectures;
        Map<String, Map> scores = new HashMap<>();

        names.add("김철수");
        names.add("이영희");

        Iterator<String> it = names.iterator();
        while (it.hasNext()) {
            String name = it.next();
            if (name.equals("김철수")) {
                lectures = new HashMap<>();
                lectures.put("국어", 100);
                lectures.put("영어", 95);
                lectures.put("수학", 80);
                scores.put(name, lectures);
            } else if (name.equals("이영희")) {
                lectures = new HashMap<String, Integer>();
                lectures.put("국어", 70);
                lectures.put("영어", 55);
                lectures.put("수학", 40);
                scores.put(name, lectures);
            }
        }

        System.out.println("scores : " + scores);

        Iterator<String> it2 = names.iterator();
        while (it2.hasNext()) {
            String name = it2.next();
            System.out.println("name = " + name);

            System.out.print("국어 : ");
            System.out.println(scores.get(name).get("국어"));

            System.out.print("영어 : ");
            System.out.println(scores.get(name).get("영어"));

            System.out.print("수학 : ");
            System.out.println(scores.get(name).get("영어"));
        }
    }

    class MyThread1 extends Thread {
        Thread thdNext = null;
        public MyThread1(String szName) {
            super(szName);
        }
        public void run() {
            for (int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(1000000);
                } catch (InterruptedException e) {
                    System.out.println(getName() + " ");
                    if (thdNext.isAlive()) {
                        thdNext.interrupt();
                    }
                }
            }
        }

        public void setNextThread(Thread t) {
            thdNext = t;
        }
    }

    @Test
    void joinTest() throws InterruptedException {
        MyThread1 my_thread1 = new MyThread1("thd1");
        MyThread1 my_thread2 = new MyThread1("thd2");
        MyThread1 my_thread3 = new MyThread1("thd3");
        my_thread1.setNextThread(my_thread2);
        my_thread2.setNextThread(my_thread3);
        my_thread3.setNextThread(my_thread1);

        my_thread1.start();
        my_thread2.start();
        my_thread3.start();

        try {
            my_thread1.interrupt();
            my_thread1.join();
            my_thread2.join();
            my_thread3.join();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        System.out.println("main");

    }

    class Counter {
        private int c = 0;
        public synchronized void increment() {
            c++;
        }

        public synchronized void decrement() {
            c--;
        }
        public int value() {
            return c;
        }
    }

    class MyThread5 implements Runnable {

        Counter c;

        public MyThread5(Counter c) {
            this.c = c;
        }

        @Override
        public void run() {
            for (int i = 0; i < 100000; i++) {
                c.increment();
            }
        }
    }

    class MyThread6 implements Runnable {
        Counter c;
        public MyThread6(Counter c) {
            this.c = c;
        }
        @Override
        public void run() {
            for (int i = 0; i < 100000; i++) {
                c.decrement();
            }
        }
    }

    @Test
    void threadTest3() throws InterruptedException {
        Counter c = new Counter();
        Thread t1 = new Thread(new MyThread5(c));
        Thread t2 = new Thread(new MyThread6(c));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println(c.value());
    }
}
