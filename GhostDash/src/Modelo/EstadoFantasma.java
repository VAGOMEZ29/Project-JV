package modelo;

// Al principio solo había 3 estados, pero se agregaron más para permitir un comportamiento más complejo y realista de los fantasmas.

public enum EstadoFantasma {

    NORMAL,       // Estado por defecto: el fantasma persigue a Pac-Man normalmente.

    HUIDA,        // Estado cuando Pac-Man obtiene un power-up de invencibilidad y los fantasmas deben huir.

    PARPADEANDO,  // Indica que el tiempo de HUIDA está por terminar. El fantasma comienza a parpadear como advertencia visual.

    COMIENDO,     // Estado en el que el fantasma ha sido "comido" por Pac-Man mientras está invencible.

    REGRESANDO    // Estado en el que el fantasma vuelve a su posición inicial después de ser comido.

}