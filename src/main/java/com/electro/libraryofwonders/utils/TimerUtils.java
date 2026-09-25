package com.electro.libraryofwonders.utils;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;

public class TimerUtils {
    // the list of currently ticking tasks
    private static final List<TimerTask> TASKS = new ArrayList<>();

    // buffer list for scheduled tasks, they sit here till the next server tick to prevent overwhelming the server and lag
    private static final List<TimerTask> PENDING_TASKS = new ArrayList<>();

    // initializes the timer system
    public static void init() {
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> tick());
    }

    // the loop that is called automatically every server tick to handle the actual things that will be done in the timer
    private static void tick() {
        if (!PENDING_TASKS.isEmpty()) {
            TASKS.addAll(PENDING_TASKS);
            PENDING_TASKS.clear();
        }

        Iterator<TimerTask> iterator = TASKS.iterator();
        while (iterator.hasNext()) {
            TimerTask task = iterator.next();
            if (task.tick()) {
                iterator.remove();
            }
        }
    }

    /**
     * Executes an action once after a specified delay in ticks.
     * (20 ticks = 1 second)
     */
    public static void schedule(int delayTicks, Runnable action) {
        PENDING_TASKS.add(new TimerTask(delayTicks, -1, action));
    }

    /**
     * Executes an action repeatedly at a fixed interval after an initial delay.
     * Note: This will run forever until the server stops.
     *
     * @param delayTicks    The initial wait time in ticks before the first execution.
     * @param intervalTicks The wait time in ticks between every subsequent execution.
     * @param action        The code block to execute repeatedly.
     *
     */
    public static void scheduleRepeating(int delayTicks, int intervalTicks, Runnable action) {
        PENDING_TASKS.add(new TimerTask(delayTicks, intervalTicks, action));
    }

    /**
     * Executes an action repeatedly, but allows the action to cancel the timer.
     * The action must return a boolean: return 'true' to cancel the timer, 'false' to keep it running.
     *
     * @param delayTicks    The initial wait time in ticks before the first execution.
     * @param intervalTicks The wait time in ticks between every subsequent execution.
     * @param action        The code block to execute. Must return a boolean:
     *                      return true to destroy the timer and stop execution,
     *                      return false to keep the timer alive for the next interval.
     */
    public static void scheduleConditional(int delayTicks, int intervalTicks, BooleanSupplier action) {
        PENDING_TASKS.add(new ConditionalTimerTask(delayTicks, intervalTicks, action));
    }

    /**
     * Instantly wipes all active and pending timers.
     */
    public static void clearAll() {
        TASKS.clear();
        PENDING_TASKS.clear();
    }

    /**
     * Internal container tracking the state and execution logic of a standard Runnable timer.
     */
    private static class TimerTask {
        protected int remainingTicks;
        protected final int intervalTicks;
        protected final Runnable action;

        public TimerTask(int delayTicks, int intervalTicks, Runnable action) {
            this.remainingTicks = delayTicks;
            this.intervalTicks = intervalTicks;
            this.action = action;
        }

        /**
         * Decrements the timer and executes the action if ready.
         *
         * @return true if the task is finished and should be deleted; false if it is still ticking.
         */
        public boolean tick() {
            if (remainingTicks > 0) {
                remainingTicks--;
                return false;
            }

            action.run();

            if (intervalTicks > 0) {
                remainingTicks = intervalTicks;
                return false;
            }

            return true;
        }
    }

    /**
     * Internal container tracking the state and execution logic of a BooleanSupplier timer.
     */
    private static class ConditionalTimerTask extends TimerTask {
        private final BooleanSupplier conditionalAction;

        public ConditionalTimerTask(int delayTicks, int intervalTicks, BooleanSupplier action) {
            super(delayTicks, intervalTicks, null);
            this.conditionalAction = action;
        }

        /**
         * Decrements the timer and executes the conditional action if ready.
         *
         * @return true if the task is finished (or self-canceled); false if it is still ticking.
         */
        @Override
        public boolean tick() {
            if (remainingTicks > 0) {
                remainingTicks--;
                return false;
            }

            boolean shouldCancel = conditionalAction.getAsBoolean();

            if (shouldCancel) {
                return true;
            }

            if (intervalTicks > 0) {
                remainingTicks = intervalTicks;
                return false;
            }

            return true;
        }
    }
}