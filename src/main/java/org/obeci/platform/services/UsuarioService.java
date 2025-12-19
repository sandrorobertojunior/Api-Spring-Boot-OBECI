package org.obeci.platform.services;

import org.obeci.platform.entities.Usuario;
import org.obeci.platform.repositories.UsuarioRepository;
import org.obeci.platform.dtos.UsuarioCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Usuario register(Usuario usuario) {
        // Verifica se já existe email
        if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("Email já existe");
        }

        // Verifica se já existe CPF
        if (usuario.getCpf() != null && usuarioRepository.findByCpf(usuario.getCpf()).isPresent()) {
            throw new RuntimeException("CPF já existe");
        }

        // Criptografa a senha e salva
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        // Produção: se não enviar roles, definir uma role padrão (ex.: PROFESSOR)
        if (usuario.getArrayRoles() == null || usuario.getArrayRoles().isEmpty()) {
            usuario.setArrayRoles(java.util.Arrays.asList("PROFESSOR"));
        }
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> login(String email, String senha) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (passwordEncoder.matches(senha, usuario.getPassword())) {
                return Optional.of(usuario);
            }
        }
        return Optional.empty();
    }

    // Sobrecarga para aceitar DTO de criação de usuário
    public Usuario register(UsuarioCreateRequest request) {
        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(request.getPassword());
        usuario.setCpf(request.getCpf());
        usuario.setArrayRoles(request.getArrayRoles());
        return register(usuario);
    }

    // Método auxiliar para verificar credenciais
    public boolean validateCredentials(String email, String password) {
        return login(email, password).isPresent();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
        // Mapeia roles da entidade para authorities do Spring (ROLE_*)
        List<String> authorities = (usuario.getArrayRoles() == null ? java.util.Collections.<String>emptyList() : usuario.getArrayRoles())
                .stream()
                .map(r -> "ROLE_" + r.toUpperCase())
                .collect(Collectors.toList());
        if (authorities.isEmpty()) {
            authorities = java.util.Arrays.asList("ROLE_USER");
        }
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities(authorities.toArray(new String[0]))
                .build();
    }

    // CRUD de usuários para administração
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Transactional
    public Optional<Usuario> update(Long id, Usuario changes) {
        return usuarioRepository.findById(id).map(existing -> {
            if (changes.getUsername() != null && !changes.getUsername().isBlank()) existing.setUsername(changes.getUsername());
            if (changes.getEmail() != null && !changes.getEmail().isBlank()) existing.setEmail(changes.getEmail());
            if (changes.getCpf() != null && !changes.getCpf().isBlank()) existing.setCpf(changes.getCpf());
            if (changes.getPassword() != null && !changes.getPassword().isBlank()) {
                existing.setPassword(passwordEncoder.encode(changes.getPassword()));
            }
            if (changes.getArrayRoles() != null && !changes.getArrayRoles().isEmpty()) {
                existing.setArrayRoles(changes.getArrayRoles());
            }
            return usuarioRepository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Busca usuários por role com filtro opcional de texto (username/email)
    public List<Usuario> findByRole(String role, String query) {
        String wanted = role == null ? "" : role.toUpperCase();
        String q = query == null ? "" : query.toLowerCase();
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getArrayRoles() != null &&
                        u.getArrayRoles().stream()
                                .map(r -> r == null ? "" : r.toUpperCase())
                                .anyMatch(r -> r.equals(wanted)))
                .filter(u -> q.isBlank() ||
                        ((u.getUsername() != null && u.getUsername().toLowerCase().contains(q)) ||
                         (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))))
                .collect(Collectors.toList());
    }
}

