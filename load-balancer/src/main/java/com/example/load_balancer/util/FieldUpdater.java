package com.example.load_balancer.util;

import java.util.Objects;
import java.util.function.BiConsumer;

public class FieldUpdater<T>{

    private int updateCount;

    public FieldUpdater(){
        updateCount = 0;
    }

    public void updateIfChanged(T oldValue, T newValue, BiConsumer<T,T> updater){
        if(newValue!=null && !Objects.equals(oldValue,newValue)){
            updater.accept(oldValue,newValue);
            updateCount+=1;
        }
    }

    public int getUpdateCount() {
        return updateCount;
    }
}
