package com.example.btprinter2.models;

public enum BarcodeSystem {
    JAN13(67),
    JAN8(68),
    CODE39(69),
    CODE128(73);

    private int value;

    private BarcodeSystem(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
