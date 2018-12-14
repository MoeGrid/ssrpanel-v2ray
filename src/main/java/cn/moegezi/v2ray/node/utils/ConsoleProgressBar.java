package cn.moegezi.v2ray.node.utils;

import java.text.DecimalFormat;

public class ConsoleProgressBar {

    private long minimum = 0;

    private long maximum = 100;

    private long barLen = 30;

    public ConsoleProgressBar() {

    }

    public ConsoleProgressBar(long minimum, long maximum, long barLen) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.barLen = barLen;
    }

    private DecimalFormat formater = new DecimalFormat("#.##%");

    public void show(long value) {
        if (value < minimum || value > maximum) {
            return;
        }
        reset();
        minimum = value;
        float rate = (float) (minimum * 1.0 / maximum);
        long len = (long) (rate * barLen);
        draw(len, rate);
        if (minimum == maximum) {
            afterComplete();
        }
    }

    private void draw(long len, float rate) {
        System.out.print("Downloading [");
        for (int i = 0; i < barLen; i++) {
            System.out.print(i < len ? '=' : ' ');
        }
        System.out.print("] ");
        System.out.print(format(rate));
    }

    private void reset() {
        System.out.print('\r');
    }

    private void afterComplete() {
        System.out.print('\n');
    }

    private String format(float num) {
        return formater.format(num);
    }

}