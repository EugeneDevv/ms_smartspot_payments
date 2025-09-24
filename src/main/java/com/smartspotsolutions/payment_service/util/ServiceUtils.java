package com.smartspotsolutions.payment_service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class ServiceUtils {
    /**
     * Updates a field using its setter if the newValue is not null and is different from the currentValue.
     * Logs the change.
     *
     * @param getter Supplier for the current value of the field.
     * @param setter Consumer to set the new value of the field.
     * @param newValue The new value to set.
     * @param fieldName The name of the field for logging purposes.
     * @param <T> The type of the field.
     */
    public static <T> void updateIfDifferent(Supplier<T> getter, Consumer<T> setter, T newValue, String fieldName) {
        if (newValue != null && !newValue.equals(getter.get())) {
            log.info("{} changed from '{}' to '{}'", fieldName, getter.get(), newValue);
            setter.accept(newValue);
        }
    }

    /**
     * Helper to chain JPA Specifications with an AND operator.
     *
     * @param base The base specification, can be null.
     * @param addition The specification to add.
     * @param <T> The entity type.
     * @return A combined specification or the addition if base is null.
     */
    public static <T> Specification<T> and(Specification<T> base, Specification<T> addition) {
        return (base == null) ? addition : base.and(addition);
    }
}
