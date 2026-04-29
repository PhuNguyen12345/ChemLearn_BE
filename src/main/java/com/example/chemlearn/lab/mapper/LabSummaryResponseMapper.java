package com.example.chemlearn.lab.mapper;

import com.example.chemlearn.core.shared.mapper.BaseMapper;
import com.example.chemlearn.lab.dto.response.LabSummaryResponse;
import com.example.chemlearn.lab.entity.Lab;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LabSummaryResponseMapper extends BaseMapper<Lab, LabSummaryResponse> {
}
