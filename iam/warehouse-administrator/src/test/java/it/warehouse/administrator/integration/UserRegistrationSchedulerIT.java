package it.warehouse.administrator.integration;

import io.quarkus.test.junit.QuarkusTest;
import it.warehouse.administrator.integration.factory.TestDataFactory;
import it.warehouse.administrator.model.UserRegistration;
import it.warehouse.administrator.model.enumerator.RegistrationStatus;
import it.warehouse.administrator.scheduler.UserRegistrationScheduler;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@DisplayName("UserRegistrationScheduler — integrazione con DB")
class UserRegistrationSchedulerIT extends AbstractBaseIT {

    @Inject
    UserRegistrationScheduler scheduler;

    @Test
    @DisplayName("record PENDING scaduto ")
    void scheduler_expiredPendingRecord_isDeleted() {
        UserRegistration expired = TestDataFactory.expiredPendingRegistration(db, TestDataFactory.FAKE_KC_USER_ID);

        scheduler.deleteUserRegistrationPending();

        UserRegistration result = db.find(UserRegistration.class, expired.getId());
        assertNull(result);
    }

    @Test
    @DisplayName("record PENDING recente ")
    void scheduler_freshPendingRecord_isNotDeleted() {
        UserRegistration fresh = TestDataFactory.pendingRegistration(db, TestDataFactory.FAKE_KC_USER_ID);

        scheduler.deleteUserRegistrationPending();

        UserRegistration result = db.find(UserRegistration.class, fresh.getId());
        assertNotNull(result);
        assertEquals(RegistrationStatus.PENDING, result.getStatus());
    }

    @Test
    @DisplayName("record APPROVED scaduto)")
    void scheduler_expiredApprovedRecord_isNotDeleted() {
        UserRegistration reg = TestDataFactory.expiredPendingRegistration(db, TestDataFactory.FAKE_KC_USER_ID);
        reg.setStatus(RegistrationStatus.APPROVED);
        reg.update();

        scheduler.deleteUserRegistrationPending();

        UserRegistration result = db.find(UserRegistration.class, reg.getId());
        assertNotNull(result);
    }
}