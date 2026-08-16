package com.salma.mini_projet_pharmacie.service;

import com.salma.mini_projet_pharmacie.model.*;
import com.salma.mini_projet_pharmacie.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ClientRepository clientRepository;
    private final PharmacienRepository pharmacienRepository;

    // inscription d'un client
    public Client registerClient(Client client) {
        client.setRole(Role.CLIENT);
        return clientRepository.save(client);
    }

    // création d'un pharmacien
    public Pharmacien createPharmacien(Pharmacien pharmacien) {
        pharmacien.setRole(Role.PHARMACIEN);
        return pharmacienRepository.save(pharmacien);
    }

    // suppression d'un pharmacien
    public void deletePharmacien(Integer id) {
        pharmacienRepository.deleteById(id);
    }
}
