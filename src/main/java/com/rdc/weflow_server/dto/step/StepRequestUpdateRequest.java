package com.rdc.weflow_server.dto.step;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StepRequestUpdateRequest {

    @Size(max = 255)
    private String title;
    private String description;

    @Size(max = 50)
    private List<Long> attachmentIds;

    @Size(max = 50)
    private List<@URL String> links;
}
