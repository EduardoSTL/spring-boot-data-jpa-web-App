package com.example.springbootdatajpa.app.models.dao;

import com.example.springbootdatajpa.app.entity.Cliente;

import java.util.List;

public interface IClienteDao {

    public List<Cliente> findAll();
}
