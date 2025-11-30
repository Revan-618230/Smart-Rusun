package entities;

public class Pemilik {
    public String nik;
    public String nama;
    public String alamat;
    public String noPonsel;
    public String peran;

    public Pemilik(String nik, String nama, String alamat, String noPonsel, String peran, String isActive) {
        this.nik = nik;
        this.nama = nama;
        this.alamat = alamat;
        this.noPonsel = noPonsel;
        this.peran = peran;
    }
}