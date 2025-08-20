package org.figuramc.figura_client.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RenderTasks {

    public static final Queue<Runnable> TASKS = new ConcurrentLinkedDeque<>();

    public static void runOnRenderThread(Runnable task) {
        TASKS.add(task);
    }

}
