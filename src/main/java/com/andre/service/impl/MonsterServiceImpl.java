package com.andre.service.impl;

import com.andre.andrespringmvc.annotation.Service;
import com.andre.entity.Monster;
import com.andre.service.MonsterService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andre Wang
 * @version 1.0
 */

@Service("myService")
public class MonsterServiceImpl implements MonsterService {

// We pretend there some service and dao implementation
    @Override
    public List<Monster> listMonster() {

        //这里就模拟数据->DB
        List<Monster> monsters =
                new ArrayList<>();
        monsters.add(new Monster(100, "hecarim", "charge", 400));
        monsters.add(new Monster(200, "jax", "flip", 200));
        return monsters;

    }

    @Override
    public List<Monster> findMonsterByName(String name) {

        List<Monster> monsters =
                new ArrayList<>();
        monsters.add(new Monster(400, "Azir", "sand shuffle", 300));
        monsters.add(new Monster(500, "Fizz", "playful", 800));
        monsters.add(new Monster(600, "Alistar", "HeadButt", 800));




        List<Monster> findMonsters =
                new ArrayList<>();

        for (Monster monster : monsters) {
            if (monster.getName().contains(name)) {
                findMonsters.add(monster);
            }
        }
        return findMonsters;
    }

    @Override
    public boolean login(String name) {

        if ("Fizz".equals(name)) {
            return true;
        } else {
            return false;
        }
    }

}
