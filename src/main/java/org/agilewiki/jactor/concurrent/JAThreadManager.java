/*
 * Copyright 2011 Bill La Forge
 *
 * This file is part of AgileWiki and is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (LGPL) as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * or navigate to the following url http://www.gnu.org/licenses/lgpl-2.1.txt
 *
 * Note however that only Scala, Java and JavaScript files are being covered by LGPL.
 * All other files are covered by the Common Public License (CPL).
 * A copy of this license is also included and can be
 * found as well at http://www.opensource.org/licenses/cpl1.0.txt
 */
package org.agilewiki.jactor.concurrent;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;

/**
 * A high performance implementation of ThreadManager.
 */
final public class JAThreadManager implements ThreadManager {
    /**
     * The taskRequest semaphore is used to wake up a thread
     * when there is a task to process.
     */
    final private Semaphore taskRequest = new Semaphore(0);

    /**
     * The tasks queue holds the tasks waiting to be processed.
     */
    final private ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<Runnable>();

    /**
     * When closing is true, concurrent exit as they finish their assigned tasks.
     */
    private Boolean closing = false;

    /**
     * The threadCount is the number of threads used.
     */
    private int threadCount;

    /**
     * The worker threads.
     */
    final private ArrayList<Thread> threads = new ArrayList<Thread>();

    /**
     * Create a JAThreadManager
     *
     * @param threadCount The number of concurrent to be used.
     * @return A new JAThreadManager.
     */
    public static ThreadManager newThreadManager(int threadCount) {
        ThreadFactory threadFactory = new JAThreadFactory("JActor-Thread");
        return newThreadManager(threadCount, threadFactory);
    }

    /**
     * Create a JAThreadManager
     *
     * @param threadCount The number of concurrent to be used.
     * @return A new JAThreadManager.
     */
    public static ThreadManager newThreadManager(int threadCount, String baseThreadName) {
        ThreadFactory threadFactory = new JAThreadFactory(baseThreadName);
        return newThreadManager(threadCount, threadFactory);
    }

    /**
     * Create a JAThreadManager
     *
     * @param threadCount   The number of concurrent to be used.
     * @param threadFactory Used to create the concurrent.
     * @return A new JAThreadManager.
     */
    public static ThreadManager newThreadManager(int threadCount, ThreadFactory threadFactory) {
        ThreadManager threadManager = new JAThreadManager();
        threadManager.start(threadCount, threadFactory);
        return threadManager;
    }

    /**
     * Create and start the concurrent.
     *
     * @param threadCount   The number of concurrent to be used.
     * @param threadFactory Used to create the concurrent.
     */
    @Override
    final public void start(int threadCount, ThreadFactory threadFactory) {
        this.threadCount = threadCount;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        taskRequest.acquire();
                    } catch (InterruptedException e) {
                    }
                    if (closing) return;
                    Runnable task = tasks.poll();
                    try {
                        task.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        int c = 0;
        while (c < threadCount) {
            c += 1;
            Thread t = threadFactory.newThread(runnable);
            threads.add(t);
            t.start();
        }
    }

    /**
     * Begin running a task.
     *
     * @param task A task to be processed on another thread.
     */
    @Override
    final public void process(Runnable task) {
        tasks.add(task);
        taskRequest.release();
    }

    /**
     * The close method is used to stop all the threads as they become idle.
     * This method sets a flag to indicate that the concurrent should stop
     * and then wakes up all the concurrent.
     * This method only returns after all the threads have died.
     */
    @Override
    final public void close() {
        closing = true;
        int c = 0;
        while (c < threadCount) {
            c += 1;
            taskRequest.release();
        }
        c = 0;
        Thread ct = Thread.currentThread();
        while (c < threadCount) {
            Thread t = threads.get(c);
            if (ct != t) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                }
            }
            c += 1;
        }
    }
}
