package com.ramussoft.report;

public class SerialNumber {

    private int number;

    public SerialNumber(int number) {
        this.number = number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return String.valueOf(number);
    }
}
