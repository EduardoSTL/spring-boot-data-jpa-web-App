package com.example.springbootdatajpa.app.models.dao;

import com.example.springbootdatajpa.app.entity.Cliente;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IClienteDao {

    List<Cliente> findAll();

    void save(Cliente cliente);

    Cliente findOne(Long id);

    void delete(Long id);
}
