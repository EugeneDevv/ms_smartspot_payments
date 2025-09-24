package com.smartspotsolutions.payment_service.io;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CallbackMetadata {
    private Item[] Item;

    @Setter
    @Getter
    public static class Item {
        private String Name;
        private Object Value;
    }
}
