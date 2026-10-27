package servicios;

import modelo.*;
import persistencia.PersistenciaJSON;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Gestiona el alta, la modificacion y la consulta de eventos, asi como la
 * inscripcion y desinscripcion de asistentes. Es el extremo navegable de la
 * asociacion con Evento: mantiene la coleccion de eventos.
 */
public class GestorEventos {

    private final List<Evento> eventos;
    private final GestorUsuarios gestorUsuarios;
    private final PersistenciaJSON persistencia;

    public GestorEventos(List<Evento> eventos, GestorUsuarios gestorUsuarios,
                         PersistenciaJSON persistencia) {
        this.eventos = eventos;
        this.gestorUsuarios = gestorUsuarios;
        this.persistencia = persistencia;
    }

    public List<Evento> getEventos() {
        return eventos;
    }

    /**
     * Registra un evento nuevo y guarda en disco.
     */
    public void registrar(LocalDate fecha, LocalTime horario, String actividad, Ciudad ciudad) {
        Evento evento = new Evento(fecha, horario, actividad, ciudad);
        eventos.add(evento);
        guardarEventos();
    }

    /**
     * Modifica un evento existente conservando su identidad.
     */
    public void modificarEvento(Evento evento, LocalDate fecha, LocalTime horario,
                                String actividad, Ciudad ciudad) {
        if (evento == null || !eventos.contains(evento)) {
            throw new IllegalArgumentException("El evento indicado no existe.");
        }
        evento.setFecha(fecha);
        evento.setHorario(horario);
        evento.setActividad(actividad);
        evento.setCiudad(ciudad);
        guardarEventos();
        // Las cuentas guardan los datos de los eventos en los que estan
        // inscriptas, por lo que hay que reescribirlas si el evento cambio.
        guardarCuentas();
    }

    public List<Evento> listarTodosOrdenados() {
        List<Evento> copia = new ArrayList<>(eventos);
        copia.sort(Comparator.comparing(Evento::getFecha).thenComparing(Evento::getHorario));
        return copia;
    }

    /**
     * Devuelve los eventos de una fecha, ordenados por horario.
     */
    public List<Evento> agendaPorFecha(LocalDate fecha) {
        List<Evento> resultado = new ArrayList<>();
        for (Evento e : eventos) {
            if (e.getFecha().equals(fecha)) {
                resultado.add(e);
            }
        }
        resultado.sort(Comparator.comparing(Evento::getHorario));
        return resultado;
    }

    /**
     * Inscribe a un asistente en un evento existente.
     */
    public void inscribir(Asistente asistente, Evento evento) {
        if (evento == null || !eventos.contains(evento)) {
            throw new IllegalArgumentException("El evento indicado no existe.");
        }
        if (asistente.estaInscrito(evento)) {
            throw new IllegalArgumentException("Ya estas inscrito en ese evento.");
        }
        asistente.inscribir(evento);
        guardarCuentas();
    }

    /**
     * Desinscribe a un asistente de un evento.
     */
    public void desinscribir(Asistente asistente, Evento evento) {
        if (!asistente.estaInscrito(evento)) {
            throw new IllegalArgumentException("No estas inscrito en ese evento.");
        }
        asistente.desinscribir(evento);
        guardarCuentas();
    }

    private void guardarEventos() {
        try {
            persistencia.guardarEventos(eventos);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar eventos.json: " + e.getMessage(), e);
        }
    }

    private void guardarCuentas() {
        try {
            persistencia.guardarCuentas(gestorUsuarios.getUsuarios());
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar cuentas.json: " + e.getMessage(), e);
        }
    }
}
