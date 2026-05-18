package org.example.server;

import org.example.net.protocol.CommandRequest;

/** Запрос и формат ответа (JSON или Java-сериализация). */
public record ParsedRequest(CommandRequest request, boolean jsonWire) {
}
