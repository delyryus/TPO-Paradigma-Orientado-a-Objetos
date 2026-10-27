import interfaz.VentanaPrincipal;
import modelo.Evento;
import modelo.Usuario;
import persistencia.PersistenciaJSON;
import servicios.GestorAlojamientos;
import servicios.GestorEventos;
import servicios.GestorReservas;
import servicios.GestorUsuarios;

import javax.swing.*;
import java.util.List;

/**
 * Credenciales de prueba documentadas  admin/admin123 (administrador), juan/maria/pedro con 1234 (asistentes), y los alojamientos sancayetano, sanfrancisco, granlujan,
 *      caritas, sanpablo con 1234
 */
public class Main {

    public static void main(String[] args) {
        PersistenciaJSON persistencia = new PersistenciaJSON();

        try {
            List<Evento> eventos = persistencia.cargarEventos();
            List<Usuario> usuarios = persistencia.cargarCuentas(eventos);
            persistencia.cargarReservas(usuarios);

            GestorUsuarios gestorUsuarios = new GestorUsuarios(usuarios, persistencia);
            GestorEventos gestorEventos = new GestorEventos(eventos, gestorUsuarios, persistencia);
            GestorReservas gestorReservas = new GestorReservas(gestorUsuarios, persistencia);
            GestorAlojamientos gestorAlojamientos = new GestorAlojamientos(gestorUsuarios, gestorReservas, persistencia);

            SwingUtilities.invokeLater(() -> new VentanaPrincipal(
                    gestorUsuarios, gestorEventos, gestorReservas, gestorAlojamientos));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo iniciar la aplicacion:\n" + e.getMessage(),
                    "Error de carga", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
