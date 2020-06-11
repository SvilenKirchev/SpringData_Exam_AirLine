package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.constants.GlobalConstants;
import softuni.exam.models.dtos.tickets.TicketSeedRootDto;
import softuni.exam.models.entities.Passenger;
import softuni.exam.models.entities.Plane;
import softuni.exam.models.entities.Ticket;
import softuni.exam.models.entities.Town;
import softuni.exam.repository.PassengerRepository;
import softuni.exam.repository.PlaneRepository;
import softuni.exam.repository.TicketRepository;
import softuni.exam.repository.TownRepository;
import softuni.exam.service.TicketService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final XmlParser xmlParser;
    private final PassengerRepository passengerRepository;
    private final TownRepository townRepository;
    private final PlaneRepository planeRepository;

    public TicketServiceImpl(TicketRepository ticketRepository, ModelMapper modelMapper, ValidationUtil validationUtil, XmlParser xmlParser, PassengerRepository passengerRepository, TownRepository townRepository, PlaneRepository planeRepository) {
        this.ticketRepository = ticketRepository;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.xmlParser = xmlParser;
        this.passengerRepository = passengerRepository;
        this.townRepository = townRepository;
        this.planeRepository = planeRepository;
    }

    @Override
    public boolean areImported() {
        return this.ticketRepository.count() > 0;
    }

    @Override
    public String readTicketsFileContent() throws IOException {
        return Files.readString(Path.of(GlobalConstants.TICKETS_FILE_PATH));
    }

    @Override
    public String importTickets() throws JAXBException, FileNotFoundException {
        StringBuilder sb = new StringBuilder();

        TicketSeedRootDto ticketSeedRootDto = this.xmlParser.parseXml(TicketSeedRootDto.class, GlobalConstants.TICKETS_FILE_PATH);

        ticketSeedRootDto.getTickets().forEach(dto -> {
            if (this.validationUtil.isValid(dto)) {
                if (this.ticketRepository.findBySerialNumber(dto.getSerialNumber()) == null) {
                    Ticket ticket = this.modelMapper.map(dto, Ticket.class);

                    Town fromTown = this.townRepository.findByName(dto.getFromTown().getName());
                    Town toTown = this.townRepository.findByName(dto.getToTown().getName());
                    Passenger passenger = this.passengerRepository.findByEmail(dto.getPassenger().getEmail());
                    Plane plane = this.planeRepository.findByRegisterNumber(dto.getPlane().getRegisterNumber());

                    ticket.setFromTown(fromTown);
                    ticket.setToTown(toTown);
                    ticket.setPassenger(passenger);
                    ticket.setPlane(plane);


                    List<Ticket> tickets = this.ticketRepository.getAllByPassengerId(ticket.getPassenger().getId());

                    passenger.setTickets(tickets);

                    this.ticketRepository.saveAndFlush(ticket);
                    sb.append(String.format("Successfully imported %s %s - %s", ticket.getClass().getSimpleName(), ticket.getFromTown().getName(), ticket.getToTown().getName()));
                } else {
                    sb.append(GlobalConstants.DUPLICATION_MSG);
                }
            } else {
                sb.append("Invalid ticket");
            }
            sb.append(System.lineSeparator());
        });

        return sb.toString();
    }
}
