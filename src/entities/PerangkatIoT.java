package entities;

public class PerangkatIoT {
    public String sn;
    public String unit;
    public String status;
    public String nik;
    public double currentUsage;
    public double totalUsage;

    public PerangkatIoT(String sn, String unit, String status, String nik) {
        this.sn = sn;
        this.unit = unit;
        this.status = status;
        this.nik = nik;
        this.currentUsage = 0.0;
        this.totalUsage = 0.0;
    }
}