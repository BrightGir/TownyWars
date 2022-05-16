package me.bright.townywars.gui;

import org.bukkit.entity.Player;

import java.util.Collection;

public abstract class PagedGui<T> extends GUI {

    protected int slot;
    protected Collection<T> objects;
    protected T targetObject;
    protected int page;

    public PagedGui(Player player, int page, String title, Collection<T> objects) {
        super(player,title,6);
        this.objects = objects;
        this.page = page;
        this.slot = 0;
        int objectNumber = 0;
        if(objects != null) {
            for (T targetObject : objects) {
                this.targetObject = targetObject;
                if (page > 1) {
                    objectNumber++;
                    if (objectNumber > (page - 1) * 45) {
                        pullSlotOfInventory(targetObject);
                    }

                } else {
                    pullSlotOfInventory(targetObject);
                }
            }
        }
    }

    public abstract void setTargetItem();

    public abstract void setNextItem();

    public abstract void setBackItem();

    private void pullSlotOfInventory(T object) {
        if(page > 1) {
            setBackItem();
        }

        if(slot >= 45) {

            if(objects.size() > page*45) {
                setNextItem();
            }
            return;

        } else {
            setTargetItem();
        }
        slot++;
    }
}
