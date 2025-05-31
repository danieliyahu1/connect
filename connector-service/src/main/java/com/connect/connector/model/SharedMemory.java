package com.connect.connector.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "shared_memory")
@Getter
public class SharedMemory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID userAId;
    private UUID userBId;
    private UUID userAMediaId;
    private UUID userBMediaId;
    private String caption;
    @Setter
    private boolean isUserAApproved;
    @Setter
    private boolean isUserBApproved;

    public SharedMemory(UUID userAId, UUID userBId, UUID userAMediaId, UUID userBMediaId, String caption) {
        this.userAId = userAId;
        this.userBId = userBId;
        this.userAMediaId = userAMediaId;
        this.userBMediaId = userBMediaId;
        this.caption = caption;
        this.isUserAApproved = false;
        this.isUserBApproved = false;
    }

    public boolean isApproved()
    {
        return isUserAApproved && isUserBApproved;
    }
}
