package modelo;

/**
 * Representa una cuenta del sistema. Es una clase abstracta porque nunca se
 * crean usuarios "genericos": toda cuenta es un asistente, un administrador o
 * un alojamiento (que a su vez puede ser parroquia, hotel o espacio de organizacion).
 */
public abstract class Usuario {

    protected String nombre;
    protected String clave;
    protected String nombreUsuario;

    public Usuario(String nombreUsuario, String clave, String nombre) {
        this.nombreUsuario = nombreUsuario;
        this.clave = clave;
        this.nombre = nombre;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Verifica si la clave ingresada coincide con la clave de esta cuenta.
     */
    public boolean validarClave(String claveIngresada) {
        return this.clave.equals(claveIngresada);
    }

    /**
     * Devuelve el rol de la cuenta, definido por cada subclase.
     */
    public abstract String getRol();
}
