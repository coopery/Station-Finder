package com.coopery.stationfinder;


public class Station {

    public Station() {

    }

    public Station(String callSign, double frequency,
           String ns, double dlat, double mlat, double slat,
           String ew, double dlon, double mlon, double slon) {

        this.callSign = callSign;
        this.frequency = frequency;
        this.format = "no format yet";

        this.dlat = dlat;
        this.mlat = mlat;
        this.slat = slat;
        this.ns = ns;

        this.dlon = dlon;
        this.mlon = mlon;
        this.slon = slon;
        this.ew = ew;
    }

    public String toString() {
        return frequency + " " + callSign + " -- " + format;
    }


    // Fields

    private String callSign;
    private double frequency;
    private String format;

    private double dlat;
    private double mlat;
    private double slat;
    private String ns;

    private double dlon;
    private double mlon;
    private double slon;
    private String ew;


    // Setters

    public void setFormat(String format) {
        this.format = format;
    }
    public void setCallSign(String callSign) {
        this.callSign = callSign;
    }
    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }
    public void setDlat(double dlat) {
        this.dlat = dlat;
    }
    public void setMlat(double mlat) {
        this.mlat = mlat;
    }
    public void setSlat(double slat) {
        this.slat = slat;
    }
    public void setNs(String ns) {
        this.ns = ns;
    }
    public void setDlon(double dlon) {
        this.dlon = dlon;
    }
    public void setMlon(double mlon) {
        this.mlon = mlon;
    }
    public void setSlon(double slon) {
        this.slon = slon;
    }
    public void setEw(String ew) {
        this.ew = ew;
    }


    // Getters

    public String getFormat() {
        return format;
    }
    public String getCallSign() {
        return callSign;
    }
    public double getFrequency() {
        return frequency;
    }
    public double getDlat() {
        return dlat;
    }
    public double getMlat() {
        return mlat;
    }
    public double getSlat() {
        return slat;
    }
    public String getNs() {
        return ns;
    }
    public double getDlon() {
        return dlon;
    }
    public double getMlon() {
        return mlon;
    }
    public double getSlon() {
        return slon;
    }
    public String getEw() {
        return ew;
    }

}
