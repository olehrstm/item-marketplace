package de.ole101.marketplace.common.i18n;

import lombok.Getter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Getter
public class TranslationContext {

    private final Map<String, Object> arguments;

    public TranslationContext() {
        this.arguments = new HashMap<>();
    }

    public Object getArgument(String key) {
        return this.arguments.get(key);
    }

    /**
     * Sets an argument value. Returns this instance for method chaining.
     */
    public TranslationContext with(String key, Object value) {
        this.arguments.put(key, value);
        return this;
    }

    /**
     * Sets a number value. Returns this instance for method chaining.
     */
    public TranslationContext withNumber(String key, Number value) {
        return with(key, value);
    }

    /**
     * Sets a date/time value. Returns this instance for method chaining.
     */
    public TranslationContext withDateTime(String key, LocalDateTime value) {
        return with(key, value);
    }

    /**
     * Sets a date value. Returns this instance for method chaining.
     */
    public TranslationContext withDate(String key, LocalDate value) {
        return with(key, value);
    }

    /**
     * Sets a list value. Returns this instance for method chaining.
     */
    public TranslationContext withList(String key, List<?> value) {
        return with(key, value);
    }

    /**
     * Sets a duration value. Returns this instance for method chaining.
     */
    public TranslationContext withDuration(String key, Duration value) {
        return with(key, value);
    }

    /**
     * Sets a value lazily, only if the key is actually used in the translation.
     * Useful for expensive computations that might not be needed.
     */
    public TranslationContext withLazy(String key, Supplier<Object> supplier) {
        this.arguments.put(key, new LazyValue(supplier));
        return this;
    }

    /**
     * Combines another context with this one. Values from the other context will
     * override any existing values with the same key in this context.
     */
    public TranslationContext merge(TranslationContext other) {
        if (other != null) {
            this.arguments.putAll(other.arguments);
        }
        return this;
    }

    /**
     * Lazily evaluated value wrapper
     */
    private static class LazyValue {

        private final Supplier<Object> supplier;
        private Object value;
        private boolean evaluated;

        public LazyValue(Supplier<Object> supplier) {
            this.supplier = supplier;
        }

        @Override
        public String toString() {
            if (!this.evaluated) {
                this.value = this.supplier.get();
                this.evaluated = true;
            }
            return this.value != null ? this.value.toString() : "";
        }
    }
}
