package ru.netology.patient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.medical.MedicalService;
import ru.netology.patient.service.medical.MedicalServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MedicalServiceTest {
    PatientInfoRepository patientInfoRepository;
    SendAlertService sendAlertService;

    @BeforeEach
    public void setupTest() {
        patientInfoRepository = Mockito.mock(PatientInfoRepository.class);

        HealthInfo healthInfo = new HealthInfo(BigDecimal.valueOf(36.6), new BloodPressure(120, 80));
        PatientInfo testPatientInfo = new PatientInfo("1", "Sergey", "Buymov", LocalDate.of(1987, 11, 14), healthInfo);

        Mockito.when(patientInfoRepository.add(testPatientInfo)).thenReturn("1");
        Mockito.when(patientInfoRepository.getById("1")).thenReturn(testPatientInfo);
        Mockito.when(patientInfoRepository.remove(Mockito.anyString())).thenReturn(testPatientInfo);
        Mockito.when(patientInfoRepository.update(testPatientInfo)).thenReturn(testPatientInfo);

        sendAlertService = Mockito.mock(SendAlertService.class);
    }

    @Test
    public void medicalService_whenCheckBloodPressureOverNormal_thenSendMessage() {
        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, sendAlertService);
        medicalService.checkBloodPressure("1", new BloodPressure(140, 80));

        ArgumentCaptor<String> argumentCaptorMessage = ArgumentCaptor.forClass(String.class);

        Mockito.verify(sendAlertService).send(argumentCaptorMessage.capture());
        Assertions.assertEquals("Warning, patient with id: 1, need help", argumentCaptorMessage.getValue());
    }

    @Test
    public void medicalService_whenCheckTemperatureLowerNormal_thenSendMessage() {
        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, sendAlertService);
        medicalService.checkTemperature("1", BigDecimal.valueOf(35));

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        Mockito.verify(sendAlertService).send(argumentCaptor.capture());
        Assertions.assertEquals("Warning, patient with id: 1, need help", argumentCaptor.getValue());
    }


    @Test
    public void medicalService_whenCheckTemperatureOrCheckPressureIsNormal_thenMessageNoSend() {
        MedicalService medicalService = new MedicalServiceImpl(patientInfoRepository, sendAlertService);
        medicalService.checkTemperature("1", BigDecimal.valueOf(36.6));
        medicalService.checkBloodPressure("1",new BloodPressure(120, 80));

        Mockito.verify(sendAlertService, Mockito.times(0)).send("Warning, patient with id: 1, need help");
    }

}
