package org.example.net.protocol;

import org.example.data.City;

import java.io.Serializable;

@lombok.Getter
@lombok.Setter
@lombok.NoArgsConstructor
public class CommandRequest implements Serializable {
    private CommandType type;
    private String arg;
    private Long id;
    private Integer index;
    private City city;

    private String login;
    private String password;
    private transient Long authenticatedUserId;

}

