package it.warehouse.administrator.unit.scheduler;

import it.warehouse.administrator.config.SchedulerConfig;
import it.warehouse.administrator.scheduler.UserRegistrationScheduler;
import it.warehouse.administrator.service.UserRegistrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRegistrationScheduler — logica di esecuzione")
class UserRegistrationSchedulerTest {

    @Mock
    SchedulerConfig schedulerConfig;

    @Mock
    UserRegistrationService userRegistrationService;

    @InjectMocks
    UserRegistrationScheduler scheduler;

    @Test
    @DisplayName("scheduler disabilitato → deleteExpiredPending non invocato")
    void scheduler_disabled_skipsExecution() {
        when(schedulerConfig.enabled()).thenReturn(false);

        scheduler.deleteUserRegistrationPending();

        verify(userRegistrationService, never()).deleteExpiredPending();
    }

    @Test
    @DisplayName("scheduler abilitato → deleteExpiredPending invocato una volta")
    void scheduler_enabled_callsDeleteExpiredPending() {
        when(schedulerConfig.enabled()).thenReturn(true);
        when(userRegistrationService.deleteExpiredPending()).thenReturn(3);

        scheduler.deleteUserRegistrationPending();

        verify(userRegistrationService, times(1)).deleteExpiredPending();
    }

    @Test
    @DisplayName("scheduler abilitato, nessun record scaduto → deleteExpiredPending invocato con risultato 0")
    void scheduler_enabled_noExpiredRecords_callsDeleteWithZeroResult() {
        when(schedulerConfig.enabled()).thenReturn(true);
        when(userRegistrationService.deleteExpiredPending()).thenReturn(0);

        scheduler.deleteUserRegistrationPending();

        verify(userRegistrationService, times(1)).deleteExpiredPending();
    }
}