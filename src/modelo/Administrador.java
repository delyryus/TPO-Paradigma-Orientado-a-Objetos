package modelo;

/**
 * Cuenta de administrador. Solo existe precargada en los datos, no se puede
 * registrar desde la interfaz.
 */
public class Administrador extends Usuario {

    public Administrador(String nombreUsuario, String clave, String nombre) {
        super(nombreUsuario, clave, nombre);
    }

    @Override
    public String getRol() {
        return "ADMINISTRADOR";
    }
}
