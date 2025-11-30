package entities;

public class UnitSarusun {
    public String id;
    public String status;
    public String pemilikNik;
    public String lantai;
    public String tower;

    public UnitSarusun(String id, String status, String pemilikNik, String lantai, String tower) {
        this.id = id;
        this.status = status;
        this.pemilikNik = pemilikNik;
        this.lantai = lantai;
        this.tower = tower;
    }
}