package com.kubemanager.user_service.entity.embeddable;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable
public class ContactInfo {


    @Column(length = 20)
    private String phoneNumber;
}
