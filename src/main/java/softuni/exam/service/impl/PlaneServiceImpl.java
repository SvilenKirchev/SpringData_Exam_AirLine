package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.constants.GlobalConstants;
import softuni.exam.models.dtos.PlaneSeedRootDto;
import softuni.exam.models.entities.Plane;
import softuni.exam.repository.PlaneRepository;
import softuni.exam.service.PlaneService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PlaneServiceImpl implements PlaneService {

    private final PlaneRepository planeRepository;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;
    private final XmlParser xmlParser;

    public PlaneServiceImpl(PlaneRepository planeRepository, ValidationUtil validationUtil, ModelMapper modelMapper, XmlParser xmlParser) {
        this.planeRepository = planeRepository;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.xmlParser = xmlParser;
    }

    @Override
    public boolean areImported() {
        return this.planeRepository.count()>0;
    }

    @Override
    public String readPlanesFileContent() throws IOException {
        return Files.readString(Path.of(GlobalConstants.PLANES_FILE_PATH));
    }

    @Override
    public String importPlanes() throws JAXBException, FileNotFoundException {
        StringBuilder sb = new StringBuilder();

        PlaneSeedRootDto planeSeedRootDto = this.xmlParser.parseXml(PlaneSeedRootDto.class, GlobalConstants.PLANES_FILE_PATH);

        planeSeedRootDto.getPlanes().forEach(dto -> {
            if (this.validationUtil.isValid(dto)){
                if (this.planeRepository.findByRegisterNumber(dto.getRegisterNumber()) == null){
                    Plane plane = this.modelMapper.map(dto, Plane.class);

                    this.planeRepository.saveAndFlush(plane);
                    sb.append(String.format("Successfully imported %s %s",plane.getClass().getSimpleName(),plane.getRegisterNumber()));

                }else {
                    sb.append(GlobalConstants.DUPLICATION_MSG);
                }
            }else {
                sb.append("Invalid plane");
            }
            sb.append(System.lineSeparator());
        });

        return sb.toString();
    }
}
