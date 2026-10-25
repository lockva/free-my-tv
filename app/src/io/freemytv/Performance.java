package io.freemytv;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.SystemClock;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Samples system CPU, memory and data-partition use off the UI thread. */
final class Performance {
    private final Context context;
    private final Runnable changed;
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "FreeMyTV-performance");
        t.setDaemon(true);
        return t;
    });
    private ScheduledFuture<?> task;
    private long previousTotal = -1, previousIdle = -1;
    private long previousProcessCpu = -1, previousProcessTime = -1;
    volatile int cpu = -1, memory = -1, storage = -1;
    volatile String cpuText = "--%", memoryText = "--%", storageText = "--%";

    Performance(Context context, Runnable changed) {
        this.context = context.getApplicationContext();
        this.changed = changed;
    }

    synchronized void start() {
        if (task == null || task.isCancelled()) task = worker.scheduleAtFixedRate(this::sample, 0, 1, TimeUnit.SECONDS);
    }

    synchronized void stop() {
        if (task != null) {
            task.cancel(false);
            task = null;
        }
    }

    void close() {
        stop();
        worker.shutdownNow();
    }

    private void sample() {
        readCpu();
        readMemory();
        readStorage();
        cpuText = percent(cpu);
        memoryText = percent(memory);
        storageText = percent(storage);
        changed.run();
    }

    private void readCpu() {
        try (BufferedReader in = new BufferedReader(new FileReader("/proc/stat"))) {
            String line = in.readLine();
            if (line == null || !line.startsWith("cpu ")) {
                readProcessCpu();
                return;
            }
            String[] fields = line.trim().split("\\s+");
            long total = 0;
            for (int i = 1; i < fields.length; i++) total += Long.parseLong(fields[i]);
            long idle = Long.parseLong(fields[4]) + (fields.length > 5 ? Long.parseLong(fields[5]) : 0);
            if (previousTotal >= 0 && total > previousTotal) {
                long elapsed = total - previousTotal;
                cpu = clamp((int) (100 * (elapsed - (idle - previousIdle)) / elapsed));
            }
            previousTotal = total;
            previousIdle = idle;
            return;
        } catch (IOException | RuntimeException ignored) {
            // Android releases may restrict /proc/stat to shell/root; fall back to this app's process CPU time.
        }
        readProcessCpu();
    }

    private void readProcessCpu() {
        long processCpu = android.os.Process.getElapsedCpuTime();
        long now = SystemClock.elapsedRealtime();
        if (previousProcessCpu >= 0 && processCpu >= previousProcessCpu && now > previousProcessTime) {
            long wall = now - previousProcessTime;
            int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
            cpu = clamp(Math.round(100f * (processCpu - previousProcessCpu) / wall / cores));
        }
        previousProcessCpu = processCpu;
        previousProcessTime = now;
    }

    private void readMemory() {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            if (manager == null) { memory = -1; return; }
            manager.getMemoryInfo(info);
            memory = info.totalMem > 0 ? clamp((int) (100 * (info.totalMem - info.availMem) / info.totalMem)) : -1;
        } catch (RuntimeException ignored) {
            memory = -1;
        }
    }

    private void readStorage() {
        try {
            StatFs stats = new StatFs(Environment.getDataDirectory().getPath());
            long total = stats.getTotalBytes(), available = stats.getAvailableBytes();
            storage = total > 0 ? clamp((int) (100 * (total - available) / total)) : -1;
        } catch (RuntimeException ignored) {
            storage = -1;
        }
    }

    private static int clamp(int value) { return Math.max(0, Math.min(100, value)); }
    private static String percent(int value) { return value < 0 ? "--%" : value + "%"; }
}
