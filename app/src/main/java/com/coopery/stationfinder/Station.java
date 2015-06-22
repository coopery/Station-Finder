package com.coopery.stationfinder;


public class Station {

    // Constructor with all data but format
    public Station(String callSign, double frequency,
                   double dlat, double mlat, double slat, String ns,
                   double dlon, double mlon, double slon, String ew) {

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


    public void setFormat(String format) {
        this.format = format;
    }


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



    // Getters

    public String getCallSign() {
        return callSign;
    }

    public double getFrequency() {
        return frequency;
    }

    public String getFormat() {
        return format;
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
