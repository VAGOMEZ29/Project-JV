package modelo;

public enum EstadoFantasma {
    NORMAL, // Comportamiento por defecto (puede ser perseguir o dispersar)
    HUIDA, // Huyendo de Pac-Man invencible.
    PARPADEANDO // El modo HUIDA está por terminar.
}