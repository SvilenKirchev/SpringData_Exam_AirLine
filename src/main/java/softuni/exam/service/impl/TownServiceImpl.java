package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.constants.GlobalConstants;
import softuni.exam.models.dtos.TownSeedDto;
import softuni.exam.models.entities.Town;
import softuni.exam.repository.TownRepository;
import softuni.exam.service.TownService;
import softuni.exam.util.ValidationUtil;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public class TownServiceImpl implements TownService {

    private final TownRepository townRepository;
    private final Gson gson;
    private final ModelMapper modelMapper;
    private final ValidationUtil validationUtil;

    public TownServiceImpl(TownRepository townRepository, Gson gson, ModelMapper modelMapper, ValidationUtil validationUtil) {
        this.townRepository = townRepository;
        this.gson = gson;
        this.modelMapper = modelMapper;
        this.validationUtil = validationUtil;
    }

    @Override
    public boolean areImported() {
        return this.townRepository.count()>0;
    }

    @Override
    public String readTownsFileContent() throws IOException {
        return Files.readString(Path.of(GlobalConstants.TOWNS_FILE_PATH));
    }

    @Override
    public String importTowns() throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();

        TownSeedDto[] townSeedDtos = this.gson.fromJson(new FileReader(GlobalConstants.TOWNS_FILE_PATH), TownSeedDto[].class);

        Arrays.stream(townSeedDtos).forEach(dto -> {
            if (this.validationUtil.isValid(dto)){
                if (this.townRepository.findByNameAndPopulation(dto.getName(),dto.getPopulation()) == null){
                    Town town = this.modelMapper.map(dto, Town.class);

                    this.townRepository.saveAndFlush(town);
                    sb.append(String.format("Successfully imported %s %s - %s",town.getClass().getSimpleName(),town.getName(), town.getPopulation()));

                }else {
                    sb.append(GlobalConstants.DUPLICATION_MSG);
                }
            }else {
                sb.append("Invalid town");
            }
            sb.append(System.lineSeparator());
        });

        return sb.toString();

    }

    @Override
    public Town getTownByName(String name) {
        return this.townRepository.findByName(name);
    }
}
