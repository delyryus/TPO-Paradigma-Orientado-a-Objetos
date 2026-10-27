package modelo;

/**
 * Alojamiento de tipo parroquia. Tiene un responsable a cargo.
 */
public class Parroquia extends Alojamiento {

    private String responsable;

    public Parroquia(String nombreUsuario, String clave, String nombre,
                     Ciudad ciudad, String direccion, int capacidad, String responsable) {
        super(nombreUsuario, clave, nombre, ciudad, direccion, capacidad);
        this.responsable = responsable;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    @Override
    public String descripcion() {
        return "Parroquia " + getNombre() + " - " + getDireccion() + ", "
                + getCiudad() + " (responsable: " + responsable
                + ", capacidad: " + getCapacidad() + ")";
    }
}
