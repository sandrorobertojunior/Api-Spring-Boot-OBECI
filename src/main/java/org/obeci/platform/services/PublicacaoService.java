package org.obeci.platform.services;

import org.obeci.platform.entities.Publicacao;
import org.obeci.platform.repositories.PublicacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PublicacaoService {

    @Autowired
    private PublicacaoRepository publicacaoRepository;

    public List<Publicacao> getAllPublicacoes() {
        return publicacaoRepository.findAll();
    }

    public Optional<Publicacao> getPublicacaoById(Long id) {
        return publicacaoRepository.findById(id);
    }

    public Publicacao createPublicacao(Publicacao publicacao) {
        return publicacaoRepository.save(publicacao);
    }

    public Publicacao updatePublicacao(Long id, Publicacao publicacaoDetails) {
        Publicacao publicacao = publicacaoRepository.findById(id).orElse(null);
        if (publicacao != null) {
            publicacao.setUsername(publicacaoDetails.getUsername());
            publicacao.setTitle(publicacaoDetails.getTitle());
            publicacao.setContent(publicacaoDetails.getContent());
            publicacao.setTurmaId(publicacaoDetails.getTurmaId());
            publicacao.setEscolaId(publicacaoDetails.getEscolaId());
            publicacao.setIsPublic(publicacaoDetails.getIsPublic());
            publicacao.setHashtags(publicacaoDetails.getHashtags());
            return publicacaoRepository.save(publicacao);
        }
        return null;
    }

    public boolean deletePublicacao(Long id) {
        if (publicacaoRepository.existsById(id)) {
            publicacaoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Publicacao> getPublicacoesByTurmaId(Long turmaId) {
        return publicacaoRepository.findByTurmaId(turmaId);
    }

    public List<Publicacao> getPublicacoesByEscolaId(Long escolaId) {
        return publicacaoRepository.findByEscolaId(escolaId);
    }

    public List<Publicacao> getPublicacoesByIsPublic(Boolean isPublic) {
        return publicacaoRepository.findByIsPublic(isPublic);
    }

    public List<Publicacao> getPublicacoesByUsername(String username) {
        return publicacaoRepository.findByUsername(username);
    }
}