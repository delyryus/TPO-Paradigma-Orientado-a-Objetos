package servicios;

import modelo.*;
import persistencia.PersistenciaJSON;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Coordina las reservas que pertenecen a cada alojamiento. La logica de alta y
 * cancelacion vive en el modelo (Alojamiento.reservar / cancelar); este gestor
 * aplica las reglas que cruzan alojamientos (una sola reserva activa por
 * asistente) y se ocupa de la persistencia.
 */
public class GestorReservas {

    private final GestorUsuarios gestorUsuarios;
    private final PersistenciaJSON persistencia;

    public GestorReservas(GestorUsuarios gestorUsuarios, PersistenciaJSON persistencia) {
        this.gestorUsuarios = gestorUsuarios;
        this.persistencia = persistencia;
    }

    /**
     * Devuelve todas las reservas de todos los alojamientos.
     */
    public List<Reserva> getReservas() {
        List<Reserva> resultado = new ArrayList<>();
        for (Alojamiento a : gestorUsuarios.listarAlojamientos()) {
            resultado.addAll(a.getReservas());
        }
        return resultado;
    }

    /**
     * Devuelve la reserva activa de un asistente, o null si no tiene.
     */
    public Reserva reservaActivaDe(Asistente asistente) {
        for (Alojamiento a : gestorUsuarios.listarAlojamientos()) {
            for (Reserva r : a.getReservas()) {
                if (r.getAsistente() == asistente && r.estaActiva()) {
                    return r;
                }
            }
        }
        return null;
    }

    /**
     * Devuelve el alojamiento al que pertenece una reserva.
     */
    public Alojamiento alojamientoDe(Reserva reserva) {
        for (Alojamiento a : gestorUsuarios.listarAlojamientos()) {
            if (a.getReservas().contains(reserva)) {
                return a;
            }
        }
        return null;
    }

    /**
     * Crea una reserva nueva validando las reglas que exceden a un alojamiento:
     * el asistente no puede tener mas de una reserva activa a la vez.
     */
    public Reserva crearReserva(Asistente asistente, Alojamiento alojamiento,
                                LocalDate ingreso, LocalDate egreso) {
        if (asistente == null) {
            throw new IllegalArgumentException("El asistente es obligatorio.");
        }
        if (alojamiento == null) {
            throw new IllegalArgumentException("El alojamiento es obligatorio.");
        }
        if (reservaActivaDe(asistente) != null) {
            throw new IllegalArgumentException("Ya tenes una reserva activa. Cancelala antes de reservar otro alojamiento.");
        }
        alojamiento.reservar(asistente, ingreso, egreso);
        guardarReservas();
        List<Reserva> reservas = alojamiento.getReservas();
        return reservas.get(reservas.size() - 1);
    }

    /**
     * Cancela la reserva activa del asistente, en el alojamiento que corresponda.
     */
    public void cancelarReserva(Asistente asistente) {
        Reserva reserva = reservaActivaDe(asistente);
        if (reserva == null) {
            throw new IllegalArgumentException("No tenes ninguna reserva activa.");
        }
        Alojamiento alojamiento = alojamientoDe(reserva);
        if (alojamiento == null) {
            throw new IllegalStateException("No se encontro el alojamiento de la reserva.");
        }
        alojamiento.cancelar(asistente);
        guardarReservas();
    }

    public List<Reserva> reservasDeAlojamiento(Alojamiento alojamiento) {
        List<Reserva> resultado = new ArrayList<>(alojamiento.getReservas());
        resultado.sort(Comparator.comparing(Reserva::getIngreso));
        return resultado;
    }

    /**
     * Cantidad de reservas activas del alojamiento que se superponen con el
     * periodo solicitado, calculada dia por dia para respetar la capacidad en
     * cada tramo (no solo en la fecha de ingreso).
     */
    public int ocupacionMaxima(Alojamiento alojamiento, LocalDate ingreso, LocalDate egreso) {
        int maximo = 0;
        for (LocalDate dia = ingreso; dia.isBefore(egreso); dia = dia.plusDays(1)) {
            int ocupados = 0;
            for (Reserva r : alojamiento.getReservas()) {
                if (r.estaActiva() && r.seSuperpone(dia, dia.plusDays(1))) {
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
     * Ocupacion maxima considerando todas las reservas activas del alojamiento
     * (se usa para validar la modificacion de capacidad).
     */
    public int ocupacionMaxima(Alojamiento alojamiento) {
        LocalDate desde = null;
        LocalDate hasta = null;
        for (Reserva r : alojamiento.getReservas()) {
            if (r.estaActiva()) {
                if (desde == null || r.getIngreso().isBefore(desde)) {
                    desde = r.getIngreso();
                }
                if (hasta == null || r.getEgreso().isAfter(hasta)) {
                    hasta = r.getEgreso();
                }
            }
        }
        if (desde == null) {
            return 0;
        }
        return ocupacionMaxima(alojamiento, desde, hasta);
    }

    /**
     * Plazas disponibles del alojamiento durante todo el periodo solicitado.
     */
    public int disponibilidad(Alojamiento alojamiento, LocalDate ingreso, LocalDate egreso) {
        return alojamiento.getCapacidad() - ocupacionMaxima(alojamiento, ingreso, egreso);
    }

    private void guardarReservas() {
        try {
            persistencia.guardarReservas(gestorUsuarios.listarAlojamientos());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar reservas.json: " + e.getMessage(), e);
        }
    }
}
