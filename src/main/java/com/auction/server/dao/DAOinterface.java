package com.auction.server.dao;

public interface DAOinterface <T> {
    void create(T obj);
    T read(Integer id);
    void update(T obj);
    void delete(Integer id);
}