package com.salma.mini_projet_pharmacie.controller;

import com.salma.mini_projet_pharmacie.dto.PharmacienDTO;
import com.salma.mini_projet_pharmacie.model.Pharmacien;
import com.salma.mini_projet_pharmacie.service.PharmacienCrudService;
import com.salma.mini_projet_pharmacie.validation.OnCreate;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pharmaciens")
public class PharmacienCrudController {

    private final PharmacienCrudService service;

    public PharmacienCrudController(PharmacienCrudService service) {
        this.service = service;
    }

    @GetMapping
    public List<Pharmacien> all() {
        return service.getAll();
    }

    @PostMapping
    public Pharmacien create(@Validated({Default.class, OnCreate.class}) @RequestBody PharmacienDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public Pharmacien update(@PathVariable Integer id, @Valid @RequestBody PharmacienDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
