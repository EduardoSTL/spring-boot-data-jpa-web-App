package com.example.springbootdatajpa.app.models.dao;

import com.example.springbootdatajpa.app.entity.Cliente;
import jakarta.persistence.EntityManager;
import java.util.List;

import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository("clienteDaoJPA")
public class ClienteDaoImpl implements IClienteDao{

    @PersistenceContext
    private EntityManager manager;

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    @Override
    public List<Cliente> findAll() {
        return manager.createQuery("from Cliente ").getResultList();
    }
}
