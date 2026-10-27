package modelo;

import java.time.LocalDate;

/**
 * Reserva de un alojamiento hecha por un asistente. Guarda una referencia
 * directa al asistente; el alojamiento es quien mantiene la coleccion de
 * reservas, tal como indica el UML.
 */
public class Reserva {

    private Asistente asistente;
    private LocalDate ingreso;
    private LocalDate egreso;
    private EstadoReserva estado;

    public Reserva(Asistente asistente, LocalDate ingreso, LocalDate egreso,
                   EstadoReserva estado) {
        this.asistente = asistente;
        setIngreso(ingreso);
        setEgreso(egreso);
        this.estado = estado;
    }

    public Asistente getAsistente() {
        return asistente;
    }

    public void setAsistente(Asistente asistente) {
        this.asistente = asistente;
    }

    public LocalDate getIngreso() {
        return ingreso;
    }

    public void setIngreso(LocalDate ingreso) {
        if (ingreso == null) {
            throw new IllegalArgumentException("La fecha de ingreso es obligatoria.");
        }
        this.ingreso = ingreso;
    }

    public LocalDate getEgreso() {
        return egreso;
    }

    /**
     * El egreso debe ser posterior al ingreso.
     */
    public void setEgreso(LocalDate egreso) {
        if (egreso == null) {
            throw new IllegalArgumentException("La fecha de egreso es obligatoria.");
        }
        if (!egreso.isAfter(this.ingreso)) {
            throw new IllegalArgumentException("El egreso debe ser posterior al ingreso.");
        }
        this.egreso = egreso;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    /**
     * Determina si esta reserva se superpone con el periodo indicado. El
     * ingreso es inclusivo y el egreso es exclusivo: si una reserva termina el
     * mismo dia en que empieza la otra, no se consideran superpuestas.
     */
    public boolean seSuperpone(LocalDate otroIngreso, LocalDate otroEgreso) {
        return this.ingreso.isBefore(otroEgreso) && otroIngreso.isBefore(this.egreso);
    }

    /**
     * Cancela la reserva.
     */
    public void cancelar() {
        this.estado = EstadoReserva.CANCELADA;
    }

    public boolean estaActiva() {
        return this.estado == EstadoReserva.ACTIVA;
    }
}
