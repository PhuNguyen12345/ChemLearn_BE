package com.example.chemlearn.pvp.service;

import com.example.chemlearn.pvp.model.BattleRoom;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory store for all active BattleRooms.
 * Uses ConcurrentHashMap — no Redis required.
 * All combat calculations happen here in RAM; DB is only written once per battle end.
 */
@Component
public class BattleRoomStore {

    private final ConcurrentHashMap<String, BattleRoom> rooms = new ConcurrentHashMap<>();

    public void save(BattleRoom room) {
        rooms.put(room.getRoomId(), room);
    }

    public Optional<BattleRoom> findById(String roomId) {
        return Optional.ofNullable(rooms.get(roomId));
    }

    public void remove(String roomId) {
        rooms.remove(roomId);
    }

    public Collection<BattleRoom> findAll() {
        return rooms.values();
    }

    /**
     * Find the room that contains a given WebSocket session ID.
     * Used when a player disconnects abruptly.
     */
    public Optional<BattleRoom> findBySessionId(String wsSessionId) {
        return rooms.values().stream()
                .filter(r ->
                        (r.getPlayer1() != null && wsSessionId.equals(r.getPlayer1().getWsSessionId())) ||
                        (r.getPlayer2() != null && wsSessionId.equals(r.getPlayer2().getWsSessionId()))
                )
                .findFirst();
    }

    /**
     * Find the room where a student is currently playing.
     */
    public Optional<BattleRoom> findByStudentId(String studentId) {
        return rooms.values().stream()
                .filter(r ->
                        (r.getPlayer1() != null && studentId.equals(r.getPlayer1().getStudentId())) ||
                        (r.getPlayer2() != null && studentId.equals(r.getPlayer2().getStudentId()))
                )
                .findFirst();
    }
}
