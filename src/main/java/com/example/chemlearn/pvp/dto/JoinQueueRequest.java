package com.example.chemlearn.pvp.dto;

import lombok.Data;

import java.util.UUID;

/**
 * Client → Server: Request to join the matchmaking queue.
 * Sent via STOMP to /app/battle/join
 */
@Data
public class JoinQueueRequest {
    /** ID of the StudentPet the student wants to fight with */
    private UUID studentPetId;
}
