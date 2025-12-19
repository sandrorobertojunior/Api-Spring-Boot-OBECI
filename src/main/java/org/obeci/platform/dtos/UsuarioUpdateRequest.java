package org.obeci.platform.dtos;

import jakarta.validation.constraints.Email;
import java.util.List;

public class UsuarioUpdateRequest {
    // Opcional para atualização: se enviado, será aplicado
    private String username;

    @Email(message = "Email inválido")
    private String email;
    
    private String cpf; // opcional para update

    private List<String> arrayRoles;

    // Senha opcional para atualização; se enviada, será re-codificada
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public List<String> getArrayRoles() { return arrayRoles; }
    public void setArrayRoles(List<String> arrayRoles) { this.arrayRoles = arrayRoles; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
