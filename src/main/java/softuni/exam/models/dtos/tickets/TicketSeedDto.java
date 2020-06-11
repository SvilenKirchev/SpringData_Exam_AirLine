package softuni.exam.models.dtos.tickets;

import org.hibernate.validator.constraints.Length;
import softuni.exam.adaptors.LocalDateTimeAdaptor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@XmlRootElement(name = "ticket")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class TicketSeedDto {

    @XmlElement(name = "serial-number")
    private String serialNumber;

    @XmlElement
    private BigDecimal price;

    @XmlElement(name = "take-off")
    @XmlJavaTypeAdapter(value = LocalDateTimeAdaptor.class)
    private LocalDateTime takeoff;

    @XmlElement(name = "from-town")
    private InnerFromTownSeedDto fromTown;

    @XmlElement(name = "to-town")
    private InnerToTownSeedDto toTown;

    @XmlElement(name = "passenger")
    private InnerPassengerSeedDto passenger;

    @XmlElement(name = "plane")
    private InnerPlaneSeedDto plane;

    public TicketSeedDto() {

    }

    @Length(min = 2)
    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @DecimalMin(value = "0")
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @NotNull
    public LocalDateTime getTakeoff() {
        return takeoff;
    }

    public void setTakeoff(LocalDateTime takeoff) {
        this.takeoff = takeoff;
    }

    @NotNull
    public InnerFromTownSeedDto getFromTown() {
        return fromTown;
    }

    public void setFromTown(InnerFromTownSeedDto fromTown) {
        this.fromTown = fromTown;
    }

    @NotNull
    public InnerToTownSeedDto getToTown() {
        return toTown;
    }

    public void setToTown(InnerToTownSeedDto toTown) {
        this.toTown = toTown;
    }

    @NotNull
    public InnerPassengerSeedDto getPassenger() {
        return passenger;
    }

    public void setPassenger(InnerPassengerSeedDto passenger) {
        this.passenger = passenger;
    }

    @NotNull
    public InnerPlaneSeedDto getPlane() {
        return plane;
    }

    public void setPlane(InnerPlaneSeedDto plane) {
        this.plane = plane;
    }
}
