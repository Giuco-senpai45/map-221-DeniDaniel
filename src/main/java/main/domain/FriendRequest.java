package main.domain;

import java.time.LocalDate;
import java.util.Objects;

public class FriendRequest extends Entity<Long>{
    private Long from;
    private Long to;
    private String status;
    private LocalDate time;

    public FriendRequest(Long from, Long to, String status) {
        this.from = from;
        this.to = to;
        this.status = status;
        this.time = LocalDate.now();
    }

    @Override
    public String toString() {
        return "Request id: " + getId() + " from: " +  from + " to: " + to + " (" + status + ")";
    }

    public LocalDate getTime() {
        return time;
    }

    public Long getFrom() {
        return from;
    }

    public Long getTo() {
        return to;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendRequest that = (FriendRequest) o;
        return Objects.equals(from, that.from) && Objects.equals(to, that.to) && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, status);
    }
}
