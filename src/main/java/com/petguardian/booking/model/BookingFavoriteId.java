package com.petguardian.booking.model;

import java.io.Serializable;
import java.util.Objects;

public class BookingFavoriteId implements Serializable {
    private Integer memId;
    private Integer sitterId;

    public BookingFavoriteId() {}

    public BookingFavoriteId(Integer memId, Integer sitterId) {
        this.memId = memId;
        this.sitterId = sitterId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingFavoriteId that = (BookingFavoriteId) o;
        return Objects.equals(memId, that.memId) && Objects.equals(sitterId, that.sitterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memId, sitterId);
    }
}
