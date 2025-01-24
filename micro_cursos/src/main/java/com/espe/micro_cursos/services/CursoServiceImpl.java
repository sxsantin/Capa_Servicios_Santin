package com.espe.micro_cursos.services;

import com.espe.micro_cursos.clients.UsuarioClientRest;
import com.espe.micro_cursos.models.Usuario;
import com.espe.micro_cursos.models.entities.Curso;
import com.espe.micro_cursos.models.entities.CursoUsuario;
import com.espe.micro_cursos.repositories.CursoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.spi.ToolProvider.findFirst;

@Service
public class CursoServiceImpl implements CursoService{

    @Autowired
    private CursoRepository repository;

    @Autowired
    private UsuarioClientRest clientRest;

    @Override
    public List<Curso> findAll() {
        return (List<Curso>) repository.findAll();
    }

    @Override
    public Optional<Curso> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Curso save(Curso curso) {
        if (curso.getNombre() == null || curso.getNombre().isEmpty()) {
            throw new IllegalArgumentException("El nombre del curso es obligatorio.");
        }
        if (curso.getDescripcion() == null || curso.getDescripcion().isEmpty()) {
            throw new IllegalArgumentException("La descripción del curso es obligatoria.");
        }
        if (curso.getCreditos() <= 0) {
            throw new IllegalArgumentException("El número de créditos debe ser mayor a cero.");
        }
        return repository.save(curso);
    }


    @Override
    public void deleteById(Long id) {
        Optional<Curso> optionalCurso = repository.findById(id);
        if (optionalCurso.isPresent()) {
            Curso curso = optionalCurso.get();
            if (!curso.getCursoUsuarios().isEmpty()) {
                throw new IllegalStateException("No se puede eliminar un curso con usuarios matriculados.");
            }
            repository.deleteById(id);
        }
    }

    @Override
    @Transactional
    public Optional<Usuario> addUser(Usuario usuario, Long id) {
        Optional<Curso> optional = repository.findById(id);
        if (optional.isPresent()) {
            Curso curso = optional.get();

            // Verificar si el usuario ya está matriculado
            boolean isAlreadyEnrolled = curso.getCursoUsuarios().stream()
                    .anyMatch(cursoUsuario -> cursoUsuario.getUsuarioId().equals(usuario.getId()));

            if (isAlreadyEnrolled) {
                throw new IllegalArgumentException("El usuario ya está matriculado en este curso.");
            }

            Usuario usuarioTemp = clientRest.findById(usuario.getId());
            CursoUsuario cursoUsuario = new CursoUsuario();
            cursoUsuario.setUsuarioId(usuarioTemp.getId());

            curso.addCursoUsuario(cursoUsuario);
            repository.save(curso);

            return Optional.of(usuarioTemp);
        }
        return Optional.empty();
    }


    @Override
    public List<Usuario> findUsersByCursoId(Long cursoId) {
        Optional<Curso> optionalCurso = repository.findById(cursoId);
        if (optionalCurso.isPresent()) {
            Curso curso = optionalCurso.get();
            List<Usuario> allUsuarios = clientRest.findAll(); // Método que debes implementar en UsuarioClientRest
            return curso.getUsuarios(allUsuarios);
        }
        return Collections.emptyList(); // Devuelve una lista vacía si no se encuentra el curso
    }

    @Override
    public Usuario addUsuario(Usuario usuario) {
        List<Usuario> usuariosExistentes = clientRest.findAll();
        boolean emailExists = usuariosExistentes.stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(usuario.getEmail()));
        if (emailExists) {
            throw new IllegalArgumentException("Ya existe un usuario con este correo electrónico.");
        }
        return clientRest.save(usuario);
    }


    @Override
    @Transactional
    public boolean removeUserFromCurso(Long cursoId, Long usuarioId) {
        Optional<Curso> optionalCurso = repository.findById(cursoId);
        if (optionalCurso.isPresent()) {
            Curso curso = optionalCurso.get();
            CursoUsuario cursoUsuarioToRemove = null;

            for (CursoUsuario cursoUsuario : curso.getCursoUsuarios()) {
                if (cursoUsuario.getUsuarioId().equals(usuarioId)) {
                    cursoUsuarioToRemove = cursoUsuario;
                    break;
                }
            }

            if (cursoUsuarioToRemove != null) {
                curso.removeCursoUsuario(cursoUsuarioToRemove);
                repository.save(curso); // Guarda el curso actualizado
                return true;
            }
        }
        return false;
    }
}
