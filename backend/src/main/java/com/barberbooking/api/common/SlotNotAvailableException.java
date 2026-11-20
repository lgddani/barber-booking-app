package com.barberbooking.api.common;

public class SlotNotAvailableException extends RuntimeException{
    public SlotNotAvailableException(){
        super("Ese horario ya no está disponible");
    }
}
