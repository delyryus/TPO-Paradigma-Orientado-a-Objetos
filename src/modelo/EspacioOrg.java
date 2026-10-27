package modelo;

/**
 * Alojamiento gestionado por una organizacion civil o religiosa.
 */
public class EspacioOrg extends Alojamiento {

    private String organizador;

    public EspacioOrg(String nombreUsuario, String clave, String nombre,
                      Ciudad ciudad, String direccion, int capacidad, String organizador) {
        super(nombreUsuario, clave, nombre, ciudad, direccion, capacidad);
        this.organizador = organizador;
    }

    public String getOrganizador() {
        return organizador;
    }

    public void setOrganizador(String organizador) {
        this.organizador = organizador;
    }

    @Override
    public String descripcion() {
        return "Espacio organizado por " + organizador + " - " + getNombre() + ", "
                + getDireccion() + ", " + getCiudad() + " (capacidad: " + getCapacidad() + ")";
    }
}
