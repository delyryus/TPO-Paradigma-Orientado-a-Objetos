package modelo;

/**
 * Alojamiento de tipo hotel. La categoria se expresa en estrellas (de 1 a 5).
 */
public class Hotel extends Alojamiento {

    /** Rango valido de categorias (estrellas). */
    public static final int CATEGORIA_MINIMA = 1;
    public static final int CATEGORIA_MAXIMA = 5;

    private int categoria;

    public Hotel(String nombreUsuario, String clave, String nombre,
                 Ciudad ciudad, String direccion, int capacidad, int categoria) {
        super(nombreUsuario, clave, nombre, ciudad, direccion, capacidad);
        setCategoria(categoria);
    }

    public int getCategoria() {
        return categoria;
    }

    /**
     * Valida que la categoria este entre 1 y 5 estrellas.
     */
    public void setCategoria(int categoria) {
        if (categoria < CATEGORIA_MINIMA || categoria > CATEGORIA_MAXIMA) {
            throw new IllegalArgumentException("La categoria debe estar entre "
                    + CATEGORIA_MINIMA + " y " + CATEGORIA_MAXIMA + " estrellas.");
        }
        this.categoria = categoria;
    }

    @Override
    public String descripcion() {
        return "Hotel " + getNombre() + " - " + getDireccion() + ", " + getCiudad()
                + " (" + categoria + " estrellas, capacidad: " + getCapacidad() + ")";
    }
}
