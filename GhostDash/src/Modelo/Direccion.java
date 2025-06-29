package Modelo;

public enum Direccion {
    ARRIBA, ABAJO, IZQUIERDA, DERECHA;

    public Direccion invertir() {
        return switch (this) {
            case ARRIBA -> ABAJO;
            case ABAJO -> ARRIBA;
            case IZQUIERDA -> DERECHA;
            case DERECHA -> IZQUIERDA;
        };
    }

    public int getDx() {
        return switch (this) {
            case IZQUIERDA -> -1;
            case DERECHA -> 1;
            default -> 0;
        };
    }

    public int getDy() {
        return switch (this) {
            case ARRIBA -> -1;
            case ABAJO -> 1;
            default -> 0;
        };
    }

    public static Direccion aleatoria() {
        Direccion[] dirs = values();
        return dirs[(int) (Math.random() * dirs.length)];
    }
}
