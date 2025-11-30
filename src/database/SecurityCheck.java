package database;

import java.util.Queue;

public class SecurityCheck {
    public static boolean isAdministrator(String noTlp) {
        String query = "select peran from Pemilik where noPonsel='" + noTlp + "'";
        Queue<String> temp = DatabaseAccess.getDataSet(query);
        return temp != null && temp.poll().equals("Administrator");
    }

    public static boolean isPengelola(String noTlp) {
        String query = "select peran from Pemilik where noPonsel='" + noTlp + "'";
        Queue<String> temp = DatabaseAccess.getDataSet(query);
        return temp != null && temp.poll().equals("Pengelola");
    }

    public static boolean isPemilik(String noTlp) {
        String query = "select peran from Pemilik where noPonsel='" + noTlp + "'";
        Queue<String> temp = DatabaseAccess.getDataSet(query);
        return temp != null && temp.poll().equals("Pemilik");
    }
}