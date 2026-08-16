package com.salma.mini_projet_pharmacie.service;

import com.salma.mini_projet_pharmacie.dto.PharmacienDTO;
import com.salma.mini_projet_pharmacie.model.Pharmacien;
import com.salma.mini_projet_pharmacie.model.Role;
import com.salma.mini_projet_pharmacie.repository.PharmacienRepository;
import com.salma.mini_projet_pharmacie.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PharmacienCrudService {

    private final PharmacienRepository pharmacienRepository;
    private final UserRepository userRepository;

    public PharmacienCrudService(PharmacienRepository pharmacienRepository, UserRepository userRepository) {
        this.pharmacienRepository = pharmacienRepository;
        this.userRepository = userRepository;
    }

    public List<Pharmacien> getAll() {
        return pharmacienRepository.findAll();
    }

    public Pharmacien create(PharmacienDTO dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new RuntimeException("Email obligatoire");
        }
        if (userRepository.findByEmail(dto.getEmail().trim()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new RuntimeException("Mot de passe obligatoire");
        }

        Pharmacien p = new Pharmacien();
        p.setNomUser(dto.getNomUser());
        p.setEmail(dto.getEmail().trim());
        p.setPassword(dto.getPassword());     // (en clair pour matcher ton login actuel)
        p.setTele(dto.getTele());
        p.setRole(Role.PHARMACIEN);

        return pharmacienRepository.save(p);
    }

    public Pharmacien update(Integer id, PharmacienDTO dto) {
        Pharmacien p = pharmacienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pharmacien introuvable"));

        if (dto.getNomUser() != null) p.setNomUser(dto.getNomUser());
        if (dto.getTele() != null) p.setTele(dto.getTele());

        // Email: si changé, vérifier unicité
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            String newEmail = dto.getEmail().trim();
            if (!newEmail.equalsIgnoreCase(p.getEmail())) {
                if (userRepository.findByEmail(newEmail).isPresent()) {
                    throw new RuntimeException("Email déjà utilisé");
                }
                p.setEmail(newEmail);
            }
        }

        // Password: si rempli -> update
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            p.setPassword(dto.getPassword());
        }

        return pharmacienRepository.save(p);
    }

    public void delete(Integer id) {
        Pharmacien p = pharmacienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pharmacien introuvable"));
        pharmacienRepository.delete(p);
    }
}
