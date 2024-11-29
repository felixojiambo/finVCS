package com.pesapal.felixvcs.utils;

/**
 * A functional interface for receiving progress updates.
 */
@FunctionalInterface
public interface ProgressListener {
    /**
     * Called to update progress.
     *
     * @param completed The number of completed tasks.
     * @param total     The total number of tasks.
     */
    void update(long completed, long total);
}
