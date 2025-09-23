package hbnu.project.zhiyancommon.utils;

import hbnu.project.zhiyancommon.utils.id.SnowflakeIdUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 雪花ID工具类使用示例和测试
 */
public class SnowflakeIdUtilTest {

    public static void main(String[] args) {
        // 基本使用示例
        basicUsageExample();

        // 并发测试
        concurrencyTest();

        // 解析测试
        parseTest();
    }

    /**
     * 基本使用示例
     */
    private static void basicUsageExample() {
        System.out.println("=== 基本使用示例 ===");

        // 1. 使用默认配置生成ID
        long id1 = SnowflakeIdUtil.nextId();
        String id1Str = SnowflakeIdUtil.nextIdStr();
        System.out.println("默认生成的雪花ID: " + id1);
        System.out.println("默认生成的雪花ID字符串: " + id1Str);

        // 2. 指定数据中心ID和工作机器ID生成ID
        long id2 = SnowflakeIdUtil.nextId(2, 3);
        String id2Str = SnowflakeIdUtil.nextIdStr(2, 3);
        System.out.println("指定参数生成的雪花ID: " + id2);
        System.out.println("指定参数生成的雪花ID字符串: " + id2Str);

        // 3. 连续生成多个ID
        System.out.println("连续生成的ID:");
        for (int i = 0; i < 5; i++) {
            System.out.println("ID " + (i + 1) + ": " + SnowflakeIdUtil.nextId());
        }

        System.out.println();
    }

    /**
     * 并发测试 - 验证ID的唯一性
     */
    private static void concurrencyTest() {
        System.out.println("=== 并发测试 ===");

        int threadCount = 100;
        int idsPerThread = 1000;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        Set<Long> allIds = new HashSet<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                Set<Long> threadIds = new HashSet<>();
                for (int j = 0; j < idsPerThread; j++) {
                    threadIds.add(SnowflakeIdUtil.nextId());
                }

                synchronized (allIds) {
                    allIds.addAll(threadIds);
                }

                latch.countDown();
            });
        }

        try {
            latch.await();
            long endTime = System.currentTimeMillis();

            int expectedCount = threadCount * idsPerThread;
            System.out.println("预期生成ID数量: " + expectedCount);
            System.out.println("实际生成ID数量: " + allIds.size());
            System.out.println("是否存在重复: " + (allIds.size() < expectedCount ? "是" : "否"));
            System.out.println("耗时: " + (endTime - startTime) + " ms");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        System.out.println();
    }

    /**
     * 解析测试
     */
    private static void parseTest() {
        System.out.println("=== 解析测试 ===");

        // 生成一个雪花ID并解析
        long snowflakeId = SnowflakeIdUtil.nextId();
        SnowflakeIdUtil.SnowflakeInfo info = SnowflakeIdUtil.parseSnowflakeId(snowflakeId);

        System.out.println("雪花ID: " + snowflakeId);
        System.out.println("解析结果: " + info);

        // 验证解析的时间戳
        System.out.println("生成时间戳: " + info.getTimestamp());
        System.out.println("当前时间戳: " + System.currentTimeMillis());
        System.out.println("数据中心ID: " + info.getDatacenterId());
        System.out.println("工作机器ID: " + info.getWorkerId());
        System.out.println("序列号: " + info.getSequence());

        System.out.println();
    }
}
