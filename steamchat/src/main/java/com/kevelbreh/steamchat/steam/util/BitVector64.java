package com.kevelbreh.steamchat.steam.util;

public class BitVector64 {

    private long value;

    public void setValue(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public BitVector64() {

    }

    public BitVector64(long value) {
        this.value = value;
    }

    public long getMask(short offset, int mask) {
        return this.value >> offset & mask;
    }

    public void setMask(short offset, long mask, long value) {
        this.value = (this.value & ~(mask << offset)) | ((value & mask) << offset);
    }

}
