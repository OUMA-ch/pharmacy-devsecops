package com.salma.mini_projet_pharmacie.service;

import com.salma.mini_projet_pharmacie.model.*;
import com.salma.mini_projet_pharmacie.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ClientRepository clientRepository;
    private final PharmacienRepository pharmacienRepository;
    private final PasswordEncoder passwordEncoder;

    // inscription d'un client
    public Client registerClient(Client client) {
        client.setPassword(passwordEncoder.encode(client.getPassword()));
        client.setRole(Role.CLIENT);
        return clientRepository.save(client);
    }

    // création d'un pharmacien
    public Pharmacien createPharmacien(Pharmacien pharmacien) {
        pharmacien.setPassword(passwordEncoder.encode(pharmacien.getPassword()));
        pharmacien.setRole(Role.PHARMACIEN);
        return pharmacienRepository.save(pharmacien);
    }

    // suppression d'un pharmacien
    public void deletePharmacien(Integer id) {
        pharmacienRepository.deleteById(id);
    }
}
