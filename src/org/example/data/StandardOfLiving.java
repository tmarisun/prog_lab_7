package org.example.data;

import lombok.Getter;

@Getter
public enum StandardOfLiving {
    HIGH(0),
    MEDIUM(1),
    LOW(2),
    ULTRA_LOW(3),
    NIGHTMARE(4);

    private final int rank;

    StandardOfLiving(int rank) {
        this.rank = rank;
    }

}