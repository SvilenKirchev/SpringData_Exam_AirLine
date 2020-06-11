package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.constants.GlobalConstants;
import softuni.exam.models.dtos.PassengerSeedDto;
import softuni.exam.models.dtos.TownSeedDto;
import softuni.exam.models.entities.Passenger;
import softuni.exam.models.entities.Town;
import softuni.exam.repository.PassengerRepository;
import softuni.exam.service.PassengerService;
import softuni.exam.service.TownService;
import softuni.exam.util.ValidationUtil;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository passengerRepository;
    private final Gson gson;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;
    private final TownService townService;

    public PassengerServiceImpl(PassengerRepository passengerRepository, Gson gson, ModelMapper modelMapper, ValidationUtil validationUtil, TownService townService) {
        this.passengerRepository = passengerRepository;
        this.gson = gson;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
        this.townService = townService;
    }

    @Override
    public boolean areImported() {
        return this.passengerRepository.count()>0;
    }

    @Override
    public String readPassengersFileContent() throws IOException {
        return Files.readString(Path.of(GlobalConstants.PASSENGERS_FILE_PATH));
    }

    @Override
    public String importPassengers() throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();

        PassengerSeedDto[] passengerSeedDtos = this.gson.fromJson(new FileReader(GlobalConstants.PASSENGERS_FILE_PATH), PassengerSeedDto[].class);

        Arrays.stream(passengerSeedDtos).forEach(dto -> {
            if (this.validationUtil.isValid(dto)){
                if (this.passengerRepository.findByFirstNameAndLastNameAndEmail(dto.getFirstName(),dto.getLastName(), dto.getEmail()) == null){
                    Passenger passenger = this.modelMapper.map(dto, Passenger.class);
                    Town town = this.townService.getTownByName(dto.getTown());

                    passenger.setTown(town);

                    this.passengerRepository.saveAndFlush(passenger);
                    sb.append(String.format("Successfully imported %s %s - %s",passenger.getClass().getSimpleName(),passenger.getLastName(), passenger.getEmail()));

                }else {
                    sb.append(GlobalConstants.DUPLICATION_MSG);
                }
            }else {
                sb.append("Invalid passenger");
            }
            sb.append(System.lineSeparator());
        });

        return sb.toString();

    }

    @Override
    public String getPassengersOrderByTicketsCountDescendingThenByEmail() {
        StringBuilder sb = new StringBuilder();

        this.passengerRepository.getAllPassengersOrderByTicketsCountThenByEmail().forEach(e -> {
            sb.append(String.format("Passenger %s  %s\n" +
                    "\tEmail - %s\n" +
                    "\tPhone - %s\n" +
                    "\tNumber of tickets - %d\n",e.getFirstName(),e.getLastName(),e.getEmail(),e.getPhoneNumber(),e.getTickets().size()));
        });

        return sb.toString();
    }
}
