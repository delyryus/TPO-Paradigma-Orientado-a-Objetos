package interfaz;

import modelo.Asistente;
import modelo.Evento;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Muestra la agenda de eventos. Segun quien este autenticado permite solo
 * consultar (publico o administrador) o tambien inscribirse y desinscribirse
 * (asistente).
 */
public class PanelAgenda extends JPanel {

    private final VentanaPrincipal ventana;
    private final boolean modoMisInscripciones;
    private final JTable tabla;
    private JTextField campoFecha;
    private List<Evento> eventosMostrados;

    public PanelAgenda(VentanaPrincipal ventana, boolean modoMisInscripciones) {
        this.ventana = ventana;
        this.modoMisInscripciones = modoMisInscripciones;
        setLayout(new BorderLayout());

        String titulo = modoMisInscripciones ? "Mis inscripciones" : "Agenda de eventos";
        add(UtilidadesInterfaz.titulo(titulo), BorderLayout.NORTH);

        tabla = new JTable();
        add(UtilidadesInterfaz.tabla(tabla), BorderLayout.CENTER);

        JPanel sur = new JPanel();
        sur.setLayout(new BoxLayout(sur, BoxLayout.Y_AXIS));

        if (modoMisInscripciones) {
            JButton botonDesinscribir = UtilidadesInterfaz.boton(
                    "Desinscribirme del evento seleccionado", e -> desinscribir());
            sur.add(UtilidadesInterfaz.panelBotones(botonDesinscribir));
        } else {
            campoFecha = new JTextField(12);
            JButton botonFiltrar = UtilidadesInterfaz.boton("Filtrar por fecha", e -> cargarPorFecha());
            JButton botonVerTodos = UtilidadesInterfaz.boton("Ver todos", e -> cargarTodos());
            JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            filtro.add(new JLabel("Fecha (AAAA-MM-DD):"));
            filtro.add(campoFecha);
            filtro.add(botonFiltrar);
            filtro.add(botonVerTodos);
            sur.add(filtro);

            if (ventana.getSesion() instanceof Asistente) {
                JButton botonInscribir = UtilidadesInterfaz.boton(
                        "Inscribirme al evento seleccionado", e -> inscribir());
                sur.add(UtilidadesInterfaz.panelBotones(botonInscribir));
            }
        }

        JButton botonVolver = UtilidadesInterfaz.boton("Volver", e -> volver());
        sur.add(UtilidadesInterfaz.panelBotones(botonVolver));

        add(sur, BorderLayout.SOUTH);

        if (modoMisInscripciones) {
            cargarInscripciones();
        } else {
            cargarTodos();
        }
    }

    private void volver() {
        if (ventana.getSesion() == null) {
            ventana.mostrarMenuPrincipal();
        } else {
            switch (ventana.getSesion().getRol()) {
                case "ASISTENTE":
                    ventana.mostrarMenuAsistente();
                    break;
                case "ADMINISTRADOR":
                    ventana.mostrarMenuAdministrador();
                    break;
                default:
                    ventana.mostrarMenuPrincipal();
            }
        }
    }

    private void cargarTodos() {
        eventosMostrados = ventana.getGestorEventos().listarTodosOrdenados();
        mostrarTabla();
    }

    private void cargarPorFecha() {
        try {
            LocalDate fecha = UtilidadesInterfaz.leerFecha(campoFecha.getText());
            eventosMostrados = ventana.getGestorEventos().agendaPorFecha(fecha);
            mostrarTabla();
        } catch (Exception ex) {
            UtilidadesInterfaz.mostrarError(this, ex.getMessage());
        }
    }

    private void cargarInscripciones() {
        Asistente asistente = (Asistente) ventana.getSesion();
        eventosMostrados = new ArrayList<>(asistente.getEventos());
        eventosMostrados.sort(Comparator.comparing(Evento::getFecha)
                .thenComparing(Evento::getHorario));
        mostrarTabla();
    }

    private void mostrarTabla() {
        String[] columnas = {"Fecha", "Horario", "Actividad", "Ciudad"};
        Object[][] filas = new Object[eventosMostrados.size()][4];
        for (int i = 0; i < eventosMostrados.size(); i++) {
            Evento e = eventosMostrados.get(i);
            filas[i] = new Object[]{e.getFecha(), e.getHorario(), e.getActividad(), e.getCiudad()};
        }
        tabla.setModel(UtilidadesInterfaz.modeloTabla(columnas, filas));
    }

    private Evento eventoSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            UtilidadesInterfaz.mostrarError(this, "Selecciona un evento de la tabla.");
            return null;
        }
        return eventosMostrados.get(fila);
    }

    private void inscribir() {
        Evento evento = eventoSeleccionado();
        if (evento == null) {
            return;
        }
        try {
            ventana.getGestorEventos().inscribir((Asistente) ventana.getSesion(), evento);
            UtilidadesInterfaz.mostrarInfo(this, "Te inscribiste al evento \"" + evento.getActividad() + "\".");
        } catch (Exception ex) {
            UtilidadesInterfaz.mostrarError(this, ex.getMessage());
        }
    }

    private void desinscribir() {
        Evento evento = eventoSeleccionado();
        if (evento == null) {
            return;
        }
        try {
            ventana.getGestorEventos().desinscribir((Asistente) ventana.getSesion(), evento);
            UtilidadesInterfaz.mostrarInfo(this, "Te desinscribiste del evento \"" + evento.getActividad() + "\".");
            cargarInscripciones();
        } catch (Exception ex) {
            UtilidadesInterfaz.mostrarError(this, ex.getMessage());
        }
    }
}
