package edu.srmist.leave;

import io.camunda.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Deployment(resources = {
        "classpath:bpmn/student-leave.bpmn",
        "classpath:bpmn/fa-approval.form"
})
public class LeaveWorkflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeaveWorkflowApplication.class, args);
    }
}
