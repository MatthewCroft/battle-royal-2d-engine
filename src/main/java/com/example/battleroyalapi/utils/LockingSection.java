package com.example.battleroyalapi.utils;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.*;

public class LockingSection {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void withRead(Runnable task) {
        lock.readLock().lock();
        try {
            task.run();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void withWrite(Runnable task) {
        lock.writeLock().lock();
        try {
            task.run();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public <T> T withRead(Supplier<T> task) {
        lock.readLock().lock();
        try {
            return task.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T> T withWrite(Supplier<T> task) {
        lock.writeLock().lock();
        try {
            return task.get();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public <T> void withWrite(Consumer<T> consumer, T arg) {
        lock.writeLock().lock();
        try {
            consumer.accept(arg);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public <T> void withRead(Consumer<T> consumer, T arg) {
        lock.readLock().lock();
        try {
            consumer.accept(arg);
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T, R> R withWrite(Function<T, R> function, T arg) {
        lock.writeLock().lock();
        try {
            return function.apply(arg);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public <T, R> R withRead(Function<T, R> function, T arg) {
        lock.readLock().lock();
        try {
            return function.apply(arg);
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T, U> void withWrite(BiConsumer<T, U> consumer, T arg1, U arg2) {
        lock.writeLock().lock();
        try {
            consumer.accept(arg1, arg2);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public <T, U> void withRead(BiConsumer<T, U> consumer, T arg1, U arg2) {
        lock.readLock().lock();
        try {
            consumer.accept(arg1, arg2);
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T, U, R> R withWrite(BiFunction<T, U, R> function, T arg1, U arg2) {
        lock.writeLock().lock();
        try {
            return function.apply(arg1, arg2);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public <T, U, R> R withRead(BiFunction<T, U, R> function, T arg1, U arg2) {
        lock.readLock().lock();
        try {
            return function.apply(arg1, arg2);
        } finally {
            lock.readLock().unlock();
        }
    }
}

