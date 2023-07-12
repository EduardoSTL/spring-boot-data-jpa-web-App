package com.example.springbootdatajpa.app.models.dao;

import com.example.springbootdatajpa.app.models.entity.Cliente;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface IClienteDao extends PagingAndSortingRepository<Cliente, Long>, CrudRepository<Cliente, Long> {

}
