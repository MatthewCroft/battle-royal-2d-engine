package com.example.battleroyalapi.quadtree;

import com.example.battleroyalapi.model.Player;
import com.example.battleroyalapi.quadtree.Bounds;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Player.class, name = "player"),
})
public abstract class QuadTreeObject {
    public String id;
    public Bounds bounds;

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

    public QuadTreeObject(String id, Bounds bounds){
        this.id = id;
        this.bounds = bounds;
    }
}
