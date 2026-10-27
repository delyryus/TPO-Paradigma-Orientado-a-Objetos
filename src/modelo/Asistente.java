package modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Asistente a la visita del Papa. Mantiene la lista de eventos a los que esta
 * inscrito como referencias directas a los objetos Evento.
 */
public class Asistente extends Usuario {

    private String dni;
    private Ciudad origen;
    private List<Evento> eventos;

    public Asistente(String nombreUsuario, String clave, String nombre,
                     String dni, Ciudad origen) {
        super(nombreUsuario, clave, nombre);
        this.dni = dni;
        this.origen = origen;
        this.eventos = new ArrayList<>();
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public Ciudad getOrigen() {
        return origen;
    }

    public void setOrigen(Ciudad origen) {
        this.origen = origen;
    }

    public List<Evento> getEventos() {
        return eventos;
    }

    /**
     * Inscribe al asistente en un evento. No permite inscripciones duplicadas.
     */
    public void inscribir(Evento evento) {
        if (evento != null && !eventos.contains(evento)) {
            eventos.add(evento);
        }
    }

    /**
     * Quita al asistente de un evento.
     */
    public void desinscribir(Evento evento) {
        eventos.remove(evento);
    }

    /**
     * Indica si el asistente ya esta inscripto en el evento indicado.
     */
    public boolean estaInscrito(Evento evento) {
        return eventos.contains(evento);
    }

    @Override
    public String getRol() {
        return "ASISTENTE";
    }
}
