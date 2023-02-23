import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class FirmwareUpdater {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws Exception {
        List<String> serverList = new ArrayList<>();
        // 添加服务器地址到 serverList 中
        // ...

        for (String serverAddress : serverList) {
            // 构造每个服务器的更新任务
            Supplier<String> updateTask = () -> {
                // TODO: 调用 redfish 的固件更新接口，返回更新结果
                // ...
                return "Firmware updated successfully for server " + serverAddress;
            };

            // 提交更新任务
            CompletableFuture<String> future = CompletableFuture.supplyAsync(updateTask, executorService);

            // 异步输出结果
            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    System.out.println("Failed to update firmware for server " + serverAddress + ": " + throwable.getMessage());
                } else {
                    System.out.println(result);
                }
            });
        }

        // 等待所有更新任务完成
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
}
