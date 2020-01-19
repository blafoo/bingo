package de.blafoo.bingo;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.vaadin.flow.shared.Registration;

/**
 * Klasse zum versenden von Push-Nachrichten
 * https://vaadin.com/docs/v14/flow/advanced/tutorial-push-broadcaster.html
 */
public class Broadcaster {
	
    private static Executor executor = Executors.newSingleThreadExecutor();

    private static LinkedList<Consumer<String>> listeners = new LinkedList<>();

    public static synchronized Registration register(Consumer<String> listener) {
        listeners.add(listener);

        return () -> {
            synchronized (Broadcaster.class) {
                listeners.remove(listener);
            }
        };
    }

    public static synchronized void broadcast(String message) {
        for (Consumer<String> listener : listeners) {
            executor.execute(() -> listener.accept(message));
        }
    }
}

