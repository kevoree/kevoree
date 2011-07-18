package org.kevoree.library.reasoner.ecj;

/**
 * Created by IntelliJ IDEA.
 * User: jbourcie
 * Date: 22/06/11
 * Time: 10:12
 * To change this template use File | Settings | File Templates.
 */
public class Precision {
    public boolean tempBeenEvaluated = false;
    public boolean smokeBeenEvaluated = false;
    public boolean humidityBeenEvaluated = false;

    public double tempPrecision = 0.0;
    public double smokePrecision = 0.0;
    public double humidityPrecision = 0.0;

    public boolean isCompleted(){
        return tempBeenEvaluated&&smokeBeenEvaluated&&humidityBeenEvaluated;
    }
    public boolean isTempBeenEvaluated() {
        return tempBeenEvaluated;
    }

    public boolean isSmokeBeenEvaluated() {
        return smokeBeenEvaluated;
    }

    public boolean isHumidityBeenEvaluated() {
        return humidityBeenEvaluated;
    }

    public double getTempPrecision() {
        return tempPrecision;
    }

    public void setTempPrecision(double tempPrecision) {
        this.tempPrecision = tempPrecision;
        if (tempPrecision > 100.0)
            this.tempPrecision = 100;
        if (tempPrecision < 0.0)
            this.tempPrecision = 0.0;
        this.tempBeenEvaluated = true;
    }

    public double getSmokePrecision() {
        return smokePrecision;
    }

    public void setSmokePrecision(double smokePrecision) {
        this.smokePrecision = smokePrecision;
        if (smokePrecision > 100.0)
            this.smokePrecision = 100;
        if (smokePrecision < 0.0)
            this.smokePrecision = 0.0;
        this.smokeBeenEvaluated = true;
    }

    public double getHumidityPrecision() {
        return humidityPrecision;
    }

    public void setHumidityPrecision(double humidityPrecision) {
        this.humidityPrecision = humidityPrecision;
        if (humidityPrecision > 100.0)
            this.humidityPrecision = 100;
        if (humidityPrecision < 0.0)
            this.humidityPrecision = 0.0;
        this.humidityBeenEvaluated = true;
    }
}
