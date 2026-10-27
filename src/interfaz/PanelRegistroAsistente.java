package interfaz;

import modelo.Asistente;
import modelo.Ciudad;

import javax.swing.*;
import java.awt.*;

/**
 * Formulario de registro de un asistente nuevo.
 */
public class PanelRegistroAsistente extends JPanel {

    private final VentanaPrincipal ventana;
    private final JTextField campoNombreUsuario;
    private final JPasswordField campoClave;
    private final JTextField campoNombre;
    private final JTextField campoDni;
    private final JComboBox<Ciudad> comboOrigen;

    public PanelRegistroAsistente(VentanaPrincipal ventana) {
        this.ventana = ventana;
        setLayout(new BorderLayout());
        add(UtilidadesInterfaz.titulo("Registro de asistente"), BorderLayout.NORTH);

        campoNombreUsuario = new JTextField(16);
        campoClave = new JPasswordField(16);
        campoNombre = new JTextField(16);
        campoDni = new JTextField(16);
        comboOrigen = new JComboBox<>(Ciudad.values());

        JPanel form = UtilidadesInterfaz.formulario(
                UtilidadesInterfaz.fila("Nombre de usuario:", campoNombreUsuario),
                UtilidadesInterfaz.fila("Clave:", campoClave),
                UtilidadesInterfaz.fila("Nombre completo:", campoNombre),
                UtilidadesInterfaz.fila("DNI:", campoDni),
                UtilidadesInterfaz.fila("Ciudad de origen:", comboOrigen)
        );
        add(form, BorderLayout.CENTER);

        JButton botonRegistrar = UtilidadesInterfaz.boton("Registrarme", e -> registrar());
        JButton botonVolver = UtilidadesInterfaz.boton("Volver", e -> ventana.mostrarMenuPrincipal());
        add(UtilidadesInterfaz.panelBotones(botonRegistrar, botonVolver), BorderLayout.SOUTH);
    }

    private void registrar() {
        try {
            String nombreUsuario = campoNombreUsuario.getText().trim();
            String clave = new String(campoClave.getPassword());
            String nombre = campoNombre.getText().trim();
            String dni = campoDni.getText().trim();
            Ciudad origen = (Ciudad) comboOrigen.getSelectedItem();

            if (nombreUsuario.isEmpty() || clave.isEmpty() || nombre.isEmpty() || dni.isEmpty()) {
                UtilidadesInterfaz.mostrarError(this, "Completa todos los campos.");
                return;
            }

            Asistente asistente = new Asistente(nombreUsuario, clave, nombre, dni, origen);
            ventana.getGestorUsuarios().registrarAsistente(asistente);
            UtilidadesInterfaz.mostrarInfo(this, "Asistente registrado correctamente.");
            ventana.mostrarMenuPrincipal();
        } catch (Exception ex) {
            UtilidadesInterfaz.mostrarError(this, ex.getMessage());
        }
    }
}
