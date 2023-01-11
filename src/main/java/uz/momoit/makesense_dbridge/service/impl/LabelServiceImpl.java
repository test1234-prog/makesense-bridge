package uz.momoit.makesense_dbridge.service.impl;

import org.springframework.stereotype.Service;
import uz.momoit.makesense_dbridge.domain.VrifysttusEnum;
import uz.momoit.makesense_dbridge.repository.AttachmentRepository;
import uz.momoit.makesense_dbridge.repository.LabelRepository;
import uz.momoit.makesense_dbridge.service.LabelService;
import uz.momoit.makesense_dbridge.service.dto.LabelsImportDTO;
import uz.momoit.makesense_dbridge.service.dto.YoloDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;

    private final AttachmentRepository attachmentRepository;

    public LabelServiceImpl(LabelRepository labelRepository, AttachmentRepository attachmentRepository) {
        this.labelRepository = labelRepository;
        this.attachmentRepository = attachmentRepository;
    }

    @Override
    public List<LabelsImportDTO> convertLabelToYolo(Long dtlSeq) {
        List<Long> attachmentIdsByDtSeq = attachmentRepository.getAttachmentIdsByDtSeq(dtlSeq);
        return attachmentIdsByDtSeq.stream()
                .map(attSeq ->
                    new LabelsImportDTO(
                        attSeq,
                        null,
                        getYoloDTOByAttSeq(attSeq)))
                .map(labelsImportDTO -> {
                    String vrifySttusByAttSeq = attachmentRepository.getVrifySttusByAttSeq(labelsImportDTO.getAttSeq());
                    if(vrifySttusByAttSeq == null) {
                        labelsImportDTO.setVrifysttus(VrifysttusEnum.UNDONE.name());
                        return labelsImportDTO;
                    }
                    switch (vrifySttusByAttSeq) {
                        case "1":
                            labelsImportDTO.setVrifysttus(VrifysttusEnum.UNCHECKED.name());
                            break;
                        case "2":
                            labelsImportDTO.setVrifysttus(VrifysttusEnum.DONE.name());
                            break;
                        case "3":
                            labelsImportDTO.setVrifysttus(VrifysttusEnum.APPROVED.name());
                            break;
                        case "4":
                            labelsImportDTO.setVrifysttus(VrifysttusEnum.REJECTED.name());
                            break;
                        default:
                            labelsImportDTO.setVrifysttus(VrifysttusEnum.UNDONE.name());
                            break;
                    }
                            return labelsImportDTO;
                }).collect(Collectors.toList());
    }

    private List<YoloDTO> getYoloDTOByAttSeq(Long attSeq) {
        return labelRepository.getLabelsByAttSeq(attSeq).stream().map(label -> {
            YoloDTO yoloDTO = new YoloDTO();
            yoloDTO.setLabelOrder(label.getLabelOrder());
            yoloDTO.setYolo1((2* label.getBboxX() + label.getBboxWidth()) / 2.0 * (1 / label.getImgWidth()));
            yoloDTO.setYolo2((2* label.getBboxY() + label.getBboxHeight()) / 2.0 * (1 / label.getImgHeight()));
            yoloDTO.setYolo3(label.getBboxWidth() * (1 / label.getImgWidth()));
            yoloDTO.setYolo4(label.getBboxHeight() * (1 / label.getImgHeight()));
            return yoloDTO;
        }).collect(Collectors.toList());
    }
}
