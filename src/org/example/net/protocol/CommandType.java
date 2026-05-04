package org.example.net.protocol;

import java.io.Serializable;

public enum CommandType implements Serializable {
    REGISTER,
    HELP,
    INFO,
    SHOW,
    ADD,
    UPDATE,
    REMOVE_BY_ID,
    CLEAR,
    EXIT,
    INSERT_AT,
    ADD_IF_MAX,
    COUNT_LESS_THAN_STANDARD_OF_LIVING,
    FILTER_BY_GOVERNOR,
    PRINT_FIELD_ASCENDING_STANDARD_OF_LIVING
}
