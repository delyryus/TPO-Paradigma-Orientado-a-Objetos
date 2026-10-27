package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Alojamiento que ofrece hospedaje durante la visita. Es abstracto porque cada
 * tipo concreto (parroquia, hotel, espacio de organizacion) define su propia
 * descripcion. Es el extremo "1" de la asociacion con Reserva: conoce y
 * administra sus propias reservas.
 */
public abstract class Alojamiento extends Usuario {

    private Ciudad ciudad;
    private String direccion;
    private int capacidad;
    private List<Reserva> reservas;

    public Alojamiento(String nombreUsuario, String clave, String nombre,
                       Ciudad ciudad, String direccion, int capacidad) {
        super(nombreUsuario, clave, nombre);
        this.ciudad = ciudad;
        this.direccion = direccion;
        setCapacidad(capacidad);
        this.reservas = new ArrayList<>();
    }

    public Ciudad getCiudad() {
        return ciudad;
    }

    public void setCiudad(Ciudad ciudad) {
        this.ciudad = ciudad;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getCapacidad() {
        return capacidad;
    }

    /**
     * La capacidad debe ser un entero positivo.
     */
    public void setCapacidad(int capacidad) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser un numero positivo.");
        }
        this.capacidad = capacidad;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    /**
     * Registra una reserva del asistente para el periodo indicado, siempre que
     * haya lugar durante todo el periodo.
     */
    public void reservar(Asistente asistente, LocalDate ingreso, LocalDate egreso) {
        if (asistente == null) {
            throw new IllegalArgumentException("El asistente es obligatorio.");
        }
        if (ingreso == null || egreso == null) {
            throw new IllegalArgumentException("Las fechas de ingreso y egreso son obligatorias.");
        }
        if (!egreso.isAfter(ingreso)) {
            throw new IllegalArgumentException("El egreso debe ser posterior al ingreso.");
        }
        if (!hayLugar(ingreso, egreso)) {
            throw new IllegalArgumentException("No hay capacidad disponible para todo el periodo solicitado "
                    + "en " + getNombre() + ".");
        }
        reservas.add(new Reserva(asistente, ingreso, egreso, EstadoReserva.ACTIVA));
    }

    /**
     * Cancela la reserva activa del asistente en este alojamiento.
     */
    public void cancelar(Asistente asistente) {
        for (Reserva reserva : reservas) {
            if (reserva.getAsistente() == asistente && reserva.estaActiva()) {
                reserva.cancelar();
                return;
            }
        }
        throw new IllegalArgumentException("El asistente no tiene una reserva activa en este alojamiento.");
    }

    /**
     * Indica si queda al menos una plaza libre durante todo el periodo. La
     * ocupacion se calcula dia por dia para respetar la capacidad en cada tramo.
     */
    public boolean hayLugar(LocalDate ingreso, LocalDate egreso) {
        return ocupacionMaxima(ingreso, egreso) < capacidad;
    }

    private int ocupacionMaxima(LocalDate ingreso, LocalDate egreso) {
        int maximo = 0;
        for (LocalDate dia = ingreso; dia.isBefore(egreso); dia = dia.plusDays(1)) {
            int ocupados = 0;
            for (Reserva reserva : reservas) {
                if (reserva.estaActiva() && reserva.seSuperpone(dia, dia.plusDays(1))) {
                    ocupados++;
                }
            }
            if (ocupados > maximo) {
                maximo = ocupados;
            }
        }
        return maximo;
    }

    /**
     * Descripcion util y legible del alojamiento para mostrar en la interfaz.
     */
    public abstract String descripcion();

    @Override
    public String getRol() {
        return "ALOJAMIENTO";
    }
}
