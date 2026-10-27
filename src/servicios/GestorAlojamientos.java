package servicios;

import modelo.*;
import persistencia.PersistenciaJSON;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona la consulta y modificacion de datos y capacidad de los alojamientos,
 * respetando las reservas vigentes.
 */
public class GestorAlojamientos {

    private final GestorUsuarios gestorUsuarios;
    private final GestorReservas gestorReservas;
    private final PersistenciaJSON persistencia;

    public GestorAlojamientos(GestorUsuarios gestorUsuarios, GestorReservas gestorReservas,
                              PersistenciaJSON persistencia) {
        this.gestorUsuarios = gestorUsuarios;
        this.gestorReservas = gestorReservas;
        this.persistencia = persistencia;
    }

    /**
     * Modifica los datos propios del alojamiento autenticado.
     */
    public void modificarDatos(Alojamiento alojamiento, String nombre,
                               Ciudad ciudad, String direccion) {
        if (alojamiento == null || !gestorUsuarios.getUsuarios().contains(alojamiento)) {
            throw new IllegalArgumentException("El alojamiento no existe.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (direccion == null || direccion.trim().isEmpty()) {
            throw new IllegalArgumentException("La direccion es obligatoria.");
        }
        alojamiento.setNombre(nombre.trim());
        alojamiento.setCiudad(ciudad);
        alojamiento.setDireccion(direccion.trim());
        guardarCuentas();
    }

    /**
     * Modifica la capacidad de un alojamiento, impidiendo que quede por debajo
     * de la ocupacion existente por reservas activas.
     */
    public void modificarCapacidad(Alojamiento alojamiento, int nuevaCapacidad) {
        if (alojamiento == null || !gestorUsuarios.getUsuarios().contains(alojamiento)) {
            throw new IllegalArgumentException("El alojamiento no existe.");
        }
        if (nuevaCapacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser un numero positivo.");
        }
        int ocupacionMaxima = gestorReservas.ocupacionMaxima(alojamiento);
        if (nuevaCapacidad < ocupacionMaxima) {
            throw new IllegalArgumentException("No se puede reducir la capacidad a " + nuevaCapacidad
                    + " porque hay una ocupacion maxima de " + ocupacionMaxima + " por reservas activas.");
        }
        alojamiento.setCapacidad(nuevaCapacidad);
        guardarCuentas();
    }

    /**
     * Devuelve las reservas activas del alojamiento que se superponen con el
     * periodo indicado, para consultar los huespedes alojados.
     */
    public List<Reserva> huespedesEnPeriodo(Alojamiento alojamiento, LocalDate desde, LocalDate hasta) {
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : gestorReservas.reservasDeAlojamiento(alojamiento)) {
            if (r.estaActiva() && r.getIngreso().isBefore(hasta) && r.getEgreso().isAfter(desde)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    private void guardarCuentas() {
        try {
            persistencia.guardarCuentas(gestorUsuarios.getUsuarios());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar cuentas.json: " + e.getMessage(), e);
        }
    }
}
