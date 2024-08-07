import java.util.Objects;

public class Seat {
    private boolean isReserved;

    private int seatId;

    public boolean isReserved() {
        return isReserved;
    }

    public void setReserved(boolean reserved) {
        isReserved = reserved;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public Seat(boolean isReserved, int seatId) {
        this.isReserved = isReserved;
        this.seatId = seatId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seat seat = (Seat) o;
        return isReserved == seat.isReserved && seatId == seat.seatId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isReserved, seatId);
    }
}
