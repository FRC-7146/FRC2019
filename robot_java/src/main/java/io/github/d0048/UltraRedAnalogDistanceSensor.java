package io.github.d0048;

import edu.wpi.first.wpilibj.AnalogInput;

public class UltraRedAnalogDistanceSensor extends AnalogInput {

    public UltraRedAnalogDistanceSensor(int channel, String name) {
        super(channel);
        this.setName(name);
        this.setAverageBits(64);
        setGlobalSampleRate(8000);
    }

    // D(value)=434.735-55.18*ln(value)
    public double getDistance() {
        return 434.735 - 55.18 * Math.log(getAverageValue());
    }
}