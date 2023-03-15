package com.andre.service;

import com.andre.entity.Monster;

import java.util.List;

/**
 * @author Andre Wang
 * @version 1.0
 */
public interface MonsterService {

    public List<Monster> listMonster();


    public List<Monster> findMonsterByName(String name);


    public boolean login(String name);
}
