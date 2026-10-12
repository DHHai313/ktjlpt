package kaito.jlpt.ktjlpt.mapper;

import kaito.jlpt.ktjlpt.dto.request.ExamUpdateRequest;
import kaito.jlpt.ktjlpt.entity.Exam;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ExamMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateExam(@MappingTarget Exam exam, ExamUpdateRequest request);
}
