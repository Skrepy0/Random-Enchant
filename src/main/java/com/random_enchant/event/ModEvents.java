package com.random_enchant.event;

import com.random_enchant.event.enchantments.infinity.PotionThrowEvent;
import com.random_enchant.event.enchantments.infinity.ThrowItemEvent;
import com.random_enchant.event.enchantments.power.ShovelBlockEvent;
import com.random_enchant.event.enchantments.power.ShovelEntityEvent;
import com.random_enchant.event.enchantments.power.ThrowFireBallEvent;
import com.random_enchant.event.enchantments.power.ThrowSlimeBallEvent;

public class ModEvents {
    public static void registerEvents() {
        new ShovelBlockEvent().registerEvent();
        new ThrowItemEvent().registerEvents();
        new PotionThrowEvent().registerEvents();
        new ThrowFireBallEvent().registerEvent();
        new ShovelEntityEvent().registerEvent();
        new ThrowSlimeBallEvent().registerEvent();
    }
}
