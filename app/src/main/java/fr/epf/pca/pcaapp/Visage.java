package fr.epf.pca.pcaapp;

public class Visage {
    public static final int HAUTEUR = 150;
    public static final int LARGEUR = 150;
    public static final int TAILLE = HAUTEUR*LARGEUR;

    private int pixels[];


    public Visage() {
        pixels = new int[TAILLE];
    }

    public Visage(int p[]) {
        this();
        pixels = p;
    }

    public Visage(int p[][]) {
        this();

        for (int colonne = 0;colonne < LARGEUR;colonne++)
            for (int ligne = 0;ligne < HAUTEUR;ligne++)
                pixels[ligne+colonne*HAUTEUR] = p[ligne][colonne];
    }


    public int[] getPixels() {
        return pixels;
    }

    public void setPixels(int p[]) {
        pixels = p;
    }
}