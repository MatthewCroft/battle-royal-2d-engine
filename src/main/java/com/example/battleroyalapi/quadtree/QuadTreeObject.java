package com.example.battleroyalapi.quadtree;

import com.example.battleroyalapi.model.Bullet;
import com.example.battleroyalapi.model.ObjectType;
import com.example.battleroyalapi.model.Player;
import com.example.battleroyalapi.quadtree.Bounds;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Player.class, name = "PLAYER"),
        @JsonSubTypes.Type(value = Bullet.class, name = "BULLET")
})
public abstract class QuadTreeObject {
    public String id;
    public Bounds bounds;
    public ObjectType type;

    @Override
    public String toString() {
        return bounds.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuadTreeObject that = (QuadTreeObject) o;
        return id.equals(that.id);
    }

    public QuadTreeObject(String id, Bounds bounds, ObjectType type){
        this.id = id;
        this.bounds = bounds;
        this.type = type;
    }
}
