package servicios;

import modelo.*;
import persistencia.PersistenciaJSON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona el registro, la autenticacion y la busqueda de cuentas. La
 * identidad de una cuenta es su nombre de usuario, que es unico.
 */
public class GestorUsuarios {

    private final List<Usuario> usuarios;
    private final PersistenciaJSON persistencia;

    public GestorUsuarios(List<Usuario> usuarios, PersistenciaJSON persistencia) {
        this.usuarios = usuarios;
        this.persistencia = persistencia;
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    /**
     * Devuelve true si ya existe una cuenta con ese nombre de usuario,
     * sin distinguir mayusculas o minusculas.
     */
    public boolean existeNombreUsuario(String nombreUsuario) {
        for (Usuario u : usuarios) {
            if (u.getNombreUsuario().equalsIgnoreCase(nombreUsuario)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Busca una cuenta por nombre de usuario y clave. Devuelve null si no coincide.
     */
    public Usuario autenticar(String nombreUsuario, String clave) {
        for (Usuario u : usuarios) {
            if (u.getNombreUsuario().equals(nombreUsuario) && u.validarClave(clave)) {
                return u;
            }
        }
        return null;
    }

    public Usuario buscar(String nombreUsuario) {
        for (Usuario u : usuarios) {
            if (u.getNombreUsuario().equals(nombreUsuario)) {
                return u;
            }
        }
        return null;
    }

    public Asistente buscarAsistente(String nombreUsuario) {
        Usuario u = buscar(nombreUsuario);
        return (u instanceof Asistente) ? (Asistente) u : null;
    }

    public Alojamiento buscarAlojamiento(String nombreUsuario) {
        Usuario u = buscar(nombreUsuario);
        return (u instanceof Alojamiento) ? (Alojamiento) u : null;
    }

    /**
     * Registra un asistente nuevo y guarda en disco.
     */
    public Asistente registrarAsistente(Asistente asistente) {
        if (existeNombreUsuario(asistente.getNombreUsuario())) {
            throw new IllegalArgumentException("El nombre de usuario ya esta en uso.");
        }
        usuarios.add(asistente);
        guardarCuentas();
        return asistente;
    }

    /**
     * Registra un alojamiento nuevo (parroquia, hotel o espacio de organizacion).
     */
    public Alojamiento registrarAlojamiento(Alojamiento alojamiento) {
        if (existeNombreUsuario(alojamiento.getNombreUsuario())) {
            throw new IllegalArgumentException("El nombre de usuario ya esta en uso.");
        }
        usuarios.add(alojamiento);
        guardarCuentas();
        return alojamiento;
    }

    public List<Alojamiento> listarAlojamientos() {
        List<Alojamiento> resultado = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u instanceof Alojamiento) {
                resultado.add((Alojamiento) u);
            }
        }
        return resultado;
    }

    public List<Alojamiento> listarAlojamientosPorCiudad(Ciudad ciudad) {
        List<Alojamiento> resultado = new ArrayList<>();
        for (Alojamiento a : listarAlojamientos()) {
            if (a.getCiudad() == ciudad) {
                resultado.add(a);
            }
        }
        return resultado;
    }

    public void guardarCuentas() {
        try {
            persistencia.guardarCuentas(usuarios);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar cuentas.json: " + e.getMessage(), e);
        }
    }
}
