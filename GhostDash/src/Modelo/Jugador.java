package modelo; 

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Jugador {
    private String nombre; // Nuevo campo: nombre real del usuario (si lo pides)
    private String nombreUsuario;
    private String contrasenaHash; // Nuevo campo: hash de la contraseña
    private int edad; // Nuevo campo: edad
    private boolean haJugadoPacManAntes; // Nuevo campo: si ha jugado antes
    private int puntajeMaximo;
    private int nivelesSuperados;
    private long tiempoJugado; // en segundos
    private int enemigosVencidos;
    private LocalDateTime fechaUltimoIngreso;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public Jugador() {
        this.fechaUltimoIngreso = LocalDateTime.now();
        this.puntajeMaximo = 0;
        this.nivelesSuperados = 0;
        this.tiempoJugado = 0;
        this.enemigosVencidos = 0;
        this.haJugadoPacManAntes = false; // Valor por defecto
    }

    // Constructor para un jugador nuevo con todos los datos
    public Jugador(String nombre, String nombreUsuario, String contrasenaHash, int edad, boolean haJugadoPacManAntes) {
        this(); // Llama al constructor por defecto para inicializar fechas y estadísticas
        this.nombre = nombre;
        this.nombreUsuario = nombreUsuario;
        this.contrasenaHash = contrasenaHash; // Ya asume que es un hash
        this.edad = edad;
        this.haJugadoPacManAntes = haJugadoPacManAntes;
    }

    // --- Métodos de acceso (getters) ---
    public String getNombre() {
        return nombre;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getContrasenaHash() { // Importante: solo obtener el hash, no la contraseña en texto plano
        return contrasenaHash;
    }

    public int getEdad() {
        return edad;
    }

    public boolean haJugadoPacManAntes() { // Convención para booleanos es is... o has...
        return haJugadoPacManAntes;
    }

    public int getPuntajeMaximo() {
        return puntajeMaximo;
    }

    public int getNivelesSuperados() {
        return nivelesSuperados;
    }

    public long getTiempoJugado() {
        return tiempoJugado;
    }

    public int getEnemigosVencidos() {
        return enemigosVencidos;
    }

    public LocalDateTime getFechaUltimoIngreso() {
        return fechaUltimoIngreso;
    }

    public String getFechaUltimoIngresoFormateada() {
        if (fechaUltimoIngreso == null)
            return "Nunca";
        return fechaUltimoIngreso.format(DATE_FORMATTER);
    }

    // --- Métodos de modificación (setters y otros) ---
    // Setters básicos si se necesitan, aunque es mejor pasar los datos en el
    // constructor
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public void setHaJugadoPacManAntes(boolean haJugadoPacManAntes) {
        this.haJugadoPacManAntes = haJugadoPacManAntes;
    }

    // Tu método `registrar` se fusiona con el constructor o un método para cargar
    // desde DB
    // public void registrar(String nombreUsuario) {
    // this.nombreUsuario = nombreUsuario;
    // this.fechaUltimoIngreso = LocalDateTime.now();
    // }

    public void iniciarSesion() {
        this.fechaUltimoIngreso = LocalDateTime.now();
    }

    public void actualizarEstadisticas(int puntuacion, int nivel, long tiempoAdicional, int enemigosComidos) {
        if (puntuacion > puntajeMaximo) {
            puntajeMaximo = puntuacion;
        }
        if (nivel > nivelesSuperados) {
            nivelesSuperados = nivel;
        }
        this.tiempoJugado += tiempoAdicional;
        this.enemigosVencidos += enemigosComidos;
    }

    // Métodos específicos para incrementar (redundantes si se usa
    // actualizarEstadisticas)
    // public void incrementarEnemigosVencidos(int cantidad) {
    // this.enemigosVencidos += cantidad;
    // }

    // public void agregarTiempoJugado(long segundos) {
    // this.tiempoJugado += segundos;
    // }

    @Override
    public String toString() {
        return String.format(
                "Jugador: %s | Nombre: %s | Edad: %d | Ha jugado Pac-Man antes: %b | Puntaje Máximo: %d | Niveles: %d | Tiempo jugado: %d s | Enemigos Vencidos: %d | Último ingreso: %s",
                nombreUsuario, nombre, edad, haJugadoPacManAntes, puntajeMaximo, nivelesSuperados, tiempoJugado,
                enemigosVencidos, getFechaUltimoIngresoFormateada());
    }
}