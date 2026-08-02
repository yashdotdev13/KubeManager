package com.kubemanager.user_service.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ProfessionalInfo {

    @Column(length = 100)
    private String organization;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String jobTitle;
}
