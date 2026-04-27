package it.warehouse.administrator.scheduler;


import io.quarkus.scheduler.Scheduled;
import it.warehouse.administrator.config.SchedulerConfig;
import it.warehouse.administrator.service.UserRegistrationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.eclipse.microprofile.faulttolerance.Retry;

import java.time.LocalDateTime;

@ApplicationScoped
@Slf4j
public class UserRegistrationScheduler {


    @Inject
    SchedulerConfig  schedulerConfig;

    @Inject
    UserRegistrationService  userRegistrationService;

    @Scheduled(identity = "userRegistration-cleanup", cron = "{scheduler.cron}")
    @SchedulerLock(name = "userRegistrationLock", lockAtLeastFor = "PT1M", lockAtMostFor = "PT9M")
    public void deleteUserRegistrationPending(){
        log.info("deleteUserRegistrationPending: Starting scheduler task for deleting user registration pending...");
        if(!schedulerConfig.enabled()){
            log.info("deleteUserRegistrationPending: scheduler disabled");
            return;
        }
        LocalDateTime startingSchedulerTime = LocalDateTime.now();
        int countDeleting = userRegistrationService.deleteExpiredPending();
        LocalDateTime endingSchedulerTime = LocalDateTime.now();
        log.info("deleteUserRegistrationPending: user registration expired: {} deleted successfully", countDeleting);
        log.info("deleteUserRegistrationPending: Scheduler Start: {} . Scheduler End: {}",startingSchedulerTime, endingSchedulerTime);
    }

}
